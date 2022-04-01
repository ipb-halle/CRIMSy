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

import static com.codeborne.selenide.Condition.exactValue;
import static com.codeborne.selenide.Selenide.$;
import static de.ipb_halle.pageobjects.util.Apply.applyCheckbox;
import static de.ipb_halle.pageobjects.util.Apply.applyIfNotNull;
import static de.ipb_halle.pageobjects.util.Apply.applyValue;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.components.PrimeFacesSelectBooleanCheckbox;
import de.ipb_halle.pageobjects.pages.AbstractPage;
import de.ipb_halle.pageobjects.pages.materials.models.HazardsModel;

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
    /**
     * Applies the hazards model.
     * <p>
     * Convention: The input element will not be evaluated in case the model field
     * is null. Use empty strings to reset fields.
     * 
     * @param model
     * @return this
     */
    public HazardsTab applyModel(HazardsModel model) {
        applyIfNotNull(model.getGhsModel(), (m) -> ghsData().applyModel(m));
        applyValue(model.gethStatements(), H_STATEMENTS_INPUT);
        applyValue(model.getpStatements(), P_STATEMENTS_INPUT);
        applyCheckbox(model.getRadioactive(), RADIOACTIVE_CHECKBOX);
        applyIfNotNull(model.getBioSafetyLevel(), (l) -> bioSafetyData().select(l));
        applyCheckbox(model.getGmo(), GMO_CHECKBOX);
        applyValue(model.getCustomRemarks(), CUSTOM_REMARKS_INPUT);

        return this;
    }

    public HazardsTab clickRadioactiveCheckbox() {
        RADIOACTIVE_CHECKBOX.click();
        return this;
    }

    public HazardsTab clickGMOCheckbox() {
        GMO_CHECKBOX.click();
        return this;
    }

    /*
     * Fluent assertions
     */
    public HazardsTab shouldHave(HazardsModel model) {
        applyIfNotNull(model.getGhsModel(), (m) -> ghsData().shouldHave(m));
        applyIfNotNull(model.gethStatements(), (statement) -> H_STATEMENTS_INPUT.shouldHave(exactValue(statement)));
        applyIfNotNull(model.getpStatements(), (statement) -> P_STATEMENTS_INPUT.shouldHave(exactValue(statement)));
        applyIfNotNull(model.getRadioactive(), (selected) -> RADIOACTIVE_CHECKBOX.shouldBe(selected));
        applyIfNotNull(model.getBioSafetyLevel(), (level) -> bioSafetyData().shouldHave(level));
        applyIfNotNull(model.getGmo(), (selected) -> GMO_CHECKBOX.shouldBe(selected));
        applyIfNotNull(model.getCustomRemarks(), (remark) -> CUSTOM_REMARKS_INPUT.shouldHave(exactValue(remark)));

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