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

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Selenide.$;
import static de.ipb_halle.pageobjects.util.Selectors.elementWithCssClasses;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

/**
 * Page object abstraction for PrimeFaces' selectOneRadio component.
 * 
 * @author flange
 */
public class PrimeFacesSelectOneRadio {
    private static final Condition CHECKED_INPUT_CONDITION = attribute("checked", "checked");
    private final ElementsCollection clickableDivs;
    private final ElementsCollection inputs;
    private final ElementsCollection labels;

    public PrimeFacesSelectOneRadio(String testId) {
        SelenideElement element = $(testId(testId));
        clickableDivs = element.$$(elementWithCssClasses("div", "ui-radiobutton-box"));
        inputs = element.$$(By.tagName("input"));
        labels = element.$$(By.tagName("label"));
    }

    /*
     * Actions
     */
    public PrimeFacesSelectOneRadio clickRadioButton(int index) {
        clickableDivs.get(index).click();
        return this;
    }

//    public PrimeFacesSelectOneRadio clickRadioButton(String label) {
//        clickableDivs.get(getLabelIndex(label)).click();
//        return this;
//    }

//    private int getLabelIndex(String label) {
//        int index = getLabels().indexOf(label);
//        if (index == -1) {
//            throw new RuntimeException("No such label: " + label);
//        }
//        return index;
//    }

    /*
     * Getters
     */
    public ElementsCollection labels() {
        return labels;
    }

    public SelenideElement label(int index) {
        return labels.get(index);
    }

    public SelenideElement checkedLabel() {
        SelenideElement checkedInput = inputs.filterBy(CHECKED_INPUT_CONDITION).shouldHave(size(1)).first();
        /*
         * Not sure if SelenideElement.equals() works reliably: Don't use List.indexOf()
         * here.
         */

        SelenideElement td = checkedInput.parent().parent().parent();
        SelenideElement label = td.$(By.tagName("label"));
        return label;
    }

//    public List<String> getLabels() {
//        List<String> textLabels = new ArrayList<>();
//        for (SelenideElement element : labels) {
//            textLabels.add(element.text());
//        }
//        return textLabels;
//    }
}