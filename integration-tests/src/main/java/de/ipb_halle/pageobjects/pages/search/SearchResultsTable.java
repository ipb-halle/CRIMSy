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
package de.ipb_halle.pageobjects.pages.search;

import static de.ipb_halle.pageobjects.util.Selectors.testId;

import java.io.File;
import java.io.FileNotFoundException;

import de.ipb_halle.pageobjects.components.table.DataTable;

/**
 * Page object for the search results table in
 * /ui/web/WEB-INF/templates/default.xhtml
 * 
 * @author flange
 */
public class SearchResultsTable extends DataTable {
    private static final String LINK = testId("search:searchResultsTable:link");
    private static final String DOWNLOAD = testId("a",
            "search:searchResultsTable:link");

    public SearchResultsTable(String testId) {
        super(testId);
    }

    /*
     * Actions
     */
    public void clickOnName(int rowIndex) {
        getCell(0, rowIndex).$(LINK).click();
    }

    public File clickOnNameAndDownload(int rowIndex) {
        try {
            return getCell(0, rowIndex).$(DOWNLOAD).download();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Getters
     */
    public String getName(int rowIndex) {
        return getCell(0, rowIndex).text();
    }

    public String getLocation(int rowIndex) {
        return getCell(1, rowIndex).text();
    }

    public String getType(int rowIndex) {
        return getCell(2, rowIndex).text();
    }

    public String getRelevance(int rowIndex) {
        return getCell(3, rowIndex).text();
    }
}