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

import static de.ipb_halle.pageobjects.util.I18n.JSF_REQUIRED_VALIDATION_ERROR_KEY;
import static de.ipb_halle.pageobjects.util.I18n.getJSFMessage;
import static de.ipb_halle.pageobjects.util.I18n.getUIMessage;
import static de.ipb_halle.pageobjects.util.I18n.isJSFMessage;
import static de.ipb_halle.pageobjects.util.I18n.isUIMessage;
import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * @author flange
 */
public class I18nTest {
    private static final String nonExistingKey = "abcdef_nonexistingkey";

    @Test
    public void test001_getJSFMessage() {
        assertEquals("{0}: Validation Error: Value is required.", getJSFMessage(JSF_REQUIRED_VALIDATION_ERROR_KEY, ENGLISH));
        assertEquals("{0}: Validierungsfehler: Eingabe erforderlich.", getJSFMessage(JSF_REQUIRED_VALIDATION_ERROR_KEY, GERMAN));
        assertNull(getJSFMessage(nonExistingKey, ENGLISH));
        assertNull(getJSFMessage(null, ENGLISH));
        assertThrows(NullPointerException.class, () -> getJSFMessage(JSF_REQUIRED_VALIDATION_ERROR_KEY, null));
    }

    @Test
    public void test002_isJSFMessage() {
        assertTrue(isJSFMessage("Password: Validation Error: Value is required.", JSF_REQUIRED_VALIDATION_ERROR_KEY, ENGLISH));
        assertTrue(isJSFMessage("Passwort: Validierungsfehler: Eingabe erforderlich.", JSF_REQUIRED_VALIDATION_ERROR_KEY, GERMAN));
        assertFalse(isJSFMessage("Password: Validation failed: Value is required.", JSF_REQUIRED_VALIDATION_ERROR_KEY, ENGLISH));
        assertFalse(isJSFMessage("", JSF_REQUIRED_VALIDATION_ERROR_KEY, ENGLISH));
        assertFalse(isJSFMessage("Password: Validation failed: Value is required.", nonExistingKey, ENGLISH));
        assertFalse(isJSFMessage(null, JSF_REQUIRED_VALIDATION_ERROR_KEY, ENGLISH));
        assertFalse(isJSFMessage("Password: Validation failed: Value is required.", null, ENGLISH));
        assertThrows(NullPointerException.class, () -> isJSFMessage("Password: Validation Error: Value is required.", JSF_REQUIRED_VALIDATION_ERROR_KEY, null));
    }

    @Test
    public void test003_getUIMessage() {
        assertEquals("Save", getUIMessage("Save", ENGLISH));
        assertEquals("Speichern", getUIMessage("Save", GERMAN));
        assertNull(getUIMessage(nonExistingKey, ENGLISH));
        assertNull(getUIMessage(null, ENGLISH));
        assertThrows(NullPointerException.class, () -> getUIMessage("Save", null));
    }

    @Test
    public void test004_isUIMessage() {
        assertTrue(isUIMessage("Save", "Save", ENGLISH));
        assertTrue(isUIMessage("Speichern", "Save", GERMAN));
        assertFalse(isUIMessage("abcdef is no valid value", "Save", ENGLISH));
        assertFalse(isUIMessage("", "Save", ENGLISH));
        assertFalse(isUIMessage("Save", nonExistingKey, ENGLISH));
        assertFalse(isUIMessage(null, "Save", ENGLISH));
        assertFalse(isUIMessage("Save", null, ENGLISH));
        assertThrows(NullPointerException.class, () -> isUIMessage("Save", "Save", null));
    }
}