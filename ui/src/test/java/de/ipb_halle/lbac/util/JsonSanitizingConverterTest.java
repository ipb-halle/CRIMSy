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
package de.ipb_halle.lbac.util;

import static org.junit.Assert.*;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import org.junit.jupiter.api.Test;

/**
 * Tests for the FacesConverter {@code jsonSanitizingConverter}.
 * 
 * @author flange
 */
public class JsonSanitizingConverterTest {
    JsonSanitizingConverter converter = new JsonSanitizingConverter();

    /*
     * Safe to use null values here, because JsonSanitizingConverter doesn't use
     * the faces context or the component.
     */
    FacesContext context = null;
    UIComponent component = null;

    String goodJson = "{\"array\":[1,\"text\",3],\"field\":\"another text\"}";

    @Test
    public void testGetAsObject() {
        assertNull(converter.getAsObject(context, component, null));
        assertEquals("null", converter.getAsObject(context, component, ""));

        assertEquals(goodJson,
                converter.getAsObject(context, component, goodJson));

        /*
         * We could repeat the tests from
         * https://github.com/OWASP/json-sanitizer/blob/master/src/test/java/com/google/json/JsonSanitizerTest.java
         * here ...
         */
        assertEquals("[1,null,3]",
                converter.getAsObject(context, component, "[1,,3,]"));
    }

    @Test
    public void testGetAsString() {
        assertEquals("", converter.getAsString(context, component, null));
        assertEquals("", converter.getAsString(context, component, ""));
        assertEquals("",
                converter.getAsString(context, component, new Object()));

        assertEquals(goodJson,
                converter.getAsString(context, component, goodJson));
        assertEquals("[1,null,3]",
                converter.getAsString(context, component, "[1,,3,]"));
    }

    @Test
    public void testInAndOut() {
        assertEquals(goodJson, converter.getAsString(context, component,
                converter.getAsObject(context, component, goodJson)));
        assertEquals("[1,null,3]", converter.getAsString(context, component,
                converter.getAsObject(context, component, "[1,,3,]")));
    }

    @Test
    public void testOutAndIn() {
        assertEquals(goodJson, converter.getAsObject(context, component,
                converter.getAsString(context, component, goodJson)));

        assertEquals("[1,null,3]", converter.getAsObject(context, component,
                converter.getAsString(context, component, "[1,,3,]")));
    }
}