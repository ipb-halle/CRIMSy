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

import de.ipb_halle.lbac.datalink.LinkedDataEntity;
import de.ipb_halle.lbac.datalink.LinkedData;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.datalink.LinkedDataType;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.structure.Structure;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class AssayRecordTest {

    private User user;
    private int projectId = 100;
    private Material struc1;
    private Material struc2;
    private Item item1, item2;
    private long exportRecordId = 0L;
    private Assay assay;
    private LinkedData record;

    @Before
    public void setUp() {
        user = new User();
        user.setId(1);
        user.setName("testUser");
        struc1 = createMaterial(10, "Structure1");
        struc2 = createMaterial(20, "Structure2");
        item1 = createItem(struc1, 1000);
        item2 = createItem(struc2, 2000);

        assay = new Assay();
        assay.setExpRecordId(exportRecordId);
        record = new LinkedData(assay,  
                LinkedDataType.ASSAY_SINGLE_POINT_OUTCOME, 1);
    }

    @Test
    public void testItemSet() {
        Assert.assertEquals(exportRecordId, (long) record.getExpRecord().getExpRecordId());

        record.setItem(item1);
        Assert.assertEquals(item1.getId(), record.getItem().getId());
        Assert.assertEquals(struc1.getId(), record.getMaterial().getId());

        record.setItem(null);
        Assert.assertNull(record.getItem());
        Assert.assertEquals(struc1.getId(), record.getMaterial().getId());

        record.setItem(item2);
        Assert.assertEquals(item2.getId(), record.getItem().getId());
        Assert.assertEquals(struc2.getId(), record.getMaterial().getId());
    }

    @Test
    public void testMaterialSet() {
        record.setMaterial(struc1);
        Assert.assertNull(record.getItem());
        Assert.assertEquals(struc1.getId(), record.getMaterial().getId());

        record.setItem(item2);
        record.setMaterial(struc1);
        Assert.assertEquals(item2.getId(), record.getItem().getId());
        Assert.assertEquals(struc2.getId(), record.getMaterial().getId());
    }

    @Test
    public void testCreateFromEntity() {
        LinkedDataEntity entity = new LinkedDataEntity();
        entity.setExpRecordId(exportRecordId);
        entity.setItemId(item1.getId());
        entity.setMaterialId(struc1.getId());
        entity.setPayload(null);
        entity.setRank(1);
        entity.setRecordId(exportRecordId);
        record = new LinkedData(entity, assay, struc1, item1);

        Assert.assertEquals(item1.getId(), record.getItem().getId());
        Assert.assertEquals(struc1.getId(), record.getMaterial().getId());
    }

    @Test
    public void testCreateEntity() {
        LinkedDataEntity entity = record.createEntity();

        Assert.assertNull("recordId is null", entity.getRecordId());
        Assert.assertEquals("expRecordId is equal", assay.getExpRecordId(), entity.getExpRecordId());
        Assert.assertEquals("payload is equal", 
                ((record.getPayload() != null) ? record.getPayload().toString() : null),
                entity.getPayload());
        Assert.assertEquals("rank is equal", record.getRank(), entity.getRank());
        Assert.assertNull("itemId is null", entity.getItemId());
        Assert.assertNull("materialId is null", entity.getMaterialId());

        record.setMaterial(struc1);
        entity = record.createEntity();
        Assert.assertEquals("materialId is equal(1)", struc1.getId(), entity.getMaterialId(), 0);

        record.setItem(item2);
        entity = record.createEntity();
        Assert.assertEquals("materialId is equal(2)", struc2.getId(), entity.getMaterialId(), 0);
        Assert.assertEquals("itemId is equal", item2.getId(), entity.getItemId(), 0);
    }

    private Material createMaterial(int id, String... names) {
        List<MaterialName> namesList = new ArrayList<>();
        for (int i = 0; i < names.length; i++) {
            namesList.add(new MaterialName(names[i], "de", i + 1));
        }
        return new Structure("", 0d, 0d, id, namesList, projectId);
    }

    private Item createItem(Material m, int id) {
        Item item = new Item();
        item.setMaterial(m);
        item.setId(id);
        return item;
    }

}
