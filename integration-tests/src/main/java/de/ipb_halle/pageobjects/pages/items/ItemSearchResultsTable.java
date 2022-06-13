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
package de.ipb_halle.pageobjects.pages.items;

import static com.codeborne.selenide.Selenide.page;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.components.PrimeFacesTooltip;
import de.ipb_halle.pageobjects.components.table.DataTable;
import de.ipb_halle.pageobjects.pages.composite.acobjectmodal.ACObjectModalPage;

/**
 * Page object for the search results table in
 * /ui/web/WEB-INF/templates/item/items.xhtml
 * 
 * @author flange
 */
public class ItemSearchResultsTable extends DataTable<ItemSearchResultsTable> {
    private static final String MATERIAL_NAMES_TEXT = testId("itemOverview:resultsTable:materialNames");
    private static final PrimeFacesTooltip MATERIAL_NAMES_TOOLTIP = new PrimeFacesTooltip(
            "itemOverview:resultsTable:materialNamesTooltip");
    private static final String EDIT_ITEM_BUTTON = testId("itemOverview:resultsTable:editItem");
    private static final String CHANGE_PERMISSIONS_BUTTON = testId("itemOverview:resultsTable:changePermissions");
    private static final String CREATE_SOLUTION_BUTTON = testId("itemOverview:resultsTable:createSolution");
    private static final String CREATE_ALIQUOT_BUTTON = testId("itemOverview:resultsTable:createAliquot");

    public ItemSearchResultsTable(String testId) {
        super(testId);
    }

    /*
     * Actions
     */
    public ItemSearchResultsTable hoverOverMaterialNames(int rowIndex) {
        getCell(0, rowIndex).$(MATERIAL_NAMES_TEXT).hover();
        return this;
    }

//    public ItemEditPage editItem(int rowIndex) {
//        getCell(7, rowIndex).$(EDIT_ITEM_BUTTON).click();
//        return page(ItemEditPage.class);
//    }

    public ACObjectModalPage changePermissions(int rowIndex) {
        getCell(7, rowIndex).$(CHANGE_PERMISSIONS_BUTTON).click();
        return page(ACObjectModalPage.class);
    }

//    public CreateSolutionPage createSolution(int rowIndex) {
//        getCell(7, rowIndex).$(CREATE_SOLUTION_BUTTON).click();
//        return page(CreateSolutionPage.class);
//    }

//    public CreateAliquotPage createAliquot(int rowIndex) {
//        getCell(7, rowIndex).$(CREATE_ALIQUOT_BUTTON).click();
//        return page(CreateAliquotPage.class);
//    }

    /*
     * Getters
     */
    public SelenideElement getLabel(int rowIndex) {
        return getCell(0, rowIndex);
    }

    public SelenideElement getMaterial(int rowIndex) {
        return getCell(1, rowIndex);
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

    public SelenideElement getProjectOwner(int rowIndex) {
        return getCell(2, rowIndex);
    }

    public SelenideElement getAmount(int rowIndex) {
        return getCell(3, rowIndex);
    }

    public SelenideElement getLocation(int rowIndex) {
        return getCell(4, rowIndex);
    }

    public SelenideElement getDescription(int rowIndex) {
        return getCell(5, rowIndex);
    }

    public SelenideElement getDates(int rowIndex) {
        return getCell(6, rowIndex);
    }
}