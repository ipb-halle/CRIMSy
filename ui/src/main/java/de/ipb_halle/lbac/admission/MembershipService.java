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
 * MembershipService provides service to load, save, create and delete (group)
 * memberships. This includes creation or removal of nested groups
 */
import de.ipb_halle.lbac.admission.AdmissionSubSystemType;
import de.ipb_halle.lbac.admission.Member;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Stateless
public class MembershipService implements Serializable {

    private static final String SQL_LOAD_NESTINGPATH_BY_MEMBERSHIP = 
            "SELECT np.nestingpathsets_id, np.memberships_id "
            + "FROM nestingpathset_memberships AS np "
            + "JOIN (SELECT id FROM nestingpathsets "
            + "WHERE membership_id=:membership_id) AS msnp "
            + "ON msnp.id=np.nestingpathsets_id";
    private static final long serialVersionUID = 1L;
    private static final UUID NON_EXISTING_UUID = UUID.fromString("ab2abbee-3f3c-4cfe-9886-ba75e9eeaebc");

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    @Inject
    private MemberService memberService;

    private Logger logger;

    /**
     * default constructor
     */
    public MembershipService() {
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    /**
     * this method does post injection error logging
     */
    @PostConstruct
    public void MembershipServiceInit() {
        if (em == null) {
            logger.error("Injection failed for EntityManager. @PersistenceContext(name = \"de.ipb_halle.lbac\")");
        }
    }

    /**
     * Add member m to group g.If m is a User, g might be a User as well. This
     * method can only add direct memberships!
     *
     * @param g the group (may be a user if member is a user as well)
     * @param m the member (user or group)
     * @return
     */
    public Membership addMembership(Member g, Member m) {

        Membership ms = load(g, m);
        if (ms == null) {
            ms = new Membership(g, m, false);
            ms = save(ms);

        } else {
            if (!ms.getNested()) {
                return ms;
            }
        }

        if (ms.getNested()) {
            // direct membership
            ms.setNested(false);

        }

        // also add all the nested memberships of the (parent) group
        resolveNestedMemberships(ms);
        save(ms);
        return ms;
    }

    /**
     * Add member m to group g. This method can only add direct memberships!
     *
     * @param g the group (must not be a User object)
     * @param m the member (user or group)
     */
    private Membership addNestedMembership(Member g, Member m) {
        Membership ms = load(g, m);
        if (ms == null) {
            ms = new Membership(g, m, true);
            ms = save(ms);
        }
        return ms;
    }

    /**
     * add the nested memberships (i.e. memberships of the group)
     *
     * @param membership a direct membership which possibly gives rise to nested
     * memberships
     */
    private void resolveNestedMemberships(Membership membership) {
        Set<Membership> memberOf = loadMemberOf(membership.getGroup());
        memberOf.add(null);
        Set<Membership> members = loadMembers(membership.getMember());
        members.add(null);

        for (Membership child : members) {
            for (Membership parent : memberOf) {
                boolean parentAndChildNull = (parent == null) && (child == null);
                boolean parentIdentityMembership = ((parent != null) && parent.getGroupId().equals(membership.getGroupId()));
                boolean childIdentityMembership = ((child != null) && child.getMemberId().equals(membership.getMemberId()));
                if (!(parentAndChildNull || parentIdentityMembership || childIdentityMembership)) {

                    Membership ms = addNestedMembership(parent == null ? membership.getGroup() : parent.getGroup(),
                            child == null ? membership.getMember() : child.getMember());

                    resolveNestedPaths(parent, child, membership, ms);
                    save(ms);
                }
            }
        }
    }

    /**
     * resolve nesting paths for a given 'nested' membership which results from
     * the 'parent' and 'child' memberships and which (additional) paths depend
     * will depend on the 'direct' membership.
     *
     * @param parent a membership up in the hierarchy
     * @param child a membership down in the hierarchy
     * @param direct the new direct membership, possibly giving rise to new
     * paths
     * @param nested the nested membership which results from all these
     * memberships
     */
    private void resolveNestedPaths(Membership parent, Membership child, Membership direct, Membership nested) {
        Set<NestingPath> npSet = new HashSet<>();
        Set<Integer> mSet = new HashSet<>();
        mSet.add(direct.getId());

        if ((parent != null) && parent.getNested()) {
            parent.getNestingPathSet().forEach((pp) -> {
                resolveNestedChildPath(npSet, pp.getPath(), child, mSet, nested.getId());
            });
        } else {
            if (parent != null) {
                mSet.add(parent.getId());
            }
            resolveNestedChildPath(npSet, null, child, mSet, nested.getId());
        }
        nested.getNestingPathSet().addAll(npSet);
    }

    /**
     * resolve nested path for the 'child' part
     *
     * @param npSet the set of additional nesting paths
     * @param parentPath a path defined by a nested parent membership or null
     * @param child the child membership
     * @param direct UUID of the nested membership
     * @param mSet path information which will be part of all new paths; this is
     */
    private void resolveNestedChildPath(Set<NestingPath> npSet, Set<Integer> parentPath, Membership child, Set<Integer> mSet, Integer direct) {
        if ((child != null) && child.getNested()) {
            child.getNestingPathSet().forEach((cp) -> {
                NestingPath np = new NestingPath();
                np.setMembership(direct);
                np.getPath().addAll(mSet);
                if (parentPath != null) {
                    np.getPath().addAll(parentPath);
                }
                np.getPath().addAll(cp.getPath());
                if (!npSet.contains(np)) {
                    np = save(np);
                    npSet.add(np);
                }
            });
        } else {
            NestingPath np = new NestingPath();
            np.setMembership(direct);
            np.getPath().addAll(mSet);
            if (child != null) {
                np.getPath().add(child.getId());
            }
            if (parentPath != null) {
                np.getPath().addAll(parentPath);
            }
            if (!npSet.contains(np)) {
                np = save(np);
                npSet.add(np);
            }
        }
    }

    /**
     * Return the size of the NestingPath set for testing and debugging
     * purposes.
     *
     * @param ms
     * @return the NestingPathSet
     */
    public int getNestingPathSetSize(Membership ms) {
        return loadById(ms.getId()).getNestingPathSet().size();
    }

    /**
     * load memberships according to the criteria in the criteria map
     *
     * @param cmap
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<Membership> load(Map<String, Object> cmap) {
        List<Membership> result = new ArrayList<>();
        List l = em.createNativeQuery(
                "SELECT ms.id, ms.group_id, ms.member_id, ms.nested "
                + "FROM memberships AS ms "
                + "JOIN usersgroups AS g ON ms.group_id = g.id "
                + "JOIN usersgroups AS m ON ms.member_id = m.id "
                + "JOIN nodes AS gn ON g.node_id = gn.id "
                + "JOIN nodes AS mn ON m.node_id = mn.id "
                + "WHERE (ms.nested = :nested OR True = :nested) "
                + "AND (g.id = :group_id OR CAST('ab2abbee-3f3c-4cfe-9886-ba75e9eeaebc' AS UUID) = :group_id) "
                + "AND (m.id = :member_id OR CAST('ab2abbee-3f3c-4cfe-9886-ba75e9eeaebc' AS UUID) = :member_id) "
                + "AND (g.subSystemType = :gSST OR -1 = :gSST) "
                + "AND (m.subSystemType = :mSST OR -1 = :mSST) "
                + "AND (gn.id = :groupNode_id OR CAST('ab2abbee-3f3c-4cfe-9886-ba75e9eeaebc' AS UUID) = :groupNode_id) "
                + "AND (mn.id = :memberNode_id OR CAST('ab2abbee-3f3c-4cfe-9886-ba75e9eeaebc' AS UUID) = :memberNode_id) ", MembershipEntity.class)
                .setParameter("nested", cmap.keySet().contains("nested") ? cmap.get("nested") : Boolean.TRUE)
                .setParameter("group_id", cmap.keySet().contains("group_id") ? cmap.get("group_id") : NON_EXISTING_UUID)
                .setParameter("member_id", cmap.keySet().contains("member_id") ? cmap.get("member_id") : NON_EXISTING_UUID)
                .setParameter("gSST", cmap.keySet().contains("group_subSystemType") ? ((AdmissionSubSystemType) cmap.get("group_subSystemType")).getIndex() : -1)
                .setParameter("mSST", cmap.keySet().contains("member_subSystemType") ? ((AdmissionSubSystemType) cmap.get("member_subSystemType")).getIndex() : -1)
                .setParameter("groupNode_id", cmap.keySet().contains("group_node") ? cmap.get("group_node") : NON_EXISTING_UUID)
                .setParameter("memberNode_id", cmap.keySet().contains("member_node") ? cmap.get("member_node") : NON_EXISTING_UUID).getResultList();
        for (MembershipEntity entity : (List<MembershipEntity>) l) {
            result.add(new Membership(
                    entity,
                    memberService.loadMemberById(entity.getGroup()),
                    memberService.loadMemberById(entity.getMember()),
                    loadNestingPathByMembership(entity.getId())));
        }

        return result;
    }

    /**
     * return an already existing Membership object for the given group and
     * member objects or null.This method is independent of the nesting state.
     *
     * @param g
     * @param m
     * @return
     */
    public Membership load(Member g, Member m) {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put("group_id", g.getId());
        cmap.put("member_id", m.getId());
        List<Membership> l = load(cmap);
        if ((l != null) && (l.size() == 1)) {
            return l.get(0);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Collection<NestingPath> loadNestingPathByMembership(Integer membershipId) {
        List<NestingPathEntity> entities = em.createNativeQuery(SQL_LOAD_NESTINGPATH_BY_MEMBERSHIP, NestingPathEntity.class)
                .setParameter("membership_id", membershipId)
                .getResultList();

        Map<Integer, NestingPath> map = new HashMap<>();
        for (NestingPathEntity e : entities) {
            NestingPath np = map.get(e.getId().getNestingpathsets_id());
            if (np == null) {
                np = new NestingPath(
                        membershipId,
                        e.getId().getNestingpathsets_id(),
                        new HashSet<>());
                map.put(e.getId().getNestingpathsets_id(), np);
            }
            np.getPath().add(e.getId().getMemberships_id());
        }

        return map.values();
    }

    /**
     * load a Membership by id
     *
     * @param id
     * @return
     */
    public Membership loadById(Integer id) {
        MembershipEntity entity = em.find(MembershipEntity.class, id);
        Member group = this.memberService.loadMemberById(entity.getGroup());
        Member member = this.memberService.loadMemberById(entity.getMember());
        return new Membership(entity,
                group,
                member,
                loadNestingPathByMembership(entity.getId()));
    }

    /**
     * load all groups, where m is a direct or nested member
     *
     * @param m
     * @return
     */
    public Set<Membership> loadMemberOf(Member m) {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put("member_id", m.getId());
        return new HashSet<>(load(cmap));
    }

    /**
     * load all members, which are direct or nested members of m
     *
     * @param m
     * @return
     */
    public Set<Membership> loadMembers(Member m) {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put("group_id", m.getId());
        return new HashSet<>(load(cmap));
    }

    /**
     * Remove a direct Membership. If called with nested memberships, the method
     * will return immediately. If a nested membership persists after removing
     * the direct membership, the nested flag will be cleared.
     *
     * This method uses the removeNestedMemberships() method to update the
     * nesting paths and eventually remove nested memberships.
     *
     * @param ms the membership
     */
    public void removeMembership(Membership ms) {
        // we only allow removal of direct memberships
        if (ms.getNested()) {
            return;
        }

        removeNestedMemberships(ms);

        if (ms.getNestingPathSet().size() > 0) {
//          this.logger.info("removeMembership(): Membership is now nested:");
//          ms.dump(this.logger);
            ms.setNested(true);
            save(ms);
        } else {
//          this.logger.info("removeMembership(): Membership is now removed:");
//          ms.dump(this.logger);
            remove(ms);
        }

    }

    /**
     * Updates the nesting paths and removes all nested memberships which have
     * an empty nesting path set.
     *
     * @param ms the membership which is about to be removed
     */
    private void removeNestedMemberships(Membership ms) {
        Iterator<Membership> parentIter = loadMembers(ms.getMember()).iterator();
//      this.logger.info("removeNestedMemberships()");
        while (parentIter.hasNext()) {
            Iterator<Membership> childIter = loadMemberOf(parentIter.next().getMember()).iterator();
            while (childIter.hasNext()) {
                boolean hasChanged = false;
                Membership child = childIter.next();

//              child.dump(this.logger);
                Set<NestingPath> pathSet = child.getNestingPathSet();
                Iterator<NestingPath> pathIter = pathSet.iterator();
                while (pathIter.hasNext()) {
                    NestingPath np = pathIter.next();
                    if (np.getPath().contains(ms.getId())) {
                        remove(np);
                        pathIter.remove();
                        hasChanged = true;
                    }
                }
                if ((pathSet.isEmpty()) && child.getNested()) {
//                  this.logger.info("removeNestedMemberships(): deleting child (see above)");
                    remove(child);
                    hasChanged = false;
                }
                if (hasChanged) {
                    save(child);
                }
            } // end while
        } // end while 
    }

    /**
     * remove a membership
     *
     * @param ms the Membership
     */
    private void remove(Membership ms) {
        this.em.createNativeQuery("DELETE FROM memberships WHERE id=:membership_id")
                .setParameter("membership_id", ms.getId())
                .executeUpdate();
    }

    /**
     * remove a nesting path
     *
     * @param np the NestingPath
     */
    private void remove(NestingPath np) {
        this.em.createNativeQuery("DELETE FROM nestingpathsets WHERE id=:nestingpath_id")
                .setParameter("nestingpath_id", np.getId())
                .executeUpdate();
    }

    /**
     * merge a Membership instance into the database
     *
     * @return the merged instance
     */
    private Membership save(Membership ms) {
        MembershipEntity mse = this.em.merge(ms.createEntity());
        return loadById(mse.getId());
    }

    /**
     * merge a NestingPath instance into the database
     *
     * @return the merged instance
     */
    private NestingPath save(NestingPath np) {

        /* 
         * xxxxxx REFACTOR THIS (id is not set automatically)
        */
        
        NestingPathSetEntity npse=this.em.merge(
                new NestingPathSetEntity(np.getId(),
                        np.getMembership()));
        
        for (NestingPathEntity entity : np.createEntity()) {
            entity.getId().setNestingpathsets_id(npse.getId());
            this.em.merge(entity);
        }

        return np;
    }

}
