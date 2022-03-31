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
import static de.ipb_halle.pageobjects.util.Selectors.testId;

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