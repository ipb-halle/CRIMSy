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
package de.ipb_halle.lbac.forum.topics;

import de.ipb_halle.lbac.forum.ForumService;
import de.ipb_halle.lbac.forum.Topic;
import de.ipb_halle.lbac.forum.TopicsList;
import de.ipb_halle.lbac.webservice.service.LbacWebService;
import de.ipb_halle.lbac.webservice.service.NotAuthentificatedException;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author fmauz
 */
@Path("/topics")
@Stateless
public class TopicsWebService extends LbacWebService {

    private Logger logger = LogManager.getLogger(TopicsWebService.class);

    @Inject
    private ForumService forumService;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    /**
     *
     * @param request
     * @return
     */
    @POST
    @Produces(MediaType.APPLICATION_XML)
    public Response getReadableTopics(TopicsWebRequest request) {
        try {
            try {
                checkAuthenticityOfRequest(request);
            } catch (NotAuthentificatedException e) {
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            TopicsList tplist = new TopicsList();
            tplist.setTopics(
                    // returns an empty list for unknown users
                    forumService.loadReadableTopics(
                            request.getUser(),
                            request.getCloud()
                    ));
            for (Topic t : tplist.getTopics()) {
                t.obfuscate();
            }
            Response r = Response.ok(tplist).build();

            return r;
        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));

        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();

    }
}
