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

import java.util.Optional;

import de.ipb_halle.pageobjects.navigation.NavigationConstants;

/**
 * 
 * @author flange
 */
public class LoginPage extends AbstractPage {
    private static final String LOGINNAME_INPUT = "login:loginName_input";
    private static final String LOGINNAME_MESSAGE = "login:loginName_message";
    private static final String PASSWORD_INPUT = "login:password_input";
    private static final String PASSWORD_MESSAGE = "login:password_message";
    private static final String LOGIN_BUTTON = "login:loginButton";

    public void navigate() {
        if (isLoggedIn()) {
            throw new RuntimeException("I am already logged in");
        }
        navigateTo(NavigationConstants.LOGIN);
    }

    public Optional<SearchPage> login(String loginName, String password) {
        /*
         * fill LOGINNAME_INPUT with loginName 
         * fill PASSWORD_INPUT with password
         * click LOGIN_BUTTON
         */
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Optional<String> getLoginNameMessage() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public Optional<String> getPasswordMessage() {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}