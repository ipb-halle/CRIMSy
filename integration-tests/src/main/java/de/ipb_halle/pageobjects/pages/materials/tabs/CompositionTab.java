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

import de.ipb_halle.pageobjects.components.MolecularFacesMolecule;
import de.ipb_halle.pageobjects.components.table.DataTable;
import de.ipb_halle.pageobjects.pages.AbstractPage;
import de.ipb_halle.pageobjects.pages.composite.SequenceSearchMaskPage;

/**
 * Page object for
 * /ui/web/WEB-INF/templates/material/components/composition.xhtml
 * 
 * @author flange
 */
public class CompositionTab extends AbstractPage implements MaterialEditTab {
    private static final SelenideElement COMPOSITION_TYPE_SELECTION = $(
            testId("select", "compositionTab:compositionType"));
    private static final SelenideElement MATERIAL_NAME_INPUT = $(
            testId("compositionTab:materialName"));
    private static final SelenideElement SEARCH_BUTTON = $(
            testId("compositionTab:search"));
    private static final SelenideElement STRUCTURE_SEARCH_TAB = $(
            testId("compositionTab:structureSearchTab"));
    private static final MolecularFacesMolecule MOL_EDITOR = new MolecularFacesMolecule(
            "compositionTab:structureSearchTab:molEditor",
            "compositionStructurePlugin");
    private static final SelenideElement SEQUENCE_SEARCH_TAB = $(
            testId("compositionTab:sequenceSearchTab"));
    private static final SelenideElement ORGANISM_SEARCH_TAB = $(
            testId("compositionTab:organismSearchTab"));
    private static final SelenideElement SEARCH_RESULT_TABLE = $(
            testId("compositionTab:searchResultTable"));
    private static final SelenideElement COMPONENTS_TABLE = $(
            testId("compositionTab:componentsTable"));

    /*
     * Actions
     */
    public CompositionTab search() {
        SEARCH_BUTTON.click();
        return this;
    }

    public CompositionTab openStructureSearchTab() {
        STRUCTURE_SEARCH_TAB.click();
        return this;
    }

    public CompositionTab openSequenceSearchTab() {
        SEQUENCE_SEARCH_TAB.click();
        return this;
    }

    public CompositionTab openOrganismSearchTab() {
        ORGANISM_SEARCH_TAB.click();
        return this;
    }

    /*
     * Getters
     */
    public SelenideElement getCompositionTypeSelection() {
        return COMPOSITION_TYPE_SELECTION;
    }

    public SelenideElement getMaterialNameInput() {
        return MATERIAL_NAME_INPUT;
    }

    public MolecularFacesMolecule getMolEditor() {
        return MOL_EDITOR;
    }

    public SequenceSearchMaskPage getSequenceSearchMask() {
        return page(SequenceSearchMaskPage.class);
    }

    public DataTable getSearchResultTable() {
        return DataTable.extract(SEARCH_RESULT_TABLE);
    }

    public DataTable getComponentsTable() {
        return DataTable.extract(COMPONENTS_TABLE);
    }
}