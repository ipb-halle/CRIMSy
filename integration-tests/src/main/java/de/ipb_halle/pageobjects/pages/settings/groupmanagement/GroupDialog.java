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

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.page;
import static de.ipb_halle.pageobjects.util.Apply.applyValue;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.components.PrimeFacesDialog;

/**
 * Page object for the group edit dialog in
 * /ui/web/WEB-INF/templates/groupManagement.xhtml
 * 
 * @author flange
 */
public class GroupDialog extends PrimeFacesDialog {
    private static final SelenideElement ID_INPUT = $(testId("input", "groupManagement:groupDialog:id"));
    private static final SelenideElement ID_MESSAGE = $(testId("groupManagement:groupDialog:idMessage"));
    private static final SelenideElement NAME_INPUT = $(testId("input", "groupManagement:groupDialog:name"));
    private static final SelenideElement NAME_MESSAGE = $(testId("groupManagement:groupDialog:nameMessage"));
    private static final SelenideElement SUBSYSTEM_INPUT = $(testId("input", "groupManagement:groupDialog:subsystem"));
    private static final SelenideElement SUBSYSTEM_MESSAGE = $(testId("groupManagement:groupDialog:subsystemMessage"));
    private static final SelenideElement CLOSE_BUTTON = $(testId("groupManagement:groupDialog:close"));
    private static final SelenideElement CONFIRM_BUTTON = $(testId("groupManagement:groupDialog:confirm"));

    /*
     * Actions
     */
    /**
     * Applies the group model to the input fields.
     * <p>
     * Convention: The input element will not be evaluated in case the model field
     * is null. Use empty strings to reset fields.
     * 
     * @param model
     * @return this
     */
    public GroupDialog applyModel(GroupModel model) {
        applyValue(model.getName(), NAME_INPUT);
        return this;
    }

    public GroupManagementPage confirm() {
        CONFIRM_BUTTON.click();
        return page(GroupManagementPage.class);
    }

    public GroupManagementPage close() {
        CLOSE_BUTTON.click();
        return page(GroupManagementPage.class);
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

    public SelenideElement subsystemInput() {
        return SUBSYSTEM_INPUT;
    }

    public static SelenideElement subsystemMessage() {
        return SUBSYSTEM_MESSAGE;
    }
}