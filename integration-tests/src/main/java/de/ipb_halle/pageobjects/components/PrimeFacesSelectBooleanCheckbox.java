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
package de.ipb_halle.pageobjects.components;

import static com.codeborne.selenide.Condition.selected;
import static com.codeborne.selenide.Selenide.$;
import static de.ipb_halle.pageobjects.util.Selectors.elementWithCssClasses;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import org.openqa.selenium.By;

import com.codeborne.selenide.SelenideElement;

/**
 * Page object abstraction for PrimeFaces' selectBooleanCheckbox component.
 * 
 * @author flange
 */
public class PrimeFacesSelectBooleanCheckbox {
    private final SelenideElement clickableDiv;
    private final SelenideElement selectInput;
    private final SelenideElement label;

    public PrimeFacesSelectBooleanCheckbox(String testId) {
        this(null, testId);
    }

    public PrimeFacesSelectBooleanCheckbox(SelenideElement parent, String testId) {
        SelenideElement element;
        if (parent != null) {
            element = parent.$(testId(testId));
        } else {
            element = $(testId(testId));
        }

        clickableDiv = element.$(elementWithCssClasses("div", "ui-chkbox-box"));
        selectInput = element.$(By.tagName("input"));
        label = element.$(By.className("ui-chkbox-label"));
    }

    /*
     * Actions
     */
    public PrimeFacesSelectBooleanCheckbox click() {
        clickableDiv.click();
        return this;
    }

    /*
     * Getters
     */
    public SelenideElement label() {
        return label;
    }

    public SelenideElement selectInput() {
        return selectInput;
    }

    public boolean isSelected() {
        return selectInput.isSelected();
    }

    /*
     * Fluent assertions
     */
    public PrimeFacesSelectBooleanCheckbox shouldBeSelected() {
        selectInput.shouldBe(selected);
        return this;
    }

    public PrimeFacesSelectBooleanCheckbox shouldNotBeSelected() {
        selectInput.shouldNotBe(selected);
        return this;
    }
}