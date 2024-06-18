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
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.webservice.service.LbacWebService;
import de.ipb_halle.lbac.webservice.service.NotAuthentificatedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

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
     * We do not save User objects although Users might not be known, as severe
     * side effects may occur. Losing a posting is less dangerous.
     *
     * @param request
     * @return
     */
    @POST
    @Produces(MediaType.APPLICATION_XML)
    public Response addPosting(PostingWebRequest request) {
        Posting p = null;
        try {
            try {
                checkAuthenticityOfRequest(request);
            } catch (NotAuthentificatedException e) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }
            // add the last Posting of the list to the database
            p = request.getTopic().getPostings().get(request.getTopic().getPostings().size() - 1);
            p.setTopic(request.getTopic());
            forumService.addPostingToTopic(request.getTopic(), p.getText(), getLocalUser(p), p.getCreated());
        } catch (Exception e) {
            logError(p, e);
        }
        return Response.ok().build();
    }

    private User getLocalUser(Posting p) throws Exception {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put("name", p.getOwner().getName());
        cmap.put("node_id", p.getOwner().getNode().getId());
        List<User> localUser = memberService.loadUsers(cmap);
        if (localUser.size() > 0) {
            return localUser.get(0);
        }
        throw new Exception("Could not find local User");
    }

    private void logError(Posting p, Exception e) {
        logger.error("Could not save posting ");
        if (p != null) {
            logger.error("  User: " + p.getOwner());
            logger.error("  Text: " + p.getText());
            logger.error("  Topic: " + p.getTopic());
            logger.error("  TopicDate: " + p.getCreated());
        }
        logger.error("stack trace:", (Throwable) e); 
    }
}
