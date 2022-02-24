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
    private final SelenideElement input;
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
        input = element.$(By.tagName("input"));
        label = element.$(By.tagName("label"));
    }

    public String getLabel() {
        return label.text();
    }

    public PrimeFacesSelectBooleanCheckbox click() {
        clickableDiv.click();
        return this;
    }

    public boolean isSelected() {
        return input.isSelected();
    }
}