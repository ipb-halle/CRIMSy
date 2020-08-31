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

import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.entity.Document;
import de.ipb_halle.lbac.file.TermFrequency;
import de.ipb_halle.lbac.search.SolrSearcher;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.faces.application.ProjectStage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author fmauz
 */
@Stateless
public class DocumentSearchService {

    private final Logger LOGGER = LogManager.getLogger(DocumentSearchService.class);

    @Inject
    private FileEntityService fileEntityService;

    @Inject
    private SolrSearcher solrSearcher;

    @Inject
    private NodeService nodeService;

    @Inject
    private CollectionService collectionService;

    @Inject
    private TermVectorEntityService termVectorEntityService;

    private final int MAX_TERMS = Integer.MAX_VALUE;

    private boolean development = false;

    private String uriOfPublicColl;

    /**
     *
     * @param searchState
     * @param collsToSearchIn
     * @param searchText
     * @param limit
     * @param offSet
     * @param uriOfPublicColl
     * @return
     * @throws Exception
     */
    public DocumentSearchState actionStartDocumentSearch(
            DocumentSearchState searchState,
            List<Collection> collsToSearchIn,
            String searchText,
            int limit,
            int offSet,
            String uriOfPublicColl) throws Exception {
        if (development) {
            infoStart(collsToSearchIn, searchText);
        }
        this.uriOfPublicColl = uriOfPublicColl;
        searchState.clearState();

        // fetches all documents of the collection and adds the total 
        // number of documents in the collection to the search state
        for (Collection coll : collsToSearchIn) {
            if (coll.getNode().equals(nodeService.getLocalNode())) {

                DocumentSearchRequest searchReq = new DocumentSearchRequest();
                searchReq.setCollectionId(coll.getId());
                searchReq.setLimit(limit);
                searchReq.setOffset(offSet);
                searchReq.setNodeId(nodeService.getLocalNode().getId());

                DocumentSearchQuery sq = new DocumentSearchQuery();
                sq.setQuery(searchText);
                searchReq.setSearchQuery(sq);

                solrSearcher.setRequest(searchReq);
                DocumentSearchRequest searchReqWithResults = solrSearcher.search();

                int totalDocsInColl = (int) collectionService.getFileCount(coll.getId());

                searchState.addToTotalDocs(totalDocsInColl);
                if (searchReqWithResults != null) {
                    List<Document> results = searchReqWithResults.getResultList();
                    for (Document d : results) {
                        if (!searchState.getFoundDocuments().contains(d)) {
                            searchState.getFoundDocuments().add(d);
                        }
                    }
                }
            }
        }
        Set<String> normalizedTerms = getNormalizedWordsInAllLanguages(searchState.getFoundDocuments(), Arrays.asList(searchText.split(" ")));

        List<Integer> docIds = new ArrayList<>();
        for (Document d : searchState.getFoundDocuments()) {
            docIds.add(d.getId());
        }

        Map<Integer, Map<String, Integer>> totalTerms = termVectorEntityService.getTermVectorForSearch(
                docIds,
                normalizedTerms);

        String normalizedSearchTerm = String.join("-", normalizedTerms);

        for (Document d : searchState.getFoundDocuments()) {
            if (development) {
                LOGGER.info("FOUND: " + d.getOriginalName());
            }
            Map<String, Integer> terms = totalTerms.get(d.getId());
            if (terms != null) {
                for (String key : terms.keySet()) {
                    d.setWordCount(d.getWordCount() + terms.get(key));
                    if (normalizedSearchTerm.toLowerCase().contains(key)) {
                        d.getTermFreqList().getTermFreq().add(new TermFrequency(key, terms.get(key)));
                    }
                }
            }
        }
        if (development) {
            //infoCollection(searchState.getFoundDocuments(), searchState.getTotalDocs());
        }

        return searchState;
    }

    /**
     * Gets the total sum of all words over all collections and all documents
     *
     * @return
     */
    public long getSumOfWordsOfAllDocs() {
        return termVectorEntityService.getSumOfAllWordsFromAllDocs();
    }

    @PostConstruct
    public void init() {
        if (FacesContext.getCurrentInstance() != null && FacesContext.getCurrentInstance().getApplication().getProjectStage() == ProjectStage.Development) {
            development = true;
        }
    }

    private void infoStart(List<Collection> colls, String searchText) {
        LOGGER.info("");
        LOGGER.info(String.format("Start documentsearch in %d collections with searchtext: %s", colls.size(), searchText));
        LOGGER.info("Searching in ");
        for (Collection c : colls) {
            LOGGER.info("--> " + c.getName() + ":" + c.getNode().getInstitution());
        }
        LOGGER.info("----");
    }

    /**
     * Concatinates the terms with and AND for the solR querry.If there is only
     * one term the term is given back
     *
     * @param tagList
     * @return string for solr querry
     */
    public String getTagStringForSeachRequest(Set<String> tagList) {
        if (tagList == null || tagList.isEmpty()) {
            return "";
        }
        if (tagList.size() == 1) {
            return new ArrayList<>(tagList).get(0);
        }
        String back = "";
        for (String s : tagList) {
            back += " AND " + s;
        }

        back = back.substring(4, back.length());
        return back.trim();
    }

    public String getUriOfPublicCollection() {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put("name", "public");
        List<Collection> colls = collectionService.load(cmap);

        String restUri = null;
        if (colls != null && colls.size() > 0) {
            restUri = colls.get(0).getIndexPath();
        }
        return restUri;
    }

    private Set<String> getNormalizedWordsInAllLanguages(List<Document> docs, List<String> splittedTerms) {
        Set<String> normalizedWords = new HashSet<>();
        for (Document d : docs) {
            normalizedWords.addAll(solrSearcher.getNormalizedSearchTerms(splittedTerms, uriOfPublicColl, d.getLanguage()));
        }
        return normalizedWords;

    }
}
