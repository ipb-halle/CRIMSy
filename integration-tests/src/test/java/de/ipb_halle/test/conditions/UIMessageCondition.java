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
package de.ipb_halle.test.conditions;

import java.util.Locale;

import com.codeborne.selenide.conditions.TextCondition;

import de.ipb_halle.pageobjects.util.I18n;

/**
 * Condition that checks if the test string matches the given entry in the
 * CRIMSy UI resource bundle. It also accounts for format strings in the entry.
 * 
 * @author flange
 */
public class UIMessageCondition extends TextCondition {
    private Locale locale;
    private String key;

    public UIMessageCondition(String key, Locale locale) {
        super("UI message", I18n.getUIMessage(key, locale));
        this.locale = locale;
        this.key = key;
    }

    @Override
    protected boolean match(String test, String expectedText) {
        return I18n.isUIMessage(test, key, locale);
    }
}