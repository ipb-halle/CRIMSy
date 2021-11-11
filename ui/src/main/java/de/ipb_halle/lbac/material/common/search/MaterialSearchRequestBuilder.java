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
package de.ipb_halle.lbac.material.common.search;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.bean.MaterialSearchMaskValues;
import de.ipb_halle.lbac.search.SearchCategory;
import de.ipb_halle.lbac.search.SearchRequestBuilder;
import de.ipb_halle.lbac.search.SearchTarget;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author fmauz
 */
public class MaterialSearchRequestBuilder extends SearchRequestBuilder {

    private String structure;
    private String index;
    private String id;
    private String userName;
    private String projectName;
    private final Set<MaterialType> types = new HashSet<>();
    private String materialName;
    private Boolean deactivated;

    public MaterialSearchRequestBuilder(User u, int firstResult, int maxResults) {
        super(u, firstResult, maxResults);
        this.target = SearchTarget.MATERIAL;
    }

    @Override
    protected void addSearchCriteria() {
        addStructure();
        addIndex();
        addTypes();
        addId();
        addUser();
        addProject();
        addMaterialName();
        addDeactivated();
    }

    public void setSearchValues(MaterialSearchMaskValues values) {
        setMaterialName(values.materialName);
        setStructure(values.molecule);
        setProjectName(values.projectName);
        setId(String.valueOf(values.id));
        setUserName(values.userName);
        setDeactivated(values.deactivated);
        for (MaterialType t : values.type) {
            types.add(t);
        }

    }

    private void addDeactivated() {
        if (deactivated != null) {
            if (deactivated) {
                request.addSearchCategory(SearchCategory.DEACTIVATED, new String[]{"deactivated"});
            } else {
                request.addSearchCategory(SearchCategory.DEACTIVATED, new String[]{"activated"});
            }
        }
    }

    private void addStructure() {
        if (structure != null) {
            request.addSearchCategory(SearchCategory.STRUCTURE, structure);
        }
    }

    private void addIndex() {
        if (index != null) {
            request.addSearchCategory(SearchCategory.INDEX, index);
        }
    }

    private void addId() {
        if (id != null && !id.equals("null")) {
            request.addSearchCategory(SearchCategory.LABEL, id);
        }
    }

    private void addUser() {
        if (userName != null) {
            request.addSearchCategory(SearchCategory.USER, userName);
        }
    }

    private void addMaterialName() {
        if (materialName != null) {
            request.addSearchCategory(SearchCategory.NAME, materialName);
        }
    }

    private void addProject() {
        if (projectName != null) {
            request.addSearchCategory(SearchCategory.PROJECT, projectName);
        }
    }

    private void addTypes() {
        if (!types.isEmpty()) {
            Set<String> typeNames = new HashSet<>();
            for (MaterialType t : types) {
                typeNames.add(t.name());
            }
            request.addSearchCategory(SearchCategory.TYPE, typeNames.toArray(new String[typeNames.size()]));
        }
    }

    public void addMaterialType(MaterialType type) {
        types.add(type);
    }

    public void setStructure(String structure) {
        this.structure = structure;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }

    public MaterialSearchRequestBuilder setDeactivated(Boolean deactivated) {
        this.deactivated = deactivated;
        return this;
    }

}
