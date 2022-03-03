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
package de.ipb_halle.pageobjects.pages.settings.usermanagement;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.navigation.Navigation;
import de.ipb_halle.pageobjects.pages.NavigablePage;

/**
 * Page object for /ui/web/WEB-INF/templates/userManagement.xhtml
 * 
 * @author flange
 */
public class UserManagementPage extends NavigablePage {
    private static final UsersTable USERS_TABLE = new UsersTable("userManagement:usersTable");
    private static final SelenideElement CREATE_USER_BUTTON = $(testId("userManagement:createUser"));
    private static final SelenideElement REFRESH_BUTTON = $(testId("userManagement:refresh"));

    @Override
    public Navigation getNavigationItem() {
        return Navigation.USER_MANAGEMENT;
    }

    /*
     * Actions
     */
    public UserDialog createUser() {
        CREATE_USER_BUTTON.click();
        return page(UserDialog.class);
    }

    public UserManagementPage refresh() {
        REFRESH_BUTTON.click();
        return this;
    }

    /*
     * Getters
     */
    public UsersTable getUsersTable() {
        return USERS_TABLE;
    }
}