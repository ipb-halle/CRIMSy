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

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.navigation.Navigation;
import de.ipb_halle.pageobjects.pages.NavigablePage;

/**
 * Page object for /ui/web/WEB-INF/templates/collectionManagement.xhtml
 * 
 * @author flange
 */
public class CollectionManagementPage extends NavigablePage<CollectionManagementPage> {
    private static final CollectionsTable COLLECTIONS_TABLE = new CollectionsTable(
            "collectionManagement:collectionsTable");
    private static final SelenideElement SHOW_LOCAL_COLLECTIONS_ONLY_CHECKBOX = $(
            testId("input", "collectionManagement:showLocalCollectionsOnly"));
    private static final SelenideElement CREATE_COLLECTION_BUTTON = $(testId("collectionManagement:createCollection"));
    private static final SelenideElement REFRESH_BUTTON = $(testId("collectionManagement:refresh"));
    private static final SelenideElement NEW_COLLECTIONS_BADGE = $(testId("collectionManagement:newCollections"));

    @Override
    public Navigation getNavigationItem() {
        return Navigation.COLLECTION_MANAGEMENT;
    }

    /*
     * Actions
     */
    public CollectionDialog createCollection() {
        CREATE_COLLECTION_BUTTON.click();
        return page(CollectionDialog.class);
    }

    public CollectionManagementPage refresh() {
        REFRESH_BUTTON.click();
        return this;
    }

    /*
     * Getters
     */
    public CollectionsTable collectionsTable() {
        return COLLECTIONS_TABLE;
    }

    public SelenideElement showLocalCollectionsOnlyCheckbox() {
        return SHOW_LOCAL_COLLECTIONS_ONLY_CHECKBOX;
    }

    public SelenideElement newCollectionsBadge() {
        return NEW_COLLECTIONS_BADGE;
    }
}