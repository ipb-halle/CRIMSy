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
package de.ipb_halle.pageobjects.pages.materials.tabs;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

/**
 * Page object for the tabView in
 * /ui/web/WEB-INF/templates/material/materialsEdit.xhtml
 * 
 * @author flange
 */
public interface MaterialEditTab {
    static final SelenideElement MATERIAL_NAMES_TAB = $(
            testId("materialEdit:materialNamesTab"));
    static final SelenideElement INDICES_TAB = $(
            testId("materialEdit:indicesTab"));
    static final SelenideElement STRUCTURE_INFOS_TAB = $(
            testId("materialEdit:structureInfosTab"));
    static final SelenideElement SEQUENCE_INFOS_TAB = $(
            testId("materialEdit:sequenceInfosTab"));
    static final SelenideElement HARZARDS_TAB = $(
            testId("materialEdit:hazardsTab"));
    static final SelenideElement STORAGE_TAB = $(
            testId("materialEdit:storageTab"));
    static final SelenideElement BIODATA_TAB = $(
            testId("materialEdit:biodataTab"));
    static final SelenideElement COMPOSITION_TAB = $(
            testId("materialEdit:compositionTab"));

    /*
     * Actions
     */
    public default MaterialNamesTab openMaterialNamesTab() {
        MATERIAL_NAMES_TAB.click();
        return page(MaterialNamesTab.class);
    }

    public default IndicesTab openIndicesTab() {
        INDICES_TAB.click();
        return page(IndicesTab.class);
    }

    public default StructureInfosTab openStructureInfosTab() {
        STRUCTURE_INFOS_TAB.click();
        return page(StructureInfosTab.class);
    }

    public default SequenceInfosTab openSequenceInfosTab() {
        SEQUENCE_INFOS_TAB.click();
        return page(SequenceInfosTab.class);
    }

    public default HazardsTab openHazardsTab() {
        HARZARDS_TAB.click();
        return page(HazardsTab.class);
    }

    public default StorageTab openStorageTab() {
        STORAGE_TAB.click();
        return page(StorageTab.class);
    }

    public default BiodataTab openBiodataTab() {
        BIODATA_TAB.click();
        return page(BiodataTab.class);
    }

    public default CompositionTab openCompositionTab() {
        COMPOSITION_TAB.click();
        return page(CompositionTab.class);
    }
}