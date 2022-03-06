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
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.open;
import static de.ipb_halle.pageobjects.util.I18n.JSF_REQUIRED_VALIDATION_ERROR_KEY;
import static de.ipb_halle.pageobjects.util.I18n.getUIMessage;
import static de.ipb_halle.test.Conditions.jsfMessage;
import static de.ipb_halle.test.Conditions.uiMessage;
import static de.ipb_halle.test.UniqueGenerators.uniqueLogin;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.codeborne.selenide.AssertionMode;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.junit5.SoftAssertsExtension;

import de.ipb_halle.pageobjects.pages.LoginPage;
import de.ipb_halle.pageobjects.pages.search.SearchPage;
import de.ipb_halle.pageobjects.pages.settings.usermanagement.UserDialog;
import de.ipb_halle.pageobjects.pages.settings.usermanagement.UserManagementPage;
import de.ipb_halle.pageobjects.pages.settings.usermanagement.UserModel;
import de.ipb_halle.pageobjects.pages.settings.usermanagement.UsersTable;
import de.ipb_halle.test.SelenideEachExtension;

/**
 * @author flange
 */
@ExtendWith({ SelenideEachExtension.class, SoftAssertsExtension.class })
@DisplayName("Test user creation")
public class CreateUserTest {
    private UserManagementPage userManagementPage;
    private Locale locale = Locale.ENGLISH;

    @BeforeEach
    public void beforeEach() {
        Configuration.assertionMode = AssertionMode.SOFT;
        userManagementPage = open("/", LoginPage.class).loginAsAdmin(SearchPage.class)
                .navigateTo(UserManagementPage.class);
    }

    @Test
    @DisplayName("After opening the create user dialog, the input fields should be empty.")
    public void test_createUserDialog_checkEmptyInputs() {
        UserDialog dialog = userManagementPage.createUser();

        dialog.idInput().shouldBe(empty);
        dialog.nameInput().shouldBe(empty);
        dialog.loginInput().shouldBe(empty);
        dialog.shortcutInput().shouldBe(empty);
        dialog.emailInput().shouldBe(empty);
        dialog.passwordInput().shouldBe(empty);
        dialog.passwordRepeatInput().shouldBe(empty);
        dialog.phoneInput().shouldBe(empty);
    }

    @Test
    @DisplayName("If inputs are empty, some validation errors should appear.")
    public void test_createUserDialog_emptyInputs_produceValidationErrorsWhenTryingToSave() {
        UserModel user = new UserModel();
        UserDialog dialog = userManagementPage.createUser();
        dialog.applyModel(user).confirm();

        dialog.nameMessage().shouldBe(visible).shouldHave(jsfMessage(JSF_REQUIRED_VALIDATION_ERROR_KEY, locale));
        dialog.loginMessage().shouldBe(visible).shouldHave(jsfMessage(JSF_REQUIRED_VALIDATION_ERROR_KEY, locale));
        dialog.passwordMessage().shouldBe(visible).shouldHave(jsfMessage(JSF_REQUIRED_VALIDATION_ERROR_KEY, locale));
    }

    @Test
    @DisplayName("If the password is too short, a validation error should appear and no new user should be created.")
    public void test_createUserDialog_tooShortPassword_producesValidationErrorWhenTryingToSave() {
        String login = uniqueLogin();
        UserModel user = validUser(login, "1234567").passwordRepeat(null);
        UserDialog dialog = userManagementPage.createUser();
        dialog.applyModel(user).confirm();

        dialog.passwordMessage().shouldBe(visible).shouldHave(uiMessage("PASSWORD_ERROR_TOO_SHORT", locale));
        dialog.close();
        userManagementPage.getUsersTable().search(login).shouldBeEmpty();
    }

    @Test
    @DisplayName("If the password and repeat do not match, a validation error should appear and new no user should be created.")
    public void test_createUserDialog_wrongPasswordRepeat_producesValidationErrorWhenTryingToSave() {
        String login = uniqueLogin();
        UserModel user = validUser(login, "12345678").passwordRepeat("123456789");
        UserDialog dialog = userManagementPage.createUser();
        dialog.applyModel(user).confirm();

        dialog.passwordMessage().shouldBe(visible).shouldHave(uiMessage("PASSWORD_ERROR__MISMATCH", locale));
        dialog.close();
        userManagementPage.getUsersTable().search(login).shouldBeEmpty();
    }

    @Test
    @DisplayName("If the login already exists, a validation error should appear and no new user should be created.")
    public void test_createUserDialog_createUserWithAlreadyExistingLogin_producesValidationErrorWhenTryingToSave() {
        String login = uniqueLogin();
        UserModel user = validUser(login, "12345678");
        // create a user
        UserDialog dialog = userManagementPage.createUser();
        dialog.applyModel(user).confirm();

        // try to create a user with the same login
        dialog = userManagementPage.createUser();
        dialog.applyModel(user).confirm();

        String expectedText = getUIMessage("admission_non_unique_user", locale) + "\n"
                + getUIMessage("admission_non_unique_user_detail", locale);
        dialog.loginMessage().shouldBe(visible).shouldHave(exactTextCaseSensitive(expectedText));

        dialog.close();
        // delete existing user
        userManagementPage.getUsersTable().search(login).deleteUser(0).confirm();
    }

    @Test
    @DisplayName("If the shortcut already exists, a validation error should appear and no new user should be created.")
    public void test_createUserDialog_createUserWithAlreadyExistingShortcut_producesValidationErrorWhenTryingToSave() {
        String login = uniqueLogin();
        String newLogin = uniqueLogin();
        UserModel user = validUser(login, "12345678").shortcut("XYZ");
        // create a user
        UserDialog dialog = userManagementPage.createUser();
        dialog.applyModel(user).confirm();

        // try to create a user with the same shortcut
        user.login(newLogin);
        dialog = userManagementPage.createUser();
        dialog.applyModel(user).confirm();

        String expectedText = getUIMessage("admission_non_unique_shortcut", locale) + "\n"
                + getUIMessage("admission_non_unique_shortcut_detail", locale);
        dialog.shortcutMessage().shouldBe(visible).shouldHave(exactTextCaseSensitive(expectedText));
        dialog.close();
        userManagementPage.getUsersTable().search(newLogin).shouldBeEmpty();

        /*
         * Remove shortcut for the existing user, so we don't run into trouble the next
         * time this test is executed.
         */
        dialog = userManagementPage.getUsersTable().search(login).editUser(0);
        dialog.shortcutInput().setValue("");
        dialog.confirm();
        // delete existing user
        userManagementPage.getUsersTable().search(login).deleteUser(0).confirm();
    }

    @Test
    @DisplayName("If the shortcut does not only contain letters, a validation error should appear and no new user should be created.")
    public void test_createUserDialog_nonAlphabeticShortcut_producesValidationErrorWhenTryingToSave() {
        String login = uniqueLogin();
        UserModel user = validUser(login, "12345678").shortcut("ABC123");
        UserDialog dialog = userManagementPage.createUser();
        dialog.applyModel(user).confirm();

        String expectedText = getUIMessage("admission_shortcut_wrongpattern", locale) + "\n"
                + getUIMessage("admission_shortcut_wrongpattern_detail", locale);
        dialog.shortcutMessage().shouldBe(visible).shouldHave(exactTextCaseSensitive(expectedText));
        dialog.close();
        userManagementPage.getUsersTable().search(login).shouldBeEmpty();
    }

    @Test
    @DisplayName("After successful creation of a user, the users table should show its data.")
    public void test_createUser_and_checkItsData() {
        String login = uniqueLogin();
        String name = "test user XYZ";
        String shortcut = "ABCDEF";
        String email = "user@test.example";
        String password = "12345678";
        String phone = "CALL-911";

        UserModel user = new UserModel().login(login).name(name).shortcut(shortcut).email(email).password(password)
                .passwordRepeat(password).phone(phone);
        UserDialog dialog = userManagementPage.createUser();
        dialog.applyModel(user).confirm();

        UsersTable table = userManagementPage.getUsersTable().search(login);
        table.getName(0).shouldHave(exactTextCaseSensitive(name));
        table.getLogin(0).shouldHave(exactTextCaseSensitive(login));
        table.getShortcut(0).shouldHave(exactTextCaseSensitive(shortcut));
        table.getEmail(0).shouldHave(exactTextCaseSensitive(email));
        table.getPhone(0).shouldHave(exactTextCaseSensitive(phone));
        table.getType(0).shouldHave(exactTextCaseSensitive("LOCAL"));
//        table.getInstitute(0).shouldHave(exactTextCaseSensitive("???"));

        /*
         * Remove shortcut for the user, so we don't run into trouble the next time this
         * test is executed.
         */
        table.editUser(0);
        dialog.shortcutInput().setValue("");
        dialog.confirm();
        // delete user
        userManagementPage.getUsersTable().search(login).deleteUser(0).confirm();
    }

    @Test
    @DisplayName("If the email is invalid, a validation error should appear and no new user should be created.")
    public void test_createUserDialog_invalidEmail_producesValidationErrorWhenTryingToSave() {
        String login = uniqueLogin();
        UserModel user = validUser(login, "12345678").email("user@test");
        UserDialog dialog = userManagementPage.createUser();
        dialog.applyModel(user).confirm();

        dialog.emailMessage().shouldBe(visible).shouldHave(uiMessage("admission_invalid_email", locale));
        dialog.close();
        userManagementPage.getUsersTable().search(login).shouldBeEmpty();
    }

    @Test
    @DisplayName("After successful creation of a user, the user should be able to log in.")
    public void test_createUser_and_login() {
        String login = uniqueLogin();
        String password = "12345678";
        String username = "testuser123";
        UserModel user = validUser(login, password).name(username);
        UserDialog dialog = userManagementPage.createUser();
        dialog.applyModel(user).confirm();
        SearchPage searchPage = userManagementPage.logout(LoginPage.class).navigateToLoginPage().login(login, password,
                SearchPage.class);

        searchPage.shouldBeLoggedIn().userNameShouldBe(username);

        // delete user
        searchPage.logout(LoginPage.class).navigateToLoginPage().loginAsAdmin(SearchPage.class)
                .navigateTo(UserManagementPage.class).getUsersTable().search(login).deleteUser(0).confirm();
    }

    private UserModel validUser(String login, String password) {
        return new UserModel().name("testuser").login(login).shortcut("").email("").password(password)
                .passwordRepeat(password).phone("");
    }
}