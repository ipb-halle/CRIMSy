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

/**
 * 
 * @author flange
 */
public enum NavigationConstants {
    // This needs improvement!
    SEARCH(false, Selectors.BIOCLOUD_DROPMENU),
    WORDCLOUD_SEARCH(false, Selectors.BIOCLOUD_DROPMENU),
    FORUM(false, Selectors.BIOCLOUD_DROPMENU),
    MATERIALS_OVERVIEW(true, Selectors.LIMS_DROPMENU),
    PROJECTS_OVERVIEW(true, Selectors.LIMS_DROPMENU),
    ITEMS_OVERVIEW(true, Selectors.LIMS_DROPMENU),
    TAXONOMY(true, Selectors.LIMS_DROPMENU),
    EXPERIMENTS(true, Selectors.LIMS_DROPMENU),
    CONTAINERS_OVERVIEW(true, Selectors.LIMS_DROPMENU),
    LOGIN(false),
    LOGOUT(true),
    MY_ACCOUNT(true, Selectors.SETTINGS_DROPMENU),
    USER_MANAGEMENT(true, Selectors.SETTINGS_DROPMENU),
    GROUP_MANAGEMENT(true, Selectors.SETTINGS_DROPMENU),
    LDAP_CONNECTION(true, Selectors.SETTINGS_DROPMENU),
    PRINTER_SETTINGS(true, Selectors.SETTINGS_DROPMENU),
    SYSTEM_SETTINGS(true, Selectors.SETTINGS_DROPMENU),
    CLOUD_NODE_MANAGEMENT(true, Selectors.SETTINGS_DROPMENU),
    COLLECTION_MANAGEMENT(true, Selectors.SETTINGS_DROPMENU);

    private static class Selectors {
        private static final String BIOCLOUD_DROPMENU = "navigation:bioCloud";
        private static final String LIMS_DROPMENU = "navigation:lims";
        private static final String SETTINGS_DROPMENU = "navigation:settings";
    }

    private final boolean requiresLogin;
    private final String dropMenuSelector;

    private NavigationConstants(boolean requiresLogin) {
        this(requiresLogin, null);
    }

    private NavigationConstants(boolean requiresLogin, String dropMenuSelector) {
        this.requiresLogin = requiresLogin;
        this.dropMenuSelector = dropMenuSelector;
    }

    public boolean isRequiresLogin() {
        return requiresLogin;
    }

    public String getDropMenuSelector() {
        return dropMenuSelector;
    }
}