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
package de.ipb_halle.pageobjects.pages.projects;

import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.components.table.DataTable;

/**
 * Page object for the ACE table in
 * /ui/web/WEB-INF/templates/project/components/template_detailrights.xhtml
 * 
 * @author flange
 */
public class ProjectACETable extends DataTable {
    private static final String PERM_READ_CHECKBOX = testId("input", "projectEdit:ACETable:permRead");
    private static final String PERM_EDIT_CHECKBOX = testId("input", "projectEdit:ACETable:permEdit");
    private static final String PERM_DELETE_CHECKBOX = testId("input", "projectEdit:ACETable:permDelete");
    private static final String PERM_SUPER_CHECKBOX = testId("input", "projectEdit:ACETable:permSuper");
    private static final String REMOVE_GROUP_BUTTON = testId("projectEdit:ACETable:removeGroup");

    public ProjectACETable(String testId) {
        super(testId);
    }

    /*
     * Actions
     */
    public void removeGroup(int rowIndex) {
        getCell(3, rowIndex).$(REMOVE_GROUP_BUTTON).click();
    }

    /*
     * Getters
     */
    public String getGroupName(int rowIndex) {
        return getCell(0, rowIndex).text();
    }

    public SelenideElement getPermReadCheckbox(int rowIndex) {
        return getCell(1, rowIndex).$(PERM_READ_CHECKBOX);
    }

    public SelenideElement getPermEditCheckbox(int rowIndex) {
        return getCell(2, rowIndex).$(PERM_EDIT_CHECKBOX);
    }

    public SelenideElement getPermDeleteCheckbox(int rowIndex) {
        return getCell(3, rowIndex).$(PERM_DELETE_CHECKBOX);
    }

    public SelenideElement getPermSuperCheckbox(int rowIndex) {
        return getCell(4, rowIndex).$(PERM_SUPER_CHECKBOX);
    }
}