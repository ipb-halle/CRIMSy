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

import static com.codeborne.selenide.Condition.exactTextCaseSensitive;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;
import static de.ipb_halle.pageobjects.util.Apply.applySelection;
import static de.ipb_halle.pageobjects.util.Apply.applyValue;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.pages.AbstractPage;
import de.ipb_halle.pageobjects.pages.composite.ChangeOwnerModalPage;
import de.ipb_halle.pageobjects.pages.composite.ChangeOwnerModalPage.UsersTable;

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
            "projectEdit:addableGroupsTable");

    /*
     * Actions
     */
    /**
     * Applies the project model.
     * <p>
     * Convention: The input element will not be evaluated in case the model field
     * is null. Use empty strings to reset fields.
     * 
     * @param model
     * @return this
     */
    public ProjectEditPage applyModel(ProjectModel model) {
        applyValue(model.getName(), PROJECT_NAME_INPUT);
        applySelection(model.getProjectType(), PROJECT_TYPE_SELECTION);

        String owner = model.getOwner();
        if (owner != null) {
            UsersTable table = openProjectOwnerModal().usersTable().search(owner);
            table.getUserName(0).shouldBe(exactTextCaseSensitive(owner));
            table.selectUser(0);
        }

        applyValue(model.getDescription(), PROJECT_DESCRIPTION_INPUT);
        // TODO: groups

        return this;
    }

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

    public ProjectEditPage setProjectName(String name) {
        PROJECT_NAME_INPUT.setValue(name);
        return this;
    }

    public ProjectEditPage selectProjectType(String type) {
        PROJECT_TYPE_SELECTION.selectOption(type);
        return this;
    }

    public ChangeOwnerModalPage openProjectOwnerModal() {
        PROJECT_OWNER_BUTTON.click();
        return page(ChangeOwnerModalPage.class);
    }

    public ProjectEditPage setProjectDescription(String description) {
        PROJECT_DESCRIPTION_INPUT.setValue(description);
        return this;
    }

    /*
     * Getters
     */
    public SelenideElement projectNameInput() {
        return PROJECT_NAME_INPUT;
    }

    public SelenideElement projectTypeSelection() {
        return PROJECT_TYPE_SELECTION;
    }

    public SelenideElement projectDescriptionInput() {
        return PROJECT_DESCRIPTION_INPUT;
    }

    public SelenideElement projectOwnerInput() {
        return PROJECT_OWNER_INPUT;
    }

    public ProjectACETable aceTable() {
        return ACE_TABLE;
    }

    public AddableGroupsTable addableGroupsTable() {
        return ADDABLE_GROUPS_TABLE;
    }
}