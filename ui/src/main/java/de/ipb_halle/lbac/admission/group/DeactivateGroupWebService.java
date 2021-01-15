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
package de.ipb_halle.lbac.admission.group;

import de.ipb_halle.lbac.admission.AdmissionSubSystemType;
import de.ipb_halle.lbac.admission.Group;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.webservice.service.LbacWebService;
import de.ipb_halle.lbac.webservice.service.NotAuthentificatedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
@Path("/groups/deactivate")
@Stateless
public class DeactivateGroupWebService extends LbacWebService {

    @Inject
    private MemberService memberService;

    private Logger logger;

    public DeactivateGroupWebService() {
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    @PostConstruct
    public void init() {

    }

    /**
     * Deactivated a remote group. Checks if this group is present and can be
     * deleted.
     *
     * @param request the request with the group to deactivate
     * @return Webresponse with status of operation
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response handleRequest(DeactivateGroupWebRequest request) {
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
            logger.warn(String.format(
                    "Request to deactivate group %s from node %s was not successfull",
                    request.getGroup().getName(),
                    request.getNodeIdOfRequest().toString()));
            logger.error(e);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
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
        throw new Exception("No group found to delete");
    }

}
