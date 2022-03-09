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

import static com.codeborne.selenide.Condition.attribute;
import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.executeJavaScript;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

/**
 * Page object abstraction for MolecularFaces' openVectorEditor component.
 * 
 * @author flange
 */
public class MolecularFacesOpenVectorEditor {
    private final SelenideElement outerDiv;
    private final SelenideElement inputElement;
    private final String widgetVar;

    public MolecularFacesOpenVectorEditor(String testId, String widgetVar) {
        outerDiv = $(testId("div", testId));
        inputElement = $(testId("input", testId));
        this.widgetVar = widgetVar;
    }

    public String getSequence() {
        String js = String.format("%s.then(plugin => plugin.getSequence());", widgetVar);
        return (String) executeJavaScript(js);
    }

    public void setSequence(String sequenceJson) {
        String js = String.format("%s.then(plugin => plugin.setSequence(\"%s\"));", widgetVar, sequenceJson);
        executeJavaScript(js);
    }

    /**
     * Can be used to determine the rendering of the JSF component. e.g. via
     * {@code outerDiv().should(exist)}.
     */
    public SelenideElement outerDiv() {
        return outerDiv;
    }

    /*
     * Fluent assertions
     */
    public MolecularFacesOpenVectorEditor sequenceShouldBe(String sequence) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    public MolecularFacesOpenVectorEditor shouldBeReadonly() {
        inputElement.shouldNotHave(attribute("name"));
        return this;
    }

    public MolecularFacesOpenVectorEditor shouldNotBeReadonly() {
        inputElement.shouldHave(attribute("name"));
        return this;
    }
}