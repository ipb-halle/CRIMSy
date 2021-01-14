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
 * Provides a webservice endpoint for deactivate groups
 *
 * @author fmauz
 */
@Path("/groups")
@Stateless
public class GroupWebService extends LbacWebService {

    @Inject
    private GlobalAdmissionContext globalAdmissionContext;

    @Inject
    private MemberService memberService;

    private Logger logger;

    /**
     * default constructor
     */
    public GroupWebService() {
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    @PostConstruct
    public void init() {

    }

    /**
     * save a remote group from a node
     *
     * @param request the current node object
     * @return the serialized node list
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response handleRequest(GroupWebRequest request) {
        try {
            checkAuthenticityOfRequest(request);
        } catch (NotAuthentificatedException e) {
            Response.status(Response.Status.FORBIDDEN).build();
        }
        try {
            Group localGroup = loadRemoteGroup(request.getGroup());
            if (memberService.canGroupBeDeactivated(request.getGroup())) {
                memberService.deactivateGroup(localGroup);
            }

            return Response.ok(request).build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    private Group loadRemoteGroup(Group g) throws Exception {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put("name", g.getName());
        cmap.put("subSystemType", AdmissionSubSystemType.LBAC_REMOTE);
        cmap.put("node_id", g.getNode().getId());
        List<Group> foundGroups = memberService.loadGroups(cmap);
        if (!foundGroups.isEmpty()) {
            return foundGroups.get(0);
        }
        throw new Exception("No goup found");
    }

}
