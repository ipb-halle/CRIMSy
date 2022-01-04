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
package de.ipb_halle.pageobjects.pages;

import static com.codeborne.selenide.Selenide.*;
import static de.ipb_halle.pageobjects.util.Selectors.css;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.navigation.NavigationConstants;

/**
 * @author flange
 */
public class LoginPage extends AbstractPage {
    private static final SelenideElement LOGINNAME_INPUT = $(
            css("input", "login:loginName_input"));
    private static final SelenideElement LOGINNAME_MESSAGE = $(
            css("login:loginName_message"));
    private static final SelenideElement PASSWORD_INPUT = $(
            css("input", "login:password_input"));
    private static final SelenideElement PASSWORD_MESSAGE = $(
            css("login:password_message"));
    private static final SelenideElement LOGIN_BUTTON = $(
            css("login:loginButton"));

    public void navigate() {
        if (isLoggedIn()) {
            throw new RuntimeException("I am already logged in");
        }
        navigateTo(NavigationConstants.LOGIN);
    }

    public AbstractPage login(String loginName, String password) {
        LOGINNAME_INPUT.setValue(loginName);
        PASSWORD_INPUT.setValue(password);
        LOGIN_BUTTON.click();

        if (isLoggedIn()) {
            return page(SearchPage.class);
        } else {
            return this;
        }
    }

    public SelenideElement getLoginNameMessage() {
        return LOGINNAME_MESSAGE;
    }

    public SelenideElement getPasswordMessage() {
        return PASSWORD_MESSAGE;
    }
}