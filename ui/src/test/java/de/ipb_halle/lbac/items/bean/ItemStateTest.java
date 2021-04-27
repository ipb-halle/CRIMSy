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
import de.ipb_halle.lbac.util.Unit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class ItemStateTest {

    @Test
    public void test001_getPreviousAndFollowingKeyTest() {
        ItemState state = new ItemState();
        Item item = new Item();
        state.setEditedItem(item);

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        cal.add(Calendar.YEAR, -1);
        Date oneYearAgo = cal.getTime();
        cal.add(Calendar.YEAR, -1);
        Date twoYearsAgo = cal.getTime();

        /* 
         * History is empty, i.e. nothing can precede or follow.
         */
        Assert.assertEquals(null, state.getPreviousKey(null));
        Assert.assertEquals(null, state.getPreviousKey(now));
        Assert.assertEquals(null, state.getFollowingKey(null));
        Assert.assertEquals(null, state.getFollowingKey(now));

        // will become starting item in history
        item.getHistory().put(twoYearsAgo, createHistory(twoYearsAgo));
        // will become last item in history
        item.getHistory().put(now, createHistory(now));
        item.getHistory().put(oneYearAgo, createHistory(oneYearAgo));

        // back in time
        Assert.assertEquals(now, state.getPreviousKey(null));
        Assert.assertEquals(oneYearAgo, state.getPreviousKey(now));
        Assert.assertEquals(twoYearsAgo, state.getPreviousKey(oneYearAgo));
        Assert.assertEquals(null, state.getPreviousKey(twoYearsAgo));

        // forward in time
        Assert.assertEquals(null, state.getFollowingKey(null));
        Assert.assertEquals(null, state.getFollowingKey(now));
        Assert.assertEquals(now, state.getFollowingKey(oneYearAgo));
        Assert.assertEquals(oneYearAgo, state.getFollowingKey(twoYearsAgo));
    }

    @Test
    public void test002_historyState() {
        ItemState state = new ItemState();
        Item item = new Item();
        state.setEditedItem(item);

        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        cal.add(Calendar.YEAR, -1);
        Date oneYearAgo = cal.getTime();
        cal.add(Calendar.YEAR, -1);
        Date twoYearsAgo = cal.getTime();

        // will become starting item in history
        item.getHistory().put(twoYearsAgo, createHistory(twoYearsAgo));
        // will become last item in history
        item.getHistory().put(now, createHistory(now));
        item.getHistory().put(oneYearAgo, createHistory(oneYearAgo));

        // latest revision
        Assert.assertNull(state.getCurrentHistoryDate());
        Assert.assertTrue(state.isLastHistoryItem());
        Assert.assertFalse(state.isStartingHistoryItem());
        Assert.assertEquals(now, state.getChangeDate());

        // jump to revision one year ago
        state.setCurrentHistoryDate(state.getPreviousKey(state.getCurrentHistoryDate()));
        Assert.assertEquals(now, state.getCurrentHistoryDate());
        Assert.assertFalse(state.isLastHistoryItem());
        Assert.assertFalse(state.isStartingHistoryItem());
        Assert.assertEquals(oneYearAgo, state.getChangeDate());

        // jump to revision two years ago
        state.setCurrentHistoryDate(state.getPreviousKey(state.getCurrentHistoryDate()));
        Assert.assertEquals(oneYearAgo, state.getCurrentHistoryDate());
        Assert.assertFalse(state.isLastHistoryItem());
        Assert.assertFalse(state.isStartingHistoryItem());
        Assert.assertEquals(twoYearsAgo, state.getChangeDate());

        // jump to original item
        state.setCurrentHistoryDate(state.getPreviousKey(state.getCurrentHistoryDate()));
        Assert.assertEquals(twoYearsAgo, state.getCurrentHistoryDate());
        Assert.assertFalse(state.isLastHistoryItem());
        Assert.assertTrue(state.isStartingHistoryItem());
        Assert.assertEquals(null, state.getChangeDate());

        /*
         * What happens when we go further back in time?
         * We end up at the latest revision again ... *Wait a minute, Doc!*
         */
        state.setCurrentHistoryDate(state.getPreviousKey(state.getCurrentHistoryDate()));
        Assert.assertEquals(null, state.getCurrentHistoryDate());
        Assert.assertTrue(state.isLastHistoryItem());
        Assert.assertFalse(state.isStartingHistoryItem());
        Assert.assertEquals(now, state.getChangeDate());
    }

    @Test
    public void test003_copyItemTest() {
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
        Assert.assertEquals(item.getUnit().getUnit(), state.getEditedItem().getUnit().getUnit());
        Assert.assertEquals(item.getcTime(), state.getEditedItem().getcTime());

    }

    private List<ItemDifference> createHistory(Date date) {
        List<ItemDifference> hist = new ArrayList<>();
        ItemHistory history = new ItemHistory();
        history.setMdate(date);
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
        item.setUnit(Unit.getUnit("kg"));
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
