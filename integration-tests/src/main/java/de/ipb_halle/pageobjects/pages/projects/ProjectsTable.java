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
package de.ipb_halle.pageobjects.pages.projects;

import static com.codeborne.selenide.Selenide.page;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import de.ipb_halle.pageobjects.components.table.DataTable;
import de.ipb_halle.pageobjects.pages.composite.acobjectmodal.ACObjectModalPage;

/**
 * Page object for the projects table in
 * /ui/web/WEB-INF/templates/project/projectOverview.xhtml
 * 
 * @author flange
 */
public class ProjectsTable extends DataTable {
    private static final String EDIT_PROJECT_BUTTON = testId("projectOverview:editProject");
    private static final String CHANGE_PERMISSIONS_BUTTON = testId("projectOverview:changePermissions");
    private static final String DEACTIVATE_PROJECT_BUTTON = testId("projectOverview:deactivateProject");

    public ProjectsTable(String testId) {
        super(testId);
    }

    /*
     * Actions
     */
    public ProjectEditPage editProject(int rowIndex) {
        getCell(3, rowIndex).$(EDIT_PROJECT_BUTTON).click();
        return page(ProjectEditPage.class);
    }

    public ACObjectModalPage changePermissions(int rowIndex) {
        getCell(3, rowIndex).$(CHANGE_PERMISSIONS_BUTTON).click();
        return page(ACObjectModalPage.class);
    }

    public ProjectsTable deactivateProject(int rowIndex) {
        getCell(3, rowIndex).$(DEACTIVATE_PROJECT_BUTTON).click();
        return this;
    }

    /*
     * Getters
     */
    public String getProjectName(int rowIndex) {
        return getCell(0, rowIndex).text();
    }

    public String getProjectType(int rowIndex) {
        return getCell(1, rowIndex).text();
    }

    public String getProjectOwner(int rowIndex) {
        return getCell(2, rowIndex).text();
    }
}