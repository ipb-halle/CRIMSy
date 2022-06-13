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

import static com.codeborne.selenide.Selenide.page;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.components.PrimeFacesTooltip;
import de.ipb_halle.pageobjects.components.table.DataTable;
import de.ipb_halle.pageobjects.pages.composite.acobjectmodal.ACObjectModalPage;
import de.ipb_halle.pageobjects.pages.items.ItemEditPage;

/**
 * Page object for the search results table in
 * /ui/web/WEB-INF/templates/material/materials.xhtml
 * 
 * @author flange
 */
public class MaterialSearchResultsTable extends DataTable<MaterialSearchResultsTable> {
    private static final String MATERIAL_NAMES_TEXT = testId("materialOverview:resultsTable:names");
    private static final PrimeFacesTooltip MATERIAL_NAMES_TOOLTIP = new PrimeFacesTooltip(
            "materialOverview:resultsTable:namesTooltip");
    private static final String EDIT_MATERIAL_BUTTON = testId("materialOverview:resultsTable:editMaterial");
    private static final String CREATE_ITEM_BUTTON = testId("materialOverview:resultsTable:createItem");
    private static final String CHANGE_PERMISSIONS_BUTTON = testId("materialOverview:resultsTable:changePermissions");
    private static final String DEACTIVATE_MATERIAL_BUTTON = testId("materialOverview:resultsTable:deactivateMaterial");

    public MaterialSearchResultsTable(String testId) {
        super(testId);
    }

    /*
     * Actions
     */
    public MaterialSearchResultsTable hoverOverMaterialNames(int rowIndex) {
        getCell(0, rowIndex).$(MATERIAL_NAMES_TEXT).hover();
        return this;
    }

    public MaterialEditPage editMaterial(int rowIndex) {
        getCell(3, rowIndex).$(EDIT_MATERIAL_BUTTON).click();
        return page(MaterialEditPage.class);
    }

    public ItemEditPage createItem(int rowIndex) {
        getCell(3, rowIndex).$(CREATE_ITEM_BUTTON).click();
        return page(ItemEditPage.class);
    }

    public ACObjectModalPage changePermissions(int rowIndex) {
        getCell(3, rowIndex).$(CHANGE_PERMISSIONS_BUTTON).click();
        return page(ACObjectModalPage.class);
    }

    public MaterialSearchResultsTable deactivateMaterial(int rowIndex) {
        getCell(3, rowIndex).$(DEACTIVATE_MATERIAL_BUTTON).click();
        return this;
    }

    /*
     * Getters
     */
    public SelenideElement getMaterialNames(int rowIndex) {
        return getCell(0, rowIndex);
    }

    /**
     * Hover over the material names text via {@link #hoverOverMaterialNames(int)}
     * before calling this getter.
     * 
     * @return tooltip
     */
    public PrimeFacesTooltip getMaterialNamesTooltip() {
        return MATERIAL_NAMES_TOOLTIP;
    }

    public SelenideElement getMaterialType(int rowIndex) {
        return getCell(1, rowIndex);
    }

    // TODO: 3rd column, which can be text, images and what not
}