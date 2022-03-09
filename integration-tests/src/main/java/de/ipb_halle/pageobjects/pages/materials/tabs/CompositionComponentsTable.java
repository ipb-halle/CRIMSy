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

import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.components.table.DataTable;

/**
 * Page object for the components table in
 * /ui/web/WEB-INF/templates/material/components/composition.xhtml
 * 
 * @author flange
 */
public class CompositionComponentsTable extends DataTable<CompositionComponentsTable> {
    private static final String AMOUNT_INPUT = testId("input", "compositionTab:componentsTable:amount");
    private static final String UNIT_SELECTION = testId("select", "compositionTab:componentsTable:unit");
    private static final String REMOVE_BUTTON = testId("compositionTab:componentsTable:remove");

    public CompositionComponentsTable(String testId) {
        super(testId);
    }

    /*
     * Actions
     */
    public CompositionComponentsTable remove(int rowIndex) {
        getCell(5, rowIndex).$(REMOVE_BUTTON).click();
        return this;
    }

    /*
     * Getters
     */
    public SelenideElement getId(int rowIndex) {
        return getCell(0, rowIndex);
    }

    public SelenideElement getName(int rowIndex) {
        return getCell(1, rowIndex);
    }

    public SelenideElement getMaterialType(int rowIndex) {
        return getCell(2, rowIndex);
    }

    public SelenideElement getAmountInput(int rowIndex) {
        return getCell(3, rowIndex).$(AMOUNT_INPUT);
    }

    public SelenideElement getUnitSelection(int rowIndex) {
        return getCell(4, rowIndex).$(UNIT_SELECTION);
    }
}