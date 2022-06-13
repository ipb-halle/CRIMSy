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

import static com.codeborne.selenide.Selenide.$;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.navigation.Navigation;
import de.ipb_halle.pageobjects.pages.NavigablePage;

/**
 * Page object for /ui/web/WEB-INF/templates/myReports.xhtml
 * 
 * @author flange
 */
public class ReportsPage extends NavigablePage<ReportsPage> {
    private static final SelenideElement REFRESH_BUTTON = $(testId("myReports:refresh"));
    private static final ReportsTable REPORTS_TABLE = new ReportsTable("myReports:reportsTable");

    @Override
    public Navigation getNavigationItem() {
        return Navigation.MY_REPORTS;
    }

    /*
     * Actions
     */
    public ReportsPage refresh() {
        REFRESH_BUTTON.click();
        return this;
    }

    /*
     * Getters
     */
    public ReportsTable reportsTable() {
        return REPORTS_TABLE;
    }
}