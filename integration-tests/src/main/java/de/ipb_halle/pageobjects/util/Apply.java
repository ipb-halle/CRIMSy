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

import de.ipb_halle.pageobjects.components.PrimeFacesSelectBooleanCheckbox;

/**
 * Apply actions to Selenide elements.
 * 
 * @author flange
 */
public class Apply {
    private Apply() {
    }

    /**
     * Applies {@code obj} to the given action if {@code obj} is not null.
     * 
     * @param obj
     * @param action
     */
    public static <T> void applyIfNotNull(T obj, Consumer<T> action) {
        if (obj != null) {
            action.accept(obj);
        }
    }

    /**
     * Sets the value if an input text field. The element will not be evaluated in
     * case {@code value} is null. Use empty strings to reset fields.
     * 
     * @param value
     * @param element
     */
    public static void applyValue(String value, SelenideElement element) {
        applyIfNotNull(value, (v) -> element.setValue(v));
    }

    /**
     * Selects an option in a select field. The element will not be evaluated in
     * case {@code option} is null.
     * 
     * @param option
     * @param element
     */
    public static void applySelection(String option, SelenideElement element) {
        applyIfNotNull(option, (o) -> element.selectOption(o));
    }

    /**
     * Sets the state of a checkbox. The element will not be evaluated in case
     * {@code state} is null.
     * 
     * @param state
     * @param element
     */
    public static void applyCheckbox(Boolean state, SelenideElement element) {
        if (state == null) {
            return;
        }
        // XOR
        if (state.booleanValue() ^ element.isSelected()) {
            element.click();
        }
    }

    /**
     * Sets the state of a checkbox. The element will not be evaluated in case
     * {@code state} is null.
     * 
     * @param state
     * @param checkbox
     */
    public static void applyCheckbox(Boolean state, PrimeFacesSelectBooleanCheckbox checkbox) {
        if (state == null) {
            return;
        }
        // XOR
        if (state.booleanValue() ^ checkbox.isSelected()) {
            checkbox.click();
        }
    }
}