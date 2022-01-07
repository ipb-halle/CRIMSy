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
import static de.ipb_halle.pageobjects.growl.Severity.INFO;
import static de.ipb_halle.pageobjects.growl.Severity.WARN;
import static de.ipb_halle.pageobjects.util.I18n.JSF_REQUIRED_VALIDATION_ERROR_KEY;
import static de.ipb_halle.test.GrowlAssert.assertGrowlI18n;
import static de.ipb_halle.test.I18nAssert.assertJSFMessage;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import de.ipb_halle.pageobjects.growl.Growl;
import de.ipb_halle.pageobjects.pages.LoginPage;
import de.ipb_halle.pageobjects.pages.SearchPage;
import de.ipb_halle.test.SelenideRule;

/**
 * @author flange
 */
public class LoginTest {
    private LoginPage loginPage;
    private Locale locale = Locale.ENGLISH;

    @Rule
    public SelenideRule selenideRule = new SelenideRule();

    @Before
    public void before() {
        loginPage = open("/", LoginPage.class);
    }

    @Test
    public void test_sucessfulLogin() {
        SearchPage searchPage = (SearchPage) loginPage.login("admin", "admin");

        assertTrue(searchPage.isLoggedIn());
        List<Growl> growls = Growl.getGrowls();
        assertThat(growls, hasSize(1));
        assertGrowlI18n("admission_login_succeeded_detail", locale, INFO,
                growls.get(0));
    }

    // Don't run too frequent/often or you'll run into the intruder lockout.
    @Test
    public void test_failedLogin() {
        assertFalse(loginPage.login("nonexistinguser", "pw").isLoggedIn());
        List<Growl> growls = Growl.getGrowls();
        assertThat(growls, hasSize(1));
        assertGrowlI18n("admission_login_failure", locale, WARN, growls.get(0));
    }

    @Test
    public void test_emptyInputs_validationErrors() {
        assertFalse(loginPage.login("", "").isLoggedIn());
        assertThat(Growl.getGrowls(), is(empty()));
        assertJSFMessage(loginPage.getLoginNameMessage().getText(),
                JSF_REQUIRED_VALIDATION_ERROR_KEY, locale);
        assertJSFMessage(loginPage.getPasswordMessage().getText(),
                JSF_REQUIRED_VALIDATION_ERROR_KEY, locale);
    }
}