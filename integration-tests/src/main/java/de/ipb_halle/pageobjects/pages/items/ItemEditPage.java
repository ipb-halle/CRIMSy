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
package de.ipb_halle.pageobjects.pages.items;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.pages.AbstractPage;
import de.ipb_halle.pageobjects.pages.items.tabs.ItemEditTab;
import de.ipb_halle.pageobjects.pages.materials.MaterialOverviewPage;

/**
 * Page object for /ui/web/WEB-INF/templates/item/itemEdit.xhtml
 * 
 * @author flange
 */
public class ItemEditPage extends AbstractPage<ItemEditPage> implements ItemEditTab {
    private static final SelenideElement MATERIAL_NAME = $(testId("itemEdit:materialName"));
    private static final SelenideElement BACKWARD_BUTTON = $(testId("itemEdit:backward"));
    private static final SelenideElement CHANGED_TEXT = $(testId("itemEdit:changedText"));
    private static final SelenideElement FORWARD_BUTTON = $(testId("itemEdit:forward"));
    private static final SelenideElement CANCEL_BUTTON = $(testId("itemEdit:cancel"));
    private static final SelenideElement SAVE_BUTTON = $(testId("itemEdit:save"));
    private static final SelenideElement ERROR_MESSAGES = $(testId("itemEdit:errorMessages"));

    /*
     * Actions
     */
    public ItemEditPage historyBackwards() {
        BACKWARD_BUTTON.click();
        return this;
    }

    public ItemEditPage historyForwards() {
        FORWARD_BUTTON.click();
        return this;
    }

    /**
     * Cancel item edit.
     * <p>
     * Should direct the browser to the materials overview page (item creation) or
     * to the item overview page (item edit), thus only {@link MaterialOverviewPage}
     * or {@link ItemsOverviewPage} are useful page object classes to be supplied in
     * the {@code expectedPageClass} parameter.
     * 
     * @param <T>
     * @param expectedPageClass expected page
     * @return page object of expected page
     */
    public <T extends AbstractPage<T>> T cancel(Class<T> expectedPageClass) {
        CANCEL_BUTTON.click();
        return page(expectedPageClass);
    }

    /**
     * Try to save the item.
     * <p>
     * Should direct the browser to the items overview page or stay on this page
     * depending on the validation outcome, thus only {@link ItemsOverviewPage} or
     * {@link ItemEditPage} are useful page object classes to be supplied in the
     * {@code expectedPageClass} parameter.
     * 
     * @param <T>
     * @param expectedPageClass expected page
     * @return page object of expected page
     */
    public <T extends AbstractPage<T>> T save(Class<T> expectedPageClass) {
        SAVE_BUTTON.click();
        return page(expectedPageClass);
    }

    /*
     * Getters
     */
    public SelenideElement materialName() {
        return MATERIAL_NAME;
    }

    public SelenideElement changedText() {
        return CHANGED_TEXT;
    }
    
    public SelenideElement errorMessages() {
        return ERROR_MESSAGES;
    }
}