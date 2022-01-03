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
package de.ipb_halle.testutil;

import static org.junit.Assert.assertTrue;

import java.util.Locale;

import de.ipb_halle.pageobjects.util.I18n;

/**
 * Assertion methods useful for writing tests for internationalization.
 * 
 * @author flange
 */
public class I18nAssert {
    private I18nAssert() {
    }

    /**
     * Asserts that a given test string matches the entry for the given
     * {@code key} in the JSF resource bundle. It also accounts for format
     * strings in the entry.
     * 
     * @param test   test string
     * @param key    JSF resource bundle key
     * @param locale
     */
    public static void assertJSFMessage(String test, String key,
            Locale locale) {
        assertTrue(I18n.isJSFMessage(test, key, locale));
    }

    /**
     * Asserts that a given test string matches the entry for the given
     * {@code key} in the CRIMSy UI resource bundle. It also accounts for format
     * strings in the entry.
     * 
     * @param test   test string
     * @param key    CRIMSy UI resource bundle key
     * @param locale
     */
    public static void assertUIMessage(String test, String key, Locale locale) {
        assertTrue(I18n.isUIMessage(test, key, locale));
    }
}