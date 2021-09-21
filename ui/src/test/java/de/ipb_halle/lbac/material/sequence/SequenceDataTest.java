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
package de.ipb_halle.lbac.material.sequence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 
 * @author flange
 */
public class SequenceDataTest {
    @Test
    public void test001_defaultsAndGettersAndSetters() {
        SequenceData data = new SequenceData();

        assertNull(data.getSequenceString());
        data.setSequenceString("abc");
        assertEquals("abc", data.getSequenceString());

        assertNull(data.getSequenceType());
        data.setSequenceType(SequenceType.PROTEIN);
        assertEquals(SequenceType.PROTEIN, data.getSequenceType());

        assertFalse(data.isCircular());
        data.setCircular(true);
        assertTrue(data.isCircular());

        assertEquals("", data.getAnnotations());
        data.setAnnotations("def");
        assertEquals("def", data.getAnnotations());
    }

    @Test
    public void test002_getSequenceLength() {
        SequenceData data = new SequenceData();

        data.setSequenceString(null);
        assertNull(data.getSequenceLength());

        data.setSequenceString("1234567");
        assertEquals(7, data.getSequenceLength().intValue());
    }
}