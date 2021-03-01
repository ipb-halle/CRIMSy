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
package de.ipb_halle.lbac.items.search;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.items.bean.SearchMaskValues;
import de.ipb_halle.lbac.search.SearchCategory;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchRequestBuilder;
import de.ipb_halle.lbac.search.SearchRequestImpl;
import de.ipb_halle.lbac.search.SearchTarget;

/**
 *
 * @author fmauz
 */
public class ItemSearchRequestBuilder extends SearchRequestBuilder {

    private String materialName;
    private String label;
    private String userName;
    private String projectName;
    private String location;
    private String description;

    public ItemSearchRequestBuilder(User u, int firstResult, int maxResults) {
        super(u, firstResult, maxResults);
        this.target = SearchTarget.ITEM;
    }

    @Override
    protected void addSearchCriteria() {
        addMaterialName();
        addItemLabel();
        addUserName();
        addProjectName();
        addLocation();
        addDescription();
    }

    public void setSearchMaskValues(SearchMaskValues values) {
        materialName = values.getMaterialName();
        label = values.getLabel();
        userName = values.getUserName();
        projectName = values.getProjectName();
        location = values.getLocation();
        description = values.getDescription();
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private void addMaterialName() {
        if (materialName != null && !materialName.isEmpty()) {
            request.addSearchCategory(SearchCategory.NAME, materialName);
        }
    }

    private void addItemLabel() {
        if (label != null && !label.isEmpty()) {
            request.addSearchCategory(SearchCategory.LABEL, label);
        }
    }

    private void addUserName() {
        if (userName != null && !userName.isEmpty()) {
            request.addSearchCategory(SearchCategory.USER, userName);
        }
    }

    private void addProjectName() {
        if (projectName != null && !projectName.isEmpty()) {
            request.addSearchCategory(SearchCategory.PROJECT, projectName);
        }
    }

    private void addLocation() {
        if (location != null && !location.isEmpty()) {
            request.addSearchCategory(SearchCategory.LOCATION, location);
        }
    }

    private void addDescription() {
        if (description != null && !description.isEmpty()) {
            request.addSearchCategory(SearchCategory.TEXT, description);
        }
    }

}
