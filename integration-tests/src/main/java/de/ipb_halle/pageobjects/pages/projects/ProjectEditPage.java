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

import de.ipb_halle.pageobjects.pages.AbstractPage;
import de.ipb_halle.pageobjects.pages.composite.ChangeOwnerModalPage;

/**
 * Page object for /ui/web/WEB-INF/templates/project/projectEdit.xhtml and its
 * child pages
 * <ul>
 * <li>/ui/web/WEB-INF/templates/project/components/commonProjectInformationContainer.xhtml</li>
 * <li>/ui/web/WEB-INF/templates/project/components/template_detailrights.xhtml</li>
 * </ul>
 * 
 * @author flange
 */
public class ProjectEditPage extends AbstractPage<ProjectEditPage> {
    private static final SelenideElement PROJECT_NAME_INPUT = $(testId("input", "projectEdit:projectName"));
    private static final SelenideElement CANCEL_BUTTON = $(testId("projectEdit:cancel"));
    private static final SelenideElement SAVE_BUTTON = $(testId("projectEdit:save"));
    private static final SelenideElement PROJECT_TYPE_SELECTION = $(testId("select", "projectEdit:projectType"));
    private static final SelenideElement PROJECT_DESCRIPTION_INPUT = $(
            testId("textarea", "projectEdit:projectDescription"));
    private static final SelenideElement PROJECT_OWNER_BUTTON = $(testId("projectEdit:projectOwnerButton"));
    private static final SelenideElement PROJECT_OWNER_INPUT = $(testId("input", "projectEdit:projectOwner"));
    private static final ProjectACETable ACE_TABLE = new ProjectACETable("projectEdit:ACETable");
    private static final AddableGroupsTable ADDABLE_GROUPS_TABLE = new AddableGroupsTable(
            testId("projectEdit:addableGroupsTable"));

    /*
     * Actions
     */
    public ProjectOverviewPage cancel() {
        CANCEL_BUTTON.click();
        return page(ProjectOverviewPage.class);
    }

    /**
     * Try to save the project.
     * <p>
     * Should direct the browser to the project overview page or stay on this page
     * depending on the validation outcome, thus only {@link ProjectOverviewPage} or
     * {@link ProjectEditPage} are useful page object classes to be supplied in the
     * {@code expectedPageClass} parameter.
     * 
     * @param <T>
     * @param expectedPageClass expected page
     * @return page object of expected page
     */
    public <T extends AbstractPage<T>> T save(Class<T> expectedPageClass) {
        SAVE_BUTTON.click();
        return page(expectedPageClass);
    }

    public ProjectEditPage selectProjectType(String type) {
        PROJECT_TYPE_SELECTION.selectOption(type);
        return this;
    }

    public ChangeOwnerModalPage openProjectOwnerModal() {
        PROJECT_OWNER_BUTTON.click();
        return page(ChangeOwnerModalPage.class);
    }

    /*
     * Getters
     */
    public SelenideElement getProjectNameInput() {
        return PROJECT_NAME_INPUT;
    }

    public SelenideElement getProjectTypeSelection() {
        return PROJECT_TYPE_SELECTION;
    }

    public SelenideElement getProjectDescriptionInput() {
        return PROJECT_DESCRIPTION_INPUT;
    }

    public SelenideElement getProjectOwnerInput() {
        return PROJECT_OWNER_INPUT;
    }

    public ProjectACETable getAceTable() {
        return ACE_TABLE;
    }

    public AddableGroupsTable getAddableGroupsTable() {
        return ADDABLE_GROUPS_TABLE;
    }
}