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
package de.ipb_halle.test;

import static de.ipb_halle.pageobjects.util.Selectors.elementWithAttribute;

import java.util.Locale;

import javax.annotation.Nonnull;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import com.codeborne.selenide.CheckResult;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Driver;
import de.ipb_halle.pageobjects.components.Severity;
import de.ipb_halle.test.conditions.JSFMessageCondition;
import de.ipb_halle.test.conditions.UIMessageCondition;

/**
 * Collection of useful conditions.
 * 
 * @author flange
 */
public class Conditions {
    private Conditions() {
    }

    public static Condition jsfMessage(String key, Locale locale) {
        return new JSFMessageCondition(key, locale);
    }

    public static Condition jsfMessage(String key) {
        return jsfMessage(key, Locale.ENGLISH);
    }

    public static Condition uiMessage(String key, Locale locale) {
        return new UIMessageCondition(key, locale);
    }

    public static Condition uiMessage(String key) {
        return uiMessage(key, Locale.ENGLISH);
    }

    /**
     * Check condition on a child. Code from
     * https://github.com/selenide/selenide/wiki/Custom-conditions#child
     * 
     * @author Olivier Grech (https://github.com/olivier-grech)
     * @param childCssSelector
     * @param condition
     * @return
     */
    private static Condition child(String childCssSelector, Condition condition) {
        return new Condition("child " + childCssSelector + " with " + condition.getName()) {
            @Nonnull
            @Override
            public CheckResult check(Driver driver, WebElement element) {
                WebElement child = element.findElement(By.cssSelector(childCssSelector));
                return condition.check(driver, child);
            }
        };
    }

    public static Condition growlI18nText(String key, Locale locale) {
        return child(elementWithAttribute("span", "data-notify", "message"), uiMessage(key, locale));
    }

    public static Condition growlI18nText(String key) {
        return growlI18nText(key, Locale.ENGLISH);
    }

    public static Condition growlSeverity(Severity severity) {
        return severity.getGrowlCondition();
    }
}