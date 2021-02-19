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
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.bean.MaterialSearchMaskValues;
import de.ipb_halle.lbac.material.common.search.MaterialSearchRequestBuilder;
import de.ipb_halle.lbac.material.structure.Molecule;
import de.ipb_halle.lbac.search.SearchCategory;
import de.ipb_halle.lbac.search.SearchQueryStemmer;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.document.DocumentSearchRequestBuilder;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class SearchFilter {

    private User user;
    private String searchTerms = "";
    private SearchableTypeFilter typeFilter;
    private MaterialTypeFilter materialTypeFilter;

    private boolean advancedSearchActive;
    private int maxresults = 50;
    private final Logger logger = LogManager.getLogger(DocumentSearchService.class);
    private String structureString = "";

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
            return createRequestsForAdvancedSearch();
        } else {
            return createRequestsForSimpleSearch();
        }
    }
    
    public Map<SearchCategory,Set<String>>  xxx(){
        return null;
    }

    private List<SearchRequest> createRequestsForSimpleSearch() {
        if (searchTerms.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.asList(
                createExperimentRequest(),
                createMaterialSearchRequest(),
                createDocumentRequest(),
                createItemRequest());
    }

    private List<SearchRequest> createRequestsForAdvancedSearch() {
        List<SearchRequest> requests = new ArrayList<>();
        if (shouldMaterialsBeSearched()) {
            requests.add(createMaterialSearchRequest());
        }
        if (typeFilter.isDocuments()) {
            requests.add(createDocumentRequest());
        }
        if (typeFilter.isItems() && (!searchTerms.trim().isEmpty() || shouldMaterialsBeSearched())) {
            requests.add(createItemRequest());
        }
        if (typeFilter.isExperiments() && !searchTerms.trim().isEmpty()) {
            requests.add(createExperimentRequest());
        }
        if (typeFilter.isProjects()) {

        }
        return requests;
    }

    private boolean shouldMaterialsBeSearched() {
        Molecule m = new Molecule(structureString, 0);

        return typeFilter.isMaterials()
                && (!searchTerms.trim().isEmpty()
                || !m.isEmptyMolecule());
    }

    private SearchRequest createMaterialSearchRequest() {
        MaterialSearchRequestBuilder materialRequestBuilder = new MaterialSearchRequestBuilder(user, 0, maxresults);
        materialRequestBuilder.addDeactivated(false);
        MaterialSearchMaskValues searchValue = new MaterialSearchMaskValues();
        searchValue.materialName = searchTerms;
        if (advancedSearchActive) {
            searchValue.type.addAll(materialTypeFilter.getTypes());
            if (searchTerms.trim().isEmpty()) {
                searchValue.type.remove(MaterialType.BIOMATERIAL);
                searchValue.type.remove(MaterialType.SEQUENCE);
            }
            Molecule mol = new Molecule(structureString, maxresults);
            if (!mol.isEmptyMolecule()) {
                searchValue.molecule = structureString;
            }
        }
        materialRequestBuilder.setConditionsBySearchValues(searchValue);
        return materialRequestBuilder.buildSearchRequest();
    }

    public void init() {
        materialTypeFilter.setBiomaterial(true);
        materialTypeFilter.setSequences(true);
        materialTypeFilter.setStructures(true);
        typeFilter.setDocuments(true);
        typeFilter.setExperiments(true);
        typeFilter.setMaterials(true);
        typeFilter.setItems(true);

    }

    private SearchRequest createDocumentRequest() {
        SearchQueryStemmer searchQueryStemmer = new SearchQueryStemmer();
        Set<String> normalizedTerms = searchQueryStemmer.stemmQuery(searchTerms.toLowerCase()).getAllStemmedWords();
        DocumentSearchRequestBuilder docBuilder = new DocumentSearchRequestBuilder(user, 0, maxresults);
        docBuilder.addWordRoots(normalizedTerms);
        return docBuilder.buildSearchRequest();
    }

    private SearchRequest createItemRequest() {
        ItemSearchRequestBuilder itemBuilder = new ItemSearchRequestBuilder(user, 0, maxresults);
        if (searchTerms != null && !searchTerms.isEmpty()) {
            itemBuilder.addLabel(searchTerms);
        }
        Molecule mol = new Molecule(structureString, maxresults);
        if (!mol.isEmptyMolecule()) {
            itemBuilder.addSubMolecule(structureString);
        }

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
        this.searchTerms = searchTerms.trim().toLowerCase();
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

    public String getStructureString() {
        return structureString;
    }

    public void setStructureString(String structureString) {
        this.structureString = structureString;
    }

}
