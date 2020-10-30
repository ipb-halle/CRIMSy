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
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.document.DocumentSearchRequestBuilder;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class SearchFilter {

    private User user;
    private String searchTerms;
    private Set<SearchTarget> searchTargets = new HashSet<>();
    private Set<MaterialType> materialTypes = new HashSet<>();
    private boolean materialTypeStructure;
    private boolean materialTypeBioMaterial;
    private boolean materialTypeSequence;
    private boolean advancedSearchActive;
    private int maxresults = 50;
    private final Logger LOGGER = LogManager.getLogger(DocumentSearchService.class);

    public SearchFilter(User user) {
        this.user = user;
    }

    public List<SearchRequest> createRequests() {
        MaterialSearchRequestBuilder materialRequestBuilder = new MaterialSearchRequestBuilder(user, 0, maxresults);

        MaterialSearchMaskValues searchValue = new MaterialSearchMaskValues();

        ItemSearchRequestBuilder itemBuilder = new ItemSearchRequestBuilder(user, 0, maxresults);
        ExperimentSearchRequestBuilder expBuilder = new ExperimentSearchRequestBuilder(user, 0, maxresults);
        DocumentSearchRequestBuilder docBuilder = new DocumentSearchRequestBuilder(user, 0, maxresults);
        if (searchTerms != null && !searchTerms.trim().isEmpty()) {
            searchValue.materialName = searchTerms;
            itemBuilder.addDescription(searchTerms);
            expBuilder.addDescription(searchTerms);
            docBuilder.addWordRoots(new HashSet(Arrays.asList(searchTerms.toLowerCase().split(" "))));

        }
        materialRequestBuilder.setConditionsBySearchValues(searchValue);

        return Arrays.asList(
                expBuilder.buildSearchRequest(),
                materialRequestBuilder.buildSearchRequest(),
                docBuilder.buildSearchRequest(),
                itemBuilder.buildSearchRequest());

    }

    public String getSearchTerms() {
        return searchTerms;
    }

    public void setSearchTerms(String searchTerms) {
        this.searchTerms = searchTerms;
    }

    public boolean isTypeDocument() {
        return searchTargets.contains(SearchTarget.DOCUMENT);
    }

    public void setTypeDocument(boolean typeDocument) {
        searchTargets.add(SearchTarget.DOCUMENT);
    }

    public boolean isTypeMaterial() {
        return searchTargets.contains(SearchTarget.MATERIAL);
    }

    public void setTypeMaterial(boolean typeMaterial) {
        searchTargets.add(SearchTarget.MATERIAL);
    }

    public boolean isTypeItem() {
        return searchTargets.contains(SearchTarget.ITEM);
    }

    public void setTypeItem(boolean typeItems) {
        searchTargets.add(SearchTarget.ITEM);
    }

    public boolean isTypeExperiment() {
        return searchTargets.contains(SearchTarget.EXPERIMENT);
    }

    public void setTypeExperiment(boolean typeExperiments) {
        searchTargets.add(SearchTarget.EXPERIMENT);
    }

    public boolean isMaterialTypeStructure() {
        return materialTypes.contains(MaterialType.STRUCTURE);
    }

    public void setMaterialTypeStructure(boolean materialTypeStructure) {
        materialTypes.add(MaterialType.STRUCTURE);
    }

    public boolean isMaterialTypeBioMaterial() {
        return materialTypes.contains(MaterialType.BIOMATERIAL);
    }

    public void setMaterialTypeBioMaterial(boolean materialTypeBioMaterial) {
        materialTypes.add(MaterialType.BIOMATERIAL);
    }

    public boolean isMaterialTypeSequence() {
        return materialTypes.contains(MaterialType.SEQUENCE);
    }

    public void setMaterialTypeSequence(boolean materialTypeSequence) {
        materialTypes.add(MaterialType.SEQUENCE);
    }

}
