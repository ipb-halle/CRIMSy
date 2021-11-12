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

import static de.ipb_halle.lbac.material.sequence.SequenceType.PROTEIN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * 
 * @author flange
 */
public class SequenceDataTest {
    @Test
    public void test001_builderDefaults() {
        SequenceData data = SequenceData.builder().build();

        assertNull(data.getSequenceString());
        assertNull(data.getSequenceType());
        assertNull(data.isCircular());
        assertNull(data.getAnnotations());
    }

    @Test
    public void test002_builderAndGetters() {
        SequenceData data = SequenceData.builder()
                .sequenceString("abc")
                .sequenceType(PROTEIN)
                .circular(true)
                .annotations("def")
                .build();

        assertEquals("abc", data.getSequenceString());
        assertEquals(PROTEIN, data.getSequenceType());
        assertTrue(data.isCircular());
        assertEquals("def", data.getAnnotations());
    }

    @Test
    public void test003_copyBuilder() {
        SequenceData data1 = SequenceData.builder()
                .sequenceString("abc")
                .sequenceType(PROTEIN)
                .circular(true)
                .annotations("def")
                .build();

        SequenceData data2 = SequenceData.builder(data1).build();

        assertEquals("abc", data2.getSequenceString());
        assertEquals(PROTEIN, data2.getSequenceType());
        assertTrue(data2.isCircular());
        assertEquals("def", data2.getAnnotations());
    }

    @Test
    public void test004_getSequenceLength() {
        SequenceData data = SequenceData.builder().build();

        assertNull(data.getSequenceLength());

        data = SequenceData.builder().sequenceString("1234567").build();
        assertEquals(7, data.getSequenceLength().intValue());
    }

    @Test
    public void test005_equalsAndHashCode() {
        SequenceData data1, data2;

        data1 = SequenceData.builder().build();
        data2 = SequenceData.builder().build();
        assertEquals(data1, data2);
        assertEquals(data2, data1);
        assertEquals(data1.hashCode(), data2.hashCode());

        data1 = SequenceData.builder()
                .sequenceString("abc")
                .sequenceType(PROTEIN)
                .circular(true)
                .annotations("def")
                .build();
        data2 = SequenceData.builder()
                .sequenceString("abc")
                .sequenceType(PROTEIN)
                .circular(true)
                .annotations("def")
                .build();
        assertEquals(data1, data2);
        assertEquals(data2, data1);
        assertEquals(data1.hashCode(), data2.hashCode());

        data2 = SequenceData.builder().build();
        assertNotEquals(data1, data2);
        assertNotEquals(data2, data1);
        assertNotEquals(data1.hashCode(), data2.hashCode());
    }
}