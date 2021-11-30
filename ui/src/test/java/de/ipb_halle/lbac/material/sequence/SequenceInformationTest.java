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

import static de.ipb_halle.lbac.material.sequence.SequenceType.DNA;
import static de.ipb_halle.lbac.material.sequence.SequenceType.PROTEIN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class SequenceInformationTest {
    private SequenceInformation seqInfo;

    @Before
    public void init() {
        seqInfo = new SequenceInformation();
    }

    @Test
    public void test001_actionSelectSequenceType() {
        seqInfo.setSequenceType(null);
        seqInfo.actionSelectSequenceType();
        SequenceData expected = SequenceData.builder().build();
        assertEquals(expected, seqInfo.getSequenceData());

        seqInfo.setSequenceType(DNA);
        seqInfo.actionSelectSequenceType();
        expected = SequenceData.builder().sequenceType(DNA).build();
        assertEquals(expected, seqInfo.getSequenceData());
    }

    @Test
    public void test002_isSequenceTypeSelected() {
        seqInfo.setSequenceType(null);
        assertFalse(seqInfo.isSequenceTypeSelected());

        seqInfo.setSequenceType(DNA);
        assertTrue(seqInfo.isSequenceTypeSelected());
    }

    @Test
    public void test003_getPossibleSequenceTypes() {
        assertEquals(SequenceType.values().length, seqInfo.getPossibleSequenceTypes().size());
    }

    @Test
    public void test004_defaultsGettersAndSetters() {
        assertNull(seqInfo.getSequenceType());
        assertEquals(SequenceData.builder().build(), seqInfo.getSequenceData());

        seqInfo.setSequenceType(DNA);
        assertEquals(DNA, seqInfo.getSequenceType());
        assertEquals(SequenceData.builder().build(), seqInfo.getSequenceData());

        SequenceData data = SequenceData.builder()
                .sequenceString("AAA")
                .circular(true)
                .sequenceType(PROTEIN)
                .annotations("annotations")
                .build();
        seqInfo.setSequenceData(data);
        assertEquals(PROTEIN, seqInfo.getSequenceType());
        assertEquals(data, seqInfo.getSequenceData());
    }
}
