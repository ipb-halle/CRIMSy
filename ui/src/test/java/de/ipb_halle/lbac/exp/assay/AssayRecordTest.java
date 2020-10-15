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

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.structure.Structure;
import java.util.ArrayList;
import java.util.List;
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
    private AssayRecord record;

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
        record = new AssayRecord(assay, 1);
    }

    @Test
    public void testItemSet() {
        Assert.assertEquals(exportRecordId, (long) record.getAssay().getExpRecordId());

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
