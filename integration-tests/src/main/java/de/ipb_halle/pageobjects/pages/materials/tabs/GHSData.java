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

import static com.codeborne.selenide.Condition.checked;
import static com.codeborne.selenide.Selenide.$;
import static de.ipb_halle.pageobjects.util.Selectors.elementWithCssClasses;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import org.openqa.selenium.By;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.pages.materials.models.GHSModel;

/**
 * Page object for the GHS checkboxes in
 * /ui/web/WEB-INF/templates/material/components/hazards.xhtml
 * 
 * @author flange
 */
/*
 * This class cannot be refactored using PrimeFacesSelectManyCheckbox, but it
 * tries to offer a similar API and implementation.
 */
public class GHSData {
    private static final SelenideElement GHS_TABLE = $(testId("hazardsTab:GHSTable"));
    private static final ElementsCollection GHS_CHECKBOXES_CLICKABLE_DIVS = GHS_TABLE
            .$$(elementWithCssClasses("div", "ui-chkbox-box"));
    private static final ElementsCollection GHS_INPUTS = GHS_TABLE.$$(By.tagName("input"));
    private static final ElementsCollection GHS_LABELS = GHS_TABLE.$$(testId("hazardsTab:GHSLabel"));
    private static final ElementsCollection GHS_IMAGES = GHS_TABLE.$$(testId("hazardsTab:GHSImage"));

    /*
     * Actions
     */
    public GHSData applyModel(GHSModel model) {
        for (int ghsNumber : model.getActivated()) {
            SelenideElement input = selectInput(ghsNumber - 1);
            if (!input.isSelected()) {
                clickCheckbox(ghsNumber - 1);
            }
        }
        for (int ghsNumber : model.getDeactivated()) {
            SelenideElement input = selectInput(ghsNumber - 1);
            if (input.isSelected()) {
                clickCheckbox(ghsNumber - 1);
            }
        }

        return this;
    }

    public GHSData clickCheckbox(int index) {
        GHS_CHECKBOXES_CLICKABLE_DIVS.get(index).click();
        return this;
    }

    /*
     * Fluent assertions
     */
    public GHSData shouldHave(GHSModel model) {
        for (int ghsNumber : model.getActivated()) {
            selectInput(ghsNumber - 1).shouldBe(checked);
        }
        for (int ghsNumber : model.getDeactivated()) {
            selectInput(ghsNumber - 1).shouldNotBe(checked);
        }

        return this;
    }

    /*
     * Getters
     */
    public SelenideElement ghsTable() {
        return GHS_TABLE;
    }

    public ElementsCollection labels() {
        return GHS_LABELS;
    }

    public SelenideElement label(int index) {
        return GHS_LABELS.get(index);
    }

    public ElementsCollection selectInputs() {
        return GHS_INPUTS;
    }

    public SelenideElement selectInput(int index) {
        return GHS_INPUTS.get(index);
    }

    public ElementsCollection images() {
        return GHS_IMAGES;
    }

    public SelenideElement image(int index) {
        return GHS_IMAGES.get(index);
    }
}