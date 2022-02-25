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

import de.ipb_halle.pageobjects.components.PrimeFacesDialog;

/**
 * Page object for the group dialog in
 * /ui/web/WEB-INF/templates/userManagement.xhtml
 * 
 * @author flange
 */
public class GroupDialog extends PrimeFacesDialog {
    private static final MembershipTable MEMBERSHIP_TABLE = new MembershipTable("userManagement:groupDialog:membershipTable");
    private static final SelenideElement TOGGLE_NESTED_BUTTON = $(testId("userManagement:groupDialog:toggleNested"));
    private static final AvailableGroupsTable AVAILABLE_GROUPS_TABLE = new AvailableGroupsTable("userManagement:groupDialog:availableGroupsTable");
    private static final SelenideElement CLOSE_BUTTON = $(testId("userManagement:groupDialog:close"));

    /*
     * Actions
     */
    public GroupDialog toggleNested() {
        TOGGLE_NESTED_BUTTON.click();
        return this;
    }

    public UserManagementPage close() {
        CLOSE_BUTTON.click();
        return page(UserManagementPage.class);
    }

    /*
     * Getters
     */
    public MembershipTable getMembershipTable() {
        return MEMBERSHIP_TABLE;
    }

    public AvailableGroupsTable getAvailableGroupsTable() {
        return AVAILABLE_GROUPS_TABLE;
    }
}