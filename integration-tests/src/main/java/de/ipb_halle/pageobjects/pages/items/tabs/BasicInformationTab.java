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
package de.ipb_halle.pageobjects.pages.items.tabs;

import static com.codeborne.selenide.Selenide.$;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

/**
 * Page object for the basic information tab in
 * /ui/web/WEB-INF/templates/item/itemEdit.xhtml
 * 
 * @author flange
 */
public class BasicInformationTab implements ItemEditTab {
    private static final SelenideElement AMOUNT_INPUT = $(testId("input", "basicInformationTab:amount"));
    private static final SelenideElement AMOUNT_UNIT_SELECTION = $(testId("select", "basicInformationTab:amountUnit"));
    private static final SelenideElement PURITY_SELECTION = $(testId("select", "basicInformationTab:purity"));
    private static final SelenideElement DIRECT_CONTAINER_CHECKBOX = $(testId("input", "basicInformationTab:directContainer"));
    private static final SelenideElement DIRECT_CONTAINER_SIZE_INPUT = $(testId("input", "basicInformationTab:directContainerSize"));
    private static final SelenideElement DIRECT_CONTAINER_UNIT_INPUT = $(testId("input", "basicInformationTab:directContainerUnit"));
    private static final SelenideElement DIRECT_CONTAINER_TYPE_SELECTION = $(testId("select", "basicInformationTab:directContainerType"));
    private static final SelenideElement SOLVED_CHECKBOX = $(testId("input", "basicInformationTab:solved"));
    private static final SelenideElement CONCENTRATION_INPUT = $(testId("input", "basicInformationTab:concentration"));
    private static final SelenideElement CONCENTRATION_UNIT_SELECTION = $(testId("select", "basicInformationTab:concentrationUnit"));
    private static final SelenideElement SOLVENT_SELECTION = $(testId("select", "basicInformationTab:solvent"));
    private static final SelenideElement CUSTOM_LABEL_CHECKBOX = $(testId("input", "basicInformationTab:customLabel"));
    private static final SelenideElement LABEL_INPUT = $(testId("input", "basicInformationTab:label"));
    private static final SelenideElement DESCRIPTION_INPUT = $(testId("textarea", "basicInformationTab:description"));

    /*
     * Actions
     */
    public BasicInformationTab selectAmountUnit(String unit) {
        AMOUNT_UNIT_SELECTION.selectOption(unit);
        return this;
    }

    public BasicInformationTab selectPurity(String purity) {
        PURITY_SELECTION.selectOption(purity);
        return this;
    }

    public BasicInformationTab clickDirectContainerCheckbox() {
        DIRECT_CONTAINER_CHECKBOX.click();
        return this;
    }

    public BasicInformationTab selectDirectContainerType(String type) {
        DIRECT_CONTAINER_TYPE_SELECTION.selectOption(type);
        return this;
    }

    public BasicInformationTab clickSolvedCheckbox() {
        SOLVED_CHECKBOX.click();
        return this;
    }

    public BasicInformationTab selectConcentrationUnit(String unit) {
        CONCENTRATION_UNIT_SELECTION.selectOption(unit);
        return this;
    }

    public BasicInformationTab selectSolvent(String solvent) {
        SOLVENT_SELECTION.selectOption(solvent);
        return this;
    }

    public BasicInformationTab clickCustomLabelCheckbox() {
        CUSTOM_LABEL_CHECKBOX.click();
        return this;
    }

    /*
     * Getters
     */
    public SelenideElement amountInput() {
        return AMOUNT_INPUT;
    }

    public SelenideElement amountUnitSelection() {
        return AMOUNT_UNIT_SELECTION;
    }

    public SelenideElement puritySelection() {
        return PURITY_SELECTION;
    }

    public SelenideElement directContainerCheckbox() {
        return DIRECT_CONTAINER_CHECKBOX;
    }

    public SelenideElement directContainerSizeInput() {
        return DIRECT_CONTAINER_SIZE_INPUT;
    }

    public SelenideElement directContainerUnitInput() {
        return DIRECT_CONTAINER_UNIT_INPUT;
    }

    public SelenideElement directContainerTypeSelection() {
        return DIRECT_CONTAINER_TYPE_SELECTION;
    }

    public SelenideElement solvedCheckbox() {
        return SOLVED_CHECKBOX;
    }

    public SelenideElement concentrationInput() {
        return CONCENTRATION_INPUT;
    }

    public SelenideElement concentrationUnitSelection() {
        return CONCENTRATION_UNIT_SELECTION;
    }

    public SelenideElement solventSelection() {
        return SOLVENT_SELECTION;
    }

    public SelenideElement customLabelCheckbox() {
        return CUSTOM_LABEL_CHECKBOX;
    }

    public SelenideElement labelInput() {
        return LABEL_INPUT;
    }

    public SelenideElement descriptionInput() {
        return DESCRIPTION_INPUT;
    }
}