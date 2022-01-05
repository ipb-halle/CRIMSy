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

import de.ipb_halle.pageobjects.navigation.NavigationConstants;

/**
 * 
 * @author flange
 */
public abstract class AbstractPage {
    private static final SelenideElement LOGIN_CMDLINK = $(
            testId("navigation:login"));
    private static final SelenideElement LOGOUT_CMDLINK = $(
            testId("navigation:logout"));

    public <T extends AbstractPage> T navigateTo(NavigationConstants target) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

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
     * @return login page or default search page
     */
    public AbstractPage logout() {
        if (!isLoggedIn()) {
            throw new RuntimeException("I am already logged out");
        }
        LOGOUT_CMDLINK.click();
        // TODO: can also be the search page
        return page(LoginPage.class);
    }
}