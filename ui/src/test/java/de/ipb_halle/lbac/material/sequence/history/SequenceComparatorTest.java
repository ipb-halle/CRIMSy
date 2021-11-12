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
package de.ipb_halle.lbac.material.sequence.history;

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.common.history.MaterialDifference;
import de.ipb_halle.lbac.material.common.history.MaterialIndexDifference;
import de.ipb_halle.lbac.material.common.history.MaterialOverviewDifference;
import de.ipb_halle.lbac.material.sequence.Sequence;
import de.ipb_halle.lbac.material.sequence.SequenceData;
import de.ipb_halle.lbac.material.sequence.SequenceType;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class SequenceComparatorTest {

    private int projectid = 1;
    private int sequenceid = 10;
    private int userid = 100;
    private int aclistid = 1000;
    Sequence originalSequence;
    Sequence editedSequence;
    List<MaterialDifference> diffs;
    User user1;
    ACList aclist1;
    SequenceComparator comparator = new SequenceComparator();

    @Before
    public void init() {
        diffs = new ArrayList<>();
        user1 = new User();
        user1.setId(userid);
        aclist1 = new ACList();
        aclist1.setId(aclistid);

        originalSequence = createEmptySequence();
        editedSequence = createEmptySequence();

    }

    @Test
    public void test001_differentNames() throws Exception {
        originalSequence.getNames().add(new MaterialName("originalName", "de", 0));
        editedSequence.getNames().add(new MaterialName("editedName", "de", 0));

        comparator.compareDifferences(diffs, originalSequence, editedSequence);
        Assert.assertEquals(1, diffs.size());
        MaterialIndexDifference diff = (MaterialIndexDifference) diffs.get(0);
        Assert.assertEquals("editedName", diff.getValuesNew().get(0));
        Assert.assertEquals("originalName", diff.getValuesOld().get(0));
    }

    @Test
    public void test002_changeOfProject() throws Exception {
        editedSequence.setProjectId(2);
        comparator.compareDifferences(diffs, originalSequence, editedSequence);
        Assert.assertEquals(1, diffs.size());
        MaterialOverviewDifference diff = (MaterialOverviewDifference) diffs.get(0);
        Assert.assertEquals(2, diff.getProjectIdNew(), 0);
        Assert.assertEquals(projectid, diff.getProjectIdOld(), 0);
    }

    @Test
    public void test003_addNewIndex() throws Exception {
        editedSequence.getIndices().add(new IndexEntry(2, "newIndexEntry", null));
        comparator.compareDifferences(diffs, originalSequence, editedSequence);
        Assert.assertEquals(1, diffs.size());
        MaterialIndexDifference diff = (MaterialIndexDifference) diffs.get(0);
        Assert.assertNull(diff.getValuesOld().get(0));
        Assert.assertEquals("newIndexEntry", diff.getValuesNew().get(0));
    }

    @Test
    public void test004_noChange() throws Exception {
        comparator.compareDifferences(diffs, originalSequence, editedSequence);
        Assert.assertEquals(0, diffs.size());
    }

    @Test
    public void test004_changeSequenceData() throws Exception {

        SequenceData dataEdited = SequenceData.builder()
                .annotations("editedAnnotation")
                .circular(Boolean.TRUE)
                .sequenceString("AAA")
                .sequenceType(SequenceType.DNA).build();
        editedSequence.setSequenceData(dataEdited);

        comparator.compareDifferences(diffs, originalSequence, editedSequence);
        Assert.assertEquals(1, diffs.size());
        SequenceDifference seqDiff = (SequenceDifference) diffs.get(0);
        Assert.assertNull(seqDiff.getOldSequenceData().getSequenceLength());
        Assert.assertNull(seqDiff.getOldSequenceData().getSequenceString());
        Assert.assertNull(seqDiff.getOldSequenceData().getAnnotations());
        Assert.assertNull(seqDiff.getOldSequenceData().getSequenceType());
        Assert.assertNull(seqDiff.getOldSequenceData().isCircular());
        Assert.assertEquals(3, seqDiff.getNewSequenceData().getSequenceLength(), 0);
        Assert.assertEquals("AAA", seqDiff.getNewSequenceData().getSequenceString());
        Assert.assertEquals("editedAnnotation", seqDiff.getNewSequenceData().getAnnotations());
        Assert.assertEquals(SequenceType.DNA, seqDiff.getNewSequenceData().getSequenceType());
        Assert.assertTrue(seqDiff.getNewSequenceData().isCircular());
    }

    @Test
    public void test005_changeSequenceData() throws Exception {

        SequenceData oriData = SequenceData.builder()
                .annotations("originalAnnotation")
                .circular(Boolean.TRUE)
                .sequenceString("AAA")
                .sequenceType(SequenceType.DNA).build();
        originalSequence.setSequenceData(oriData);

        comparator.compareDifferences(diffs, originalSequence, editedSequence);
        Assert.assertEquals(1, diffs.size());
        SequenceDifference seqDiff = (SequenceDifference) diffs.get(0);
        Assert.assertNull(seqDiff.getNewSequenceData().getSequenceLength());
        Assert.assertNull(seqDiff.getNewSequenceData().getSequenceString());
        Assert.assertNull(seqDiff.getNewSequenceData().getAnnotations());
        Assert.assertNull(seqDiff.getNewSequenceData().getSequenceType());
        Assert.assertNull(seqDiff.getNewSequenceData().isCircular());

        Assert.assertEquals(3, seqDiff.getOldSequenceData().getSequenceLength(), 0);
        Assert.assertEquals("AAA", seqDiff.getOldSequenceData().getSequenceString());
        Assert.assertEquals("originalAnnotation", seqDiff.getOldSequenceData().getAnnotations());
        Assert.assertEquals(SequenceType.DNA, seqDiff.getOldSequenceData().getSequenceType());
        Assert.assertTrue(seqDiff.getOldSequenceData().isCircular());
    }

    @Test
    public void test006_changeSequenceData() throws Exception {

        SequenceData oriData = SequenceData.builder()
                .annotations("originalAnnotation")
                .circular(Boolean.TRUE)
                .sequenceString("AAA")
                .sequenceType(SequenceType.DNA).build();
        originalSequence.setSequenceData(oriData);

        SequenceData dataEdited = SequenceData.builder()
                .annotations("editedAnnotation")
                .circular(Boolean.FALSE)
                .sequenceString("TTTT")
                .sequenceType(SequenceType.RNA).build();
        editedSequence.setSequenceData(dataEdited);

        comparator.compareDifferences(diffs, originalSequence, editedSequence);
        Assert.assertEquals(1, diffs.size());
        SequenceDifference seqDiff = (SequenceDifference) diffs.get(0);
        Assert.assertEquals(4, seqDiff.getNewSequenceData().getSequenceLength(), 0);
        Assert.assertEquals("TTTT", seqDiff.getNewSequenceData().getSequenceString());
        Assert.assertEquals("editedAnnotation", seqDiff.getNewSequenceData().getAnnotations());
        Assert.assertEquals(SequenceType.RNA, seqDiff.getNewSequenceData().getSequenceType());
        Assert.assertFalse(seqDiff.getNewSequenceData().isCircular());

        Assert.assertEquals(3, seqDiff.getOldSequenceData().getSequenceLength(), 0);
        Assert.assertEquals("AAA", seqDiff.getOldSequenceData().getSequenceString());
        Assert.assertEquals("originalAnnotation", seqDiff.getOldSequenceData().getAnnotations());
        Assert.assertEquals(SequenceType.DNA, seqDiff.getOldSequenceData().getSequenceType());
        Assert.assertTrue(seqDiff.getOldSequenceData().isCircular());
    }

    private Sequence createEmptySequence() {
        Sequence s = new Sequence(
                sequenceid,
                new ArrayList<>(),
                projectid,
                new HazardInformation(),
                new StorageInformation(),
                SequenceData.builder().build());
        s.setACList(aclist1);
        s.setOwner(user1);

        return s;
    }
}
