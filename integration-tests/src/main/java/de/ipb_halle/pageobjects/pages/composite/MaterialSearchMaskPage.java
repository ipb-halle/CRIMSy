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
import static de.ipb_halle.pageobjects.util.Apply.applySelection;
import static de.ipb_halle.pageobjects.util.Apply.applyValue;
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
    /**
     * Applies the search mask model.
     * <p>
     * Convention: The input element will not be evaluated in case the model field
     * is null. Use empty strings to reset fields.
     * 
     * @param model
     * @return this
     */
    public MaterialSearchMaskPage applyModel(MaterialSearchMaskModel model) {
        applyValue(model.getName(), NAME_INPUT);
        applyValue(model.getId(), ID_INPUT);
        applyValue(model.getUserName(), USER_NAME_INPUT);
        applyValue(model.getProjectName(), PROJECT_NAME_INPUT);
        applyValue(model.getIndex(), INDEX_INPUT);
        applySelection(model.getMaterialType(), MATERIAL_TYPE_SELECTION);

        String molfile = model.getMolfile();
        if (molfile != null) {
            MOL_EDITOR.setMolecule(molfile);
        }

        return this;
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
     *
     * A better approach would be to use a fluent assertion instead of a getter.
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