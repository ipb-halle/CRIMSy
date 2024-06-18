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
package de.ipb_halle.lbac.material.sequence.util;

import static de.ipb_halle.lbac.material.sequence.util.FastaFileFormat.generateFastaString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.sequence.Sequence;
import de.ipb_halle.lbac.material.sequence.SequenceData;

/**
 * @author flange
 */
public class FastaFileFormatTest {
    @Test
    public void test_generateFastaStringWithDescriptionAndSequence() {
        assertThrows(IllegalArgumentException.class, () -> generateFastaString(null, null));
        assertThrows(IllegalArgumentException.class, () -> generateFastaString("", null));
        assertThrows(IllegalArgumentException.class, () -> generateFastaString("abc", null));
        assertThrows(IllegalArgumentException.class, () -> generateFastaString("abc", ""));

        assertEquals(">desc\nTGA\nATG", generateFastaString("desc", "TGA\nATG"));
    }

    @Test
    public void test_generateFastaStringWithSequence() {
        Sequence sequence;
        List<MaterialName> names = Arrays.asList(new MaterialName("firstName", "en", 1),
                new MaterialName("secondName", "de", 100));
        String expected;

        /*
         * This will fail as soon as Material.getId() returns an Integer instead of an
         * int. >:)
         */
        assertThrows(NullPointerException.class, () -> generateFastaString(buildSequence(null, names, "abc")));

        sequence = null;
        assertEquals("", generateFastaString(sequence));
        sequence = buildSequence(42, names, null);
        assertEquals("", generateFastaString(sequence));
        sequence = buildSequence(42, names, "");
        assertEquals("", generateFastaString(sequence));
        assertEquals("", generateFastaString(new Sequence(42, names, null, null, null, null)));

        sequence = buildSequence(42, names, "TGA\nATG");
        expected = ">42 firstName\nTGA\nATG";
        assertEquals(expected, generateFastaString(sequence));

        sequence = buildSequence(42, null, "TGA\nATG");
        expected = ">42\nTGA\nATG";
        assertEquals(expected, generateFastaString(sequence));

        sequence = buildSequence(42, new ArrayList<>(), "TGA\nATG");
        expected = ">42\nTGA\nATG";
        assertEquals(expected, generateFastaString(sequence));
    }

    @Test
    public void test_generateFastaStringWithSequences() {
        Sequence sequence1 = buildSequence(1, Arrays.asList(new MaterialName("firstSequence", "en", 1)), "TGA\nATG");
        Sequence sequence2 = buildSequence(2, Arrays.asList(new MaterialName("secondSequence", "de", 1)),
                "MFGA\nRQH\nTNK");
        Sequence sequence3 = buildSequence(3, null, "unknown sequence");

        List<Sequence> sequences = Arrays.asList(sequence1, sequence2, sequence3);
        String expected = ">1 firstSequence\nTGA\nATG"
                + "\n>2 secondSequence\nMFGA\nRQH\nTNK"
                + "\n>3\nunknown sequence";
        assertEquals(expected, generateFastaString(sequences));

        sequences = Arrays.asList(null, null, sequence1, null, sequence2, null, sequence3, null);
        expected = ">1 firstSequence\nTGA\nATG"
                + "\n>2 secondSequence\nMFGA\nRQH\nTNK"
                + "\n>3\nunknown sequence";
        assertEquals(expected, generateFastaString(sequences));

        sequences = new ArrayList<>();
        assertEquals("", generateFastaString(sequences));

        sequences = null;
        assertEquals("", generateFastaString(sequences));
    }

    private Sequence buildSequence(Integer id, List<MaterialName> names, String sequence) {
        SequenceData data = SequenceData.builder().sequenceString(sequence).build();
        return new Sequence(id, names, null, null, null, data);
    }
}
