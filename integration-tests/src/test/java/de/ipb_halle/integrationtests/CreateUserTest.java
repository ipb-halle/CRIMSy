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

import static com.codeborne.selenide.Condition.exactTextCaseSensitive;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.open;
import static de.ipb_halle.pageobjects.util.I18n.JSF_REQUIRED_VALIDATION_ERROR_KEY;
import static de.ipb_halle.pageobjects.util.I18n.getUIMessage;
import static de.ipb_halle.test.Conditions.jsfMessage;
import static de.ipb_halle.test.Conditions.uiMessage;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.ipb_halle.pageobjects.pages.LoginPage;
import de.ipb_halle.pageobjects.pages.search.SearchPage;
import de.ipb_halle.pageobjects.pages.settings.usermanagement.UserDialog;
import de.ipb_halle.pageobjects.pages.settings.usermanagement.UserManagementPage;
import de.ipb_halle.pageobjects.pages.settings.usermanagement.UserModel;
import de.ipb_halle.test.SelenideEachExtension;

/**
 * @author flange
 */
@ExtendWith(SelenideEachExtension.class)
@DisplayName("Test user creation")
public class CreateUserTest {
    private static UserManagementPage userManagementPage;
    private Locale locale = Locale.ENGLISH;

    @BeforeEach
    public void beforeEach() {
        userManagementPage = open("/", LoginPage.class).loginAsAdmin(SearchPage.class)
                .navigateTo(UserManagementPage.class);
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
        String username = "test-001";
        UserModel user = validUser(username, "1234567").passwordRepeat(null);
        UserDialog dialog = userManagementPage.createUser();
        dialog.applyModel(user).confirm();

        dialog.passwordMessage().shouldBe(visible).shouldHave(uiMessage("PASSWORD_ERROR_TOO_SHORT", locale));
        dialog.close();
        userManagementPage.getUsersTable().search(username).shouldBeEmpty();
    }

    @Test
    @DisplayName("If the password and repeat do not match, a validation error should appear and new no user should be created.")
    public void test_createUserDialog_wrongPasswordRepeat_producesValidationErrorWhenTryingToSave() {
        String username = "test-002";
        UserModel user = validUser(username, "12345678").passwordRepeat("123456789");
        UserDialog dialog = userManagementPage.createUser();
        dialog.applyModel(user).confirm();

        dialog.passwordMessage().shouldBe(visible).shouldHave(uiMessage("PASSWORD_ERROR__MISMATCH", locale));
        dialog.close();
        userManagementPage.getUsersTable().search(username).shouldBeEmpty();
    }

    @Test
    @DisplayName("If the login already exists, a validation error should appear and no new user should be created.")
    public void test_createUserDialog_createUserWithAlreadyExistingLogin_producesValidationErrorWhenTryingToSave() {
        String username = "test-003";
        UserModel user = validUser(username, "12345678");
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
        userManagementPage.getUsersTable().search(username).deleteUser(0).confirm();
    }

    @Test
    @DisplayName("If the shortcut already exists, a validation error should appear and no new user should be created.")
    public void test_createUserDialog_createUserWithAlreadyExistingShortcut_producesValidationErrorWhenTryingToSave() {
        String username = "test-004";
        String newUsername = "test-004-1";
        UserModel user = validUser(username, "12345678").shortcut("XYZ");
        // create a user
        UserDialog dialog = userManagementPage.createUser();
        dialog.applyModel(user).confirm();

        // try to create a user with the same shortcut
        user.login(newUsername);
        dialog = userManagementPage.createUser();
        dialog.applyModel(user).confirm();

        String expectedText = getUIMessage("admission_non_unique_shortcut", locale) + "\n"
                + getUIMessage("admission_non_unique_shortcut_detail", locale);
        dialog.shortcutMessage().shouldBe(visible).shouldHave(exactTextCaseSensitive(expectedText));
        dialog.close();
        userManagementPage.getUsersTable().search(newUsername).shouldBeEmpty();

        /*
         * Remove shortcut for the existing user, so we don't run into trouble the next
         * time this test is executed.
         */
        dialog = userManagementPage.getUsersTable().search(username).editUser(0);
        dialog.shortcutInput().setValue("");
        dialog.confirm();
        // delete existing user
        userManagementPage.getUsersTable().search(username).deleteUser(0).confirm();
    }

    private UserModel validUser(String login, String password) {
        return new UserModel().name("testuser").login(login).password(password).passwordRepeat(password);
    }
}