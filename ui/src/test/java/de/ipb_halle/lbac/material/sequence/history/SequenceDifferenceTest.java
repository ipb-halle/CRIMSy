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

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

import de.ipb_halle.lbac.material.common.history.HistoryEntityId;
import de.ipb_halle.lbac.material.sequence.SequenceData;
import de.ipb_halle.lbac.material.sequence.SequenceType;

/**
 * 
 * @author flange
 */
public class SequenceDifferenceTest {
    @Test
    public void test001_fromSequenceHistoryEntity() {
        Date mDate = new Date(123456789L);
        HistoryEntityId id = new HistoryEntityId(42, mDate, 123);

        SequenceHistoryEntity entity;
        entity = new SequenceHistoryEntity();
        entity.setId(id);
        entity.setSequenceString_old("sequenceString_old");
        entity.setSequenceString_new("sequenceString_new");
        entity.setSequenceType_old("DNA");
        entity.setSequenceType_new("PROTEIN");
        entity.setAnnotations_old("annotations_old");
        entity.setAnnotations_new("annotations_new");
        entity.setCircular_old(false);
        entity.setCircular_new(false);

        SequenceData expectedOldSequenceData = SequenceData.builder()
                .sequenceString("sequenceString_old")
                .sequenceType(SequenceType.DNA)
                .annotations("annotations_old")
                .circular(false)
                .build();
        SequenceData expectedNewSequenceData = SequenceData.builder()
                .sequenceString("sequenceString_new")
                .sequenceType(SequenceType.PROTEIN)
                .annotations("annotations_new")
                .circular(false)
                .build();

        SequenceDifference diff = SequenceDifference
                .fromSequenceHistoryEntity(entity);
        assertEquals(42, diff.getMaterialId());
        assertEquals(123, diff.getActorId().intValue());
        assertEquals(mDate, diff.getModificationDate());
        assertEquals(expectedOldSequenceData, diff.getOldSequenceData());
        assertEquals(expectedNewSequenceData, diff.getNewSequenceData());

        // other variations of the boolean circular property
        entity.setCircular_old(true);
        entity.setCircular_new(false);
        expectedOldSequenceData = SequenceData.builder(expectedOldSequenceData).circular(true).build();
        expectedNewSequenceData = SequenceData.builder(expectedNewSequenceData).circular(false).build();
        diff = SequenceDifference.fromSequenceHistoryEntity(entity);
        assertEquals(expectedOldSequenceData, diff.getOldSequenceData());
        assertEquals(expectedNewSequenceData, diff.getNewSequenceData());

        entity.setCircular_old(false);
        entity.setCircular_new(true);
        expectedOldSequenceData = SequenceData.builder(expectedOldSequenceData).circular(false).build();
        expectedNewSequenceData = SequenceData.builder(expectedNewSequenceData).circular(true).build();
        diff = SequenceDifference.fromSequenceHistoryEntity(entity);
        assertEquals(expectedOldSequenceData, diff.getOldSequenceData());
        assertEquals(expectedNewSequenceData, diff.getNewSequenceData());

        entity.setCircular_old(true);
        entity.setCircular_new(true);
        expectedOldSequenceData = SequenceData.builder(expectedOldSequenceData).circular(true).build();
        expectedNewSequenceData = SequenceData.builder(expectedNewSequenceData).circular(true).build();
        diff = SequenceDifference.fromSequenceHistoryEntity(entity);
        assertEquals(expectedOldSequenceData, diff.getOldSequenceData());
        assertEquals(expectedNewSequenceData, diff.getNewSequenceData());
    }

    @Test
    public void test002_createEntity() {
        Date mDate = new Date(123456789L);
        SequenceData oldSequenceData = SequenceData.builder()
                .sequenceString("sequenceString_old")
                .sequenceType(SequenceType.DNA)
                .annotations("annotations_old")
                .circular(false)
                .build();
        SequenceData newSequenceData = SequenceData.builder()
                .sequenceString("sequenceString_new")
                .sequenceType(SequenceType.PROTEIN)
                .annotations("annotations_new")
                .circular(false)
                .build();

        SequenceDifference diff = new SequenceDifference();
        diff.initialise(123, 456, mDate);
        diff.setOldSequenceData(oldSequenceData);
        diff.setNewSequenceData(newSequenceData);

        SequenceHistoryEntity entity = diff.createEntity();

        assertEquals(new HistoryEntityId(123, mDate, 456), entity.getId());
    }
}