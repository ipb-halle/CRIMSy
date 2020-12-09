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
package de.ipb_halle.lbac.items.bean.history;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.ItemHistory;
import de.ipb_halle.lbac.items.ItemPositionHistoryList;
import de.ipb_halle.lbac.items.ItemPositionsHistory;
import de.ipb_halle.lbac.items.bean.ContainerController;
import de.ipb_halle.lbac.items.bean.ItemBean;
import de.ipb_halle.lbac.items.bean.ItemState;
import de.ipb_halle.lbac.project.Project;
import java.util.Arrays;
import java.util.Calendar;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author fmauz
 */
public class HistoryOperationTest {

    private User actor;
    private ItemBean bean;
    private Item item;
    private HistoryOperation operation;
    private ItemState state;

    @Before
    public void setUp() {
        actor = new User();
        actor.setId(1);
        actor.setName("actor");
        item = new Item();
        Container c = createContainer(10, "ContainerWithDimension");
        c.setDimension("4;4;1");
        c.setItems(new Item[4][4][1]);
        state = new ItemState(item);
        bean = new ItemBean();
        bean.setState(state);
        bean.setContainerController(new ContainerController(bean, c));
        this.operation = new HistoryOperation(state, bean.getContainerController());
    }

    @Test
    public void test001_changeAmount() {
        ItemHistory diff1 = createEmptyHistory(6, 8, 2012);
        ItemHistory diff2 = createEmptyHistory(6, 8, 2010);
        diff1.setAmountNew(10d);
        diff1.setAmountOld(8d);
        diff2.setAmountNew(8d);
        diff2.setAmountOld(6d);

        operation.applyNextNegativeDifference();
        Assert.assertEquals(8, state.getEditedItem().getAmount(), 0);
        operation.applyNextNegativeDifference();
        Assert.assertEquals(6, state.getEditedItem().getAmount(), 0);
        operation.applyNextPositiveDifference();
        Assert.assertEquals(8, state.getEditedItem().getAmount(), 0);
        operation.applyNextPositiveDifference();
        Assert.assertEquals(10, state.getEditedItem().getAmount(), 0);
    }

    @Test
    public void test002_changeAmount() {
        ItemHistory diff1 = createEmptyHistory(6, 8, 2012);
        ItemHistory diff2 = createEmptyHistory(6, 8, 2010);
        diff1.setConcentrationNew(10d);
        diff1.setConcentrationOld(8d);
        diff2.setConcentrationNew(8d);
        diff2.setConcentrationOld(6d);

        operation.applyNextNegativeDifference();
        Assert.assertEquals(8, state.getEditedItem().getConcentration(), 0);
        operation.applyNextNegativeDifference();
        Assert.assertEquals(6, state.getEditedItem().getConcentration(), 0);
        operation.applyNextPositiveDifference();
        Assert.assertEquals(8, state.getEditedItem().getConcentration(), 0);
        operation.applyNextPositiveDifference();
        Assert.assertEquals(10, state.getEditedItem().getConcentration(), 0);
    }

    @Test
    public void test003_changeDescription() {
        ItemHistory diff1 = createEmptyHistory(6, 8, 2012);
        ItemHistory diff2 = createEmptyHistory(6, 8, 2010);
        diff1.setDescriptionNew("latest description");
        diff1.setDescriptionOld("intermediate description");
        diff2.setDescriptionNew("intermediate description");
        diff2.setDescriptionOld("first description");

        operation.applyNextNegativeDifference();
        Assert.assertEquals("intermediate description", state.getEditedItem().getDescription());
        operation.applyNextNegativeDifference();
        Assert.assertEquals("first description", state.getEditedItem().getDescription());
        operation.applyNextPositiveDifference();
        Assert.assertEquals("intermediate description", state.getEditedItem().getDescription());
        operation.applyNextPositiveDifference();
        Assert.assertEquals("latest description", state.getEditedItem().getDescription());
    }

    @Test
    public void test004_changeOwner() {
        User owner_1 = createUser(2, "U2");
        User owner_2 = createUser(3, "U3");

        ItemHistory diff1 = createEmptyHistory(6, 8, 2012);
        ItemHistory diff2 = createEmptyHistory(6, 8, 2010);
        diff1.setOwnerNew(actor);
        diff1.setOwnerOld(owner_1);
        diff2.setOwnerNew(owner_1);
        diff2.setOwnerOld(owner_2);

        operation.applyNextNegativeDifference();
        Assert.assertEquals(owner_1.getId(), state.getEditedItem().getOwner().getId());
        operation.applyNextNegativeDifference();
        Assert.assertEquals(owner_2.getId(), state.getEditedItem().getOwner().getId());
        operation.applyNextPositiveDifference();
        Assert.assertEquals(owner_1.getId(), state.getEditedItem().getOwner().getId());
        operation.applyNextPositiveDifference();
        Assert.assertEquals(actor.getId(), state.getEditedItem().getOwner().getId());
    }

    @Test
    public void test005_changeProject() {
        ItemHistory diff1 = createEmptyHistory(6, 8, 2012);
        ItemHistory diff2 = createEmptyHistory(6, 8, 2010);
        Project p1 = createProject(1, "P1");
        Project p2 = createProject(2, "P2");
        diff1.setProjectNew(p2);
        diff1.setProjectOld(p1);
        diff2.setProjectNew(p1);
        diff2.setProjectOld(p2);

        operation.applyNextNegativeDifference();
        Assert.assertEquals(p1.getId(), state.getEditedItem().getProject().getId());
        operation.applyNextNegativeDifference();
        Assert.assertEquals(p2.getId(), state.getEditedItem().getProject().getId());
        operation.applyNextPositiveDifference();
        Assert.assertEquals(p1.getId(), state.getEditedItem().getProject().getId());
        operation.applyNextPositiveDifference();
        Assert.assertEquals(p2.getId(), state.getEditedItem().getProject().getId());
    }

    @Test
    public void test006_changeContainer() {
        ItemHistory diff1 = createEmptyHistory(6, 8, 2012);
        ItemHistory diff2 = createEmptyHistory(6, 8, 2010);
        Container c1 = createContainer(1, "C1");
        Container c2 = createContainer(1, "C2");
        diff1.setParentContainerNew(null);
        diff1.setParentContainerOld(c2);
        diff2.setParentContainerNew(c2);
        diff2.setParentContainerOld(c1);

        operation.applyNextNegativeDifference();
        Assert.assertEquals(c1.getId(), bean.getContainerController().getContainer().getId());
        operation.applyNextNegativeDifference();
        Assert.assertEquals(c2.getId(), bean.getContainerController().getContainer().getId());
        operation.applyNextPositiveDifference();
        Assert.assertEquals(c1.getId(), bean.getContainerController().getContainer().getId());
        operation.applyNextPositiveDifference();
        Assert.assertNull(bean.getContainerController().getContainer());
    }

    @Test
    public void test007_changePurity() {
        ItemHistory diff1 = createEmptyHistory(6, 8, 2012);
        ItemHistory diff2 = createEmptyHistory(6, 8, 2010);
        diff1.setPurityNew("latest purity");
        diff1.setPurityOld("intermediate purity");
        diff2.setPurityNew("intermediate purity");
        diff2.setPurityOld("first purity");

        operation.applyNextNegativeDifference();
        Assert.assertEquals("intermediate purity", state.getEditedItem().getPurity());
        operation.applyNextNegativeDifference();
        Assert.assertEquals("first purity", state.getEditedItem().getPurity());
        operation.applyNextPositiveDifference();
        Assert.assertEquals("intermediate purity", state.getEditedItem().getPurity());
        operation.applyNextPositiveDifference();
        Assert.assertEquals("latest purity", state.getEditedItem().getPurity());
    }

    @Test
    public void test008_changecontainerPosition() {
        createPositionList(8, 10, 2012, 1, 2, null, null);
        createPositionList(6, 10, 2012, null, null, 0, 0);

        operation.applyNextNegativeDifference();
        checkForPositions(bean.getContainerController().getItemPositions(), null);

        operation.applyNextNegativeDifference();
        checkForPositions(bean.getContainerController().getItemPositions(), new int[]{0, 0});

        operation.applyNextPositiveDifference();
        checkForPositions(bean.getContainerController().getItemPositions(), null);

        operation.applyNextPositiveDifference();
        checkForPositions(bean.getContainerController().getItemPositions(), new int[]{2, 1});

    }

    private void checkForPositions(boolean[][] positions, int[] expected) {
        for (int i = 0; i < positions.length; i++) {
            for (int j = 0; j < positions.length; j++) {
                if (expected == null) {
                    Assert.assertFalse(positions[i][j]);
                } else {
                    if (expected[0] == i && expected[1] == j) {
                        Assert.assertTrue(positions[i][j]);
                    } else {
                        Assert.assertFalse(positions[i][j]);
                    }
                }
            }
        }

    }

    private ItemHistory createEmptyHistory(int day, int month, int year) {
        ItemHistory diff1 = new ItemHistory();
        diff1.setActor(actor);
        diff1.setItem(item);
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        diff1.setMdate(cal.getTime());

        item.getHistory().put(cal.getTime(), Arrays.asList(diff1));
        return diff1;
    }

    private User createUser(int id, String name) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        return u;
    }

    private Project createProject(int id, String name) {
        Project p = new Project();
        p.setId(id);
        p.setName(name);
        return p;
    }

    private Container createContainer(int id, String label) {
        Container c = new Container();
        c.setId(id);
        c.setLabel(label);
        return c;
    }

    private ItemPositionHistoryList createPositionList(int day, int month, int year, Integer newRow, Integer newCol, Integer oldRow, Integer oldCol) {
        ItemPositionHistoryList positions = new ItemPositionHistoryList();
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        ItemPositionsHistory his = new ItemPositionsHistory();
        his.setmDate(cal.getTime());
        his.setUser(actor);
        his.setRowNew(newRow);
        his.setColNew(newCol);

        his.setRowOld(oldRow);
        his.setColOld(oldCol);
        positions.addHistory(his);
        item.getHistory().put(cal.getTime(), Arrays.asList(positions));
        return positions;

    }
}