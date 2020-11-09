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
package de.ipb_halle.lbac.search;

import de.ipb_halle.lbac.webservice.service.LbacWebService;
import de.ipb_halle.lbac.webservice.service.NotAuthentificatedException;
import java.util.ArrayList;
import java.util.List;
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

@Path("/search")
@Stateless
public class SearchWebService extends LbacWebService {

    RemoteTransformerFactory transformerFactory;

    @PostConstruct
    public void init() {
        transformerFactory = new RemoteTransformerFactory();
    }

    @Inject
    private SearchService searchService;

    private final Logger LOGGER = LogManager.getLogger(SearchWebService.class);

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response search(SearchWebRequest request) {
        try {
            SearchWebResponse response = new SearchWebResponse();
            try {
                checkAuthenticityOfRequest(request);
                List<SearchRequest> requests = new ArrayList<>();
                for (SearchRequestImpl i : request.getSearchRequests()) {
                    requests.add((SearchRequest) i);
                }
                SearchResult localResult = searchService.search(requests);
                for (Searchable searchable : localResult.getAllFoundObjects(localResult.getNode())) {
                    RemoteTransformer transformer = transformerFactory.createSpecificTransformer(searchable);
                    response.addFoundObject(transformer.transformToRemote());

                }
            } catch (NotAuthentificatedException e2) {
                response.setStatusCode("401:" + e2.getMessage());
            }
            return Response.ok(response).build();
        } catch (Exception e) {
            LOGGER.error(e);
            return Response.serverError().build();
        }
    }

}
