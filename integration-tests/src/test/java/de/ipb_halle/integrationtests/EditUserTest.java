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
package de.ipb_halle.integrationtests;

import static com.codeborne.selenide.Condition.empty;
import static com.codeborne.selenide.Condition.exactTextCaseSensitive;
import static com.codeborne.selenide.Condition.value;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.open;
import static de.ipb_halle.pageobjects.util.I18n.JSF_REQUIRED_VALIDATION_ERROR_KEY;
import static de.ipb_halle.pageobjects.util.I18n.getUIMessage;
import static de.ipb_halle.pageobjects.util.conditions.Conditions.jsfMessage;
import static de.ipb_halle.pageobjects.util.conditions.Conditions.uiMessage;
import static de.ipb_halle.test.UniqueGenerators.uniqueLogin;
import static java.time.Duration.ZERO;

import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
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
import de.ipb_halle.pageobjects.pages.settings.usermanagement.UsersTable;
import de.ipb_halle.test.ModelTools;
import de.ipb_halle.test.SelenideEachExtension;

/**
 * @author flange
 */
@ExtendWith({ SelenideEachExtension.class, SoftAssertsExtension.class })
@DisplayName("Test user edit")
public class EditUserTest {
    private UserManagementPage userManagementPage;
    private Locale locale = Locale.ENGLISH;
    private UserModel userToEdit;

    @BeforeEach
    public void beforeEach() {
        String password = "12345678";
        userToEdit = new UserModel().login(uniqueLogin()).name("test user XYZ").shortcut("ABCDEF")
                .email("user@test.example").password(password).passwordRepeat(password).phone("CALL-911");
        LoginPage loginPage = ModelTools.createUser(userToEdit, open("/", LoginPage.class));

        userManagementPage = loginPage.loginAsAdmin(SearchPage.class).navigateTo(UserManagementPage.class);
    }

    @AfterEach
    public void afterEach() {
        LoginPage loginPage = userManagementPage.logout(LoginPage.class).navigateToLoginPage();
        ModelTools.deleteUser(userToEdit.getLogin(), loginPage);
    }

    @Test
    @DisplayName("After opening the edit user dialog, it should have the correct title, the input fields should be filled with the user's data and the password fields should be empty.")
    public void test_editUserDialog_checkTitleAndUserData_and_passwordsAreEmpty() {
        UserDialog dialog = userManagementPage.getUsersTable().search(userToEdit.getLogin()).editUser(0);

        dialog.title().shouldBe(uiMessage("userMgr_mode_updateUser", locale));
        dialog.nameInput().shouldHave(value(userToEdit.getName()));
        dialog.loginInput().shouldHave(value(userToEdit.getLogin()));
        dialog.shortcutInput().shouldHave(value(userToEdit.getShortcut()));
        dialog.emailInput().shouldHave(value(userToEdit.getEmail()));
        dialog.phoneInput().shouldHave(value(userToEdit.getPhone()));
        dialog.idInput().shouldNotBe(empty, ZERO);
        dialog.newPasswordInput().shouldBe(empty);
        dialog.newPasswordRepeatInput().shouldBe(empty);

        dialog.close();
    }

    @Test
    @DisplayName("If inputs are empty, some validation errors should appear.")
    public void test_editUserDialog_emptyInputs_produceValidationErrorsWhenTryingToSave() {
        UserModel emptyModel = new UserModel().name("").login("").shortcut("").email("").phone("");
        UserDialog dialog = userManagementPage.getUsersTable().search(userToEdit.getLogin()).editUser(0);
        dialog.applyModel(emptyModel).confirm();

        dialog.nameMessage().shouldBe(visible).shouldHave(jsfMessage(JSF_REQUIRED_VALIDATION_ERROR_KEY, locale));
        dialog.loginMessage().shouldBe(visible).shouldHave(jsfMessage(JSF_REQUIRED_VALIDATION_ERROR_KEY, locale));

        dialog.close();
    }

    @Test
    @DisplayName("If the login already exists, a validation error should appear.")
    public void test_editUserDialog_changeUserLoginToAlreadyExistingLogin_producesValidationErrorWhenTryingToSave() {
        String alreadyExistingLogin = uniqueLogin();
        UserModel user = validUser(alreadyExistingLogin, "12345678");
        // create another user
        UserDialog dialog = userManagementPage.createUser();
        dialog.applyModel(user).confirm();

        // edit the user and try to save it with the already existing login
        dialog = userManagementPage.getUsersTable().search(userToEdit.getLogin()).editUser(0);
        dialog.loginInput().setValue(alreadyExistingLogin);
        dialog.confirm();

        String expectedText = getUIMessage("admission_non_unique_user", locale) + "\n"
                + getUIMessage("admission_non_unique_user_detail", locale);
        dialog.loginMessage().shouldBe(visible).shouldHave(exactTextCaseSensitive(expectedText));

        dialog.close();
        // delete the user created in this test
        userManagementPage.getUsersTable().search(alreadyExistingLogin).deleteUser(0).confirm();
    }

    @Test
    @DisplayName("If the shortcut already exists, a validation error should appear.")
    public void test_editUserDialog_cahngeUserShortcutToAlreadyExistingShortcut_producesValidationErrorWhenTryingToSave() {
        String login = uniqueLogin();
        String alreadyExistingShortcut = "UVWXYZ";
        UserModel user = validUser(login, "12345678").shortcut(alreadyExistingShortcut);
        // create another user
        UserDialog dialog = userManagementPage.createUser();
        dialog.applyModel(user).confirm();

        // edit the user and try to save it with the already existing shortcut
        dialog = userManagementPage.getUsersTable().search(userToEdit.getLogin()).editUser(0);
        dialog.shortcutInput().setValue(alreadyExistingShortcut);
        dialog.confirm();

        String expectedText = getUIMessage("admission_non_unique_shortcut", locale) + "\n"
                + getUIMessage("admission_non_unique_shortcut_detail", locale);
        dialog.shortcutMessage().shouldBe(visible).shouldHave(exactTextCaseSensitive(expectedText));

        dialog.close();
        /*
         * Remove shortcut for the existing user, so we don't run into trouble the next
         * time this test is executed.
         */
        dialog = userManagementPage.getUsersTable().search(login).editUser(0);
        dialog.shortcutInput().setValue("");
        dialog.confirm();
        // delete the user created in this test
        userManagementPage.getUsersTable().search(login).deleteUser(0).confirm();
    }

    @Test
    @DisplayName("After successful edit of a user, the users table should show its changed data.")
    public void test_editUser_and_checkItsData() {
        UserDialog dialog = userManagementPage.getUsersTable().search(userToEdit.getLogin()).editUser(0);
        userToEdit.login(uniqueLogin()).name("changed test user name").shortcut("MNO")
                .email("changedemail@test.example").password(null).passwordRepeat(null).phone("CALL-110");

        dialog.applyModel(userToEdit).confirm();

        UsersTable table = userManagementPage.getUsersTable().search(userToEdit.getLogin());
        table.getName(0).shouldHave(exactTextCaseSensitive(userToEdit.getName()));
        table.getLogin(0).shouldHave(exactTextCaseSensitive(userToEdit.getLogin()));
        table.getShortcut(0).shouldHave(exactTextCaseSensitive(userToEdit.getShortcut()));
        table.getEmail(0).shouldHave(exactTextCaseSensitive(userToEdit.getEmail()));
        table.getPhone(0).shouldHave(exactTextCaseSensitive(userToEdit.getPhone()));
        table.getType(0).shouldHave(exactTextCaseSensitive("LOCAL"));
//      table.getInstitute(0).shouldHave(exactTextCaseSensitive("???"));
    }

    @Test
    @DisplayName("After successful change of a user's login, the user should be not able to log in the old login and should be able to log in with the new login.")
    public void test_changeLogin_and_login() {
        String newLogin = uniqueLogin();
        String newUsername = "changed test user name";
        UserDialog dialog = userManagementPage.getUsersTable().search(userToEdit.getLogin()).editUser(0);
        dialog.loginInput().setValue(newLogin);
        dialog.nameInput().setValue(newUsername);

        dialog.confirm();

        // try to log in with old login
        LoginPage loginPage = userManagementPage.logout(LoginPage.class).navigateToLoginPage()
                .login(userToEdit.getLogin(), userToEdit.getPassword(), LoginPage.class);
        loginPage.shouldNotBeLoggedIn();

        // apply changes to the model and log in with new login
        userToEdit.login(newLogin).name(newUsername);
        SearchPage searchPage = loginPage.login(userToEdit.getLogin(), userToEdit.getPassword(), SearchPage.class);
        searchPage.shouldBeLoggedIn().userNameShouldBe(userToEdit.getName());
    }

    @Test
    @DisplayName("If the password is too short, a validation error should appear.")
    public void test_changePassword_tooShortPassword_producesValidationErrorWhenTryingToSave() {
        UserDialog dialog = userManagementPage.getUsersTable().search(userToEdit.getLogin()).editUser(0);
        dialog.newPasswordInput().setValue("1234567");

        dialog.confirmChangePassword();

        dialog.newPasswordMessage().shouldBe(visible).shouldHave(uiMessage("PASSWORD_ERROR_TOO_SHORT", locale));

        dialog.close();
    }

    @Test
    @DisplayName("If the password and repeat do not match, a validation error should appear.")
    public void test_changePassword_wrongPasswordRepeat_producesValidationErrorWhenTryingToSave() {
        UserDialog dialog = userManagementPage.getUsersTable().search(userToEdit.getLogin()).editUser(0);
        dialog.newPasswordInput().setValue("12345678");
        dialog.newPasswordRepeatInput().setValue("123456789");

        dialog.confirmChangePassword();

        dialog.newPasswordMessage().shouldBe(visible).shouldHave(uiMessage("PASSWORD_ERROR__MISMATCH", locale));

        dialog.close();
    }

    @Test
    @DisplayName("After successful change of a user's password, the user should be able to log in with this new password.")
    public void test_changePassword_and_login() {
        String newPassword = "newPassword12345678";
        UserDialog dialog = userManagementPage.getUsersTable().search(userToEdit.getLogin()).editUser(0);
        dialog.newPasswordInput().setValue(newPassword);
        dialog.newPasswordRepeatInput().setValue(newPassword);

        dialog.confirmChangePassword();

        // log in
        SearchPage searchPage = userManagementPage.logout(LoginPage.class).navigateToLoginPage()
                .login(userToEdit.getLogin(), newPassword, SearchPage.class);

        searchPage.shouldBeLoggedIn().userNameShouldBe(userToEdit.getName());
    }

    private UserModel validUser(String login, String password) {
        return new UserModel().name("testuser").login(login).password(password).passwordRepeat(password);
    }
}