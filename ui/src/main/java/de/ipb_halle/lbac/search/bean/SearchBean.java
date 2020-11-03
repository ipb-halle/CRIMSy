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
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.bean.MaterialOverviewBean;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.search.NetObject;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.search.SearchService;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.document.Document;
import de.ipb_halle.lbac.search.document.StemmedWordGroup;
import de.ipb_halle.lbac.search.relevance.RelevanceCalculator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    @Inject
    private SearchService searchService;

    protected NetObjectPresenter netObjectPresenter = new NetObjectPresenter();
    protected SearchState searchState = new SearchState();
    protected Logger logger = LogManager.getLogger(this.getClass().getName());
    protected List<NetObject> shownObjects = new ArrayList<>();
    protected SearchFilter searchFilter;
    protected RelevanceCalculator relevanceCalculator = new RelevanceCalculator(new ArrayList<>());
    protected User currentUser;
    StemmedWordGroup normalizedTerms;
    protected boolean advancedSearch;

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

    public SearchBean() {
    }

    public SearchBean(
            SearchService searchService,
            User user) {
        this.searchService = searchService;
        setCurrentAccount(new LoginEvent(user));

    }

    public NetObjectPresenter getNetObjectPresenter() {
        return netObjectPresenter;
    }

    public void setCurrentAccount(@Observes LoginEvent evt) {
        currentUser = evt.getCurrentAccount();
        searchFilter = new SearchFilter(currentUser);
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
        relevanceCalculator = new RelevanceCalculator(parseSearchTerms());
        searchState = doSearch();
        actionAddFoundObjectsToShownObjects();
    }

    private SearchState doSearch() {
        SearchState searchState = new SearchState();
        SearchResult result = searchService.search(searchFilter.createRequests());
        searchState.addNetObjects(result.getAllFoundObjects());
        searchState.addNewStats(
                result.getDocumentStatistic().totalDocsInNode,
                result.getDocumentStatistic().averageWordLength);
        return searchState;
    }

    private List<String> parseSearchTerms() {
        List<String> back = new ArrayList<>();
        if (searchFilter.getSearchTerms() != null) {
            back = Arrays.asList(searchFilter.getSearchTerms()
                    .toLowerCase()
                    .replace("(", "")
                    .replace(")", "")
                    .replace(" or ", " ")
                    .trim()
                    .split(" "));
        }
        return back;
    }

    public SearchFilter getSearchFilter() {
        return searchFilter;
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
        if (no.getSearchable().getTypeToDisplay().getGeneralType() == SearchTarget.MATERIAL) {
            materialBean.actionEditMaterial((Material) no.getSearchable());
        }
        if (no.getSearchable().getTypeToDisplay().getGeneralType() == SearchTarget.ITEM) {
            itemBean.actionStartItemEdit((Item) no.getSearchable());
        }
        if (no.getSearchable().getTypeToDisplay().getGeneralType() == SearchTarget.EXPERIMENT) {
            experimentBean.setExperiment((Experiment) no.getSearchable());
            experimentBean.loadExpRecords();
            navigator.navigate("exp/experiments");

        }

    }

    public boolean isAdvancedSearch() {
        return advancedSearch;
    }

    public void toogleAdvancedSearch() {
        searchFilter.toogleAdvancedSearch();
       
    }

    public int getTextFieldLength() {
        if (!userBean.hasUploadPermission()) {
            return 8;
        } else {
            return 6;
        }
    }

}
