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

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.navigation.Navigation;

/**
 * Parent class for page objects.
 * 
 * @author flange
 */
public abstract class AbstractPage {
    protected static final SelenideElement LOGIN_CMDLINK = $(
            testId("navigation:login"));
    protected static final SelenideElement LOGOUT_CMDLINK = $(
            testId("navigation:logout"));
    private static final SelenideElement CURRENT_USERNAME = $(
            testId("navigation:username"));

    /*
     * Actions
     */
    /**
     * Navigate to the given target page.
     * 
     * @param <T>
     * @param target
     * @return page object of target page
     */
    @SuppressWarnings("unchecked")
    public <T extends AbstractPage> T navigateTo(Navigation target) {
        target.getMenu().activate();
        target.getNavCmdLink().click();
        return (T) page(target.getPageObjectClass());
    }

    public <T extends AbstractPage> T navigateTo(Navigation target,
            Class<T> clazz) {
        target.getMenu().activate();
        target.getNavCmdLink().click();
        return page(clazz);
    }

    public <T extends NavigablePage> T navigateTo(Class<T> clazz) {
        T page = page(clazz);
        Navigation target = page.getNavigationItem();
        target.getMenu().activate();
        target.getNavCmdLink().click();
        return page;
    }

    /**
     * Log out
     * 
     * @return page object for the login page or the default search page
     */
    public AbstractPage logout() {
        if (!isLoggedIn()) {
            throw new RuntimeException("I am already logged out");
        }
        LOGOUT_CMDLINK.click();
        // TODO: can also be the search page
        return page(LoginPage.class);
    }

    /*
     * Getters
     */
    /**
     * @return {@code true} if the user is logged in
     */
    public boolean isLoggedIn() {
        boolean loginDisplayed = LOGIN_CMDLINK.isDisplayed();
        boolean logoutDisplayed = LOGOUT_CMDLINK.isDisplayed();

        if (logoutDisplayed && !loginDisplayed) {
            return true;
        }
        if (!logoutDisplayed && loginDisplayed) {
            return false;
        }
        throw new RuntimeException("Ambiguous login state");
    }

    /**
     * @return name of the currently logged in user
     */
    public String getCurrentUsername() {
        Navigation.Menu.SETTINGS.activate();
        return CURRENT_USERNAME.getText();
    }
}