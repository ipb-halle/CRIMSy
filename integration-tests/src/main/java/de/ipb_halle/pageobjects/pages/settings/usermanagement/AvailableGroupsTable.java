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

import static de.ipb_halle.pageobjects.util.Selectors.testId;

import de.ipb_halle.pageobjects.components.table.DataTable;

/**
 * Page object for the available groups table in the group dialog in
 * /ui/web/WEB-INF/templates/userManagement.xhtml
 * 
 * @author flange
 */
public class AvailableGroupsTable extends DataTable<AvailableGroupsTable> {
    private static final String ADD_BUTTON = testId("userManagement:groupDialog:add");

    public AvailableGroupsTable(String testId) {
        super(testId);
    }

    /*
     * Actions
     */
    public AvailableGroupsTable add(int rowIndex) {
        getCell(2, rowIndex).$(ADD_BUTTON).click();
        return this;
    }

    /*
     * Getters
     */
    public String getName(int rowIndex) {
        return getCell(0, rowIndex).text();
    }

    public String getInstitution(int rowIndex) {
        return getCell(1, rowIndex).text();
    }
}