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
package de.ipb_halle.lbac.search.wordcloud;

import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.entity.Document;
import de.ipb_halle.lbac.file.TermFrequencyList;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.search.document.DocumentSearchState;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.webservice.service.LbacWebService;
import de.ipb_halle.lbac.webservice.service.NotAuthentificatedException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

/**
 * REST endpoint for WordCloud Documents.
 * <ol>
 * <li> search for Documents in readable collections matching the terms</li>
 * <li> getting all Terms and ther frequencys of found documents</li>
 * <li> sending documents with terms back</li>
 * </ol>
 *
 */
@Path("/termvector")
@Stateless
public class WordCloudWebService extends LbacWebService {

    @Inject
    private DocumentSearchService searchService;

    @Inject
    private CollectionService collectionService;

    @Inject
    private TermVectorEntityService termVectorService;

    private Logger logger;

    @PostConstruct
    public void TermVectorWebServiceInit() {
        logger = LogManager.getLogger(WordCloudWebService.class);
    }

    /**
     * Consumes a REST REquest with and loads all Documents with the given
     * search terms, gathers all terms for each document and sends them back
     *
     * @param request
     * @return Webrequest with found documents
     */
    @POST
    @Produces(MediaType.APPLICATION_XML)
    public WordCloudWebRequest getDocumentsWithTermVector(WordCloudWebRequest request) {
        try {
            checkAuthenticityOfRequest(request);
        } catch (NotAuthentificatedException e) {
            request.setStatusCode("403:webrequest not authetificated." + e.getMessage());
            return request;
        }
        try {
            List<Collection> collections = new ArrayList<>();
            for (Integer id : request.getIdsOfReadableCollections()) {
                collections.add(collectionService.loadById(id));
            }

            DocumentSearchState docSeachState = new DocumentSearchState();
            docSeachState = searchService.actionStartDocumentSearch(
                    docSeachState,
                    collections,
                    searchService.getTagStringForSeachRequest(request.getTerms()),
                    Integer.MAX_VALUE, 0, searchService.getUriOfPublicCollection());
            
            for (Document d : docSeachState.getFoundDocuments()) {
                d.setTermFreqList(
                        new TermFrequencyList(
                                termVectorService.getTermVector(
                                        Arrays.asList(d.getId()),
                                        request.getMaxTerms()
                                )
                        ));
                d.getTermFreqList()
                        .getUnstemmedWords().addAll(
                                termVectorService.loadUnstemmedWordsOfDocument(
                                        d.getId(),
                                        d.getTermFreqList().getWordRoots()
                                ));

                request.getDocumentsWithTerms().add(d);
            }

        } catch (Exception e) {
            logger.error(e);
        }
        return request;

    }

}
