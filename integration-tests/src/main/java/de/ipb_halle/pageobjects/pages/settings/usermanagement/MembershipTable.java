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

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.components.table.DataTable;

/**
 * Page object for the membership table in the group memberships dialog in
 * /ui/web/WEB-INF/templates/userManagement.xhtml
 * 
 * @author flange
 */
public class MembershipTable extends DataTable<MembershipTable> {
    private static final String DELETE_BUTTON = testId("userManagement:groupMembershipsDialog:delete");

    public MembershipTable(String testId) {
        super(testId);
    }

    /*
     * Actions
     */
    public MembershipTable delete(int rowIndex) {
        getCell(4, rowIndex).$(DELETE_BUTTON).click();
        return this;
    }

    /*
     * Getters
     */
    public SelenideElement getName(int rowIndex) {
        return getCell(0, rowIndex);
    }

    public SelenideElement getInstitution(int rowIndex) {
        return getCell(1, rowIndex);
    }

    public SelenideElement getType(int rowIndex) {
        return getCell(2, rowIndex);
    }

    public SelenideElement getNested(int rowIndex) {
        return getCell(3, rowIndex);
    }
}