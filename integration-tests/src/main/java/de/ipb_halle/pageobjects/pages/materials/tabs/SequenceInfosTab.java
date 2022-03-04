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

import java.util.List;

import com.codeborne.selenide.SelenideElement;

import de.ipb_halle.pageobjects.components.MolecularFacesOpenVectorEditor;
import de.ipb_halle.pageobjects.components.PrimeFacesSelectOneRadio;
import de.ipb_halle.pageobjects.pages.AbstractPage;

/**
 * Page object for /ui/web/WEB-INF/templates/material/components/sequences.xhtml
 * 
 * @author flange
 */
public class SequenceInfosTab extends AbstractPage<SequenceInfosTab> implements MaterialEditTab {
    private static final PrimeFacesSelectOneRadio SEQUENCE_TYPE_RADIO = new PrimeFacesSelectOneRadio(
            "sequenceInfosTab:sequenceType");
    private static final MolecularFacesOpenVectorEditor SEQUENCE_EDITOR = new MolecularFacesOpenVectorEditor(
            "sequenceInfosTab:sequenceEditor", "sequenceEditorVar");
    private static final SelenideElement SEQUENCE_LENGTH_INPUT = $(
            testId("input", "sequenceInfosTab:sequenceLength"));
    private static final SelenideElement SEQUENCE_STRING_INPUT = $(
            testId("input", "sequenceInfosTab:sequenceString"));

    /*
     * Actions
     */
    public SequenceInfosTab selectSequenceType(String type) {
        SEQUENCE_TYPE_RADIO.clickRadioButton(type);
        return this;
    }

    /*
     * Getters
     */
    public List<String> getAllSelectableSequenceTypes() {
        return SEQUENCE_TYPE_RADIO.getLabels();
    }

    public String getSelectedSequenceType() {
        return SEQUENCE_TYPE_RADIO.getSelectedLabel();
    }

    public MolecularFacesOpenVectorEditor getSequenceEditor() {
        return SEQUENCE_EDITOR;
    }

    public SelenideElement getSequenceLengthInput() {
        return SEQUENCE_LENGTH_INPUT;
    }

    public SelenideElement getSequenceStringInput() {
        return SEQUENCE_STRING_INPUT;
    }
}