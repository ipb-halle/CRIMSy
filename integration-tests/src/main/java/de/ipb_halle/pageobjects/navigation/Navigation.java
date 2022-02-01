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
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import java.util.function.Consumer;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.pages.NavigablePage;
import de.ipb_halle.pageobjects.pages.materials.MaterialOverviewPage;
import de.ipb_halle.pageobjects.pages.projects.ProjectOverviewPage;
import de.ipb_halle.pageobjects.pages.search.SearchPage;

/**
 * Page object for /ui/web/WEB-INF/templates/navigation.xhtml
 * 
 * @author flange
 */
public enum Navigation {
    SEARCH(Menu.BIOCLOUD, "navigation:search", SearchPage.class),
    WORDCLOUD_SEARCH(Menu.BIOCLOUD, "navigation:wordCloudSearch", null),
    SEQUENCE_SEARCH(Menu.BIOCLOUD, "navigation:sequenceSearch", null),
    FORUM(Menu.BIOCLOUD, "navigation:forum", null),
    MATERIALS_OVERVIEW(Menu.LIMS, "navigation:materials", MaterialOverviewPage.class),
    PROJECTS_OVERVIEW(Menu.LIMS, "navigation:projects", ProjectOverviewPage.class),
    ITEMS_OVERVIEW(Menu.LIMS, "navigation:items", null),
    TAXONOMY(Menu.LIMS, "navigation:taxonomy", null),
    EXPERIMENTS(Menu.LIMS, "navigation:experiments", null),
    CONTAINERS_OVERVIEW(Menu.LIMS, "navigation:containers", null),
    MY_ACCOUNT(Menu.SETTINGS, "navigation:myAccount", null),
    USER_MANAGEMENT(Menu.SETTINGS, "navigation:userManagement", null),
    GROUP_MANAGEMENT(Menu.SETTINGS, "navigation:groupManagement", null),
    LDAP_CONNECTION(Menu.SETTINGS, "navigation:ldapSettings", null),
    PRINTER_SETTINGS(Menu.SETTINGS, "navigation:printerSettings", null),
    SYSTEM_SETTINGS(Menu.SETTINGS, "navigation:systemSettings", null),
    CLOUD_NODE_MANAGEMENT(Menu.SETTINGS, "navigation:cloudNodeManagement", null),
    COLLECTION_MANAGEMENT(Menu.SETTINGS, "navigation:collectionManagement", null);

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
    private final Class<? extends NavigablePage> pageObjectClass;

    private Navigation(Menu menu, String navCmdLinkTestId,
            Class<? extends NavigablePage> pageObjectClass) {
        this.menu = menu;
        this.navCmdLink = $(testId(navCmdLinkTestId));
        this.pageObjectClass = pageObjectClass;
    }

    public Menu getMenu() {
        return menu;
    }

    public SelenideElement getNavCmdLink() {
        return navCmdLink;
    }

    public Class<? extends NavigablePage> getPageObjectClass() {
        return pageObjectClass;
    }
}