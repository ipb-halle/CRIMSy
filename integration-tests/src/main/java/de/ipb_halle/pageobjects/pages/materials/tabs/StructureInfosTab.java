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

import de.ipb_halle.pageobjects.components.MolecularFacesMolecule;
import de.ipb_halle.pageobjects.pages.AbstractPage;

/**
 * Page object for
 * /ui/web/WEB-INF/templates/material/components/structures.xhtml
 * 
 * @author flange
 */
public class StructureInfosTab extends AbstractPage<StructureInfosTab> implements MaterialEditTab {
    private static final MolecularFacesMolecule MOL_EDITOR = new MolecularFacesMolecule("structureInfosTab:molEditor",
            "structurePlugin");
    private static final SelenideElement AUTO_CALC_CHECKBOX = $(testId("input", "structureInfosTab:autoCalc"));
    private static final SelenideElement SUM_FORMULA_INPUT = $(testId("input", "structureInfosTab:sumFormula"));
    private static final SelenideElement AVERAGE_MOLAR_MASS_INPUT = $(testId("input", "structureInfosTab:averageMolarMass"));
    private static final SelenideElement EXACT_MOLAR_MASS_INPUT = $(testId("input", "structureInfosTab:exactMolarMass"));

    /*
     * Getters
     */
    public MolecularFacesMolecule molEditor() {
        return MOL_EDITOR;
    }

    public SelenideElement autoCalcCheckbox() {
        return AUTO_CALC_CHECKBOX;
    }

    public SelenideElement sumFormulaInput() {
        return SUM_FORMULA_INPUT;
    }

    public SelenideElement averageMolarMassInput() {
        return AVERAGE_MOLAR_MASS_INPUT;
    }

    public SelenideElement exactMolarMassInput() {
        return EXACT_MOLAR_MASS_INPUT;
    }
}