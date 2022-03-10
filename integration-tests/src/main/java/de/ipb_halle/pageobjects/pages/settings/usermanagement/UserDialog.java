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
package de.ipb_halle.pageobjects.pages.settings.usermanagement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;
import static de.ipb_halle.pageobjects.util.Apply.applyValue;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.components.PrimeFacesDialog;

/**
 * Page object for the user edit dialog in
 * /ui/web/WEB-INF/templates/userManagement.xhtml
 * 
 * @author flange
 */
public class UserDialog extends PrimeFacesDialog {
    private static final SelenideElement ID_INPUT = $(testId("input", "userManagement:userDialog:id"));
    private static final SelenideElement ID_MESSAGE = $(testId("userManagement:userDialog:idMessage"));
    private static final SelenideElement NAME_INPUT = $(testId("input", "userManagement:userDialog:name"));
    private static final SelenideElement NAME_MESSAGE = $(testId("userManagement:userDialog:nameMessage"));
    private static final SelenideElement LOGIN_INPUT = $(testId("input", "userManagement:userDialog:login"));
    private static final SelenideElement LOGIN_MESSAGE = $(testId("userManagement:userDialog:loginMessage"));
    private static final SelenideElement SHORTCUT_INPUT = $(testId("input", "userManagement:userDialog:shortcut"));
    private static final SelenideElement SHORTCUT_MESSAGE = $(testId("userManagement:userDialog:shortcutMessage"));
    private static final SelenideElement EMAIL_INPUT = $(testId("input", "userManagement:userDialog:email"));
    private static final SelenideElement EMAIL_MESSAGE = $(testId("userManagement:userDialog:emailMessage"));
    private static final SelenideElement PASSWORD_INPUT = $(testId("input", "userManagement:userDialog:password"));
    private static final SelenideElement PASSWORD_MESSAGE = $(testId("userManagement:userDialog:passwordMessage"));
    private static final SelenideElement PASSWORD_REPEAT_INPUT = $(testId("input", "userManagement:userDialog:passwordRepeat"));
    private static final SelenideElement PHONE_INPUT = $(testId("input", "userManagement:userDialog:phone"));
    private static final SelenideElement PHONE_MESSAGE = $(testId("userManagement:userDialog:phoneMessage"));
    private static final SelenideElement CONFIRM_BUTTON = $(testId("userManagement:userDialog:confirm"));
    private static final SelenideElement NEW_PASSWORD_INPUT = $(testId("input", "userManagement:userDialog:newPassword"));
    private static final SelenideElement NEW_PASSWORD_MESSAGE = $(testId("userManagement:userDialog:newPasswordMessage"));
    private static final SelenideElement NEW_PASSWORD_REPEAT_INPUT = $(testId("input", "userManagement:userDialog:newPasswordRepeat"));
    private static final SelenideElement CHANGE_PASSWORD_BUTTON = $(testId("userManagement:userDialog:changePassword"));
    private static final SelenideElement CLOSE_BUTTON = $(testId("userManagement:userDialog:close"));

    /*
     * Actions
     */
    /**
     * Applies the user model to the input fields.
     * <p>
     * Convention: The input element will not be evaluated in case the model field
     * is null. Use empty strings to reset fields.
     * 
     * @param model
     * @return this
     */
    public UserDialog applyModel(UserModel model) {
        applyValue(model.getName(), NAME_INPUT);
        applyValue(model.getLogin(), LOGIN_INPUT);
        applyValue(model.getShortcut(), SHORTCUT_INPUT);
        applyValue(model.getEmail(), EMAIL_INPUT);
        applyValue(model.getPassword(), PASSWORD_INPUT);
        applyValue(model.getPasswordRepeat(), PASSWORD_REPEAT_INPUT);
        applyValue(model.getPhone(), PHONE_INPUT);

        return this;
    }

    public UserManagementPage confirm() {
        CONFIRM_BUTTON.click();
        return page(UserManagementPage.class);
    }

    public UserManagementPage confirmChangePassword() {
        CHANGE_PASSWORD_BUTTON.click();
        return page(UserManagementPage.class);
    }

    public UserManagementPage close() {
        CLOSE_BUTTON.click();
        return page(UserManagementPage.class);
    }

    /*
     * Getters
     */
    public SelenideElement idInput() {
        return ID_INPUT;
    }

    public SelenideElement idMessage() {
        return ID_MESSAGE;
    }

    public SelenideElement nameInput() {
        return NAME_INPUT;
    }

    public SelenideElement nameMessage() {
        return NAME_MESSAGE;
    }

    public SelenideElement loginInput() {
        return LOGIN_INPUT;
    }

    public SelenideElement loginMessage() {
        return LOGIN_MESSAGE;
    }

    public SelenideElement shortcutInput() {
        return SHORTCUT_INPUT;
    }

    public SelenideElement shortcutMessage() {
        return SHORTCUT_MESSAGE;
    }

    public SelenideElement emailInput() {
        return EMAIL_INPUT;
    }

    public SelenideElement emailMessage() {
        return EMAIL_MESSAGE;
    }

    public SelenideElement passwordInput() {
        return PASSWORD_INPUT;
    }

    public SelenideElement passwordMessage() {
        return PASSWORD_MESSAGE;
    }

    public SelenideElement passwordRepeatInput() {
        return PASSWORD_REPEAT_INPUT;
    }

    public SelenideElement phoneInput() {
        return PHONE_INPUT;
    }

    public SelenideElement phoneMessage() {
        return PHONE_MESSAGE;
    }

    public SelenideElement newPasswordInput() {
        return NEW_PASSWORD_INPUT;
    }

    public SelenideElement newPasswordMessage() {
        return NEW_PASSWORD_MESSAGE;
    }

    public SelenideElement newPasswordRepeatInput() {
        return NEW_PASSWORD_REPEAT_INPUT;
    }
}