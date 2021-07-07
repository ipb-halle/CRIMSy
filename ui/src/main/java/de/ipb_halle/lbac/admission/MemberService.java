/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2020 Leibniz-Institut f. Pflanzenbiochemie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.ipb_halle.lbac.admission;

/**
 * MemberService provides service to load, save, update users and groups.
 */
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.service.NodeService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Stateless
public class MemberService implements Serializable {

    public final static String PARAM_MEMBER_ID = "id";
    public final static String PARAM_NODE_ID = "node_id";
    public final static String PARAM_SUBSYSTEM_TYPE = "subSystemType";
    public final static String PARAM_SUBSYSTEM_DATA = "subSystemData";
    public final static String PARAM_NAME = "name";
    public final static String PARAM_LOGIN = "login";
    public final static String PARAM_SHORTCUT = "shortCut";
    private final String ADMIN_GROUP_NAME = "Admin Group";

    private static final long serialVersionUID = 1L;
    private final String SQL_GET_SIMILAR_NAMES
            = "SELECT name "
            + "FROM usersgroups "
            + "WHERE LOWER(name) LIKE LOWER(:name) AND membertype='U' AND name <> 'deactivated'";

    private final String SQL_GET_GROUPS
            = "SELECT "
            + "ug.id, "
            + "ug.membertype, "
            + "ug.subsystemtype, "
            + "ug.subsystemdata, "
            + "ug.modified, "
            + "ug.node_id, "
            + "ug.login, "
            + "ug.name, "
            + "ug.email, "
            + "ug.password, "
            + "ug.phone "
            + "FROM usersgroups ug "
            + "JOIN nodes n ON ug.node_id=n.id "
            + "WHERE membertype='G' "
            + "AND (n.institution ILIKE (:INSTITUTE) OR :INSTITUTE='no_institution_filter') "
            + "AND (ug.name ILIKE (:NAME) OR :NAME='no_name_filter')";

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    @Inject
    private MembershipService membershipService;
    @Inject
    private NodeService nodeService;

    private Logger logger;

    public MemberService() {
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    public void deactivateGroup(Group group) {
        if (canGroupBeDeactivated(group)) {
            for (Membership ms : membershipService.loadMemberOf(group)) {
                membershipService.removeMembership(ms);
            }
            for (Membership ms : membershipService.loadMembers(group)) {
                membershipService.removeMembership(ms);
            }
            group.setName(GlobalAdmissionContext.NAME_OF_DEACTIVATED_USER);
            em.merge(group.createEntity());
        }
    }

    public User loadLocalAdminUser() {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(PARAM_LOGIN, "admin");
        cmap.put(PARAM_NODE_ID, nodeService.getLocalNodeId());
        List<User> users = loadUsers(cmap);
        if (users.isEmpty()) {
            return null;
        }
        if (users.size() > 1) {
            for (User u : users) {
                logger.info(u.toString());
            }
            throw new IllegalStateException("More than one admin account found");
        }
        return users.get(0);
    }

    public boolean canGroupBeDeactivated(Group group) {
        if (group == null) {
            return false;
        }
        if (AdmissionSubSystemType.BUILTIN == group.getSubSystem().getSubSystemType()) {
            return false;
        }
        return !group.getName().equals(ADMIN_GROUP_NAME);
    }

    /**
     * load groups which match the definition given by the criteria map.
     *
     * @param cmap criteria map
     * @return list of groups matching the definition
     */
    @SuppressWarnings("unchecked")
    public List<Group> loadGroups(Map<String, Object> cmap) {

        CriteriaBuilder builder = this.em.getCriteriaBuilder();

        CriteriaQuery<GroupEntity> criteriaQuery = builder.createQuery(GroupEntity.class);
        Root<GroupEntity> groupRoot = criteriaQuery.from(GroupEntity.class);
        criteriaQuery.select(groupRoot);
        List<Predicate> predicates = new ArrayList<>();

        if (cmap.get(PARAM_MEMBER_ID) != null) {
            predicates.add(builder.equal(groupRoot.get(PARAM_MEMBER_ID), cmap.get(PARAM_MEMBER_ID)));
        }
        if (cmap.get(PARAM_NODE_ID) != null) {
            predicates.add(builder.equal(groupRoot.get("node"), cmap.get(PARAM_NODE_ID)));
        }
        if (cmap.get(PARAM_NAME) != null) {
            predicates.add(builder.equal(groupRoot.get(PARAM_NAME), cmap.get(PARAM_NAME)));
        }
        if (cmap.get(PARAM_SUBSYSTEM_TYPE) != null) {
            if (cmap.get(PARAM_SUBSYSTEM_TYPE).getClass().equals(AdmissionSubSystemType.class)) {
                // single subsystem type
                predicates.add(builder.equal(groupRoot.get(PARAM_SUBSYSTEM_TYPE), cmap.get(PARAM_SUBSYSTEM_TYPE)));
            } else {
                // list of subsystem types
                List<Predicate> pl = Arrays.stream((AdmissionSubSystemType[]) cmap.get(PARAM_SUBSYSTEM_TYPE))
                        .map(t -> builder.equal(groupRoot.get(PARAM_SUBSYSTEM_TYPE), t))
                        .collect(Collectors.toList());
                predicates.add(builder.or(pl.toArray(new Predicate[0])));
            }
        }
        if (cmap.get(PARAM_SUBSYSTEM_DATA) != null) {
            predicates.add(builder.equal(groupRoot.get(PARAM_SUBSYSTEM_DATA), cmap.get(PARAM_SUBSYSTEM_DATA)));
        }

        criteriaQuery.where(builder.and(predicates.toArray(new Predicate[0])));

        List<Group> result = new ArrayList<>();
        for (GroupEntity ge : this.em.createQuery(criteriaQuery).getResultList()) {
            if (!ge.getName().equals(GlobalAdmissionContext.NAME_OF_DEACTIVATED_USER)) {
                Node node = this.nodeService.loadById(ge.getNode());
                result.add(new Group(ge, node));
            }
        }
        return result;
    }

    /**
     * Loads all groups matching the pattern given by the cmap
     *
     * @param cmap
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Group> loadGroupsFuzzy(Map<String, Object> cmap) {
        List<Group> resultGroups = new ArrayList<>();
        List<GroupEntity> results = this.em.createNativeQuery(SQL_GET_GROUPS, GroupEntity.class)
                .setParameter("NAME", cmap.getOrDefault("NAME", "no_name_filter"))
                .setParameter("INSTITUTE", cmap.getOrDefault("INSTITUTE", "no_institution_filter"))
                .getResultList();

        for (GroupEntity ge : results) {
            Node node = this.nodeService.loadById(ge.getNode());
            resultGroups.add(new Group(ge, node));
        }
        return resultGroups;
    }

    public List<User> loadLocalUsers() {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(PARAM_NODE_ID, nodeService.getLocalNodeId());
        return loadUsers(cmap);
    }

    /**
     * Gets all materialnames which matches the pattern %name%
     *
     * @param name name for searching
     * @return List of matching materialnames
     */
    @SuppressWarnings("unchecked")
    public Set<String> loadSimilarUserNames(String name) {
        return new HashSet<>(this.em.createNativeQuery(SQL_GET_SIMILAR_NAMES)
                .setParameter("name", "%" + name + "%")
                .getResultList());

    }

    /**
     * load members which match the definition given by the criteria map.
     *
     * @param cmap criteria map
     * @return list of Members matching the definition
     */
    @SuppressWarnings("unchecked")
    public List<User> loadUsers(Map<String, Object> cmap) {

        CriteriaBuilder builder = this.em.getCriteriaBuilder();

        CriteriaQuery<UserEntity> criteriaQuery = builder.createQuery(UserEntity.class);
        Root<UserEntity> userRoot = criteriaQuery.from(UserEntity.class);
        criteriaQuery.select(userRoot);
        List<Predicate> predicates = new ArrayList<>();

        if (cmap.get(PARAM_MEMBER_ID) != null) {
            predicates.add(builder.equal(userRoot.get(PARAM_MEMBER_ID), cmap.get(PARAM_MEMBER_ID)));
        }
        if (cmap.get(PARAM_LOGIN) != null) {
            predicates.add(builder.equal(userRoot.get(PARAM_LOGIN), cmap.get(PARAM_LOGIN)));
        }
        if (cmap.get(PARAM_NAME) != null) {
            predicates.add(builder.equal(userRoot.get(PARAM_NAME), cmap.get(PARAM_NAME)));
        }
        if (cmap.get(PARAM_SHORTCUT) != null) {
            predicates.add(builder.equal(userRoot.get(PARAM_SHORTCUT), ((String) cmap.get(PARAM_SHORTCUT)).toUpperCase()));
        }
        if (cmap.get(PARAM_NODE_ID) != null) {
            predicates.add(builder.equal(userRoot.get("node"), cmap.get(PARAM_NODE_ID)));
        }
        if (cmap.get(PARAM_SUBSYSTEM_TYPE) != null) {
            if (cmap.get(PARAM_SUBSYSTEM_TYPE).getClass().equals(AdmissionSubSystemType.class)) {
                // single subsystem type
                predicates.add(builder.equal(userRoot.get(PARAM_SUBSYSTEM_TYPE), cmap.get(PARAM_SUBSYSTEM_TYPE)));
            } else {
                // list of subsystem types
                List<Predicate> pl = Arrays.stream((AdmissionSubSystemType[]) cmap.get(PARAM_SUBSYSTEM_TYPE))
                        .map(t -> builder.equal(userRoot.get(PARAM_SUBSYSTEM_TYPE), t))
                        .collect(Collectors.toList());
                predicates.add(builder.or(pl.toArray(new Predicate[0])));
            }
        }
        if (cmap.get(PARAM_SUBSYSTEM_DATA) != null) {
            predicates.add(builder.equal(userRoot.get(PARAM_SUBSYSTEM_DATA), cmap.get(PARAM_SUBSYSTEM_DATA)));
        }

        criteriaQuery.where(builder.and(predicates.toArray(new Predicate[]{})));

        List<User> result = new ArrayList<>();
        for (UserEntity ue : this.em.createQuery(criteriaQuery).getResultList()) {
            Node node = this.nodeService.loadById(ue.getNode());
            result.add(new User(ue, node));
        }
        return result;
    }

    public User mapRemoteUserToLocalUser(User u, Node n) {
        if (u.getId().equals(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID)) {
            return u;
        }
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(MemberService.PARAM_SUBSYSTEM_DATA, u.getId().toString());
        cmap.put(MemberService.PARAM_SUBSYSTEM_TYPE, AdmissionSubSystemType.LBAC_REMOTE);
        cmap.put(MemberService.PARAM_NODE_ID, n.getId());
        List<User> localUserList = loadUsers(cmap);
        if ((localUserList != null) && (localUserList.size() == 1)) {
            return localUserList.get(0);
        }
        return null;
    }

    /**
     * load a Group object by id
     *
     * @param id group id
     * @return the Group object
     */
    public Group loadGroupById(Integer id) {
        GroupEntity ge = this.em.find(GroupEntity.class, id);
        if (ge != null) {
            Node node = this.nodeService.loadById(ge.getNode());
            return new Group(ge, node);
        }
        return null;
    }

    /**
     * load a User object by id
     *
     * @param id user id
     * @return the User object
     */
    public User loadUserById(Integer id) {
        UserEntity ue = this.em.find(UserEntity.class, id);
        if (ue != null) {
            Node node = this.nodeService.loadById(ue.getNode());
            return new User(ue, node);
        }
        return null;
    }

    /**
     *
     * @param id
     * @return
     */
    public Member loadMemberById(Integer id) {
        Member member = loadGroupById(id);
        if (member == null) {
            return loadUserById(id);
        }
        return member;
    }

    /**
     * save a single group
     *
     * @param g the Group to save
     * @return
     */
    public Group save(Group g) {
        GroupEntity ge = g.createEntity();
        ge = this.em.merge(ge);
        if (ge != null) {
            return new Group(ge, g.getNode());
        }
        return null;
    }

    /**
     * save a member; redirects to save(User) or save(Group)
     *
     * @param m
     * @return
     */
    public Member save(Member m) {
        if (m.isGroup()) {
            return save((Group) m);
        } else {
            return save((User) m);
        }
    }

    /**
     * save a single user
     *
     * @param u the User to save
     * @return
     */
    public User save(User u) {
        try {
            if (u.getShortcut() != null) {
                u.setShortcut(u.getShortcut().toUpperCase());
            }
            UserEntity ue = u.createEntity();
            ue = em.merge(ue);
            if (ue != null) {
                return new User(ue, u.getNode());
            }
        } catch (Exception e) {
            if (e.getMessage().contains("ConstraintViolationException")) {
                throw new DuplicateShortcutException(
                        String.format("shortcut '%s' already in use", u.getShortcut()),
                        e);
            } else {
                throw e;
            }
        }
        return null;
    }
}
