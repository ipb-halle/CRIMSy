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
package de.ipb_halle.lbac.exp.search;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.structure.Molecule;
import de.ipb_halle.lbac.search.SearchCategory;
import de.ipb_halle.lbac.search.SearchRequestBuilder;
import de.ipb_halle.lbac.search.SearchTarget;

/**
 *
 * @author fmauz
 */
public class ExperimentSearchRequestBuilder extends SearchRequestBuilder {

    private String text;
    private String materialName;
    private String itemLabel;
    private String userName;
    private String structure;
    private String id;
    private String template;
    private String code;

    public ExperimentSearchRequestBuilder(User u, int firstResult, int maxResults) {
        super(u, firstResult, maxResults);
        this.target = SearchTarget.EXPERIMENT;
    }

    public ExperimentSearchRequestBuilder setText(String text) {
        this.text = text;
        return this;
    }

    @Override
    public void addSearchCriteria() {
        addItemLabel();
        addMaterialName();
        addStructure();
        addUserName();
        addText();
        addTemplate();
        addId();
        addCode();
    }

    public void setMaterialName(String name) {
        this.materialName = name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    private void addMaterialName() {
        if (materialName != null && !materialName.trim().isEmpty()) {
            request.addSearchCategory(SearchCategory.NAME, materialName);
        }
    }

    private void addStructure() {
        Molecule m = new Molecule(structure, -1);
        if (!m.isEmptyMolecule()) {
            request.addSearchCategory(SearchCategory.STRUCTURE, structure);
        }
    }

    private void addItemLabel() {
        if (itemLabel != null && !itemLabel.trim().isEmpty()) {
            request.addSearchCategory(SearchCategory.LABEL, itemLabel);
        }
    }

    private void addUserName() {
        if (userName != null && !userName.trim().isEmpty()) {
            request.addSearchCategory(SearchCategory.USER, userName);
        }
    }
        private void addCode() {
        if (code != null && !code.trim().isEmpty()) {
            request.addSearchCategory(SearchCategory.EXP_CODE, code);
        }
    }

    private void addTemplate() {
        if (template != null && !template.trim().isEmpty()) {
            request.addSearchCategory(SearchCategory.TEMPLATE, template);
        }
    }

    private void addText() {
        if (text != null && !text.trim().isEmpty()) {
            request.addSearchCategory(SearchCategory.TEXT, text);
        }
    }

    private void addId() {
        if (id != null && !id.isEmpty()) {
            request.addSearchCategory(SearchCategory.LABEL, id);
        }
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setStructure(String structure) {
        this.structure = structure;
    }
    
    public void setCode(String code){
        this.code=code;
    }

}
