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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private static final Condition EMPTY_CONDITION = new CssClass(
            "dataTables_empty");

    private final List<String> columns;
    private final Map<String, List<SelenideElement>> data;

    private DataTable(List<String> columns,
            Map<String, List<SelenideElement>> data) {
        this.columns = columns;
        this.data = data;
    }

    /**
     * Immediately extract all cells of the given table to a page object.
     * 
     * @param table
     * @return page object
     */
    public static DataTable extract(SelenideElement table) {
        Map<String, List<SelenideElement>> data = new HashMap<>();
        List<String> cols = table.$$("thead tr th").texts();
        for (String col : cols) {
            data.put(col, new ArrayList<>());
        }

        ElementsCollection rows = table.$$("tbody tr");
        for (SelenideElement row : rows) {
            ElementsCollection elements = row.$$("td");
            for (int i = 0; i < elements.size(); i++) {
                data.get(cols.get(i)).add(elements.get(i));
            }
        }

        return new DataTable(cols, data);
    }

    /**
     * @return {@code true} if the table is empty
     */
    public boolean isEmpty() {
        return getCell(columns.get(0), 0).has(EMPTY_CONDITION);
    }

    /**
     * @return all column names
     */
    public List<String> getColumns() {
        return columns;
    }

    /**
     * @param colName
     * @return list with row elements (&lt;td&gt;) for the given column name or
     *         {@code null} if the column does not exist.
     */
    public List<SelenideElement> getCol(String colName) {
        return data.get(colName);
    }

    /**
     * @param colName
     * @param row
     * @return cell content for the given column name and row number (starting
     *         with index 0)
     */
    public SelenideElement getCell(String colName, int row) {
        return data.get(colName).get(row);
    }
}