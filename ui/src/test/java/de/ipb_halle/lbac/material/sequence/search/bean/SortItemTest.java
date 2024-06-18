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
package de.ipb_halle.lbac.material.sequence.search.bean;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.ipb_halle.fasta_search_service.models.fastaresult.FastaResult;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.sequence.Sequence;
import de.ipb_halle.lbac.material.sequence.search.display.FastaResultDisplayWrapper;

/**
 * @author flange
 */
public class SortItemTest {
    private FastaResultDisplayWrapper wrapper1, wrapper2, wrapper3;
    private List<FastaResultDisplayWrapper> wrappers;

    @BeforeEach
    public void init() {
        List<MaterialName> names1 = Arrays.asList(new MaterialName("firstName1", "en", 1),
                new MaterialName("secondName1", "de", 100));
        List<MaterialName> names2 = Arrays.asList(new MaterialName("firstName2", "en", 1),
                new MaterialName("secondName2", "de", 100));
        List<MaterialName> names3 = Arrays.asList(new MaterialName("firstName3", "en", 1),
                new MaterialName("secondName3", "de", 100));

        Sequence sequence1 = new Sequence(1, names1, 1, null, null, null);
        Sequence sequence2 = new Sequence(2, names2, 1, null, null, null);
        Sequence sequence3 = new Sequence(3, names3, 1, null, null, null);

        FastaResult fastaResult1 = new FastaResult();
        FastaResult fastaResult2 = new FastaResult();
        FastaResult fastaResult3 = new FastaResult();

        fastaResult1.setSubjectSequenceLength(1);
        fastaResult2.setSubjectSequenceLength(2);
        fastaResult3.setSubjectSequenceLength(3);
        fastaResult1.setBitScore(1);
        fastaResult2.setBitScore(2);
        fastaResult3.setBitScore(3);
        fastaResult1.setExpectationValue(1.0);
        fastaResult2.setExpectationValue(2.0);
        fastaResult3.setExpectationValue(3.0);
        fastaResult1.setSmithWatermanScore(1);
        fastaResult2.setSmithWatermanScore(2);
        fastaResult3.setSmithWatermanScore(3);
        fastaResult1.setIdentity(0.1);
        fastaResult2.setIdentity(0.2);
        fastaResult3.setIdentity(0.3);
        fastaResult1.setSimilarity(0.1);
        fastaResult2.setSimilarity(0.2);
        fastaResult3.setSimilarity(0.3);

        wrapper1 = new FastaResultDisplayWrapper(sequence1, fastaResult1);
        wrapper2 = new FastaResultDisplayWrapper(sequence2, fastaResult2);
        wrapper3 = new FastaResultDisplayWrapper(sequence3, fastaResult3);

        wrappers = Arrays.asList(wrapper2, wrapper1, wrapper3);
    }

    @Test
    public void test_SUBJECTNAME_Comparator() {
        wrappers.sort(SortItem.SUBJECTNAME.getComparator());
        // alphabetical order
        assertEquals(Arrays.asList(wrapper1, wrapper2, wrapper3), wrappers);
    }

    @Test
    public void test_LENGTH_Comparator() {
        wrappers.sort(SortItem.LENGTH.getComparator());
        // greater wins
        assertEquals(Arrays.asList(wrapper3, wrapper2, wrapper1), wrappers);
    }

    @Test
    public void test_BITSCORE_Comparator() {
        wrappers.sort(SortItem.BITSCORE.getComparator());
        // greater wins
        assertEquals(Arrays.asList(wrapper3, wrapper2, wrapper1), wrappers);
    }

    @Test
    public void test_EVALUE_Comparator() {
        wrappers.sort(SortItem.EVALUE.getComparator());
        // lesser wins
        assertEquals(Arrays.asList(wrapper1, wrapper2, wrapper3), wrappers);
    }

    @Test
    public void test_SMITHWATERMANSCORE_Comparator() {
        wrappers.sort(SortItem.SMITHWATERMANSCORE.getComparator());
        // greater wins
        assertEquals(Arrays.asList(wrapper3, wrapper2, wrapper1), wrappers);
    }

    @Test
    public void test_IDENTITY_Comparator() {
        wrappers.sort(SortItem.IDENTITY.getComparator());
        // greater wins
        assertEquals(Arrays.asList(wrapper3, wrapper2, wrapper1), wrappers);
    }

    @Test
    public void test_SIMILARITY_Comparator() {
        wrappers.sort(SortItem.SIMILARITY.getComparator());
        // greater wins
        assertEquals(Arrays.asList(wrapper3, wrapper2, wrapper1), wrappers);
    }
}
