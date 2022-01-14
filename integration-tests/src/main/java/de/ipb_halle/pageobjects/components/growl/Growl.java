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
package de.ipb_halle.pageobjects.components.growl;

import static com.codeborne.selenide.Selenide.$$;
import static de.ipb_halle.pageobjects.util.Selectors.elementWithAttribute;
import static de.ipb_halle.pageobjects.util.Selectors.elementWithCssClasses;

import java.util.ArrayList;
import java.util.List;

import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;

/**
 * Page object representing a BootsFaces growl.
 * 
 * @author flange
 */
public class Growl {
    /*
     * BootsFaces doesn't support JSF passthrough attributes in growls at the
     * moment. The CSS class is a workaround.
     */
    private static final ElementsCollection GROWL_DIVS = $$(
            elementWithCssClasses("div", "growlMessages"));

    private final String message;
    private final Severity severity;

    private Growl(Severity severity, String message) {
        this.message = message;
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public Severity getSeverity() {
        return severity;
    }

    /**
     * @return all currently visible growls
     */
    public static List<Growl> getGrowls() {
        List<Growl> growls = new ArrayList<>();

        for (SelenideElement div : GROWL_DIVS) {
            String message = extractMessage(div);
            Severity severity = extractSeverity(div);
            growls.add(new Growl(severity, message));
        }

        return growls;
    }

    private static String extractMessage(SelenideElement div) {
        return div.$(elementWithAttribute("span", "data-notify", "message"))
                .getText();
    }

    private static Severity extractSeverity(SelenideElement div) {
        for (Severity s : Severity.values()) {
            if (div.has(s.getGrowlCondition())) {
                return s;
            }
        }
        return null;
    }
}