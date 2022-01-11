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
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.pages.AbstractPage;

/**
 * Page object for /ui/web/WEB-INF/templates/material/materialsEdit.xhtml
 * 
 * @author flange
 */
public class MaterialEditPage extends AbstractPage {
    private static final SelenideElement PROJECT_SELECTION = $(
            testId("materialEdit:project"));
    private static final SelenideElement BACKWARD_BUTTON = $(
            testId("materialEdit:backward"));
    private static final SelenideElement CHANGED_TEXT = $(
            testId("materialEdit:changedText"));
    private static final SelenideElement FORWARD_BUTTON = $(
            testId("materialEdit:forward"));
    private static final SelenideElement CANCEL_BUTTON = $(
            testId("materialEdit:cancel"));
    private static final SelenideElement SAVE_BUTTON = $(
            testId("materialEdit:save"));
    private static final SelenideElement MATERIAL_TYPE_SELECTION = $(
            testId("materialEdit:materialType"));
    private static final SelenideElement MODE_TEXT = $(
            testId("materialEdit:mode"));
    private static final SelenideElement ERROR_MESSAGES = $(
            testId("materialEdit:errorMessages"));
    private static final SelenideElement MATERIAL_NAMES_TAB = $(
            testId("materialEdit:materialNamesTab"));
    private static final SelenideElement INDICES_TAB = $(
            testId("materialEdit:indicesTab"));
    private static final SelenideElement STRUCTURE_INFOS_TAB = $(
            testId("materialEdit:structureInfosTab"));
    private static final SelenideElement SEQUENCE_INFOS_TAB = $(
            testId("materialEdit:sequenceInfosTab"));
    private static final SelenideElement HARZARDS_TAB = $(
            testId("materialEdit:hazardsTab"));
    private static final SelenideElement STORAGE_TAB = $(
            testId("materialEdit:storageTab"));
    private static final SelenideElement BIODATA_TAB = $(
            testId("materialEdit:materialNamesTab"));
    private static final SelenideElement COMPOSITION_TAB = $(
            testId("materialEdit:compositionTab"));

    /*
     * Actions
     */
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
     * 
     * @return {@link MaterialOverviewPage} object (if material is saved) or
     *         {@link MaterialEditPage} object (if material cannot be saved due
     *         to validation errors)
     */
    public AbstractPage save() {
        SAVE_BUTTON.click();
        if (ERROR_MESSAGES.isDisplayed()) {
            return this;
        } else {
            return page(MaterialOverviewPage.class);
        }
    }

    public MaterialNamesTab openMaterialNamesTab() {
        MATERIAL_NAMES_TAB.click();
        return page(MaterialNamesTab.class);
    }

    public IndicesTab openIndicesTab() {
        INDICES_TAB.click();
        return page(IndicesTab.class);
    }

    public StructureInfosTab openStructureInfosTab() {
        STRUCTURE_INFOS_TAB.click();
        return page(StructureInfosTab.class);
    }

    public SequenceInfosTab openSequenceInfosTab() {
        SEQUENCE_INFOS_TAB.click();
        return page(SequenceInfosTab.class);
    }

    public HarzardsTab openHazardsTab() {
        HARZARDS_TAB.click();
        return page(HarzardsTab.class);
    }

    public StorageTab openStorageTab() {
        STORAGE_TAB.click();
        return page(StorageTab.class);
    }

    public BiodataTab openBiodataTab() {
        BIODATA_TAB.click();
        return page(BiodataTab.class);
    }

    public CompositionTab openCompositionTab() {
        COMPOSITION_TAB.click();
        return page(CompositionTab.class);
    }

    /*
     * Getters
     */
    public SelenideElement getProjectSelection() {
        return PROJECT_SELECTION;
    }

    public SelenideElement getChangedText() {
        return CHANGED_TEXT;
    }

    public SelenideElement getMaterialTypeSelection() {
        return MATERIAL_TYPE_SELECTION;
    }

    public SelenideElement getModeText() {
        return MODE_TEXT;
    }

    public SelenideElement getErrorMessages() {
        return ERROR_MESSAGES;
    }
}