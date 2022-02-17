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

import static de.ipb_halle.pageobjects.util.Selectors.testId;

import de.ipb_halle.pageobjects.components.PrimeFacesDialog;
import de.ipb_halle.pageobjects.components.table.DataTable;

/**
 * Page object for /ui/web/resources/crimsy/changeOwnerModal.xhtml
 * 
 * @author flange
 */
public class ChangeOwnerModalPage extends PrimeFacesDialog {
    private static final UsersTable USERS_TABLE = new UsersTable(testId("changeOwnerModal:usersTable"));

    /*
     * Getters
     */
    public UsersTable getUsersTable() {
        return USERS_TABLE;
    }

    public static class UsersTable extends DataTable {
        private static final String SELECT_USER_BUTTON = testId("changeOwnerModal:usersTable:selectUser");

        public UsersTable(String testId) {
            super(testId);
        }

        /*
         * Actions
         */
        public void selectUser(int rowIndex) {
            getCell(3, rowIndex).$(SELECT_USER_BUTTON).click();
        }

        /*
         * Getters
         */
        public String getUserName(int rowIndex) {
            return getCell(0, rowIndex).text();
        }

        public String getPhone(int rowIndex) {
            return getCell(1, rowIndex).text();
        }

        public String getEmail(int rowIndex) {
            return getCell(2, rowIndex).text();
        }
    }
}