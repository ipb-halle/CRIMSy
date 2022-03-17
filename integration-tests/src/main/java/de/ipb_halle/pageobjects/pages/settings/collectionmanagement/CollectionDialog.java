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
import static de.ipb_halle.pageobjects.util.Apply.applyValue;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.components.PrimeFacesDialog;

/**
 * Page object for the collection edit dialog in
 * /ui/web/WEB-INF/templates/collectionManagement.xhtml
 * 
 * @author flange
 */
public class CollectionDialog extends PrimeFacesDialog {
    private static final SelenideElement ID_INPUT = $(testId("input", "collectionManagement:collectionDialog:id"));
    private static final SelenideElement ID_MESSAGE = $(testId("collectionManagement:collectionDialog:idMessage"));
    private static final SelenideElement NAME_INPUT = $(testId("input", "collectionManagement:collectionDialog:name"));
    private static final SelenideElement NAME_MESSAGE = $(testId("collectionManagement:collectionDialog:nameMessage"));
    private static final SelenideElement DESCRIPTION_INPUT = $(
            testId("input", "collectionManagement:collectionDialog:description"));
    private static final SelenideElement DESCRIPTION_MESSAGE = $(
            testId("collectionManagement:collectionDialog:descriptionMessage"));
    private static final SelenideElement INDEX_PATH_INPUT = $(
            testId("input", "collectionManagement:collectionDialog:indexPath"));
    private static final SelenideElement INDEX_PATH_MESSAGE = $(
            testId("collectionManagement:collectionDialog:indexPathMessage"));
    private static final SelenideElement STORAGE_PATH_INPUT = $(
            testId("input", "collectionManagement:collectionDialog:storagePath"));
    private static final SelenideElement STORAGE_PATH_MESSAGE = $(
            testId("collectionManagement:collectionDialog:storagePathMessage"));
    private static final SelenideElement INSTITUTION_INPUT = $(
            testId("input", "collectionManagement:collectionDialog:institution"));
    private static final SelenideElement INSTITUTION_MESSAGE = $(
            testId("collectionManagement:collectionDialog:institutionMessage"));
    private static final SelenideElement CLOSE_BUTTON = $(testId("collectionManagement:collectionDialog:close"));
    private static final SelenideElement CONFIRM_BUTTON = $(testId("collectionManagement:collectionDialog:confirm"));

    /*
     * Actions
     */
    /**
     * Applies the collection model to the input fields.
     * <p>
     * Convention: The input element will not be evaluated in case the model field
     * is null. Use empty strings to reset fields.
     * 
     * @param model
     * @return this
     */
    public CollectionDialog applyModel(CollectionModel model) {
        applyValue(model.getName(), NAME_INPUT);
        applyValue(model.getName(), DESCRIPTION_INPUT);
        return this;
    }

    public CollectionManagementPage confirm() {
        CONFIRM_BUTTON.click();
        return page(CollectionManagementPage.class);
    }

    public CollectionManagementPage close() {
        CLOSE_BUTTON.click();
        return page(CollectionManagementPage.class);
    }

    /*
     * Getters
     */
    public SelenideElement idInput() {
        return ID_INPUT;
    }

    public SelenideElement idMessage() {
        return ID_MESSAGE;
    }

    public SelenideElement nameInput() {
        return NAME_INPUT;
    }

    public SelenideElement nameMessage() {
        return NAME_MESSAGE;
    }

    public SelenideElement descriptionInput() {
        return DESCRIPTION_INPUT;
    }

    public SelenideElement descriptionMessage() {
        return DESCRIPTION_MESSAGE;
    }

    public SelenideElement indexPathInput() {
        return INDEX_PATH_INPUT;
    }

    public SelenideElement indexPathMessage() {
        return INDEX_PATH_MESSAGE;
    }

    public SelenideElement storagePathInput() {
        return STORAGE_PATH_INPUT;
    }

    public SelenideElement storagePathMessage() {
        return STORAGE_PATH_MESSAGE;
    }

    public SelenideElement institutionInput() {
        return INSTITUTION_INPUT;
    }

    public SelenideElement institutionMessage() {
        return INSTITUTION_MESSAGE;
    }
}