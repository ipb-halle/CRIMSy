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
package de.ipb_halle.test;

import java.util.Random;

import de.ipb_halle.pageobjects.pages.LoginPage;
import de.ipb_halle.pageobjects.pages.projects.ProjectModel;
import de.ipb_halle.pageobjects.pages.projects.ProjectOverviewPage;
import de.ipb_halle.pageobjects.pages.search.SearchPage;
import de.ipb_halle.pageobjects.pages.settings.usermanagement.UserDialog;
import de.ipb_halle.pageobjects.pages.settings.usermanagement.UserManagementPage;
import de.ipb_halle.pageobjects.pages.settings.usermanagement.UserModel;

/**
 * 
 * @author flange
 */
public class ModelTools {
    private ModelTools() {
    }

    /**
     * Creates a user according to the model.
     * <p>
     * Requirements: Not logged in and browser is on the login page.
     * <p>
     * Outcome: Not logged in and browser is on the login page.
     * 
     * @param model
     * @param loginPage
     * @return
     */
    public static LoginPage createUser(UserModel model, LoginPage loginPage) {
        return loginPage.loginAsAdmin(SearchPage.class).navigateTo(UserManagementPage.class).createUser()
                .applyModel(model).confirm().logout(LoginPage.class).navigateToLoginPage();
    }

    /**
     * Deletes the user with the given login. It removes the shortcut before.
     * <p>
     * Requirements: Not logged in and browser is on the login page.
     * <p>
     * Outcome: Not logged in and browser is on the login page.
     * 
     * @param login
     * @param loginPage
     * @return
     */
    public static LoginPage deleteUser(String login, LoginPage loginPage) {
        UserDialog dialog = loginPage.loginAsAdmin(SearchPage.class).navigateTo(UserManagementPage.class)
                .getUsersTable().search(login).editUser(0);
        dialog.shortcutInput().setValue("");
        return dialog.confirm().getUsersTable().search(login).deleteUser(0).confirm().logout(LoginPage.class)
                .navigateToLoginPage();
    }

    /**
     * Creates a project according to the model.
     * <p>
     * Requirements: Not logged in and browser is on the login page.
     * <p>
     * Outcome: Not logged in and browser is on the login page.
     * 
     * @param model
     * @param loginPage
     * @return
     */
    public static LoginPage createProject(ProjectModel model, LoginPage loginPage) {
        return loginPage.loginAsAdmin(SearchPage.class).navigateTo(ProjectOverviewPage.class).newProject()
                .applyModel(model).save(ProjectOverviewPage.class).logout(LoginPage.class).navigateToLoginPage();
    }

    /**
     * Deactivates the project with the given name. It renames the project to a
     * random name before.
     * <p>
     * Requirements: Not logged in and browser is on the login page.
     * <p>
     * Outcome: Not logged in and browser is on the login page.
     * 
     * @param name
     * @param loginPage
     * @return
     */
    public static LoginPage deactivateProject(String name, LoginPage loginPage) {
        String newRandomName = String.format("deactivatedProject-%d", new Random().nextInt());

        ProjectOverviewPage page = loginPage.loginAsAdmin(SearchPage.class).navigateTo(ProjectOverviewPage.class)
                .getProjectsTable().search(name).editProject(0).setProjectName(newRandomName)
                .save(ProjectOverviewPage.class);
        page.getProjectsTable().search(newRandomName).deactivateProject(0);

        return page.logout(LoginPage.class).navigateToLoginPage();
    }
}