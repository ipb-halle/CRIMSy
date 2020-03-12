/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.announcement.membership;

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
import de.ipb_halle.lbac.admission.AdmissionSubSystemType;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.entity.Group;
import de.ipb_halle.lbac.entity.Membership;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.service.MemberService;
import de.ipb_halle.lbac.service.MembershipService;
import de.ipb_halle.lbac.service.NodeService;

import de.ipb_halle.lbac.webservice.service.LbacWebService;
import de.ipb_halle.lbac.webservice.service.NotAuthentificatedException;

import java.util.Iterator;
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

import org.apache.log4j.Logger;

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
        this.logger = Logger.getLogger(this.getClass().getName());
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
        if ((node != null) && this.nodeService.isRemoteNode(node)) {

            boolean userAdded = handleUser(user, node);
            boolean currentMembershipsSet = handleGroups(user, request.getGroups(), node);
            boolean vanishedMembershipsCleared = clearVanishedMemberships(user, request.getGroups(), node);

            result = userAdded
                    && vanishedMembershipsCleared
                    && currentMembershipsSet;

        } else {
            this.logger.warn("handleRequest() received update request for illegal node");
        }
        if (!result) {
            request.setStatusCode("500:Could not announce user" + user.getId().toString() + " at node " + node.getId());
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
     * @param remoteUser the user
     * @param gs the list of groups from webRequest
     * @param remoteNode the local database instance of the remote node
     * @return false on failure
     */
    private boolean handleGroups(
            User remoteUser,
            Set<Group> gs,
            Node remoteNode) {

        for (Group group : gs) {
            try {

                boolean isPublicNode = group.getNode().getId()
                        .toString().equals(GlobalAdmissionContext.PUBLIC_NODE_ID);
                // if the node of the group to edit is not the node from the 
                // request or the anonymus public node do nothing
                if (!group.getNode().equals(remoteNode)
                        && !isPublicNode) {
                    throw new Exception(
                            "handleGroups(): illegal node on remote group "
                            + remoteNode.getInstitution()
                            + " Group " + group.getName());
                }

                // load the local represantation of the remote group
                Group localGroup = this.memberService.loadGroupById(group.getId());

                if (isPublicNode) {

                } else if (localGroup != null && localGroup.getNode().equals(remoteNode)) {
                    //throw new Exception(String.format("handleGroups(): remote group would illegally override local group"));
                } else {
                    mergeGroupAsRemoteGroup(group, remoteNode);
                }

                this.membershipService.addMembership(group, remoteUser);

            } catch (Exception e) {
                this.logger.error(e.getMessage());
            }

        }
        return true;
    }

    /**
     * Clears all memberships for a user remoteUser from the local database, if
     * the groups are not present in gs
     *
     * @param remoteUser
     * @param gs
     * @param localNode
     * @return
     */
    private boolean clearVanishedMemberships(
            User remoteUser,
            Set<Group> gs,
            Node localNode) {
        try {
            Map<UUID, Membership> nonLocalGroupMemberships = this.membershipService.loadMemberOf(remoteUser)
                    .stream()
                    .filter(m -> m.getGroup().isGroup()) // only Membership objects with groups
                    .filter(m -> !m.getGroup().getNode().equals(localNode)) // only Memberships with non-local group 
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
     * Saves the group in the datavase as a remote group
     *
     * @param group
     * @param n home node of the group
     */
    private void mergeGroupAsRemoteGroup(Group group, Node n) {
        // update group (e.g. name changes)
        group.obfuscate();
        group.setSubSystemType(AdmissionSubSystemType.LBAC_REMOTE);
        group.setNode(n);
        this.memberService.save(group);
        this.membershipService.addMembership(group, group);
    }

    /**
     * check user object for plausibility and persist object in the database.
     * Add also standard memberships (i.e. publicGroup and the user itself).
     *
     * @param u the user
     * @param n the local database instance of the remote node
     * @return false on failure
     */
    private boolean handleUser(User u, Node n) {

        User localUser = this.memberService.loadUserById(u.getId());

        if (localUser != null) {

            if ((!localUser.getNode().equals(n))
                    || (localUser.getSubSystemType() != AdmissionSubSystemType.LBAC_REMOTE)) {
                return false;
            }
        }
        u.obfuscate();
        u.setSubSystemType(AdmissionSubSystemType.LBAC_REMOTE);
        u.setNode(n);
        this.memberService.save(u);
        this.membershipService.addMembership(u, u);
        this.membershipService.addMembership(this.globalAdmissionContext.getPublicGroup(), u);
        return true;
    }

}
