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

import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.collections.CollectionBean;
import de.ipb_halle.lbac.entity.Document;
import de.ipb_halle.lbac.i18n.UIMessage;
import de.ipb_halle.lbac.search.SolrSearcher;
import de.ipb_halle.lbac.search.relevance.RelevanceCalculator;
import de.ipb_halle.lbac.service.CollectionService;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.faces.application.ProjectStage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

/**
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class DocumentSearchBean implements Serializable {

    private final int MAX_RESULTS = Integer.MAX_VALUE;
    private final int POLLING_INTERVALL_ACTIVE = 1000;
    private final int POLLING_INTERVALL_INACTIVE = 1000 * 60 * 60;
    private final Logger logger = LogManager.getLogger(DocumentSearchBean.class);
    private int pollCounter = 0;
    private boolean tableUpdateLocked = false;
    private final List<Document> shownDocs = new ArrayList<>();
    protected SimpleDateFormat SDF = new SimpleDateFormat("mm:ss:SSSS");
    private DocumentSearchState documentSearchState = new DocumentSearchState();
    private RelevanceCalculator relevanceCalculator = new RelevanceCalculator(); 
    private String searchFieldText;
    private boolean develop = false;
    private boolean delayedPresentationEnabled = false;

    @Inject
    private DocumentSearchService documentSearchService;

    @Inject
    private FileEntityService fileEntityService;

    @Inject
    private CollectionBean collectionBean;

    @Inject
    private CollectionService collectionService;

    @Inject
    DocumentSearchOrchestrator orchestrator;

    @Inject
    private TermVectorEntityService termVectorEntityService;

    @Inject
    private SolrSearcher solrSearcher;

    @Inject
    private NodeService nodeService;

    /**
     * Adds new results from remote nodes to the list of shown documents. It
     * will recalculate the relevances for all docs based on the new information
     */
    public void addNewSearchResultsToTable() {
        try {
            relevanceCalculator.setSearchTerms(
                    getNormalizedSearchTerms(
                            documentSearchState.getFoundDocuments(),
                            relevanceCalculator.getOriginalSearchTerms())
            );
            relevanceCalculator.calculateRelevanceFactors(
                    documentSearchState.getTotalDocs(),
                    documentSearchState.getAverageDocLength(),
                    documentSearchState.getFoundDocuments()
            );
            addDocumentsToShownList(documentSearchState.getFoundDocuments());
        } catch (Exception e) {
            logger.error("Error", e);
        }
    }

    /**
     * Triggers a new search. Deletes all current found docs and searches for
     * local documents first. After that it triggers a remote search on all
     * found remote collections and puts asynchronly the remote documents into
     * the documentsearchstate.
     */
    public void actionStartSearch() {
        documentSearchState.clearState();
        pollCounter = 0;
        shownDocs.clear();

        //The solR request must be without brackets (syntax request of solr)
        // the OR Operator is implicite
        String filteredSearchText = searchFieldText
                .toLowerCase()
                .replace("(", "")
                .replace(")", "")
                .replace(" or ", " ");

        List<String> searchTerms = Arrays.asList(
                filteredSearchText
                        .split(" ")
        );

        relevanceCalculator = new RelevanceCalculator(searchTerms);
        relevanceCalculator.setDevelop(develop);
        try {
            documentSearchState = documentSearchService.actionStartDocumentSearch(
                    documentSearchState,
                    collectionBean.getCollectionSearchState().getCollections(),
                    filteredSearchText,
                    MAX_RESULTS,
                    0,
                    documentSearchService.getUriOfPublicCollection());

        } catch (Exception e) {
            logger.error("Error at fetching local docs", e);
            UIMessage.error("Error at getting Documents");
        }

        documentSearchState.setTotalDocs(fileEntityService.load(null).size());

        documentSearchState.getStats()
                .addSearchResult(
                        nodeService.getLocalNodeId(),
                        documentSearchState.getTotalDocs(),
                        documentSearchService.getSumOfWordsOfAllDocs());

        relevanceCalculator.setSearchTerms(
                getNormalizedSearchTerms(
                        documentSearchState.getFoundDocuments(),
                        relevanceCalculator.getOriginalSearchTerms())
        );
        relevanceCalculator.calculateRelevanceFactors(
                documentSearchState.getTotalDocs(),
                documentSearchState.getAverageDocLength(),
                documentSearchState.getFoundDocuments()
        );
        addDocumentsToShownList(documentSearchState.getFoundDocuments());
        try {
            orchestrator.orchestrate(
                    collectionBean.getCollectionSearchState().getCollections(),
                    filteredSearchText,
                    documentSearchState);
        } catch (Exception e) {
            logger.error("Error at starting remote document ", e);
        }

    }

    public int getPollIntervall() {
        if (isSearchActive()) {
            return POLLING_INTERVALL_ACTIVE;
        } else {
            return POLLING_INTERVALL_INACTIVE;
        }
    }

    public int getTableRefreshPollIntervall() {
        return POLLING_INTERVALL_ACTIVE + 250;
    }

    public List<Document> getFoundDocuments() {
        Collections.sort(shownDocs);
        return shownDocs;
    }

    /**
     * Gets all different languages of a list of documents.
     *
     * @param docs
     * @return
     */
    private Set<String> getLanguagesOfDocs(List<Document> docs) {
        Set<String> languages = new HashSet<>();
        for (Document d : docs) {
            languages.add(d.getLanguage());
        }
        return languages;
    }

    /**
     * Normalizes the search terms for all languages of the documents with the
     * solr analyzing functionality. The analyzer pipeline of the public
     * collection is used for all normalizsations.
     *
     * @param docs
     * @param terms
     * @return
     */
    private HashMap<String, Set<String>> getNormalizedSearchTerms(
            List<Document> docs,
            List<String> terms) {

        HashMap<String, Set<String>> normalizedTermMap = new HashMap<>();

        Set<String> languages = getLanguagesOfDocs(docs);
        for (String lang : languages) {
            normalizedTermMap.put(lang,
                    solrSearcher.getNormalizedSearchTerms(
                            terms,
                            documentSearchService.getUriOfPublicCollection(),
                            lang)
            );
        }
        return normalizedTermMap;
    }

    public void clearBean(@Observes LoginEvent evt) {
        clearBean();
    }

    public void clearBean() {
        documentSearchState.clearState();
        shownDocs.clear();
        searchFieldText = null;
    }

    /**
     * Returns the styl class for the "add Result" button. If there are docs to
     * show and the search is completed, it will pulsate.
     *
     * @return
     */
    public String getActualizeButtonStyleClass() {
        int newDocumentsToShow = documentSearchState.getFoundDocuments().size() - shownDocs.size();
        if (!isSearchActive() && newDocumentsToShow > 0) {
            return "pc_refreshButton pulsingButton";
        } else {
            return "pc_refreshButton";
        }
    }

    /**
     * Updates the number and and animation state of the "add result" button. If
     * it is the fist poll of the search, it will also add the found results and
     * calculate the relevance
     *
     * @param event
     */
    public void refreshDocumentsToShow(ActionEvent event) {
        pollCounter++;
        int newDocumentsToShow = documentSearchState.getFoundDocuments().size() - shownDocs.size();
        if (newDocumentsToShow > 0 && pollCounter == 1&&delayedPresentationEnabled) {
            try {

                addNewSearchResultsToTable();
            } catch (Exception e) {
                logger.error("Error at adding results", e);
            }
        }

    }

    /**
     * Retuns the state (active or not) of the poll which renders the datatable
     * to show the fast results directly without clicking the button
     *
     * @return
     */
    public String getTableRefreshPollState() {
        if (!delayedPresentationEnabled) {
            return "true";
        }
        if (pollCounter == 0 && isSearchActive()) {
            return "false";
        }
        return "true";
    }

    /**
     * Adds a found document to the list of shown documents. Doublets are not
     * added
     *
     * @param newDocs
     */
    private void addDocumentsToShownList(List<Document> newDocs) {
        for (Document d : newDocs) {
            boolean isIn = false;
            for (Document d2 : shownDocs) {
                if (d.getId().equals(d2.getId())) {
                    isIn = true;
                }
            }
            if (!isIn) {
                shownDocs.add(d);
            }
        }
    }

    public int getNewDocumentsToShow() {
        return documentSearchState.getFoundDocuments().size() - shownDocs.size();
    }

    public boolean isSearchActive() {
        return !documentSearchState.getUnfinishedCollectionRequests().isEmpty();
    }

    public String getSearchFieldText() {
        return searchFieldText;
    }

    public void setSearchFieldText(String searchFieldText) {
        this.searchFieldText = searchFieldText;
    }

    public DocumentSearchState getDocumentSearchState() {
        return documentSearchState;
    }

    @PostConstruct
    public void init() {
        try {
            if (FacesContext.getCurrentInstance() != null && FacesContext.getCurrentInstance().getApplication().getProjectStage() == ProjectStage.Development) {
                develop = true;
            }

            String tmp = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("document.search.enableDelayedPresentation");
            delayedPresentationEnabled = Boolean.parseBoolean(tmp);
        } catch (Exception e) {
            delayedPresentationEnabled = false;
        }
    }

}
