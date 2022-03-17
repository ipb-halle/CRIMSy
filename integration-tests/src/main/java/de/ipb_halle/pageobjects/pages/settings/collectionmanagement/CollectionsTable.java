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
package de.ipb_halle.pageobjects.pages.settings.collectionmanagement;

import static com.codeborne.selenide.Selenide.page;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.components.table.DataTable;
import de.ipb_halle.pageobjects.pages.composite.acobjectmodal.ACObjectModalPage;

/**
 * Page object for the collections table in
 * /ui/web/WEB-INF/templates/collectionManagement.xhtml
 * 
 * @author flange
 */
public class CollectionsTable extends DataTable<CollectionsTable> {
    private static final String EDIT_COLLECTION_BUTTON = testId("collectionManagement:editCollection");
    private static final String CLEAR_COLLECTION_BUTTON = testId("collectionManagement:clearCollection");
    private static final String DELETE_COLLECTION_BUTTON = testId("collectionManagement:deleteCollection");
    private static final String CHANGE_PERMISSIONS_BUTTON = testId("collectionManagement:changePermissions");

    public CollectionsTable(String testId) {
        super(testId);
    }

    /*
     * Actions
     */
    public CollectionDialog editCollection(int rowIndex) {
        getCell(5, rowIndex).$(EDIT_COLLECTION_BUTTON).click();
        return page(CollectionDialog.class);
    }

    public CollectionDialog clearCollection(int rowIndex) {
        getCell(5, rowIndex).$(CLEAR_COLLECTION_BUTTON).click();
        return page(CollectionDialog.class);
    }

    public CollectionDialog deleteCollection(int rowIndex) {
        getCell(5, rowIndex).$(DELETE_COLLECTION_BUTTON).click();
        return page(CollectionDialog.class);
    }

    public ACObjectModalPage changePermissions(int rowIndex) {
        getCell(5, rowIndex).$(CHANGE_PERMISSIONS_BUTTON).click();
        return page(ACObjectModalPage.class);
    }

    /*
     * Getters
     */
    public SelenideElement getName(int rowIndex) {
        return getCell(0, rowIndex);
    }

    public SelenideElement getDescription(int rowIndex) {
        return getCell(1, rowIndex);
    }

    public SelenideElement getOwner(int rowIndex) {
        return getCell(2, rowIndex);
    }

    public SelenideElement getNumberOfDocuments(int rowIndex) {
        return getCell(3, rowIndex);
    }

    public SelenideElement getInstitution(int rowIndex) {
        return getCell(4, rowIndex);
    }
}