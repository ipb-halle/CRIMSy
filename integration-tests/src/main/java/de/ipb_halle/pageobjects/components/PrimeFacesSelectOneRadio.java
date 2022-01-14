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

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

/**
 * Page object abstraction for PrimeFaces' selectOneRadio component.
 * 
 * @author flange
 */
public class PrimeFacesSelectOneRadio {
    private final ElementsCollection clickableDivs;
    private final ElementsCollection inputs;
    private final ElementsCollection labelElements;

    public PrimeFacesSelectOneRadio(String testId) {
        SelenideElement element = $(testId(testId));
        clickableDivs = element
                .$$(elementWithCssClasses("div", "ui-radiobutton-box"));
        inputs = element.$$(By.tagName("input"));
        labelElements = element.$$(By.tagName("label"));
    }

    public List<String> getLabels() {
        List<String> labels = new ArrayList<>();
        for (SelenideElement element : labelElements) {
            labels.add(element.text());
        }
        return labels;
    }

    private int getLabelIndex(String label) {
        int index = getLabels().indexOf(label);
        if (index == -1) {
            throw new RuntimeException("No such label: " + label);
        }
        return index;
    }

    public PrimeFacesSelectOneRadio clickRadioButton(String label) {
        clickableDivs.get(getLabelIndex(label)).click();
        return this;
    }

    public String getSelectedLabel() {
        List<String> selected = new ArrayList<>();

        List<String> allLabels = getLabels();
        for (int i = 0; i < allLabels.size(); i++) {
            String label = allLabels.get(i);
            if (inputs.get(i).isSelected()) {
                selected.add(label);
            }
        }

        if (selected.size() > 1) {
            throw new RuntimeException(
                    "A radio group cannot have more than one selected radio button!");
        } else if (selected.size() == 1) {
            return selected.get(0);
        } else {
            return null;
        }
    }
}