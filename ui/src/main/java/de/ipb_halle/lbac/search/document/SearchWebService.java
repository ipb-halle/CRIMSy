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
package de.ipb_halle.lbac.search.document;

import de.ipb_halle.lbac.entity.Collection;
import de.ipb_halle.lbac.entity.Document;
import de.ipb_halle.lbac.service.CollectionService;
import de.ipb_halle.lbac.webservice.service.LbacWebService;
import de.ipb_halle.lbac.webservice.service.NotAuthentificatedException;
import java.util.Arrays;
import javax.annotation.PostConstruct;

import javax.ejb.Stateless;
import javax.faces.application.ProjectStage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

/**
 * Consumes a SearchRequest and repsones a list of matching documents
 * <ol>
 * <li> checks if the request is authentificated</li>
 * <li> seaches for all matching documents via @see DocumentSearchService</li>
 * <li> generates a response with the found documents and the total amount</li>
 * </ol> documents in the collection
 */
@Path("/search")
@Stateless
public class SearchWebService extends LbacWebService {
    
    @Inject
    DocumentSearchService documentSearchService;
    
    @Inject
    CollectionService collectionService;
    
    private final Logger LOGGER = LogManager.getLogger(SearchWebService.class);
    
    private boolean develop = false;

    /**
     * Gets all documents matching the search term
     *
     * @param request
     * @return the request with the found Documents and the total amount of
     * Documents in the collection. In the field statusCode there are
     * informations if a Exception happend:
     * <ol>
     * <li> 200 - OK</li>
     * <li> 500 - Unexpected Exception</li>
     * <li> 401 - Not authentificated Request</li>
     * </ol>
     *
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response search(DocumentSearchRequest request) {
        try {
            if (develop) {
                infoStart(request);
            }
            
            try {
                checkAuthenticityOfRequest(request);
                
                DocumentSearchState state = new DocumentSearchState();
                
                Collection col = collectionService.loadById(request.getCollectionId());
                
                state = documentSearchService.actionStartDocumentSearch(
                        state,
                        Arrays.asList(col),
                        request.getSearchQuery().getQuery(),
                        (int) request.getLimit(),
                        (int) request.getOffset(),
                        documentSearchService.getUriOfPublicCollection());
                
                request.addAll(state.getFoundDocuments());
                
                int totalDocsInColl = (int) collectionService.getFileCount(col.getId());
                request.setTotalDocsInCollection(totalDocsInColl);
                
                long totalWordsOfNode = documentSearchService.getSumOfWordsOfAllDocs();
                request.setSumOfWordsOfNode(totalWordsOfNode);
                
                if (develop) {
                    infoFinish(request);
                }
            } catch (NotAuthentificatedException e2) {
                request.setStatusCode("401:" + e2.getMessage());
            } catch (Exception e) {
                request.setStatusCode("500:" + e.getMessage());
            }
            
            return Response.ok(request).build();
        } catch (Exception e) {
            LOGGER.error(e);
            return Response.serverError().build();
        }
    }
    
    @PostConstruct
    public void init() {
        try {
            if (FacesContext.getCurrentInstance() != null && FacesContext.getCurrentInstance().getApplication().getProjectStage() == ProjectStage.Development) {
                develop = true;
            }
        } catch (Exception e) {
            
        }
    }
    
    private void infoStart(DocumentSearchRequest request) {
        LOGGER.info("");
        LOGGER.info("Start webservice ");
        LOGGER.info(" Coll-ID " + request.getCollectionId());
        LOGGER.info(" Search Term " + request.getSearchQuery().getQuery());
    }
    
    private void infoFinish(DocumentSearchRequest request) {
        LOGGER.info("");
        LOGGER.info("finished webservice ");
        LOGGER.info(" total found docs " + request.getTotalDocsInCollection());
        for (Document d : request.getResultList()) {
            LOGGER.info("-- " + d.getOriginalName());
        }
        LOGGER.info("--------");
    }
}
