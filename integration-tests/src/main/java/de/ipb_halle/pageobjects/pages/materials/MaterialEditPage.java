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
import static com.codeborne.selenide.Selenide.page;
import static de.ipb_halle.pageobjects.util.Apply.applyIfNotNull;
import static de.ipb_halle.pageobjects.util.Apply.applySelection;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.pages.AbstractPage;
import de.ipb_halle.pageobjects.pages.materials.models.MaterialModel;
import de.ipb_halle.pageobjects.pages.materials.tabs.MaterialEditTab;

/**
 * Page object for /ui/web/WEB-INF/templates/material/materialsEdit.xhtml
 * 
 * @author flange
 */
public class MaterialEditPage extends AbstractPage<MaterialEditPage> implements MaterialEditTab {
    private static final SelenideElement PROJECT_SELECTION = $(testId("select", "materialEdit:project"));
    private static final SelenideElement BACKWARD_BUTTON = $(testId("materialEdit:backward"));
    private static final SelenideElement CHANGED_TEXT = $(testId("materialEdit:changedText"));
    private static final SelenideElement FORWARD_BUTTON = $(testId("materialEdit:forward"));
    private static final SelenideElement CANCEL_BUTTON = $(testId("materialEdit:cancel"));
    private static final SelenideElement SAVE_BUTTON = $(testId("materialEdit:save"));
    private static final SelenideElement MATERIAL_TYPE_SELECTION = $(testId("select", "materialEdit:materialType"));
    private static final SelenideElement MODE_TEXT = $(testId("materialEdit:mode"));
    private static final SelenideElement ERROR_MESSAGES = $(testId("materialEdit:errorMessages"));

    /*
     * Actions
     */
    /**
     * Applies the material model.
     * <p>
     * Convention: The input element will not be evaluated in case the model field
     * is null. Use empty strings to reset fields.
     * 
     * @param model
     * @return this
     */
    public MaterialEditPage applyModel(MaterialModel model) {
        applySelection(model.getProject(), PROJECT_SELECTION);
        applySelection(model.getMaterialType(), MATERIAL_TYPE_SELECTION);
        applyIfNotNull(model.getMaterialNamesModel(), (m) -> openMaterialNamesTab().applyModel(m));
        applyIfNotNull(model.getIndicesModel(), (m) -> openIndicesTab().applyModel(m));
        applyIfNotNull(model.getStructureInfosModel(), (m) -> openStructureInfosTab().applyModel(m));
        applyIfNotNull(model.getHazardsModel(), (m) -> openHazardsTab().applyModel(m));
        applyIfNotNull(model.getStorageModel(), (m) -> openStorageTab().applyModel(m));

        return this;
    }

    public MaterialEditPage selectProject(String project) {
        PROJECT_SELECTION.selectOption(project);
        return this;
    }

    public MaterialEditPage selectMaterialType(String materialType) {
        MATERIAL_TYPE_SELECTION.selectOption(materialType);
        return this;
    }

    public MaterialEditPage historyBackwards() {
        BACKWARD_BUTTON.click();
        return this;
    }

    public MaterialEditPage historyForwards() {
        FORWARD_BUTTON.click();
        return this;
    }

    public MaterialOverviewPage cancel() {
        CANCEL_BUTTON.click();
        return page(MaterialOverviewPage.class);
    }

    /**
     * Try to save the material.
     * <p>
     * Should direct the browser to the materials overview page or stay on this page
     * depending on the validation outcome, thus only {@link MaterialOverviewPage}
     * or {@link MaterialEditPage} are useful page object classes to be supplied in
     * the {@code expectedPageClass} parameter.
     * 
     * @param <T>
     * @param expectedPageClass expected page
     * @return page object of expected page
     */
    public <T extends AbstractPage<T>> T save(Class<T> expectedPageClass) {
        SAVE_BUTTON.click();
        return page(expectedPageClass);
    }

    /*
     * Getters
     */
    public SelenideElement projectSelection() {
        return PROJECT_SELECTION;
    }

    public SelenideElement changedText() {
        return CHANGED_TEXT;
    }

    public SelenideElement materialTypeSelection() {
        return MATERIAL_TYPE_SELECTION;
    }

    public SelenideElement modeText() {
        return MODE_TEXT;
    }

    public SelenideElement errorMessages() {
        return ERROR_MESSAGES;
    }
}