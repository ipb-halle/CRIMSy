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
package de.ipb_halle.pageobjects.pages.settings.reports;

import static de.ipb_halle.pageobjects.util.Selectors.testId;

import java.io.File;
import java.io.FileNotFoundException;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.components.table.DataTable;

/**
 * Page object for the reports table in
 * /ui/web/WEB-INF/templates/myReports.xhtml
 * 
 * @author flange
 */
public class ReportsTable extends DataTable<ReportsTable> {
    private static final String DOWNLOAD_REPORT_BUTTON = testId("myReports:downloadReport");
    private static final String DELETE_REPORT_BUTTON = testId("myReports:deleteReport");

    public ReportsTable(String testId) {
        super(testId);
    }

    /*
     * Actions
     */
    public File downloadReport(int rowIndex) {
        try {
            return getCell(2, rowIndex).$(DOWNLOAD_REPORT_BUTTON).download();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public ReportsTable deleteReport(int rowIndex) {
        getCell(2, rowIndex).$(DELETE_REPORT_BUTTON).click();
        return this;
    }

    /*
     * Getters
     */
    public SelenideElement getSubmissionDate(int rowIndex) {
        return getCell(0, rowIndex);
    }

    public SelenideElement getStatus(int rowIndex) {
        return getCell(1, rowIndex);
    }
}