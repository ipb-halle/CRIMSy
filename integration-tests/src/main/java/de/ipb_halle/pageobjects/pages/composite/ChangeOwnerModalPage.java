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
package de.ipb_halle.pageobjects.pages.composite;

import static com.codeborne.selenide.Selenide.page;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.components.PrimeFacesDialog;
import de.ipb_halle.pageobjects.components.table.DataTable;
import de.ipb_halle.pageobjects.pages.projects.ProjectEditPage;

/**
 * Page object for /ui/web/resources/crimsy/changeOwnerModal.xhtml
 * 
 * @author flange
 */
public class ChangeOwnerModalPage extends PrimeFacesDialog {
    private static final UsersTable USERS_TABLE = new UsersTable("changeOwnerModal:usersTable");

    /*
     * Getters
     */
    public UsersTable usersTable() {
        return USERS_TABLE;
    }

    public static class UsersTable extends DataTable<UsersTable> {
        private static final String SELECT_USER_BUTTON = testId("changeOwnerModal:usersTable:selectUser");

        public UsersTable(String testId) {
            super(testId);
        }

        /*
         * Actions
         */
        public ProjectEditPage selectUser(int rowIndex) {
            getCell(3, rowIndex).$(SELECT_USER_BUTTON).click();
            return page(ProjectEditPage.class);
        }

        /*
         * Getters
         */
        public SelenideElement getUserName(int rowIndex) {
            return getCell(0, rowIndex);
        }

        public SelenideElement getPhone(int rowIndex) {
            return getCell(1, rowIndex);
        }

        public SelenideElement getEmail(int rowIndex) {
            return getCell(2, rowIndex);
        }
    }
}