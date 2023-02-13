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
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.mock.UserBeanMock;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.ItemHistory;
import de.ipb_halle.lbac.items.ItemPositionHistoryList;
import de.ipb_halle.lbac.items.ItemPositionsHistory;
import de.ipb_halle.lbac.items.bean.ContainerController;
import de.ipb_halle.lbac.items.bean.ItemBean;
import de.ipb_halle.lbac.items.bean.ItemState;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author fmauz
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class HistoryOperationTest extends TestBase {

    @Inject
    private ContainerService containerService;

    @Inject
    private UserBeanMock userBean;
    private User actor;
    private ItemBean bean;
    private Item item;
    private HistoryOperation operation;
    private ItemState state;

    @BeforeEach
    public void init() {
        actor = new User();
        actor.setId(1);
        actor.setName("actor");
        item = new Item();
        userBean.setCurrentAccount(actor);
        Container c = createContainer(10, "ContainerWithDimension");
        c.setRows(4);
        c.setColumns(4);
        c.setItems(new Item[4][4]);
        item.setContainer(c);
        state = new ItemState(item);
        bean = new ItemBean();
        bean.setState(state);
        bean.setContainerController(
                new ContainerController(
                        item, containerService,
                        userBean, MessagePresenterMock.getInstance()));
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

        bean.getState().getCurrentHistoryDate();
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

    @Test
    public void test009_navigateInHistory() {
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        cal.setTime(now);
        cal.add(Calendar.YEAR, -1);
        Date oneYearAgo = cal.getTime();
        cal.add(Calendar.YEAR, -1);
        Date twoYearsAgo = cal.getTime();

        createEmptyHistory(oneYearAgo);
        // will become last item in history
        createEmptyHistory(now);
        // will become starting item in history
        createEmptyHistory(twoYearsAgo);

        // We start with now.
        Assert.assertEquals(now, state.getChangeDate());
        Assert.assertTrue(state.isLastHistoryItem());
        Assert.assertFalse(state.isStartingHistoryItem());

        // Can we go further into future? Nope.
        operation.applyNextPositiveDifference();
        Assert.assertEquals(now, state.getChangeDate());
        Assert.assertTrue(state.isLastHistoryItem());
        Assert.assertFalse(state.isStartingHistoryItem());

        // Go to oneYearAgo.
        operation.applyNextNegativeDifference();
        Assert.assertEquals(oneYearAgo, state.getChangeDate());
        Assert.assertFalse(state.isLastHistoryItem());
        Assert.assertFalse(state.isStartingHistoryItem());

        // Back to now.
        operation.applyNextPositiveDifference();
        Assert.assertEquals(now, state.getChangeDate());
        Assert.assertTrue(state.isLastHistoryItem());
        Assert.assertFalse(state.isStartingHistoryItem());

        // Go to twoYearsAgo.
        operation.applyNextNegativeDifference();
        operation.applyNextNegativeDifference();
        Assert.assertEquals(twoYearsAgo, state.getChangeDate());
        Assert.assertFalse(state.isLastHistoryItem());
        Assert.assertFalse(state.isStartingHistoryItem());

        // Go to original item.
        operation.applyNextNegativeDifference();
        Assert.assertEquals(null, state.getChangeDate());
        Assert.assertFalse(state.isLastHistoryItem());
        Assert.assertTrue(state.isStartingHistoryItem());

        // Can we go further back in time? Nope.
        operation.applyNextNegativeDifference();
        Assert.assertEquals(null, state.getChangeDate());
        Assert.assertFalse(state.isLastHistoryItem());
        Assert.assertTrue(state.isStartingHistoryItem());
    }

    private void checkForPositions(boolean[][] positions, int[] expected) {
        if (positions == null) {
            return;
        }
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
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);

        return createEmptyHistory(cal.getTime());
    }

    private ItemHistory createEmptyHistory(Date date) {
        ItemHistory diff = new ItemHistory();
        diff.setActor(actor);
        diff.setItem(item);
        diff.setMdate(date);

        item.getHistory().put(date, Arrays.asList(diff));
        return diff;
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

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("HistoryOperationTest.war")
                .addClass(Navigator.class)
                .addClass(ProjectService.class);
        return ItemDeployment.add(UserBeanDeployment.add(deployment));
    }
}
