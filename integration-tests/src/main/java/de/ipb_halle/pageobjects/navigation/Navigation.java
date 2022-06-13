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
package de.ipb_halle.pageobjects.navigation;

import static com.codeborne.selenide.Selenide.$;
import static de.ipb_halle.pageobjects.navigation.Navigation.Menu.BIOCLOUD;
import static de.ipb_halle.pageobjects.navigation.Navigation.Menu.LIMS;
import static de.ipb_halle.pageobjects.navigation.Navigation.Menu.SETTINGS;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import java.util.function.Consumer;

import com.codeborne.selenide.SelenideElement;

/**
 * Page object for /ui/web/WEB-INF/templates/navigation.xhtml
 * 
 * @author flange
 */
public enum Navigation {
    SEARCH(BIOCLOUD, "navigation:search"),
    WORDCLOUD_SEARCH(BIOCLOUD, "navigation:wordCloudSearch"),
    SEQUENCE_SEARCH(BIOCLOUD, "navigation:sequenceSearch"),
    FORUM(BIOCLOUD, "navigation:forum"),
    MATERIALS_OVERVIEW(LIMS, "navigation:materials"),
    PROJECTS_OVERVIEW(LIMS, "navigation:projects"),
    ITEMS_OVERVIEW(LIMS, "navigation:items"),
    TAXONOMY(LIMS, "navigation:taxonomy"),
    EXPERIMENTS(LIMS, "navigation:experiments"),
    CONTAINERS_OVERVIEW(LIMS, "navigation:containers"),
    MY_ACCOUNT(SETTINGS, "navigation:myAccount"),
    MY_REPORTS(SETTINGS, "navigation:myReports"),
    USER_MANAGEMENT(SETTINGS, "navigation:userManagement"),
    GROUP_MANAGEMENT(SETTINGS, "navigation:groupManagement"),
    LDAP_CONNECTION(SETTINGS, "navigation:ldapSettings"),
    PRINTER_SETTINGS(SETTINGS, "navigation:printerSettings"),
    SYSTEM_SETTINGS(SETTINGS, "navigation:systemSettings"),
    CLOUD_NODE_MANAGEMENT(SETTINGS, "navigation:cloudNodeManagement"),
    COLLECTION_MANAGEMENT(SETTINGS, "navigation:collectionManagement");

    public enum Menu {
        BIOCLOUD("navigation:bioCloud", e -> e.hover()),
        LIMS("navigation:lims", e -> e.hover()),
        SETTINGS("navigation:settings", e -> e.click());

        private final SelenideElement element;
        private final Consumer<SelenideElement> action;

        private Menu(String testId, Consumer<SelenideElement> action) {
            element = $(testId(testId));
            this.action = action;
        }

        /**
         * Activate the menu.
         */
        public void activate() {
            action.accept(element);
        }
    }

    private final Menu menu;
    private final SelenideElement navCmdLink;

    private Navigation(Menu menu, String navCmdLinkTestId) {
        this.menu = menu;
        this.navCmdLink = $(testId(navCmdLinkTestId));
    }

    public void navigate() {
        menu.activate();
        navCmdLink.click();
    }
}