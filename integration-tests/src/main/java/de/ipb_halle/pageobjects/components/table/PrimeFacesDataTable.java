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
package de.ipb_halle.pageobjects.components.table;

import static com.codeborne.selenide.Selenide.$;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.conditions.ExactTextCaseSensitive;

/**
 * Page object representing a PrimeFaces DataTable.
 * 
 * @author flange
 */
/*
 * This is a quick-and-dirty solution. Column names are broken, because thead is
 * repeated several times in the same table.
 */
public class PrimeFacesDataTable<T extends PrimeFacesDataTable<T>> extends DataTable<T> {
    private static final Condition EMPTY_CONDITION = new ExactTextCaseSensitive("No records found.");

    public PrimeFacesDataTable(String testId) {
        super($(testId(testId)).$("table", 1));
    }

    /*
     * Getters
     */
    @Override
    public boolean isEmpty() {
        return getCell(0, 0).has(EMPTY_CONDITION);
    }

    /*
     * Fluent assertions
     */
    @Override
    public PrimeFacesDataTable<T> shouldBeEmpty() {
        getCell(0, 0).shouldBe(EMPTY_CONDITION);
        return this;
    }

    @Override
    public PrimeFacesDataTable<T> shouldNotBeEmpty() {
        getCell(0, 0).shouldNotBe(EMPTY_CONDITION);
        return this;
    }
}