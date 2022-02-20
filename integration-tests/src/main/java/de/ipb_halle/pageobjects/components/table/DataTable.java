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

import java.util.ArrayList;
import java.util.List;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import com.codeborne.selenide.conditions.CssClass;

/**
 * Page object representing a BootsFaces DataTable.
 * 
 * @author flange
 */
public class DataTable {
    private static final Condition EMPTY_CONDITION = new CssClass("dataTables_empty");

    private final SelenideElement table;

    public DataTable(String testId) {
        table = $(testId(testId));
    }

    /*
     * Getters
     */
    /**
     * @return {@code true} if the table is empty
     */
    public boolean isEmpty() {
        return getCell(0, 0).has(EMPTY_CONDITION);
    }

    /**
     * @return all column names
     */
    public List<String> getColumnNames() {
        return table.$$("thead tr th").texts();
    }

    /**
     * @param colName
     * @return list with row elements (&lt;td&gt;) for the given column name
     */
    public List<SelenideElement> getCol(String colName) {
        int index = getIndexOfCol(colName);
        return getCol(index);
    }

    /**
     * @param colIndex
     * @return list with row elements (&lt;td&gt;) for the given column index
     */
    public List<SelenideElement> getCol(int colIndex) {
        List<SelenideElement> results = new ArrayList<>();

        ElementsCollection rows = table.$$("tbody tr");
        for (SelenideElement row : rows) {
            results.add(row.$$("td").get(colIndex));
        }
        return results;
    }

    /**
     * @param rowIndex
     * @return list with column elements (&lt;td&gt;) for the given row index
     */
    public ElementsCollection getRow(int rowIndex) {
        return table.$$("tbody tr").get(rowIndex).$$("td");
    }

    /**
     * @param colName
     * @param rowIndex
     * @return cell content for the given column name and row index
     */
    public SelenideElement getCell(String colName, int rowIndex) {
        int col = getIndexOfCol(colName);
        return getCell(col, rowIndex);
    }

    /**
     * @param colIndex
     * @param rowIndex
     * @return cell content for the given column and row indices
     */
    public SelenideElement getCell(int colIndex, int rowIndex) {
        return getRow(rowIndex).get(colIndex);
    }

    private int getIndexOfCol(String colName) {
        ElementsCollection cols = table.$$("thead tr th");
        for (int i = 0; i < cols.size(); i++) {
            String name = cols.get(i).text();
            if (colName.equals(name)) {
                return i;
            }
        }
        throw new RuntimeException("Column with name '" + colName + "' does not exist in table " + table);
    }

    /*
     * Fluent assertions
     */
    public DataTable shouldBeEmpty() {
        getCell(0, 0).shouldBe(EMPTY_CONDITION);
        return this;
    }

    public DataTable shouldNotBeEmpty() {
        getCell(0, 0).shouldNotBe(EMPTY_CONDITION);
        return this;
    }
}