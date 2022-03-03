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

import static com.codeborne.selenide.Selenide.page;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.components.table.DataTable;

/**
 * Page object for the users table in
 * /ui/web/WEB-INF/templates/userManagement.xhtml
 * 
 * @author flange
 */
public class UsersTable extends DataTable<UsersTable> {
    private static final String EDIT_USER_BUTTON = testId("userManagement:editUser");
    private static final String EDIT_MEMBERSHIPS_BUTTON = testId("userManagement:editMemberships");
    private static final String DELETE_USER_BUTTON = testId("userManagement:deleteUser");

    public UsersTable(String testId) {
        super(testId);
    }

    /*
     * Actions
     */
    public UserDialog editUser(int rowIndex) {
        getCell(7, rowIndex).$(EDIT_USER_BUTTON).click();
        return page(UserDialog.class);
    }

    public GroupDialog editMemberships(int rowIndex) {
        getCell(7, rowIndex).$(EDIT_MEMBERSHIPS_BUTTON).click();
        return page(GroupDialog.class);
    }

    public UserDialog deleteUser(int rowIndex) {
        getCell(7, rowIndex).$(DELETE_USER_BUTTON).click();
        return page(UserDialog.class);
    }

    /*
     * Getters
     */
    public SelenideElement getName(int rowIndex) {
        return getCell(0, rowIndex);
    }

    public SelenideElement getLogin(int rowIndex) {
        return getCell(1, rowIndex);
    }

    public SelenideElement getShortcut(int rowIndex) {
        return getCell(2, rowIndex);
    }

    public SelenideElement getEmail(int rowIndex) {
        return getCell(3, rowIndex);
    }

    public SelenideElement getPhone(int rowIndex) {
        return getCell(4, rowIndex);
    }

    public SelenideElement getType(int rowIndex) {
        return getCell(5, rowIndex);
    }

    public SelenideElement getInstitute(int rowIndex) {
        return getCell(6, rowIndex);
    }
}