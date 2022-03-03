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

import static com.codeborne.selenide.CollectionCondition.empty;
import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Selenide.open;
import static de.ipb_halle.pageobjects.components.Severity.INFO;
import static de.ipb_halle.pageobjects.components.Severity.WARN;
import static de.ipb_halle.pageobjects.util.I18n.JSF_REQUIRED_VALIDATION_ERROR_KEY;
import static de.ipb_halle.pageobjects.util.TestConstants.ADMIN_PASSWORD;
import static de.ipb_halle.pageobjects.util.TestConstants.ADMIN_LOGIN;
import static de.ipb_halle.pageobjects.util.TestConstants.ADMIN_NAME;
import static de.ipb_halle.test.Conditions.growlI18nText;
import static de.ipb_halle.test.Conditions.growlSeverity;
import static de.ipb_halle.test.Conditions.jsfMessage;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.ipb_halle.pageobjects.pages.LoginPage;
import de.ipb_halle.pageobjects.pages.search.SearchPage;
import de.ipb_halle.test.SelenideEachExtension;

/**
 * @author flange
 */
@ExtendWith(SelenideEachExtension.class)
public class LoginTest {
    private LoginPage loginPage;
    private Locale locale = Locale.ENGLISH;

    @BeforeEach
    public void before() {
        loginPage = open("/", LoginPage.class);
    }

    @Test
    public void test_sucessfulLogin() {
        loginPage.login(ADMIN_LOGIN, ADMIN_PASSWORD, SearchPage.class).shouldBeLoggedIn().userNameShouldBe(ADMIN_NAME)
                .growls().shouldHave(size(1)).get(0)
                .shouldHave(growlI18nText("admission_login_succeeded_detail", locale)).shouldHave(growlSeverity(INFO));
    }

    // Don't run too frequent/often or you'll run into the intruder lockout.
    @Test
    public void test_failedLogin() {
        loginPage.login("nonexistinguser", "pw", LoginPage.class).shouldNotBeLoggedIn().growls().shouldHave(size(1))
                .get(0).shouldHave(growlI18nText("admission_login_failure", locale)).shouldHave(growlSeverity(WARN));
    }

    @Test
    public void test_emptyInputs_validationErrors() {
        loginPage.login("", "", LoginPage.class).shouldNotBeLoggedIn().growls().shouldBe(empty);
        loginPage.loginNameMessage().shouldHave(jsfMessage(JSF_REQUIRED_VALIDATION_ERROR_KEY, locale));
        loginPage.passwordMessage().shouldHave(jsfMessage(JSF_REQUIRED_VALIDATION_ERROR_KEY, locale));
    }
}