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

import static com.codeborne.selenide.Selenide.$;
import static de.ipb_halle.pageobjects.util.Selectors.elementWithCssClasses;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import java.util.ArrayList;
import java.util.List;
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
    /*
     * GHS table
     */
    private static final SelenideElement GHS_TABLE = $(
            testId("hazardsTab:GHSTable"));
    private static final ElementsCollection GHS_CHECKBOXES_CLICKABLE_DIVS = GHS_TABLE
            .$$(elementWithCssClasses("div", "ui-chkbox-box"));
    private static final ElementsCollection GHS_INPUTS = GHS_TABLE
            .$$(By.tagName("input"));
    private static final ElementsCollection GHS_LABELS = GHS_TABLE
            .$$(testId("hazardsTab:GHSLabel"));
    private static final ElementsCollection GHS_IMAGES = GHS_TABLE
            .$$(testId("hazardsTab:GHSImage"));
    /*
     * H/P statements
     */
    private static final SelenideElement H_STATEMENTS_INPUT = $(
            testId("hazardsTab:hStatements"));
    private static final SelenideElement P_STATEMENTS_INPUT = $(
            testId("hazardsTab:pStatements"));
    /*
     * radioactive
     */
    private static final PrimeFacesSelectBooleanCheckbox RADIOACTIVE_CHECKBOX = new PrimeFacesSelectBooleanCheckbox(
            "hazardsTab:radioactive");
    /*
     * biosafety
     */
    private static final SelenideElement BIOSAFETY_LEVEL_TABLE = $(
            testId("hazardsTab:bioSafetyLevelTable"));
    private static final ElementsCollection BIOSAFETY_CLICKABLE_DIVS = BIOSAFETY_LEVEL_TABLE
            .$$(elementWithCssClasses("div", "ui-radiobutton-box"));
    private static final ElementsCollection BIOSAFETY_INPUTS = BIOSAFETY_LEVEL_TABLE
            .$$(By.tagName("input"));
    private static final ElementsCollection BIOSAFETY_LABELS = BIOSAFETY_LEVEL_TABLE
            .$$(testId("hazardsTab:bioSafetyLevelLabel"));
    private static final ElementsCollection BIOSAFETY_IMAGES = BIOSAFETY_LEVEL_TABLE
            .$$(testId("hazardsTab:bioSafetyLevelImage"));
    /*
     * GMO
     */
    private static final PrimeFacesSelectBooleanCheckbox GMO_CHECKBOX = new PrimeFacesSelectBooleanCheckbox(
            "hazardsTab:gmo");
    /*
     * custom remarks
     */
    private static final SelenideElement CUSTOM_REMARKS_INPUT = $(
            testId("hazardsTab:customRemarks"));

    /**
     * Page model for the GHS checkboxes. This class holds no state, thus all
     * methods use the current state of the page.
     * 
     * @author flange
     */
    // TODO: maybe refactor with PrimeFacesSelectBooleanCheckbox
    public class GHSDataModel {
        private GHSDataModel() {
        }

        public List<String> getLabels() {
            List<String> labels = new ArrayList<>();
            for (SelenideElement element : GHS_LABELS) {
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

        public GHSDataModel clickCheckbox(String label) {
            GHS_CHECKBOXES_CLICKABLE_DIVS.get(getLabelIndex(label)).click();
            return this;
        }

        public boolean isSelected(String label) {
            return GHS_INPUTS.get(getLabelIndex(label)).isSelected();
        }

        public SelenideElement getImage(String label) {
            return GHS_IMAGES.get(getLabelIndex(label));
        }
    }

    /**
     * Page model for the biosafety radio group. This class holds no state, thus
     * all methods use the current state of the page.
     * 
     * @author flange
     */
    // TODO: refactor to PrimeFacesSelectOneRadio
    public static class BioSafetyDataModel {
        private BioSafetyDataModel() {
        }

        public List<String> getLabels() {
            List<String> labels = new ArrayList<>();
            for (SelenideElement element : BIOSAFETY_LABELS) {
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

        public BioSafetyDataModel clickRadioButton(String label) {
            BIOSAFETY_CLICKABLE_DIVS.get(getLabelIndex(label)).click();
            return this;
        }

        public String getSelectedLabel() {
            List<String> selected = new ArrayList<>();

            List<String> allLabels = getLabels();
            for (int i = 0; i < allLabels.size(); i++) {
                String label = allLabels.get(i);
                if (BIOSAFETY_INPUTS.get(i).isSelected()) {
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

        public SelenideElement getImage(String label) {
            return BIOSAFETY_IMAGES.get(getLabelIndex(label));
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
    public GHSDataModel getGHSDataModel() {
        return new GHSDataModel();
    }

    public SelenideElement getHStatementsInput() {
        return H_STATEMENTS_INPUT;
    }

    public SelenideElement getPStatementsInput() {
        return P_STATEMENTS_INPUT;
    }

    public boolean isRadioactiveSelected() {
        return RADIOACTIVE_CHECKBOX.isSelected();
    }

    public boolean isGMOSelected() {
        return GMO_CHECKBOX.isSelected();
    }

    public BioSafetyDataModel getBioSafetyDataModel() {
        return new BioSafetyDataModel();
    }

    public SelenideElement getCustomRemarksInput() {
        return CUSTOM_REMARKS_INPUT;
    }
}