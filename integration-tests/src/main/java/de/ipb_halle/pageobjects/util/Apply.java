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
package de.ipb_halle.pageobjects.util;

import java.util.function.Consumer;

import com.codeborne.selenide.SelenideElement;

/**
 * Apply actions to Selenide elements.
 * 
 * @author flange
 */
public class Apply {
    private Apply() {
    }

    /**
     * Apply an action to a Selenide element.
     * 
     * @param element
     * @param action
     */
    public static void apply(SelenideElement element, Consumer<SelenideElement> action) {
        action.accept(element);
    }

    /**
     * Sets the value if an input text field. The element will not be evaluated in
     * case {@code value} is null. Use empty strings to reset fields.
     * 
     * @param value
     * @param element
     */
    public static void applyValue(String value, SelenideElement element) {
        if (value != null) {
            apply(element, e -> e.setValue(value));
        }
    }

    /**
     * Selects an option in a select field. The element will not be evaluated in
     * case {@code option} is null.
     * 
     * @param option
     * @param element
     */
    public static void applySelection(String option, SelenideElement element) {
        if (option != null) {
            apply(element, e -> e.selectOption(option));
        }
    }
}