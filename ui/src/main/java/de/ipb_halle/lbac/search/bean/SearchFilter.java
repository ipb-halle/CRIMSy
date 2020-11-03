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

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.exp.search.ExperimentSearchRequestBuilder;
import de.ipb_halle.lbac.items.search.ItemSearchRequestBuilder;
import de.ipb_halle.lbac.material.common.bean.MaterialSearchMaskValues;
import de.ipb_halle.lbac.material.common.search.MaterialSearchRequestBuilder;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.document.DocumentSearchRequestBuilder;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class SearchFilter {

    private User user;
    private String searchTerms;
    private SearchableTypeFilter typeFilter;
    private MaterialTypeFilter materialTypeFilter;

    private boolean advancedSearchActive;
    private int maxresults = 50;
    private final Logger logger = LogManager.getLogger(DocumentSearchService.class);

    public SearchFilter(User user) {
        this.user = user;
        typeFilter = new SearchableTypeFilter();
        materialTypeFilter = new MaterialTypeFilter();
    }

    public SearchableTypeFilter getTypeFilter() {
        return typeFilter;
    }

    public List<SearchRequest> createRequests() {
        if (advancedSearchActive) {
            logger.info("Do advanced Search");
            return createRequestsForAdvancedSearch();
        } else {
            logger.info("Do simple Search");
            return createRequestsForSimpleSearch();
        }

    }

    private List<SearchRequest> createRequestsForSimpleSearch() {
        return Arrays.asList(
                createExperimentRequest(),
                createMaterialSearchRequest(),
                createDocumentRequest(),
                createItemRequest());
    }

    private List<SearchRequest> createRequestsForAdvancedSearch() {
        List<SearchRequest> requests = new ArrayList<>();
        if (typeFilter.isMaterials()) {
            logger.info("Do advanced Search - materials");
            requests.add(createMaterialSearchRequest());
        }
        if (typeFilter.isDocuments()) {
            logger.info("Do advanced Search - docs");
            requests.add(createDocumentRequest());
        }
        if (typeFilter.isItems()) {
            logger.info("Do advanced Search - items");
            requests.add(createItemRequest());
        }
        if (typeFilter.isExperiments()) {
            logger.info("Do advanced Search - exp");
            requests.add(createExperimentRequest());
        }
        if (typeFilter.isProjects()) {

        }
        return requests;
    }

    private SearchRequest createMaterialSearchRequest() {
        MaterialSearchRequestBuilder materialRequestBuilder = new MaterialSearchRequestBuilder(user, 0, maxresults);
        MaterialSearchMaskValues searchValue = new MaterialSearchMaskValues();
        searchValue.materialName = searchTerms;
        if (advancedSearchActive) {
            searchValue.type.addAll(materialTypeFilter.getTypes());
        }
        materialRequestBuilder.setConditionsBySearchValues(searchValue);
        return materialRequestBuilder.buildSearchRequest();
    }

    private SearchRequest createDocumentRequest() {
        DocumentSearchRequestBuilder docBuilder = new DocumentSearchRequestBuilder(user, 0, maxresults);
        docBuilder.addWordRoots(new HashSet(Arrays.asList(searchTerms.toLowerCase().split(" "))));
        return docBuilder.buildSearchRequest();
    }

    private SearchRequest createItemRequest() {
        ItemSearchRequestBuilder itemBuilder = new ItemSearchRequestBuilder(user, 0, maxresults);
        itemBuilder.addDescription(searchTerms);
        return itemBuilder.buildSearchRequest();
    }

    private SearchRequest createExperimentRequest() {
        ExperimentSearchRequestBuilder expBuilder = new ExperimentSearchRequestBuilder(user, 0, maxresults);
        expBuilder.addDescription(searchTerms);
        return expBuilder.buildSearchRequest();
    }

    public String getSearchTerms() {
        return searchTerms;

    }

    public void setSearchTerms(String searchTerms) {
        this.searchTerms = searchTerms;
    }

    public void toogleAdvancedSearch() {
        this.advancedSearchActive = !this.advancedSearchActive;
    }

    public boolean isAdvancedSearch() {
        return advancedSearchActive;
    }

    public MaterialTypeFilter getMaterialTypeFilter() {
        return materialTypeFilter;
    }

}
