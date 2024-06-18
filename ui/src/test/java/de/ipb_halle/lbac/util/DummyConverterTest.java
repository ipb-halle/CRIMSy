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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;

import org.junit.jupiter.api.Test;

/**
 * 
 * @author flange
 */
public class DummyConverterTest {
    private UIComponent comp = null;
    private FacesContext fc = null;
    private DummyConverter conv = new DummyConverter();

    @Test
    public void test001_getAsObject() {
        // method contract
        assertNull(conv.getAsObject(fc, comp, null));

        assertEquals("", conv.getAsObject(fc, comp, ""));
        assertEquals("abc", conv.getAsObject(fc, comp, "abc"));
    }

    @Test
    public void test002_getAsString() {
        // method contract
        assertEquals("", conv.getAsString(fc, comp, null));

        assertEquals("", conv.getAsString(fc, comp, ""));
        assertEquals("abc", conv.getAsString(fc, comp, "abc"));
    }
}
