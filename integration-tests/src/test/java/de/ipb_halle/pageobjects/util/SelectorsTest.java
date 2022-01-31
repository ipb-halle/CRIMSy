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

import static de.ipb_halle.pageobjects.util.Selectors.cssClasses;
import static de.ipb_halle.pageobjects.util.Selectors.elementWithCssClasses;
import static de.ipb_halle.pageobjects.util.Selectors.elementWithAttribute;
import static de.ipb_halle.pageobjects.util.Selectors.testId;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * @author flange
 */
public class SelectorsTest {
    @Test
    public void test_testId() {
        assertEquals("[data-test-id='myId']", testId("myId"));
        assertEquals("div[data-test-id='myId']", testId("div", "myId"));
    }

    @Test
    public void test_cssClasses() {
        assertEquals(".class1", cssClasses("class1"));
        assertEquals(".class1.class2", cssClasses("class1", "class2"));
    }

    @Test
    public void test_elementWithCssClasses() {
        assertEquals("div", elementWithCssClasses("div"));
        assertEquals("div.class1", elementWithCssClasses("div", "class1"));
        assertEquals("div.class1.class2",
                elementWithCssClasses("div", "class1", "class2"));
    }

    @Test
    public void test_elementWithAttribute() {
        assertEquals("div[myAttribute='value']",
                elementWithAttribute("div", "myAttribute", "value"));
    }
}