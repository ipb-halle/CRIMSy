/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.pageobjects.pages.composite;

import static com.codeborne.selenide.Selenide.$;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.components.MolecularFacesMolecule;
import de.ipb_halle.pageobjects.pages.AbstractPage;

/**
 * Page object for /ui/web/resources/crimsy/materialSearchMask.xhtml
 * 
 * @author flange
 */
public class MaterialSearchMaskPage extends AbstractPage<MaterialSearchMaskPage> {
    private static final SelenideElement NAME_INPUT = $(
            testId("materialSearchMask:name"));
    private static final SelenideElement ID_INPUT = $(
            testId("input", "materialSearchMask:id"));
    private static final SelenideElement USER_NAME_INPUT = $(
            testId("materialSearchMask:userName"));
    private static final SelenideElement PROJECT_NAME_INPUT = $(
            testId("materialSearchMask:projectName"));
    private static final SelenideElement INDEX_INPUT = $(
            testId("input", "materialSearchMask:index"));
    private static final SelenideElement MATERIAL_TYPE_SELECTION = $(
            testId("select", "materialSearchMask:materialType"));
    private static final MolecularFacesMolecule MOL_EDITOR = new MolecularFacesMolecule(
            "materialSearchMask:molEditor", "molEditorWidgetVar");
    private static final SelenideElement CLEAR_BUTTON = $(
            testId("materialSearchMask:clearButton"));
    private static final SelenideElement SEARCH_BUTTON = $(
            testId("materialSearchMask:searchButton"));

    /*
     * Actions
     */
    public MaterialSearchMaskPage applyModel(MaterialSearchMaskModel model) {
        applyIfNotNull(model.getName(), NAME_INPUT);
        applyIfNotNull(model.getId(), ID_INPUT);
        applyIfNotNull(model.getUserName(), USER_NAME_INPUT);
        applyIfNotNull(model.getProjectName(), PROJECT_NAME_INPUT);
        applyIfNotNull(model.getIndex(), INDEX_INPUT);

        String materialType = model.getMaterialType();
        if (materialType != null) {
            MATERIAL_TYPE_SELECTION.selectOption(materialType);
        }

        String molfile = model.getMolfile();
        if (molfile != null) {
            MOL_EDITOR.setMolecule(molfile);
        }

        return this;
    }

    private void applyIfNotNull(String value, SelenideElement element) {
        if (value != null) {
            element.setValue(value);
        }
    }

    public MaterialSearchMaskPage clear() {
        CLEAR_BUTTON.click();
        return this;
    }

    public MaterialSearchMaskPage search() {
        SEARCH_BUTTON.click();
        return this;
    }

    /*
     * Getters
     */
    /*
     * TODO: Need to think about situations where not all input fields are
     * available, because their appearance is configurable in
     * materialSearchMask.xhtml. This is not problematic in applyModel(),
     * because fields can simply be left null and the input elements are not
     * interacted with.
     */
    public MaterialSearchMaskModel getModel() {
        return new MaterialSearchMaskModel()
                .name(NAME_INPUT.text())
                .id(ID_INPUT.text())
                .userName(USER_NAME_INPUT.text())
                .projectName(PROJECT_NAME_INPUT.text())
                .index(INDEX_INPUT.text())
                .materialType(MATERIAL_TYPE_SELECTION.getSelectedValue())
                .molfile(MOL_EDITOR.getMolecule());
    }
}