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

import java.util.List;

import de.ipb_halle.pageobjects.navigation.NavigationConstants;

/**
 * 
 * @author flange
 */
public abstract class AbstractPage {
    private static final String LOGIN_CMDLINK = "navigation:login";
    private static final String LOGOUT_CMDLINK = "navigation:logout";

    public <T extends AbstractPage> T navigateTo(NavigationConstants target) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public boolean isLoggedIn() {
        // check if there is LOGIN_CMDLINK or LOGOUT_CMDLINK in the navigation bar (or none or both)
        throw new UnsupportedOperationException("Not implemented yet");
    }

    /**
     * 
     * @return login page or default search page
     */
    public AbstractPage logout() {
        throw new UnsupportedOperationException("Not implemented yet");
        if (!isLoggedIn()) {
            throw new RuntimeException("I am already logged out");
        }
        // click on LOGOUT_CMDLINK
    }

    /*
     * I have the suspicion that growls must become a singleton.
     * Or should there be a "wait for growl"?
     */
    public List<String> getGrowlMessages() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public void clearGrowlMessages() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}