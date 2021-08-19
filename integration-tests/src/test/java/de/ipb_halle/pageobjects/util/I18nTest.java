/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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

import static org.junit.Assert.*;
import static de.ipb_halle.pageobjects.util.I18n.*;
import static java.util.Locale.*;

import org.junit.Test;

/**
 * 
 * @author flange
 */
public class I18nTest {
    @Test
    public void test001_getJSFMessage() {
        assertEquals("{0}: Validation Error: Value is required.",
                getJSFMessage(REQUIRED_VALIDATION_ERROR_KEY, ENGLISH));
        assertEquals("{0}: Validierungsfehler: Eingabe erforderlich.",
                getJSFMessage(REQUIRED_VALIDATION_ERROR_KEY, GERMAN));
        assertNull(getJSFMessage("abcdef_nonexistingkey", ENGLISH));
    }

    @Test
    public void test002_isJSFMessage() {
        assertTrue(
                isJSFMessage("Password: Validation Error: Value is required.",
                        REQUIRED_VALIDATION_ERROR_KEY, ENGLISH));
        assertTrue(isJSFMessage(
                "Passwort: Validierungsfehler: Eingabe erforderlich.",
                REQUIRED_VALIDATION_ERROR_KEY, GERMAN));
        assertFalse(
                isJSFMessage("Password: Validation failed: Value is required.",
                        REQUIRED_VALIDATION_ERROR_KEY, ENGLISH));
    }

    @Test
    public void test003_getUIMessage() {
        assertEquals("Save", getUIMessage("Save", ENGLISH));
        assertEquals("Speichern", getUIMessage("Save", GERMAN));
        assertNull(getUIMessage("abcdef non-existing key", ENGLISH));
    }

    @Test
    public void test004_isUIMessage() {
        assertTrue(isUIMessage("Save", "Save", ENGLISH));
        assertTrue(isUIMessage("Speichern", "Save", GERMAN));
        assertFalse(isUIMessage("abcdef is no valid value", "Save", ENGLISH));
    }
}