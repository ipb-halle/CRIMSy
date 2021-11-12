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

import de.ipb_halle.molecularfaces.component.openvectoreditor.OpenVectorEditorCore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class SequenceInformationTest {
    
    SequenceInformation seqInfo;
    String emptyJson = "{\"sequence\":null,\"circular\":false}";
    String seqJson1 = "{\"sequence\":\"AAA\",\"circular\":true,\"features\":\"features\"}";
    String seqJson2 = "{\"isProtein\":true,\"proteinSequence\":\"TTT\",\"circular\":false,\"features\":\"features2\"}";
    
    @Before
    public void init() {
        seqInfo = new SequenceInformation();
    }
    
    @Test
    public void test001_actionSelectSequenceType() {
        seqInfo.setSequenceType(SequenceType.DNA);
        seqInfo.actionSelectSequenceType();
        Assert.assertEquals("", seqInfo.getSequenceJson());
        
        seqInfo.setSequenceType(SequenceType.PROTEIN);
        seqInfo.actionSelectSequenceType();
        Assert.assertEquals(SequenceType.PROTEIN, seqInfo.getSequenceType());
        Assert.assertTrue(seqInfo.isSequenceTypeSelected());
        Assert.assertEquals(OpenVectorEditorCore.EMPTY_PROTEIN_SEQUENCE_JSON, seqInfo.getSequenceJson());
    }
    
    @Test
    public void test002_setSequenceData() {
        SequenceData emptyData = SequenceData.builder().build();
        seqInfo.setSequenceData(emptyData);
        Assert.assertEquals(emptyJson, seqInfo.getSequenceJson());
        
        SequenceData rnaData = SequenceData.builder()
                .annotations("\"features\"")
                .circular(Boolean.TRUE)
                .sequenceType(SequenceType.RNA)
                .sequenceString("AAA")
                .build();
        
        seqInfo.setSequenceData(rnaData);
        Assert.assertEquals(SequenceType.RNA, seqInfo.getSequenceType());
        Assert.assertEquals(seqJson1, seqInfo.getSequenceJson());
        
        rnaData = SequenceData.builder()
                .annotations("\"features2\"")
                .sequenceType(SequenceType.PROTEIN)
                .sequenceString("TTT")
                .build();
        
        seqInfo.setSequenceData(rnaData);
        Assert.assertEquals(SequenceType.PROTEIN, seqInfo.getSequenceType());
        Assert.assertEquals(seqJson2, seqInfo.getSequenceJson());
    }
    
    @Test
    public void test003_getPossibleSequenceTypes() {
        Assert.assertEquals(SequenceType.values().length, seqInfo.getPossibleSequenceTypes().size());
    }
    
    @Test
    public void test004_setSequenceJson() {
        seqInfo.setSequenceType(SequenceType.PROTEIN);
        seqInfo.setSequenceJson(seqJson2);
       
        Assert.assertTrue(seqInfo.isSequenceTypeSelected());
        Assert.assertEquals(seqJson2, seqInfo.getSequenceJson());
        Assert.assertEquals("TTT", seqInfo.getSequenceData().getSequenceString());
        Assert.assertFalse(seqInfo.getSequenceData().isCircular());
        Assert.assertEquals("\"features2\"", seqInfo.getSequenceData().getAnnotations());
        
    }
    
}
