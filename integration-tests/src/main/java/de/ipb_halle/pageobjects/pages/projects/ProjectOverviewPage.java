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

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.navigation.Navigation;
import de.ipb_halle.pageobjects.pages.NavigablePage;

/**
 * Page object for /ui/web/WEB-INF/templates/project/projectOverview.xhtml
 * 
 * @author flange
 */
public class ProjectOverviewPage extends NavigablePage {
    private static final SelenideElement NEW_PROJECT_BUTTON = $(testId("projectOverview:newProject"));
    private static final ProjectsTable PROJECTS_TABLE = new ProjectsTable("projectOverview:projectsTable");

    @Override
    public Navigation getNavigationItem() {
        return Navigation.PROJECTS_OVERVIEW;
    }

    /*
     * Actions
     */
    public ProjectEditPage newProject() {
        NEW_PROJECT_BUTTON.click();
        return page(ProjectEditPage.class);
    }

    /*
     * Getters
     */
    /**
     * @return page object of the projects data table
     */
    public ProjectsTable getProjectsTable() {
        return PROJECTS_TABLE;
    }
}