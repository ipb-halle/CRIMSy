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

import static com.codeborne.selenide.Selenide.$;
import static de.ipb_halle.pageobjects.util.Selectors.elementWithCssClasses;
import com.codeborne.selenide.SelenideElement;

/**
 * Page object abstraction for PrimeFaces' dialog component.
 * 
 * @author flange
 */
public class PrimeFacesDialog {
    private static final SelenideElement TITLE = $(
            elementWithCssClasses("span", "ui-dialog-title"));
    private static final SelenideElement CANCEL_BUTTON = $(
            elementWithCssClasses("a", "ui-dialog-titlebar-close"));

    /*
     * Actions
     */
    public void cancel() {
        CANCEL_BUTTON.click();
    }

    /*
     * Getters
     */
    public String getTitle() {
        return TITLE.text();
    }
}