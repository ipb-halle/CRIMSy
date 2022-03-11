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
package de.ipb_halle.pageobjects.pages.settings.groupmanagement;

import static com.codeborne.selenide.Selenide.page;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.components.table.DataTable;

/**
 * Page object for the groups table in
 * /ui/web/WEB-INF/templates/groupManagement.xhtml
 * 
 * @author flange
 */
public class GroupsTable extends DataTable<GroupsTable> {
    private static final String EDIT_GROUP_BUTTON = testId("groupManagement:editGroup");
    private static final String EDIT_MEMBERSHIPS_BUTTON = testId("groupManagement:editMemberships");
    private static final String DELETE_GROUP_BUTTON = testId("groupManagement:deleteGroup");

    public GroupsTable(String testId) {
        super(testId);
    }

    /*
     * Actions
     */
    public GroupDialog editGroup(int rowIndex) {
        getCell(3, rowIndex).$(EDIT_GROUP_BUTTON).click();
        return page(GroupDialog.class);
    }

    public GroupMembershipsDialog editMemberships(int rowIndex) {
        getCell(3, rowIndex).$(EDIT_MEMBERSHIPS_BUTTON).click();
        return page(GroupMembershipsDialog.class);
    }

    public GroupDialog deleteGroup(int rowIndex) {
        getCell(3, rowIndex).$(DELETE_GROUP_BUTTON).click();
        return page(GroupDialog.class);
    }

    /*
     * Getters
     */
    public SelenideElement getName(int rowIndex) {
        return getCell(0, rowIndex);
    }

    public SelenideElement getType(int rowIndex) {
        return getCell(1, rowIndex);
    }

    public SelenideElement getInstitute(int rowIndex) {
        return getCell(2, rowIndex);
    }
}