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

import static de.ipb_halle.test.I18nAssert.assertUIMessage;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import de.ipb_halle.pageobjects.components.growl.Growl;
import de.ipb_halle.pageobjects.components.growl.Severity;

/**
 * Assertion methods for {@link Growl}s.
 * 
 * @author flange
 */
public class GrowlAssert {
    private GrowlAssert() {
    }

    /**
     * Assert that the given {@link Growl} has the expected message and
     * severity.
     * 
     * @param expectedMessage
     * @param expectedSeverity
     * @param actual
     */
    public static void assertGrowl(String expectedMessage,
            Severity expectedSeverity, Growl actual) {
        assertEquals(expectedMessage, actual.getMessage());
        assertEquals(expectedSeverity, actual.getSeverity());
    }

    /**
     * Assert that the given {@link Growl} has a message that matches the entry
     * for the given key in the CRIMSy UI resource bundle and the given
     * severity.
     * 
     * @param expectedKey
     * @param locale
     * @param expectedSeverity
     * @param actual
     */
    public static void assertGrowlI18n(String expectedKey, Locale locale,
            Severity expectedSeverity, Growl actual) {
        assertUIMessage(actual.getMessage(), expectedKey, locale);
        assertEquals(expectedSeverity, actual.getSeverity());
    }
}