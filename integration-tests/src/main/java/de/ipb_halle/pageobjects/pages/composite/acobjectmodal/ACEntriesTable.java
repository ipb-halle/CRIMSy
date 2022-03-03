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

import static de.ipb_halle.pageobjects.util.Selectors.testId;

import de.ipb_halle.pageobjects.components.PrimeFacesSelectBooleanCheckbox;
import de.ipb_halle.pageobjects.components.table.PrimeFacesDataTable;

/**
 * Page object for the ACEntries table in
 * /ui/web/resources/crimsy/acobjectModal.xhtml
 * 
 * @author flange
 */
public class ACEntriesTable extends PrimeFacesDataTable<ACEntriesTable> {
    private static final String PERM_READ_CHECKBOX = "acObjectModal:acEntriesTable:permRead";
    private static final String PERM_EDIT_CHECKBOX = "acObjectModal:acEntriesTable:permEdit";
    private static final String PERM_CREATE_CHECKBOX = "acObjectModal:acEntriesTable:permCreate";
    private static final String PERM_DELETE_CHECKBOX = "acObjectModal:acEntriesTable:permDelete";
    private static final String PERM_CHOWN_CHECKBOX = "acObjectModal:acEntriesTable:permChown";
    private static final String PERM_GRANT_CHECKBOX = "acObjectModal:acEntriesTable:permGrant";
    private static final String PERM_SUPER_CHECKBOX = "acObjectModal:acEntriesTable:permSuper";
    private static final String REMOVE_GROUP_BUTTON = testId("acObjectModal:acEntriesTable:removeGroup");

    public ACEntriesTable(String testId) {
        super(testId);
    }

    /*
     * Actions
     */
    public ACEntriesTable removeGroup(int rowIndex) {
        getCell(9, rowIndex).$(REMOVE_GROUP_BUTTON).click();
        return this;
    }

    /*
     * Getters
     */
    public String getGroupName(int rowIndex) {
        return getCell(0, rowIndex).text();
    }

    public String getInstitution(int rowIndex) {
        return getCell(1, rowIndex).text();
    }

    public PrimeFacesSelectBooleanCheckbox getPermReadCheckbox(int rowIndex) {
        return new PrimeFacesSelectBooleanCheckbox(getCell(2, rowIndex), PERM_READ_CHECKBOX);
    }

    public PrimeFacesSelectBooleanCheckbox getPermEditCheckbox(int rowIndex) {
        return new PrimeFacesSelectBooleanCheckbox(getCell(3, rowIndex), PERM_EDIT_CHECKBOX);
    }

    public PrimeFacesSelectBooleanCheckbox getPermCreateCheckbox(int rowIndex) {
        return new PrimeFacesSelectBooleanCheckbox(getCell(4, rowIndex), PERM_CREATE_CHECKBOX);
    }

    public PrimeFacesSelectBooleanCheckbox getPermDeleteCheckbox(int rowIndex) {
        return new PrimeFacesSelectBooleanCheckbox(getCell(5, rowIndex), PERM_DELETE_CHECKBOX);
    }

    public PrimeFacesSelectBooleanCheckbox getPermChownCheckbox(int rowIndex) {
        return new PrimeFacesSelectBooleanCheckbox(getCell(6, rowIndex), PERM_CHOWN_CHECKBOX);
    }

    public PrimeFacesSelectBooleanCheckbox getPermGrantCheckbox(int rowIndex) {
        return new PrimeFacesSelectBooleanCheckbox(getCell(7, rowIndex), PERM_GRANT_CHECKBOX);
    }

    public PrimeFacesSelectBooleanCheckbox getPermSuperCheckbox(int rowIndex) {
        return new PrimeFacesSelectBooleanCheckbox(getCell(8, rowIndex), PERM_SUPER_CHECKBOX);
    }
}