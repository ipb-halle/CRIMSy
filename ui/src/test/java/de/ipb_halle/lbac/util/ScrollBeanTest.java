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

import org.junit.Test;

/*
 * 
 */
public class ScrollBeanTest {
    @Test
    public void test001_scrollToCSS() {
        ScrollBean bean = new ScrollBean();
        assertEquals("$('.myClass').get()[0].scrollIntoView(true);",
                bean.scrollToCSS("myClass"));
        assertEquals("$('.myClass').get()[0].scrollIntoView(true);",
                bean.scrollToCSS("myClass", 0));
        assertEquals("$('.myClass').get()[0].scrollIntoView(true);window.scrollBy(0,42);",
                bean.scrollToCSS("myClass", 42));
    }
}