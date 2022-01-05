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
package de.ipb_halle.pageobjects.plugins;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.executeJavaScript;
import static de.ipb_halle.pageobjects.util.Selectors.testId;

import com.codeborne.selenide.SelenideElement;

/**
 * Page object for MolecularFaces' molecule component.
 * 
 * @author flange
 */
public class MolPlugin {
    private final SelenideElement inputElement;
    private final String widgetVar;

    public MolPlugin(String testId, String widgetVar) {
        this.inputElement = $(testId("input", testId));
        this.widgetVar = widgetVar;
    }

    public boolean isReadonly() {
        /*
         * The trick here is that the <input> element has no name attribute when
         * the component is readonly.
         */
        return inputElement.attr("name") == null;
    }

    public String getMolecule() {
        String js = String.format("%s.then(plugin => plugin.getMolecule());",
                widgetVar);
        return (String) executeJavaScript(js);
    }

    /**
     * @param molfile unescaped V2000 Molfile
     */
    public void setMolecule(String molfile) {
        String js = String.format(
                "%s.then(plugin => plugin.setMolecule(\"%s\"));", widgetVar,
                escape(molfile));
        executeJavaScript(js);
    }

    private String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "");
    }
}