/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2020 Leibniz-Institut f. Pflanzenbiochemie
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

import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;

/**
 * This class will provide some test cases for the NonEmpty class 
 */
public class NonEmptyTest {

    @Test
    public void testNonEmtpy() {
        assertNull("null", NonEmpty.nullOrNonEmpty(null));
        assertNull("empty String", NonEmpty.nullOrNonEmpty(""));
        assertEquals("non-emptyString", "non-empty", NonEmpty.nullOrNonEmpty("non-empty"));

    }

    @Test
    public void testNonZero() {
        assertNull("null", NonEmpty.nullOrNonZero(null));
        assertNull("empty String", NonEmpty.nullOrNonZero(""));
        assertNull("zero", NonEmpty.nullOrNonZero("0"));
        assertEquals("non-zero", Integer.valueOf(1), NonEmpty.nullOrNonZero("1"));
    }
}
