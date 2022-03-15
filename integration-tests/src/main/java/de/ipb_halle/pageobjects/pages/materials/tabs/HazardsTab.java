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

import de.ipb_halle.pageobjects.components.PrimeFacesSelectBooleanCheckbox;
import de.ipb_halle.pageobjects.pages.AbstractPage;

/**
 * Page object for /ui/web/WEB-INF/templates/material/components/hazards.xhtml
 * 
 * @author flange
 */
public class HazardsTab extends AbstractPage<HazardsTab> implements MaterialEditTab {
    private static final SelenideElement H_STATEMENTS_INPUT = $(testId("hazardsTab:hStatements"));
    private static final SelenideElement P_STATEMENTS_INPUT = $(testId("hazardsTab:pStatements"));
    private static final PrimeFacesSelectBooleanCheckbox RADIOACTIVE_CHECKBOX = new PrimeFacesSelectBooleanCheckbox(
            "hazardsTab:radioactive");
    private static final SelenideElement RADIOACTIVE_LABEL = $(testId("hazardsTab:radioactiveLabel"));
    private static final SelenideElement RADIOACTIVE_IMAGE = $(testId("hazardsTab:radioactiveImage"));
    private static final PrimeFacesSelectBooleanCheckbox GMO_CHECKBOX = new PrimeFacesSelectBooleanCheckbox(
            "hazardsTab:gmo");
    private static final SelenideElement CUSTOM_REMARKS_INPUT = $(testId("hazardsTab:customRemarks"));

    /**
     * Page object for the GHS checkboxes.
     * 
     * @author flange
     */
    /*
     * This class cannot be refactored using PrimeFacesSelectManyCheckbox, but it
     * tries to offer a similar API and implementation.
     */
    public static class GHSData {
        private static final SelenideElement GHS_TABLE = $(testId("hazardsTab:GHSTable"));
        private static final ElementsCollection GHS_CHECKBOXES_CLICKABLE_DIVS = GHS_TABLE
                .$$(elementWithCssClasses("div", "ui-chkbox-box"));
        private static final ElementsCollection GHS_INPUTS = GHS_TABLE.$$(By.tagName("input"));
        private static final ElementsCollection GHS_LABELS = GHS_TABLE.$$(testId("hazardsTab:GHSLabel"));
        private static final ElementsCollection GHS_IMAGES = GHS_TABLE.$$(testId("hazardsTab:GHSImage"));

        /*
         * Actions
         */
        public GHSData clickCheckbox(int index) {
            GHS_CHECKBOXES_CLICKABLE_DIVS.get(index).click();
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

    /**
     * Page object for the biosafety radio group.
     * 
     * @author flange
     */
    /*
     * This class cannot be refactored using PrimeFacesSelectOneRadio, but it tries
     * to offer a similar API and implementation.
     */
    public static class BioSafetyData {
        private static final SelenideElement BIOSAFETY_LEVEL_TABLE = $(testId("hazardsTab:bioSafetyLevelTable"));
        private static final ElementsCollection BIOSAFETY_CLICKABLE_DIVS = BIOSAFETY_LEVEL_TABLE
                .$$(elementWithCssClasses("div", "ui-radiobutton-box"));
        private static final ElementsCollection BIOSAFETY_INPUTS = BIOSAFETY_LEVEL_TABLE.$$(By.tagName("input"));
        private static final ElementsCollection BIOSAFETY_LABELS = BIOSAFETY_LEVEL_TABLE
                .$$(testId("hazardsTab:bioSafetyLevelLabel"));
        private static final ElementsCollection BIOSAFETY_IMAGES = BIOSAFETY_LEVEL_TABLE
                .$$(testId("hazardsTab:bioSafetyLevelImage"));

        /*
         * Actions
         */
        public BioSafetyData clickRadioButton(int index) {
            BIOSAFETY_CLICKABLE_DIVS.get(index).click();
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

    /*
     * Actions
     */
    public HazardsTab clickRadioactiveCheckbox() {
        RADIOACTIVE_CHECKBOX.click();
        return this;
    }

    public HazardsTab clickGMOCheckbox() {
        GMO_CHECKBOX.click();
        return this;
    }

    /*
     * Getters
     */
    public GHSData ghsData() {
        return new GHSData();
    }

    public SelenideElement hStatementsInput() {
        return H_STATEMENTS_INPUT;
    }

    public SelenideElement pStatementsInput() {
        return P_STATEMENTS_INPUT;
    }

    public PrimeFacesSelectBooleanCheckbox radioActiveCheckbox() {
        return RADIOACTIVE_CHECKBOX;
    }

    public SelenideElement radioactiveLabel() {
        return RADIOACTIVE_LABEL;
    }

    public SelenideElement radioactiveImage() {
        return RADIOACTIVE_IMAGE;
    }

    public BioSafetyData bioSafetyData() {
        return new BioSafetyData();
    }

    public PrimeFacesSelectBooleanCheckbox gmoCheckbox() {
        return GMO_CHECKBOX;
    }

    public SelenideElement customRemarksInput() {
        return CUSTOM_REMARKS_INPUT;
    }
}