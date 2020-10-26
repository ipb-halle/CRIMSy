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
import de.ipb_halle.lbac.search.NetObject;
import de.ipb_halle.lbac.search.SearchQueryStemmer;
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
    protected RelevanceCalculator relevanceCalculator = new RelevanceCalculator();
    protected User currentUser;
    StemmedWordGroup normalizedTerms;

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
        String filteredSearchText = searchFilter.getSearchTerms()
                .toLowerCase()
                .replace("(", "")
                .replace(")", "")
                .replace(" or ", " ");

        List<String> searchTerms = Arrays.asList(
                filteredSearchText
                        .split(" ")
        );

        relevanceCalculator = new RelevanceCalculator(searchTerms);

        SearchQueryStemmer searchQueryStemmer = new SearchQueryStemmer();
        // fetches all documents of the collection and adds the total 
        // number of documents in the collection to the search state
        normalizedTerms = searchQueryStemmer.stemmQuery(filteredSearchText);

        relevanceCalculator.setSearchTerms(normalizedTerms);

        searchState = new SearchState();

        SearchResult result = searchService.search(searchFilter.createRequests());
        searchState.addNetObjects(result.getAllFoundObjects());
        searchState.addNewStats(
                result.getDocumentStatistic().totalDocsInNode,
                result.getDocumentStatistic().averageWordLength);
        actionAddFoundObjectsToShownObjects();
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

}
