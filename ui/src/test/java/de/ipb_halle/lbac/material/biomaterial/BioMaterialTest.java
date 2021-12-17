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
package de.ipb_halle.lbac.material.biomaterial;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.consumable.Consumable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class BioMaterialTest {

    private int projectId = 100;
    private int bioMatId = 10;
    private int bioMatId2 = 11;
    private int consumableId = 12;
    private int taxonomyId = 20;
    private int tissueId = 30;
    private int userId = 40;
    private User user;
    private Taxonomy taxonomy;
    private Tissue tissue;
    BioMaterial bioMat, anotherBioMat;

    @Before
    public void before() {
        user = new User();
        user.setId(userId);
        taxonomy = new Taxonomy(taxonomyId, new ArrayList<>(), new HazardInformation(), new StorageInformation(), new ArrayList<>(), user, new Date());
        tissue = new Tissue(tissueId, Arrays.asList(new MaterialName("Tissue1", "de", 0)), taxonomy);
        bioMat = new BioMaterial(bioMatId, new ArrayList<>(), projectId, new HazardInformation(), new StorageInformation(), taxonomy, tissue);
        anotherBioMat = new BioMaterial(bioMatId2, new ArrayList<>(), projectId, new HazardInformation(), new StorageInformation(), taxonomy, tissue);
    }

    @Test
    public void test001_isEqualTo() {
        Consumable consumable = new Consumable(consumableId, new ArrayList<>(), projectId, new HazardInformation(), new StorageInformation());
        Assert.assertTrue(bioMat.isEqualTo(bioMat));
        Assert.assertFalse(bioMat.isEqualTo(anotherBioMat));
        Assert.assertFalse(bioMat.isEqualTo(consumable));
    }

    @Test
    public void test002_getTissueName() {
        Assert.assertEquals("Tissue1", bioMat.getTissueName());
        Assert.assertEquals("", anotherBioMat.getTissueName());
    }

}
