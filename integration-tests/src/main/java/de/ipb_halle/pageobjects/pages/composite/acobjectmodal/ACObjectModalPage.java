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
package de.ipb_halle.pageobjects.pages.composite.acobjectmodal;

import static com.codeborne.selenide.Selenide.$;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.components.PrimeFacesDialog;

/**
 * Page object for /ui/web/resources/crimsy/acobjectModal.xhtml
 * 
 * @author flange
 */
public class ACObjectModalPage extends PrimeFacesDialog {
    private static final ACEntriesTable AC_ENTRIES_TABLE = new ACEntriesTable("acObjectModal:acEntriesTable");
    private static final SelenideElement INSTITUTION_INPUT = $(testId("acObjectModal:searchFilter:institution"));
    private static final SelenideElement GROUP_NAME_INPUT = $(testId("acObjectModal:searchFilter:groupName"));
    private static final SelenideElement CLEAR_BUTTON = $(testId("acObjectModal:searchFilter:clear"));
    private static final SelenideElement SEARCH_BUTTON = $(testId("acObjectModal:searchFilter:search"));
    private static final AddableGroupsTable ADDABLE_GROUPS_TABLE = new AddableGroupsTable(
            "acObjectModal:addableGroupsTable");
    private static final SelenideElement CANCEL_BUTTON = $(testId("acObjectModal:cancel"));
    private static final SelenideElement SAVE_BUTTON = $(testId("acObjectModal:save"));

    /*
     * Actions
     */
    public ACObjectModalPage clearSearchFilters() {
        CLEAR_BUTTON.click();
        return this;
    }

    public ACObjectModalPage search() {
        SEARCH_BUTTON.click();
        return this;
    }

    // TODO: make fluent
    public void cancel() {
        CANCEL_BUTTON.click();
    }

    // TODO: make fluent
    public void save() {
        SAVE_BUTTON.click();
    }

    /*
     * Getters
     */
    public ACEntriesTable getAcEntriesTable() {
        return AC_ENTRIES_TABLE;
    }

    public SelenideElement getInstitutionInput() {
        return INSTITUTION_INPUT;
    }

    public SelenideElement getGroupNameInput() {
        return GROUP_NAME_INPUT;
    }

    public AddableGroupsTable getAddableGroupsTable() {
        return ADDABLE_GROUPS_TABLE;
    }
}