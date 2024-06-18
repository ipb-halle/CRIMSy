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
package de.ipb_halle.lbac.util.jsf;

import static de.ipb_halle.lbac.util.jsf.NumberFunctions.formatAmount;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.jupiter.api.Test;

public class NumberFunctionsTest {
    @Test
    public void test_formatAmount() {
        assertNull(formatAmount(null));

        assertEquals("10000.0", formatAmount(10000));
        assertEquals("10000.0", formatAmount(10000.0));
        assertEquals("10000.1", formatAmount(10000.1));
        assertEquals("10000.01", formatAmount(10000.01));
        assertEquals("10000.001", formatAmount(10000.001));
        assertEquals("10000.0", formatAmount(10000.0001));
        assertEquals("0.0", formatAmount(0.0));
        assertEquals("0.1", formatAmount(0.1));
        assertEquals("0.01", formatAmount(0.01));
        assertEquals("0.001", formatAmount(0.001));
        assertEquals("0.0", formatAmount(0.0001));
    }
}
