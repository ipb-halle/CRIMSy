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
package de.ipb_halle.lbac.items.bean;

import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.ItemDifference;
import de.ipb_halle.lbac.items.ItemHistory;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.StorageClassInformation;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.project.Project;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class ItemStateTest {

    @Test
    public void test001_getPreviousAndFollowingKeyTest() {
        Calendar cal = Calendar.getInstance();
        ItemState state = new ItemState();
        Item item = new Item();
        item.getHistory().put(createHistory(2020).get(0).getMdate(), createHistory(2020));
        item.getHistory().put(createHistory(2021).get(0).getMdate(), createHistory(2021));
        item.getHistory().put(createHistory(2022).get(0).getMdate(), createHistory(2022));
        item.getHistory().put(createHistory(2023).get(0).getMdate(), createHistory(2023));
        state.setEditedItem(item);

        Assert.assertNull(state.getCurrentHistoryDate());
        cal.setTime(state.getPreviousKey(null));
        Assert.assertEquals(2023, cal.get(1));

        cal.setTime(state.getPreviousKey(cal.getTime()));
        Assert.assertEquals(2022, cal.get(1));

        cal.setTime(state.getPreviousKey(cal.getTime()));
        Assert.assertEquals(2021, cal.get(1));

        cal.setTime(state.getPreviousKey(cal.getTime()));
        Assert.assertEquals(2020, cal.get(1));

        cal.setTime(state.getFollowingKey(cal.getTime()));
        Assert.assertEquals(2021, cal.get(1));

        cal.setTime(state.getFollowingKey(cal.getTime()));
        Assert.assertEquals(2022, cal.get(1));

        cal.setTime(state.getFollowingKey(cal.getTime()));
        Assert.assertEquals(2023, cal.get(1));

        Assert.assertNull(state.getFollowingKey(cal.getTime()));
    }

    @Test
    public void test002_copyItemTest() {
        Item item = createItem();
        ItemState state = new ItemState(item);

        Assert.assertEquals(item.getACList().getId(), state.getEditedItem().getACList().getId());
        Assert.assertEquals(item.getAmount(), state.getEditedItem().getAmount());
        Assert.assertEquals(item.getArticle(), state.getEditedItem().getArticle());
        Assert.assertEquals(item.getConcentration(), state.getEditedItem().getConcentration());
        Assert.assertEquals(item.getContainer().getId(), state.getEditedItem().getContainer().getId());
        Assert.assertEquals(item.getContainerSize(), state.getEditedItem().getContainerSize());
        Assert.assertEquals(item.getContainerType(), state.getEditedItem().getContainerType());
        Assert.assertEquals(item.getDescription(), state.getEditedItem().getDescription());
        Assert.assertEquals(item.getId(), state.getEditedItem().getId());
        Assert.assertEquals(item.getMaterial().getId(), state.getEditedItem().getMaterial().getId());
        Assert.assertEquals(item.getNestedContainer().size(), state.getEditedItem().getNestedContainer().size());
        Assert.assertEquals(item.getNestedLocation(), state.getEditedItem().getNestedLocation());
        Assert.assertEquals(item.getOwner().getId(), state.getEditedItem().getOwner().getId());
        Assert.assertEquals(item.getProject().getId(), state.getEditedItem().getProject().getId());
        Assert.assertEquals(item.getPurity(), state.getEditedItem().getPurity());
        Assert.assertEquals(item.getSolvent(), state.getEditedItem().getSolvent());
        Assert.assertEquals(item.getUnit(), state.getEditedItem().getUnit());
        Assert.assertEquals(item.getcTime(), state.getEditedItem().getcTime());

    }

    private List<ItemDifference> createHistory(int year) {
        List<ItemDifference> hist = new ArrayList<>();
        Calendar cal = Calendar.getInstance();
        ItemHistory history = new ItemHistory();
        cal.set(year, 4, 0);
        history.setMdate(cal.getTime());
        hist.add(history);
        return hist;
    }

    private Item createItem() {
        Container c = new Container();
        c.setId(10);
        c.setLabel("test-container");
        Project project = new Project();
        project.setId(1);
        ACList acl = new ACList();
        acl.setId(1000);
        acl.setName("test-acl");
        Structure s = new Structure("", 0d, 0d, 1, new ArrayList<>(), project.getId(), new HazardInformation(), new StorageClassInformation(), null);
        Item item = new Item();
        item.setAmount(23d);
        item.setACList(acl);
        item.setUnit("kg");
        item.setArticle(null);
        item.setConcentration(32d);
        item.setContainer(c);
        item.setContainerSize(100d);
        item.setDescription("description");
        item.setMaterial(s);
        User u = new User();
        u.setId(1001);
        u.setName("testUser");
        item.setOwner(u);
        item.setProject(project);
        item.setPurity("rein");
        item.setcTime(new Date());
        item.setSolvent(null);
        return item;
    }
}
