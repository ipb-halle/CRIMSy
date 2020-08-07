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
package de.ipb_halle.lbac.forum.postings;

import de.ipb_halle.lbac.forum.ForumService;
import de.ipb_halle.lbac.forum.Posting;
import de.ipb_halle.lbac.service.MemberService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.webservice.service.LbacWebService;
import de.ipb_halle.lbac.webservice.service.NotAuthentificatedException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

/**
 *
 * @author fmauz
 */
@Path("/postings")
@Stateless
public class PostingWebService extends LbacWebService {

    private Logger logger = LogManager.getLogger(PostingWebService.class);

    @Inject
    private ForumService forumService;

    @Inject
    private MemberService memberService;

    @Inject
    private NodeService nodeService;

    /**
     * We do not save User objects although Users might not be known,
     * as severe side effects may occur. Losing a posting is less 
     * dangerous.
     * 
     * @param request
     * @return 
     */
    @POST
    @Produces(MediaType.APPLICATION_XML)
    public Response addPosting(PostingWebRequest request) {
        try {
            try {
                checkAuthenticityOfRequest(request);
            } catch (NotAuthentificatedException e) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
            // add the last Posting of the list to the database
            Posting p = request.getTopic().getPostings().get(request.getTopic().getPostings().size() - 1);
            p.setTopic(request.getTopic());

            forumService.addPostingToTopic(request.getTopic(), p.getText(), p.getOwner(), p.getCreated());

        } catch (Exception e) {
            logger.error(e);
        }
        return Response.ok().build();
    }
}
