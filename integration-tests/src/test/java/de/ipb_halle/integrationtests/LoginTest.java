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

import static com.codeborne.selenide.Selenide.open;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.ipb_halle.pageobjects.pages.LoginPage;
import de.ipb_halle.test.SelenideRule;

/**
 * 
 * @author flange
 */
public class LoginTest {
    private LoginPage loginPage;

    @Rule
    public SelenideRule selenideRule = new SelenideRule();

    @Before
    public void before() {
        loginPage = open("/", LoginPage.class);
    }

    @Test
    public void test() {
        assertTrue(loginPage.login("admin", "admin").isLoggedIn());
    }

//    @Test
//    public void test001_sucessfulLogin() {
//        Optional<SearchPage> nextPage = loginPage.login("admin", "admin");
//
//        assertTrue(nextPage.isPresent());
//        assertTrue(nextPage.get().isLoggedIn());
//
//        List<String> growls = loginPage.getGrowlMessages();
//        assertEquals(1, growls.size());
//        assertTrue(I18n.isUIMessage(growls.get(0), "admission_login_succeeded", locale));
//    }
//
//    @Test
//    public void test002_emptyInputs() {
//        Optional<SearchPage> nextpage = loginPage.login("", "");
//
//        assertTrue(nextpage.isEmpty());
//        assertFalse(loginPage.isLoggedIn());
//
//        assertTrue(I18n.isJSFMessage(loginPage.getLoginNameMessage().get(),
//                I18n.JSF_REQUIRED_VALIDATION_ERROR_KEY, locale));
//        assertTrue(I18n.isJSFMessage(loginPage.getPasswordMessage().get(),
//                I18n.JSF_REQUIRED_VALIDATION_ERROR_KEY, locale));
//    }
//
//    @Test
//    public void test003_failedLogin() {
//        Optional<SearchPage> nextPage = loginPage.login("nonexistinguser", "password");
//
//        assertTrue(nextPage.isEmpty());
//        assertFalse(loginPage.isLoggedIn());
//
//        List<String> growls = loginPage.getGrowlMessages();
//        assertEquals(1, growls.size());
//        assertTrue(I18n.isUIMessage(growls.get(0), "admission_login_failure", locale));
//    }
}