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
package de.ipb_halle.pageobjects.pages.materials;

import static com.codeborne.selenide.Selenide.$;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.pages.AbstractPage;
import de.ipb_halle.pageobjects.plugins.MolPlugin;

/**
 * Page object for /ui/web/WEB-INF/templates/material/materialSearchMask.xhtml
 * 
 * @author flange
 */
public class MaterialSearchMaskPage extends AbstractPage {
    private static final SelenideElement NAME_INPUT = $(
            testId("materialSearchMask:name"));
    private static final SelenideElement ID_INPUT = $(
            testId("input", "materialSearchMask:id"));
    private static final SelenideElement USER_NAME_INPUT = $(
            testId("input", "materialSearchMask:userName"));
    private static final SelenideElement PROJECT_NAME_INPUT = $(
            testId("materialSearchMask:projectName"));
    private static final SelenideElement INDEX_INPUT = $(
            testId("input", "materialSearchMask:index"));
    private static final SelenideElement MATERIAL_TYPE_SELECTION = $(
            testId("materialSearchMask:materialType"));
    private static final String MOLEDITOR_TESTID = "materialSearchMask:molEditor";
    private static final String MOLEDITOR_WIDGETVAR = "molEditorWidgetVar";
    private static final SelenideElement CLEAR_BUTTON = $(
            testId("materialSearchMask:clearButton"));
    private static final SelenideElement SEARCH_BUTTON = $(
            testId("materialSearchMask:searchButton"));

    private MolPlugin molEditor = new MolPlugin(MOLEDITOR_TESTID,
            MOLEDITOR_WIDGETVAR);

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
            molEditor.setMolecule(molfile);
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
    public MaterialSearchMaskModel getModel() {
        return new MaterialSearchMaskModel()
                .name(NAME_INPUT.text())
                .id(ID_INPUT.text()).userName(USER_NAME_INPUT.text())
                .projectName(PROJECT_NAME_INPUT.text())
                .index(INDEX_INPUT.text())
                .materialType(MATERIAL_TYPE_SELECTION.getSelectedValue())
                .molfile(molEditor.getMolecule());
    }
}