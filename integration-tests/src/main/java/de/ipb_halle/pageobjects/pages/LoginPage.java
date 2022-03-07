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
import static de.ipb_halle.pageobjects.util.Selectors.testId;
import static de.ipb_halle.pageobjects.util.TestConstants.ADMIN_PASSWORD;
import static de.ipb_halle.pageobjects.util.TestConstants.ADMIN_LOGIN;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.pages.search.SearchPage;

/**
 * Page object for /ui/web/WEB-INF/templates/login.xhtml
 * 
 * @author flange
 */
public class LoginPage extends AbstractPage<LoginPage> {
    private static final SelenideElement LOGINNAME_INPUT = $(testId("input", "login:loginName_input"));
    private static final SelenideElement LOGINNAME_MESSAGE = $(testId("login:loginName_message"));
    private static final SelenideElement PASSWORD_INPUT = $(testId("input", "login:password_input"));
    private static final SelenideElement PASSWORD_MESSAGE = $(testId("login:password_message"));
    private static final SelenideElement LOGIN_BUTTON = $(testId("login:loginButton"));

    /*
     * Actions
     */
    /**
     * Log in.
     * <p>
     * Should direct the browser either to the search page (successful login) the
     * login page (failed login), thus only {@link SearchPage} or {@link LoginPage}
     * are useful page object classes to be supplied in the
     * {@code expectedPageClass} parameter.
     * 
     * @param <T>
     * @param loginName
     * @param password
     * @param expectedPageClass expected page
     * @return page object of expected page
     */
    public <T extends AbstractPage<T>> T login(String loginName, String password, Class<T> expectedPageClass) {
        LOGINNAME_INPUT.setValue(loginName);
        PASSWORD_INPUT.setValue(password);
        LOGIN_BUTTON.click();

        return page(expectedPageClass);
    }

    /**
     * Log in as admin.
     * <p>
     * Should direct the browser either to the search page (successful login) the
     * login page (failed login), thus only {@link SearchPage} or {@link LoginPage}
     * are useful page object classes to be supplied in the
     * {@code expectedPageClass} parameter.
     * 
     * @param <T>
     * @param expectedPageClass expected page
     * @return page object of expected page
     */
    public <T extends AbstractPage<T>> T loginAsAdmin(Class<T> expectedPageClass) {
        return login(ADMIN_LOGIN, ADMIN_PASSWORD, expectedPageClass);
    }

    /*
     * Getters
     */
    public SelenideElement loginNameMessage() {
        return LOGINNAME_MESSAGE;
    }

    public SelenideElement passwordMessage() {
        return PASSWORD_MESSAGE;
    }
}