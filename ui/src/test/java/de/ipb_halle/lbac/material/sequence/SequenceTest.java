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
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.common.entity.MaterialEntity;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.bean.Type;

/**
 * 
 * @author flange
 */
public class SequenceTest {
    private SequenceData data;
    private Sequence sequence;

    @Before
    public void init() {
        data = new SequenceData();
        data.setSequenceString("AGTTAAGCGTGA");
        data.setSequenceType(SequenceType.PROTEIN);
        data.setCircular(true);
        data.setAnnotations("no features ;)");
        sequence = new Sequence(21, new ArrayList<>(), 42, new HazardInformation(),
                new StorageInformation(), data);
    }

    @Test
    public void test001_copyMaterial() {
        Sequence copy = sequence.copyMaterial();
        SequenceData copiedData = copy.getData();

        assertNotSame(sequence, copy);
        assertNotSame(data, copiedData);

        assertEquals("AGTTAAGCGTGA", copiedData.getSequenceString());
        assertEquals(SequenceType.PROTEIN, copiedData.getSequenceType());
        assertTrue(copiedData.isCircular());
        assertEquals("no features ;)", copiedData.getAnnotations());
    }

    @Test
    public void test002_createEntity() {
        SequenceEntity entity = sequence.createEntity();

        assertEquals(21, entity.getId().intValue());
        assertEquals("AGTTAAGCGTGA", entity.getSequenceString());
        assertEquals(SequenceType.PROTEIN.name(), entity.getSequenceType());
        assertTrue(entity.isCircular());
        assertEquals("no features ;)", entity.getAnnotations());
    }

    @Test
    public void test003_isEqualTo() {
        Structure structure = new Structure("C", 0d, 0d, 3, new ArrayList<>(),
                2);
        Sequence otherSequence = new Sequence(22, new ArrayList<>(), 42,
                new HazardInformation(), new StorageInformation(), data);

        Assert.assertTrue(sequence.isEqualTo(sequence));
        Assert.assertFalse(sequence.isEqualTo(otherSequence));
        Assert.assertFalse(sequence.isEqualTo(structure));
        Assert.assertFalse(sequence.isEqualTo(null));
    }

    @Test
    public void test004_getTypeToDisplay() {
        Type type = sequence.getTypeToDisplay();
        Assert.assertEquals(SearchTarget.MATERIAL, type.getGeneralType());
        Assert.assertEquals(MaterialType.SEQUENCE, type.getMaterialType());
    }

    @Test
    public void test005_getAndSetData() {
        assertSame(data, sequence.getData());
        SequenceData newData = new SequenceData();
        sequence.setData(newData);
        assertSame(newData, sequence.getData());
    }
    
    @Test
    public void test006_fromEntity() {
        MaterialEntity materialEntity = new MaterialEntity();
        SequenceEntity sequenceEntity = new SequenceEntity();
        sequenceEntity.setId(42);
        sequenceEntity.setSequenceString("AGTTAAGCGTGA");
        sequenceEntity.setSequenceType(SequenceType.PROTEIN.name());
        sequenceEntity.setCircular(true);
        sequenceEntity.setAnnotations("some features");
        
        Sequence result = Sequence.fromEntities(materialEntity, sequenceEntity);
        
        assertEquals(42, result.getId());
        assertEquals("AGTTAAGCGTGA", result.getData().getSequenceString());
        assertEquals(SequenceType.PROTEIN, result.getData().getSequenceType());
        assertTrue(result.getData().isCircular());
        assertEquals("some features", result.getData().getAnnotations());
    }
}