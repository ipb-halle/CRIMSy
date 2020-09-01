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
 * MembershipWebService This service takes user, group and membership
 * announcements from other nodes and stores them into the local database. The
 * service has to make sure, that the following constraints are met:
 * <ul>
 * <li> local users and groups (AdmissionSubSystems BUILTIN, LOCAL, LDAP) cannot
 * be overwritten
 * <li> remote users originate from the node contacting this service
 * <li> there are only direct memberships of remote users in remote groups, i.e.
 * no nesting
 * <li> passwords and other sensitive information is wiped
 * </ul>
 */
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.service.NodeService;

import de.ipb_halle.lbac.webservice.service.LbacWebService;
import de.ipb_halle.lbac.webservice.service.NotAuthentificatedException;

import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Provides a webservice endpoint for propagating user and groups from a local
 * node to a remote node.
 *
 * @author fmauz
 */
@Path("/members")
@Stateless
public class MembershipWebService extends LbacWebService {

    @Inject
    private GlobalAdmissionContext globalAdmissionContext;

    @Inject
    private MemberService memberService;

    @Inject
    private MembershipService membershipService;

    @Inject
    private NodeService nodeService;

    private Logger logger;

    /**
     * default constructor
     */
    public MembershipWebService() {
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    @PostConstruct
    public void membershipWebServiceInit() {
        if ((this.memberService == null)
                || (this.membershipService == null)
                || (this.nodeService == null)) {
            this.logger.error("Injections failed.");
        }
    }

    /**
     * save a remote node in the database and return a list of all nodes known
     * locally.* @param request the current node object
     *
     * @param request
     * @return the serialized node list
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response handleRequest(MembershipWebRequest request) {
        try {
            checkAuthenticityOfRequest(request);
        } catch (NotAuthentificatedException e) {
            Response.status(Response.Status.FORBIDDEN).build();
        }

        Boolean result = Boolean.FALSE;
        User user = request.getUser();
        Node node = this.nodeService.loadById(user.getNode().getId());

        // do not allow to update the local node
        // and make sure the user originates from the node given in the request
        if ((node != null) 
                && this.nodeService.isRemoteNode(node)
                && node.getId().equals(request.getNodeIdOfRequest())) {

            user = handleUser(user, node);

            if (user != null) {
                Set<Group> remoteGroupSet = handleGroups(user, request.getGroups(), node);
                if (remoteGroupSet != null) {
                    result = clearVanishedMemberships(user, remoteGroupSet, node);
                }
            }
        } else {
            this.logger.warn("handleRequest() received update request for illegal node");
        }
        if (!result) {
            request.setStatusCode("500:Could not announce user" 
                    + user.getId().toString() 
                    + " at node " 
                    + node.getId().toString());
        }

        return Response.ok(request).build();
    }

    /**
     * check group memberships for plausibility and persist them in the
     * database. Create group objects as necessary. This method affects only
     * memberships from the users node. Remote users are only allowed direct
     * memberships in remote groups and only in groups from the local node or
     * the remote node of the user. No memberships from other remote nodes will
     * be recognized.
     *
     * @param user the user
     * @param gs the list of groups from webRequest
     * @param remoteNode the local database instance of the remote node
     * @return false on failure
     */
    private Set<Group> handleGroups(
            User user,
            Set<Group> gs,
            Node remoteNode) {

        Set<Group> groupSet = new HashSet<> ();
        for (Group group : gs) {
            try {
                Map<String, Object> cmap = new HashMap<> ();
                cmap.put(MemberService.PARAM_SUBSYSTEM_DATA, group.getId().toString());
                cmap.put(MemberService.PARAM_SUBSYSTEM_TYPE, AdmissionSubSystemType.LBAC_REMOTE);
                cmap.put(MemberService.PARAM_NODE_ID, remoteNode.getId());
                List<Group> localGroupList = this.memberService.loadGroups(cmap);
                Group localGroup = null;

                if ((localGroupList != null) && (localGroupList.size() == 1)) {
                    localGroup = localGroupList.get(0);
                    groupSet.add(localGroup);
                } else {
                    if ((group.getId() != null)
                            && (group.getSubSystemType() != AdmissionSubSystemType.BUILTIN)
                            && (group.getSubSystemType() != AdmissionSubSystemType.LBAC_REMOTE)
                            && (remoteNode.getId().equals(group.getNode().getId()))) {
                        localGroup = mergeGroup(group, remoteNode);
                    }
                }

                if (localGroup != null) {
                    this.membershipService.addMembership(localGroup, user);
                    groupSet.add(localGroup);
                }

            } catch (Exception e) {
                this.logger.error(e.getMessage());
            }

        }
        return groupSet;
    }

    /**
     * Clears all memberships for a user remoteUser from the local database, if
     * the groups are not present in gs
     *
     * @param remoteUser
     * @param gs
     * @param remoteNode
     * @return
     */
    private boolean clearVanishedMemberships(
            User remoteUser,
            Set<Group> gs,
            Node remoteNode) {
        try {
            Map<Integer, Membership> nonLocalGroupMemberships = this.membershipService.loadMemberOf(remoteUser)
                    .stream()
                    .filter(m -> m.getGroup().isGroup()) // only Membership objects with groups
                    .filter(m -> m.getGroup().getSubSystemType().equals(AdmissionSubSystemType.LBAC_REMOTE))
                    .collect(Collectors.toMap(k -> k.getGroup().getId(), v -> v));

            for (Group group : gs) {
                Membership ms = nonLocalGroupMemberships.get(group.getId());
                if (ms != null) {
                    nonLocalGroupMemberships.remove(group.getId());
                }
            }

            nonLocalGroupMemberships.values().forEach(m -> this.membershipService.removeMembership(m));
            return true;
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }

    /**
     * Saves the group in the database as a remote group
     * The SubSystemType is changed to LBAC_REMOTE and SubSystemData is 
     * set with the groupId from the remote system.
     * @param group
     * @param n home node of the group
     * @return the persisted group
     */
    private Group mergeGroup(Group remoteGroup, Node n) {
        Group localGroup = new Group(remoteGroup.createEntity(), n);
        localGroup.obfuscate();
        localGroup.setSubSystemType(AdmissionSubSystemType.LBAC_REMOTE);
        localGroup.setSubSystemData(localGroup.getId().toString());
        localGroup.setId(null);
        localGroup.setNode(n);
        this.memberService.save(localGroup);
        this.membershipService.addMembership(localGroup, localGroup);
        return localGroup;
    }

    /**
     * check user object for plausibility and persist object in the database.
     * Add also standard memberships (i.e. publicGroup and the user itself).
     *
     * @param u the remote user (id must not be empty!)
     * @param n the local database instance of the remote node
     * @return the persisted local user DTO
     */
    private User handleUser(User u, Node n) {

        if (u.getId() == null) {
            this.logger.warn("handleUser(): Attempt to announce remote user without Id");
            return null;
        }

        Map<String, Object> cmap = new HashMap<> ();
        cmap.put(MemberService.PARAM_SUBSYSTEM_DATA, u.getId().toString());
        cmap.put(MemberService.PARAM_SUBSYSTEM_TYPE, AdmissionSubSystemType.LBAC_REMOTE);
        cmap.put(MemberService.PARAM_NODE_ID, n.getId());
        List<User> localUserList = this.memberService.loadUsers(cmap);
        if ((localUserList != null) && (localUserList.size() ==  1)) {
            return localUserList.get(0);
        }

        u.obfuscate();
        u.setSubSystemData(u.getId().toString());
        u.setSubSystemType(AdmissionSubSystemType.LBAC_REMOTE);
        u.setId(null);
        User localUser = this.memberService.save(u);
        this.membershipService.addMembership(localUser, localUser);
        this.membershipService.addMembership(this.globalAdmissionContext.getPublicGroup(), localUser);
        return localUser;
    }

}
