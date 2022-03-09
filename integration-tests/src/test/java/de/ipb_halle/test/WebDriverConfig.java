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
package de.ipb_halle.test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openqa.selenium.remote.DesiredCapabilities;

import com.codeborne.selenide.AssertionMode;
import com.codeborne.selenide.Configuration;
import com.codeborne.selenide.WebDriverRunner;

/**
 * Configure the Selenide default browser.
 * 
 * @author flange
 */
public class WebDriverConfig {
    private static final String DEFAULT_BROWSER = "firefox";

    private static final String browserName = System.getProperty("integrationtests.browserName", DEFAULT_BROWSER);
    private static final String browserVersion = System.getProperty("integrationtests.browserVersion", null);
    private static final String selenoidUrl = System.getProperty("integrationtests.selenoidUrl", null);
    private static final String crimsyUrl = System.getProperty("integrationtests.crimsyUrl", null);

    private static final AtomicBoolean FIRST_RUN = new AtomicBoolean(true);

    private WebDriverConfig() {
    }

    public static void configure() {
        if (!FIRST_RUN.getAndSet(false)) {
            return;
        }

        validateOrFail();

        Configuration.baseUrl = crimsyUrl;
        Configuration.browser = browserName;
        Configuration.assertionMode = AssertionMode.SOFT;

        // scroll to element before clicking
        int navbarOffset = 47;
        WebDriverRunner.addListener(new ScrollWebdriverListener(navbarOffset));

        if ((selenoidUrl == null) || selenoidUrl.trim().isEmpty()) {
            configureLocal();
        } else {
            configureSelenoid();
        }
    }

    private static void validateOrFail() {
        try {
            new URL(crimsyUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException("URL to CRIMSy instance is incorrect, please set the system"
                    + " property \"integrationtests.crimsyUrl\" correctly", e);
        }
    }

    private static void configureSelenoid() {
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability("browserName", browserName);
        if (browserVersion != null) {
            capabilities.setCapability("browserVersion", browserVersion);
        }
        capabilities.setCapability("enableVNC", true);
        capabilities.setCapability("additionalNetworks", Arrays.asList("dist_lbac_private"));
        // setting locale: https://aerokube.com/selenoid/latest/#_per_session_environment_variables_env

        Configuration.browserCapabilities = capabilities;
        Configuration.remote = selenoidUrl;
    }

    private static void configureLocal() {
    }
}