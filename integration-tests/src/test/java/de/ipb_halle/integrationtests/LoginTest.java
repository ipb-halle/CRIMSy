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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import de.ipb_halle.pageobjects.pages.LoginPage;
import de.ipb_halle.pageobjects.util.I18n;

/**
 * 
 * @author flange
 */
public class LoginTest {
    private LoginPage loginPage;

    // this could become a parameter in a parameterized test
    private Locale locale = Locale.ENGLISH;

    @Before
    public void before() {
        loginPage = new LoginPage();
        loginPage.navigate();
    }

    @Test
    public void test001_sucessfulLogin() {
        Optional<SearchPage> nextPage = loginPage.login("admin", "admin");

        assertTrue(nextPage.isPresent());
        assertTrue(nextPage.get().isLoggedIn());

        List<String> growls = loginPage.getGrowlMessages();
        assertEquals(1, growls.size());
        assertTrue(I18n.isUIMessage(growls.get(0), "admission_login_succeeded", locale));
    }

    @Test
    public void test002_emptyInputs() {
        Optional<SearchPage> nextpage = loginPage.login("", "");

        assertTrue(nextpage.isEmpty());
        assertFalse(loginPage.isLoggedIn());

        assertTrue(I18n.isJSFMessage(loginPage.getLoginNameMessage().get(),
                I18n.JSF_REQUIRED_VALIDATION_ERROR_KEY, locale));
        assertTrue(I18n.isJSFMessage(loginPage.getPasswordMessage().get(),
                I18n.JSF_REQUIRED_VALIDATION_ERROR_KEY, locale));
    }

    @Test
    public void test003_failedLogin() {
        Optional<SearchPage> nextPage = loginPage.login("nonexistinguser", "password");

        assertTrue(nextPage.isEmpty());
        assertFalse(loginPage.isLoggedIn());

        List<String> growls = loginPage.getGrowlMessages();
        assertEquals(1, growls.size());
        assertTrue(I18n.isUIMessage(growls.get(0), "admission_login_failure", locale));
    }
}