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

import static com.codeborne.selenide.Condition.exactTextCaseSensitive;
import static com.codeborne.selenide.Condition.not;
import static com.codeborne.selenide.Condition.visible;
import static com.codeborne.selenide.Selenide.*;
import static de.ipb_halle.pageobjects.util.Selectors.elementWithCssClasses;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.navigation.Navigation;

/**
 * Parent class for page objects.
 * 
 * @author flange
 */
public abstract class AbstractPage {
    protected static final SelenideElement LOGIN_CMDLINK = $(testId("navigation:login"));
    protected static final SelenideElement LOGOUT_CMDLINK = $(testId("navigation:logout"));
    private static final SelenideElement CURRENT_USERNAME = $(testId("navigation:username"));

    /*
     * BootsFaces does not support JSF passthrough attributes in growls at the
     * moment. The CSS class is a workaround.
     */
    private static final ElementsCollection GROWL_DIVS = $$(elementWithCssClasses("div", "growlMessages"));

    /*
     * Actions
     */
    /**
     * Navigate to the given target page.
     * 
     * @param <T>
     * @param clazz class of the target page
     * @return page object of the target page
     */
    public <T extends NavigablePage> T navigateTo(Class<T> clazz) {
        T page = page(clazz);
        page.getNavigationItem().navigate();
        return page;
    }

    /**
     * Log out.
     * <p>
     * Should direct the browser either to the login page or the search page
     * depending on CRIMSy's settings, thus only {@link LoginPage} or
     * {@link SearchPage} are useful page object classes to be supplied in the
     * {@code clazz} parameter.
     * 
     * @param <T>
     * @param clazz expected page
     * @return page object of expected page
     */
    public <T extends AbstractPage> T logout(Class<T> clazz) {
        LOGOUT_CMDLINK.click();
        return page(clazz);
    }

    /*
     * Getters
     */
    /**
     * @return name of the currently logged in user
     */
    public String getCurrentUsername() {
        boolean settingMenuWasNotActive = false;
        if (CURRENT_USERNAME.is(not(visible))) {
            settingMenuWasNotActive = true;
            Navigation.Menu.SETTINGS.activate();
        }
        String username = CURRENT_USERNAME.getText();
        if (settingMenuWasNotActive) {
            Navigation.Menu.SETTINGS.activate();
        }
        return username;
    }

    public ElementsCollection growls() {
        return GROWL_DIVS;
    }

    /*
     * Fluent assertions
     */
    public AbstractPage shouldBeLoggedIn() {
        LOGIN_CMDLINK.shouldNotBe(visible);
        LOGOUT_CMDLINK.shouldBe(visible);
        return this;
    }

    public AbstractPage shouldNotBeLoggedIn() {
        LOGIN_CMDLINK.shouldBe(visible);
        LOGOUT_CMDLINK.shouldNotBe(visible);
        return this;
    }

    public AbstractPage userNameShouldBe(String name) {
        boolean settingMenuWasNotActive = false;
        if (CURRENT_USERNAME.is(not(visible))) {
            settingMenuWasNotActive = true;
            Navigation.Menu.SETTINGS.activate();
        }
        CURRENT_USERNAME.shouldBe(exactTextCaseSensitive(name));
        if (settingMenuWasNotActive) {
            Navigation.Menu.SETTINGS.activate();
        }
        return this;
    }
}