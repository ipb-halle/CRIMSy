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
package de.ipb_halle.lbac.exp.assay;

import de.ipb_halle.lbac.exp.ExpRecord;
import de.ipb_halle.lbac.datalink.LinkedData;
import de.ipb_halle.lbac.datalink.LinkedDataType;
import de.ipb_halle.lbac.material.biomaterial.BioMaterial;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.structure.Structure;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 *
 * @author fmauz
 */
public class AssayTest {

    @Test
    public void test001_isValide() {
        //No Target
        Assay assay = new Assay();
        Assert.assertFalse(assay.validate());
        Assert.assertTrue(assay.getErrors().contains(ExpRecord.ValidationError.NO_TARGET));

        //Target set
        BioMaterial bioMaterial = createBioMaterial("slime");
        assay.setTarget(bioMaterial);
        Assert.assertTrue(assay.validate());

        //Assay Record without object set
        LinkedData link = new LinkedData(assay, LinkedDataType.LINK_MATERIAL, 0);
        link.setPayload(new SinglePointOutcome("g"));
        assay.getLinkedData().add(link);
        Assert.assertFalse(assay.validate());
        Assert.assertTrue(assay.getErrors().contains(ExpRecord.ValidationError.ASSAY_RECORD_HAS_NO_OBJECT));

        //AssayRecord with objects
        link.setMaterial(createBioMaterial("water"));
        Assert.assertTrue(assay.validate());
        Assert.assertTrue(assay.getErrors().isEmpty());

    }
    
    @Test
    public void test002_isDiagrammButtonVisible(){
         Assay assay = new Assay();
         assay.computeSinglePointBarChart();
    }

    protected static BioMaterial createBioMaterial(String name) {
        return new BioMaterial(1, Arrays.asList(new MaterialName(name, "en", 0)), 0, new HazardInformation(), new StorageInformation(), null, null);
    }

    private Structure createStructure(String name) {
        return new Structure("", 0d, 0d, 2, Arrays.asList(new MaterialName(name, "en", 0)), 0);
    }

}
