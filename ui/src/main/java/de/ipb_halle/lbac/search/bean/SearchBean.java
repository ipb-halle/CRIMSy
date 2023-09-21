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
package de.ipb_halle.lbac.search.bean;

import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.exp.Experiment;
import de.ipb_halle.lbac.exp.ExperimentBean;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.bean.ItemOverviewBean;
import de.ipb_halle.lbac.material.JsfMessagePresenter;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.bean.MaterialOverviewBean;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.search.NetObject;
import de.ipb_halle.lbac.search.SearchOrchestrator;
import de.ipb_halle.lbac.search.SearchQueryStemmer;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.search.SearchService;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.document.Document;
import de.ipb_halle.lbac.search.relevance.RelevanceCalculator;
import de.ipb_halle.lbac.service.NodeService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class SearchBean implements Serializable {

    private final int POLLING_INTERVALL_ACTIVE = 1000;
    private final int POLLING_INTERVALL_INACTIVE = 1000 * 60 * 60;

    @Inject
    private SearchService searchService;

    protected NetObjectPresenter netObjectPresenter;
    protected SearchState searchState = new SearchState();
    protected Logger logger = LogManager.getLogger(this.getClass().getName());
    protected List<NetObject> shownObjects = new ArrayList<>();
    protected SearchFilter searchFilter;
    protected RelevanceCalculator relevanceCalculator = new RelevanceCalculator(new HashSet<>());
    protected User currentUser;

    @Inject
    private MaterialOverviewBean materialBean;

    @Inject
    private ItemOverviewBean itemBean;

    @Inject
    private Navigator navigator;

    @Inject
    private ExperimentBean experimentBean;

    @Inject
    private UserBean userBean;

    @Inject
    private SearchOrchestrator orchestrator;

    @Inject
    private NodeService nodeService;

    public SearchBean() {
    }

    /**
     * Constructor for tests
     *
     * @param searchService
     * @param user
     * @param nodeService
     */
    public SearchBean(
            SearchService searchService,
            User user,
            NodeService nodeService) {
        this.searchService = searchService;
        setCurrentAccount(new LoginEvent(user));
        this.nodeService = nodeService;

    }

    public NetObjectPresenter getNetObjectPresenter() {
        return netObjectPresenter;
    }

    public void setCurrentAccount(@Observes LoginEvent evt) {
        currentUser = evt.getCurrentAccount();
        searchFilter = new SearchFilter(currentUser);
        this.netObjectPresenter = new NetObjectPresenter(currentUser, JsfMessagePresenter.getInstance());
    }

    public void actionAddFoundObjectsToShownObjects() {
        for (NetObject noToAdd : searchState.getFoundObjects()) {
            boolean alreadyIn = false;
            for (NetObject no : shownObjects) {
                if (no.isEqualTo(noToAdd)) {
                    alreadyIn = true;
                }
            }
            if (!alreadyIn) {
                shownObjects.add(noToAdd);
            }
        }
        relevanceCalculator.calculateRelevanceFactors(
                searchState.getTotalDocs(),
                searchState.getAverageDocLength(),
                getDocumentsFromResults());

    }

    public int getAmountOfNotShownObjects() {
        return searchState.getFoundObjects().size() - shownObjects.size();
    }

    public String getActualizeButtonStyleClass() {
        if (!isSearchActive() && getAmountOfNotShownObjects() > 0) {
            return "pc_refreshButton pulsingButton";
        } else {
            return "pc_refreshButton";
        }
    }

    private List<Document> getDocumentsFromResults() {
        List<Document> docs = new ArrayList<>();
        for (NetObject no : shownObjects) {
            if (no.getSearchable().getTypeToDisplay().getGeneralType() == SearchTarget.DOCUMENT) {
                docs.add((Document) no.getSearchable());
            }
        }
        return docs;
    }

    public void actionTriggerSearch() {
        shownObjects.clear();
        Set<String> terms = new SearchQueryStemmer().stemmQuery(searchFilter.getSearchTerms());
        relevanceCalculator = new RelevanceCalculator(terms);
        searchState = doSearch();
        actionAddFoundObjectsToShownObjects();
    }

    private SearchState doSearch() {
        searchState = new SearchState();
        //local
        SearchResult result = searchService.search(
                searchFilter.createRequests(),
                nodeService.getLocalNode());
        searchState.addNetObjects(result.getAllFoundObjects());
        searchState.addNewStats(
                result.getDocumentStatistic().getTotalDocsInNode(),
                result.getDocumentStatistic().getAverageWordLength());
        //remote
        orchestrator.startRemoteSearch(searchState, currentUser, searchFilter.createRequests());

        return searchState;
    }

    public SearchFilter getSearchFilter() {
        return searchFilter;
    }

    public boolean isMaterialTypeVisible() {
        return searchFilter.getTypeFilter().isMaterials();
    }

    public List<NetObject> getShownObjects() {
        return shownObjects;
    }

    public SearchState getSearchState() {
        return searchState;
    }

    public boolean isSearchActive() {
        return searchState.isSearchActive();
    }

    public int getUnshownButFoundObjects() {
        return searchState.getFoundObjects().size() - shownObjects.size();
    }

    public void navigateToObject(NetObject no) {
        if (currentUser.isPublicAccount()) {
            return;
        }
        if (no.getSearchable().getTypeToDisplay().getGeneralType() == SearchTarget.MATERIAL) {
            materialBean.actionEditMaterial((Material) no.getSearchable());
        }
        if (no.getSearchable().getTypeToDisplay().getGeneralType() == SearchTarget.ITEM) {
            itemBean.actionStartItemEdit((Item) no.getSearchable());
        }
        if (no.getSearchable().getTypeToDisplay().getGeneralType() == SearchTarget.EXPERIMENT) {
            Experiment experiment = (Experiment) no.getSearchable();
            experimentBean.setExperiment(experiment);
            experimentBean.setSearchTerm(experiment.getCode());
            experimentBean.actionActualizeExperimentsList();
            experimentBean.loadExpRecords();
            navigator.navigate("exp/experiments");
        }
    }

    public void toogleAdvancedSearch() {
        searchFilter.toogleAdvancedSearch();
        if (searchFilter.isAdvancedSearch()) {
            searchFilter.init();
        }

    }

    public int getTextFieldLength() {
        if (!userBean.hasUploadPermission()) {
            return 8;
        } else {
            return 6;
        }
    }

    public boolean isMolEditorVisible() {
        return isMaterialTypeVisible()
                && searchFilter.isAdvancedSearch()
                && searchFilter.getMaterialTypeFilter().isStructures();
    }

    public String getAdvancedSearchIcon() {
        if (searchFilter != null && searchFilter.isAdvancedSearch()) {
            return "fa-minus-circle";
        } else {
            return "fa-plus-circle";
        }
    }

    public void setOrchestrator(SearchOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    public int getPollIntervall() {
        if (isSearchActive()) {
            return POLLING_INTERVALL_ACTIVE;
        } else {
            return POLLING_INTERVALL_INACTIVE;
        }
    }

}
