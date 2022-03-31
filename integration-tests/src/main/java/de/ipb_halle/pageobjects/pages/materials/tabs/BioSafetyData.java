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
package de.ipb_halle.pageobjects.pages.materials.tabs;

import static com.codeborne.selenide.CollectionCondition.size;
import static com.codeborne.selenide.Condition.selected;
import static com.codeborne.selenide.Selenide.$;
import static de.ipb_halle.pageobjects.util.Selectors.elementWithCssClasses;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

/**
 * Page object for the biosafety radio group.
 * 
 * @author flange
 */
/*
 * This class cannot be refactored using PrimeFacesSelectOneRadio, but it tries
 * to offer a similar API and implementation.
 */
public class BioSafetyData {
    private static final SelenideElement BIOSAFETY_LEVEL_TABLE = $(testId("hazardsTab:bioSafetyLevelTable"));
    private static final ElementsCollection BIOSAFETY_CLICKABLE_DIVS = BIOSAFETY_LEVEL_TABLE
            .$$(elementWithCssClasses("div", "ui-radiobutton-box"));
    private static final ElementsCollection BIOSAFETY_INPUTS = BIOSAFETY_LEVEL_TABLE.$$(By.tagName("input"));
    private static final ElementsCollection BIOSAFETY_LABELS = BIOSAFETY_LEVEL_TABLE
            .$$(testId("hazardsTab:bioSafetyLevelLabel"));
    private static final ElementsCollection BIOSAFETY_IMAGES = BIOSAFETY_LEVEL_TABLE
            .$$(testId("hazardsTab:bioSafetyLevelImage"));

    public enum Level {
        LEVEL1(0),
        LEVEL2(1),
        LEVEL3(2),
        LEVEL4(3),
        UNCLASSIFIED(4);

        private final int index;

        private Level(int index) {
            this.index = index;
        }
    }

    /*
     * Actions
     */
    public BioSafetyData select(Level level) {
        clickRadioButton(level.index);
        return this;
    }

    public BioSafetyData clickRadioButton(int index) {
        BIOSAFETY_CLICKABLE_DIVS.get(index).click();
        return this;
    }

    /*
     * Fluent assertions
     */
    public BioSafetyData shouldHave(Level level) {
        BIOSAFETY_INPUTS.get(level.index).shouldBe(selected);
        return this;
    }

    /*
     * Getters
     */
    public SelenideElement biosafetyLevelTable() {
        return BIOSAFETY_LEVEL_TABLE;
    }

    public ElementsCollection labels() {
        return BIOSAFETY_LABELS;
    }

    public SelenideElement label(int index) {
        return BIOSAFETY_LABELS.get(index);
    }

    public SelenideElement checkedLabel() {
        SelenideElement checkedInput = BIOSAFETY_INPUTS.filter(selected).shouldHave(size(1)).first();
        SelenideElement tr = checkedInput.parent().parent().parent().parent();
        SelenideElement label = tr.$(By.tagName("label"));
        return label;
    }

    public ElementsCollection images() {
        return BIOSAFETY_IMAGES;
    }

    public SelenideElement image(int index) {
        return BIOSAFETY_IMAGES.get(index);
    }
}