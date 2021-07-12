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
package de.ipb_halle.lbac.material.composition;

import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.common.entity.MaterialCompositionEntity;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.bean.Type;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class MaterialCompositionTest {

    MaterialComposition compo1, compo2;
    Structure struc1, struc2;

    @Before
    public void init() {
        int projectid = 10;

        compo1 = new MaterialComposition(1, new ArrayList<>(), projectid, new HazardInformation(), new StorageInformation());
        compo1.getNames().add(new MaterialName("compo1-name1", "de", 0));
        compo1.getNames().add(new MaterialName("compo1-name2", "de", 1));
        compo2 = new MaterialComposition(2, new ArrayList<>(), projectid, new HazardInformation(), new StorageInformation());
        struc1 = new Structure("C", 0d, 0d, 3, new ArrayList<>(), projectid);
        struc2 = new Structure("O", 0d, 0d, 4, new ArrayList<>(), projectid);
    }

    @Test
    public void test001_createDBEntity() {
        //Composition without a component
        List<MaterialCompositionEntity> entities = compo1.createCompositionEntities();
        Assert.assertEquals(1, entities.size());

        //Composition with two components
        compo1.addComponent(struc1, 0d);
        compo1.addComponent(struc2, 0d);
        entities = compo1.createCompositionEntities();
        Assert.assertEquals(3, entities.size());
    }

    @Test
    public void test002_isEqualTo() {
        Assert.assertTrue(compo1.isEqualTo(compo1));
        Assert.assertFalse(compo1.isEqualTo(compo2));
        Assert.assertFalse(compo1.isEqualTo(struc1));
        Assert.assertFalse(compo1.isEqualTo(null));
    }

    @Test
    public void test003_getTypeToDisplay() {
        Type type = compo1.getTypeToDisplay();
        Assert.assertEquals(SearchTarget.MATERIAL, type.getGeneralType());
        Assert.assertEquals(MaterialType.COMPOSITION, type.getMaterialType());
    }

    @Test
    public void test004_copy() {
        //Because the copy must be a deep copy the objectids are here checked 
        //to be different of the inner objects of the material composition
        MaterialComposition copy = compo1.copyMaterial();
        Assert.assertFalse(compo1 == copy);
        Assert.assertEquals(compo1.getNames().size(), copy.getNames().size());
        for (int i = 0; i < copy.getNames().size(); i++) {
            Assert.assertFalse(copy.getNames().get(i) == compo1.getNames().get(i));
        }
        Assert.assertFalse(compo1.getHazards() == copy.getHazards());
        Assert.assertFalse(compo1.getStorageInformation() == copy.getStorageInformation());
    }

    @Test
    public void test005_createEntity() {
        Assert.assertThrows(UnsupportedOperationException.class, () -> {
            compo1.createEntity();
        });
    }
}
