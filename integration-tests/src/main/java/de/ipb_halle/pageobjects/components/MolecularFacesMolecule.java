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

import de.ipb_halle.pageobjects.util.conditions.MolfileMatchesCondition;

/**
 * Page object abstraction for MolecularFaces' molecule component.
 * 
 * @author flange
 */
public class MolecularFacesMolecule {
    private final SelenideElement inputElement;
    private final String widgetVar;

    public MolecularFacesMolecule(String testId, String widgetVar) {
        this.inputElement = $(testId("input", testId));
        this.widgetVar = widgetVar;
    }

    public String getMolecule() {
        String js = String.format("return %s.then(plugin => plugin.getMolecule());", widgetVar);
        return (String) executeJavaScript(js);
    }

    /**
     * @param molfile unescaped V2000 Molfile
     */
    public void setMolecule(String molfile) {
        String js = String.format("%s.then(plugin => plugin.setMolecule(\"%s\"));", widgetVar, escape(molfile));
        executeJavaScript(js);
    }

    private String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "");
    }

    /*
     * Fluent assertions
     */
    /**
     * @param molfile unescaped V2000 Molfile
     * @return this
     */
    public MolecularFacesMolecule moleculeShouldBe(String molfile) {
        inputElement.shouldHave(new MolfileMatchesCondition("value", molfile));
        return this;
    }

    public MolecularFacesMolecule shouldBeReadonly() {
        inputElement.shouldNotHave(attribute("name"));
        return this;
    }

    public MolecularFacesMolecule shouldNotBeReadonly() {
        inputElement.shouldHave(attribute("name"));
        return this;
    }

    public MolecularFacesMolecule pluginTypeShouldBe(String type) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}