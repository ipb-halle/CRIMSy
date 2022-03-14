/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.integrationtests.settings.usermanagement;

import static com.codeborne.selenide.Condition.empty;
import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Selenide.open;
import static de.ipb_halle.pageobjects.util.conditions.Conditions.uiMessage;
import static de.ipb_halle.test.UniqueGenerators.uniqueLogin;
import static java.time.Duration.ZERO;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.codeborne.selenide.junit5.SoftAssertsExtension;

import de.ipb_halle.pageobjects.pages.LoginPage;
import de.ipb_halle.pageobjects.pages.search.SearchPage;
import de.ipb_halle.pageobjects.pages.settings.usermanagement.UserDialog;
import de.ipb_halle.pageobjects.pages.settings.usermanagement.UserManagementPage;
import de.ipb_halle.pageobjects.pages.settings.usermanagement.UserModel;
import de.ipb_halle.test.ModelTools;
import de.ipb_halle.test.SelenideEachExtension;

/**
 * @author flange
 */
@ExtendWith({ SelenideEachExtension.class, SoftAssertsExtension.class })
@DisplayName("Test user deletion")
public class DeleteUserTest {
    private UserManagementPage userManagementPage;
    private Locale locale = Locale.ENGLISH;

    @BeforeEach
    public void beforeEach() {
        userManagementPage = open("/", LoginPage.class).loginAsAdmin(SearchPage.class)
                .navigateTo(UserManagementPage.class);
    }

    private void createUser(UserModel user) {
        userManagementPage.createUser().applyModel(user).confirm();
    }

    private void deleteUser(UserModel user) {
        LoginPage loginPage = userManagementPage.logout(LoginPage.class).navigateToLoginPage();
        ModelTools.deleteUser(user.getLogin(), loginPage);
    }

    @Test
    @DisplayName("After opening the delete user dialog, it should have the correct title and the input fields should be filled with the user's data.")
    public void test_deleteUserDialog_checkTitleAndUserData() {
        String password = "12345678";
        UserModel userToDelete = new UserModel().login(uniqueLogin()).name("test user XYZ").shortcut("ABCDEF")
                .email("user@test.example").password(password).passwordRepeat(password).phone("CALL-911");
        createUser(userToDelete);

        UserDialog dialog = userManagementPage.getUsersTable().search(userToDelete.getLogin()).deleteUser(0);

        dialog.title().shouldBe(uiMessage("userMgr_mode_deleteUser", locale));
        dialog.nameInput().shouldHave(value(userToDelete.getName()));
        dialog.loginInput().shouldHave(value(userToDelete.getLogin()));
        dialog.shortcutInput().shouldHave(value(userToDelete.getShortcut()));
        dialog.emailInput().shouldHave(value(userToDelete.getEmail()));
        dialog.phoneInput().shouldHave(value(userToDelete.getPhone()));
        dialog.idInput().shouldNotBe(empty, ZERO);

        dialog.close();
        deleteUser(userToDelete);
    }

    @Test
    @DisplayName("After successful user deletion, the users table should not list this user and the user should not be able to log in anymore.")
    public void test_deleteUser_emptyTable_and_userCannotLogin() {
        String password = "12345678";
        UserModel userToDelete = new UserModel().login(uniqueLogin()).name("test user XYZ").password(password)
                .passwordRepeat(password);
        createUser(userToDelete);
        UserDialog dialog = userManagementPage.getUsersTable().search(userToDelete.getLogin()).deleteUser(0);

        dialog.confirm();

        userManagementPage.getUsersTable().search(userToDelete.getLogin()).shouldBeEmpty();

        LoginPage loginPage = userManagementPage.logout(LoginPage.class).navigateToLoginPage();
        loginPage.login(userToDelete.getLogin(), userToDelete.getPassword(), LoginPage.class);

        loginPage.shouldNotBeLoggedIn();
    }
}