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
package de.ipb_halle.lbac.items.service;

import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.EntityManagerService;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.container.service.ContainerPositionService;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.ItemDifference;
import de.ipb_halle.lbac.items.ItemHistory;
import de.ipb_halle.lbac.items.ItemPositionHistoryList;
import de.ipb_halle.lbac.items.ItemPositionsHistory;
import de.ipb_halle.lbac.items.search.ItemSearchRequestBuilder;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.util.units.Quantity;
import de.ipb_halle.lbac.util.units.Unit;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.UUID;

import jakarta.ejb.EJBException;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
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
public class ItemServiceTest extends TestBase {

    private static final long serialVersionUID = 1L;

    private Container c0, c1, c2;
    @Inject
    private ItemService instance;

    @Inject
    private EntityManagerService emService;

    @Inject
    private ContainerService containerService;

    @Inject
    private GlobalAdmissionContext globalContext;

    @Inject
    private ContainerPositionService positionService;

    @Inject
    private ProjectService projectService;
    private User owner;
    private Project project;
    private Integer ownerid;
    private Integer userGroups;

    String INSERT_MATERIAL_SQL = "INSERT INTO MATERIALS VALUES("
            + "1,"
            + "1,"
            + "now(),"
            + " %d, "
            + "%d, "
            + "false,%d)";

    @BeforeEach
    public void init() {
        creationTools = new CreationTools("", "", "", memberService, projectService);

        cleanItemsFromDb();
        cleanMaterialsFromDB();
        createAndSaveMaterial();

        c0 = new Container();
        c0.setBarCode(null);
        c0.setRows(3);
        c0.setColumns(3);
        c0.setFireArea("F1");
        c0.setGmoSafetyLevel("S0");
        c0.setLabel("R302");
        c0.setType(new ContainerType("ROOM", 90, false, true));
        c0.setItems(new Item[3][3]);

        c1 = new Container();
        c1.setBarCode("9845893457");
        c0.setRows(2);
        c0.setColumns(2);
        c1.setFireArea(c0.getFireArea());
        c1.setGmoSafetyLevel(c0.getGmoSafetyLevel());
        c1.setLabel("Schrank1");
        c1.setParentContainer(c0);
        c1.setType(new ContainerType("CUPBOARD", 90, true, false));
        c1.setItems(new Item[2][2]);

        c2 = new Container();
        c2.setBarCode("43753456");
        c2.setFireArea(c1.getFireArea());
        c2.setGmoSafetyLevel(c1.getGmoSafetyLevel());
        c2.setLabel("Karton3");
        c2.setParentContainer(c1);
        c2.setType(new ContainerType("CARTON", 90, true, false));

        containerService.saveContainer(c0);
        containerService.saveContainer(c1);
        containerService.saveContainer(c2);

    }

    @AfterEach
    public void finish() {
        cleanItemsFromDb();
        cleanMaterialsFromDB();
        entityManagerService.doSqlUpdate("DELETE FROM usersgroups where login='itemServiceTestUser'");
        entityManagerService.doSqlUpdate("DELETE FROM nested_containers");
        entityManagerService.doSqlUpdate("DELETE FROM containers");
    }

    @Test
    public void test001_saveAndLoadItem() {
        //Create and save one item
        Item item = createItem();
        instance.saveItem(item);
        Assert.assertEquals("Testcase 001: One Item must be found after save (native Query)", 1, emService.doSqlQuery("select * from items").size());

        //Load item by description
        ItemSearchRequestBuilder builder = new ItemSearchRequestBuilder(owner, 0, 25);
        builder.setDescription("description");
        SearchResult result = instance.loadItems(builder.build());
        List<Item> items = result.getAllFoundObjects(Item.class, nodeService.getLocalNode());
        Assert.assertEquals(1, instance.loadItemAmount(builder.build()));
        Assert.assertEquals("Testcase 001: One Item must be found after load", 1, items.size());
        checkItem(items.get(0));
        //Load item by label
        builder = new ItemSearchRequestBuilder(owner, 0, 25);
        builder.setLabel(item.getLabel());
        result = instance.loadItems(builder.build());
        items = result.getAllFoundObjects(Item.class, nodeService.getLocalNode());
        Assert.assertEquals(1, instance.loadItemAmount(builder.build()));
        Assert.assertEquals("Testcase 001: One Item must be found after load", 1, items.size());
        checkItem(items.get(0));

        //Load item by project
        builder = new ItemSearchRequestBuilder(owner, 0, 25);
        builder.setProjectName("%biochemi%");
        result = instance.loadItems(builder.build());
        items = result.getAllFoundObjects(Item.class, nodeService.getLocalNode());
        Assert.assertEquals(1, instance.loadItemAmount(builder.build()));
        Assert.assertEquals("Testcase 001: One Item must be found after load", 1, items.size());
        checkItem(items.get(0));

        //Load item by location
        builder = new ItemSearchRequestBuilder(owner, 0, 25);
        builder.setLocation("Schrank1");
        result = instance.loadItems(builder.build());
        items = result.getAllFoundObjects(Item.class, nodeService.getLocalNode());
        Assert.assertEquals(1, instance.loadItemAmount(builder.build()));
        Assert.assertEquals("Testcase 001: One Item must be found after load", 1, items.size());
        checkItem(items.get(0));

    }

    @Test
    public void test002_saveAndLoadItemHistory() throws InterruptedException {
        Item item = createItem();
        Item item2 = createItem();
        instance.saveItem(item);
        instance.saveItem(item2);

        ItemHistory history1 = new ItemHistory();
        Thread.sleep(100);
        Date date1 = new Date();
        history1.setActor(owner);
        history1.setItem(item);
        history1.setMdate(date1);
        history1.setAmountOld(23d);
        history1.setAmountNew(40d);
        history1.setAction("EDIT");
        history1.setParentContainerOld(null);
        history1.setParentContainerOld(null);
        instance.saveItemHistory(history1);
        Thread.sleep(100);
        ItemHistory history2 = new ItemHistory();
        history2.setActor(owner);
        Date date2 = new Date();
        history2.setItem(item);
        history2.setMdate(date2);
        history2.setConcentrationNew(0.5);
        history2.setConcentrationOld(32d);
        history2.setAction("EDIT");
        history2.setParentContainerOld(c0);
        history2.setParentContainerOld(null);
        instance.saveItemHistory(history2);
        Thread.sleep(100);
        Project project2 = creationTools.createProject("project2");
        ItemHistory history3 = new ItemHistory();
        history3.setActor(owner);
        history3.setItem(item);
        Date date3 = new Date();
        history3.setMdate(date3);
        history3.setProjectNew(project2);
        history3.setProjectOld(project);
        history3.setAction("EDIT");
        history3.setParentContainerOld(null);
        history3.setParentContainerOld(c0);
        instance.saveItemHistory(history3);
        Thread.sleep(100);
        User user2 = createUser("itemServiceTestUser", "itemServiceTestUser");
        ItemHistory history4 = new ItemHistory();
        history4.setActor(owner);
        history4.setItem(item);
        Date date4 = new Date();
        history4.setMdate(date4);
        history4.setOwnerNew(user2);
        history4.setOwnerOld(owner);
        history4.setAction("EDIT");
        history3.setParentContainerOld(c1);
        history3.setParentContainerOld(c0);
        instance.saveItemHistory(history4);
        Thread.sleep(100);
        ItemHistory history5 = new ItemHistory();
        history5.setActor(owner);
        history5.setItem(item2);
        history5.setMdate(new Date());
        history5.setOwnerNew(user2);
        history5.setOwnerOld(owner);
        history5.setAction("EDIT");
        instance.saveItemHistory(history5);

        SortedMap<Date, List<ItemDifference>> histories = instance.loadHistoryOfItem(item);

        Assert.assertEquals("4 Histories must be found", 4, histories.size());
        Iterator i = histories.keySet().iterator();

        Assert.assertTrue("test002: history1 differs", compareHistories(history1, histories.get(i.next())));
        Assert.assertTrue("test002: history2 differs", compareHistories(history2, histories.get(i.next())));
        Assert.assertTrue("test002: history3 differs", compareHistories(history3, histories.get(i.next())));
        Assert.assertTrue("test002: history4 differs", compareHistories(history4, histories.get(i.next())));

    }

    @Test
    public void test003_editUserRights() {
        Item item = createItem();
        instance.saveItem(item);
        ACList acl = new ACList();
        acl.setName("test003_editUserRights - test-acl");
        acl.addACE(owner, new ACPermission[]{ACPermission.permREAD});
        item.setACList(acl);
        instance.saveItem(item);
        Item i = instance.loadItemById(item.getId());
        Assert.assertEquals("test003_editUserRights - test-acl", i.getACList().getName());
    }

    @Test
    public void test004_moveItem() {
        //Create two items
        Item item = createItem();
        item = instance.saveItem(item);
        Item item2 = createItem();
        item2 = instance.saveItem(item2);
        //Create two wellplates with different dimensions
        Container wellPlate_1 = new Container();
        wellPlate_1.setRows(5);
        wellPlate_1.setColumns(2);
        wellPlate_1.setLabel("Wellplate-1");
        wellPlate_1.setParentContainer(c0);
        wellPlate_1.setType(new ContainerType("WELLPLATE", 50, false, true));
        wellPlate_1 = containerService.saveContainer(wellPlate_1);
        Container wellPlate_2 = new Container();
        wellPlate_2.setRows(1);
        wellPlate_2.setColumns(1);
        wellPlate_2.setLabel("Wellplate-2");
        wellPlate_2.setParentContainer(c0);
        wellPlate_2.setType(new ContainerType("WELLPLATE", 50, false, true));
        wellPlate_2 = containerService.saveContainer(wellPlate_2);
        //Put container to item container hierarchy
        item.getNestedContainer().add(wellPlate_1);
        item.getNestedContainer().add(c0);
        item2.getNestedContainer().add(wellPlate_2);
        item2.getNestedContainer().add(c0);
        //Move item 1 to wellplate 1
        Set<int[]> places = new HashSet<>();
        places.add(new int[]{0, 0});
        positionService.moveItemToNewPosition(item, wellPlate_1, places, owner, new Date());
        //Check if movement was correct
        Container loadedWellPlate = containerService.loadContainerById(wellPlate_1.getId());
        Assert.assertEquals(item.getId(), loadedWellPlate.getItemAtPos(0, 0).getId());
 
        //Move item 2 to wellplate 1
        places = new HashSet<>();
        places.add(new int[]{1, 1});
        positionService.moveItemToNewPosition(item2, wellPlate_1, places, owner, new Date());

        //Check if movement was correct
        loadedWellPlate = containerService.loadContainerById(wellPlate_1.getId());
        Assert.assertEquals(item.getId(), loadedWellPlate.getItemAtPos(0, 0).getId());
        Assert.assertEquals(item2.getId(), loadedWellPlate.getItemAtPos(1, 1).getId());
        // Check if item is now in new wellplate
        places = new HashSet<>();
        places.add(new int[]{0, 0});
        positionService.moveItemToNewPosition(item2, wellPlate_2, places, owner, new Date());
        loadedWellPlate = containerService.loadContainerById(wellPlate_2.getId());
        Assert.assertEquals(item2.getId(), loadedWellPlate.getItemAtPos(0, 0).getId());
        // check if item is removed from old wellplate
        loadedWellPlate = containerService.loadContainerById(wellPlate_1.getId());
        Assert.assertEquals(item.getId(), loadedWellPlate.getItemAtPos(0, 0).getId());
        Assert.assertNull(loadedWellPlate.getItemAtPos(1, 1));

        //Item should not be able to move to wellPlate_2 because item 2 is blocking the slot
        Assert.assertFalse(positionService.moveItemToNewPosition(item, wellPlate_2, places, owner, new Date()));
    }

    @Test
    public void test005_saveAndLoadEditedItem() {
        Item original = createItem();
        original.setContainer(c0);
        c0.getItems()[2][1] = original;
       

        original.setAmount(1.5);
        instance.saveItem(original);
        positionService.saveItemInContainer(original.getId(), c0.getId(), 2, 1);
    
        Set<int[]> foundPositions = c0.getPositionsOfItem(original.getId());
        for (int[] pos : foundPositions) {
            Assert.assertTrue(pos[0] == 2 && pos[1] == 1 || pos[0] == 1 && pos[1] == 1);
        }
        Assert.assertEquals(1, foundPositions.size());
        Item edited = original.copy();

        // change to new container with new positions
        edited.setContainer(c1);
        edited.setAmount(1.25);
        Set<int[]> positions = new HashSet<>();
        positions.add(new int[]{1, 1});
        instance.saveEditedItem(edited, original, owner, positions);

        Item loadedItem = instance.loadItemById(original.getId());

        Assert.assertEquals(c1.getId(), loadedItem.getContainer().getId());
        Assert.assertEquals(1, loadedItem.getHistory().size());
        List<ItemDifference> diffs = loadedItem.getHistory().values().iterator().next();
        Assert.assertEquals(2, diffs.size());
        for (ItemDifference d : diffs) {
            if (d instanceof ItemHistory) {
                ItemHistory history = (ItemHistory) d;
                Assert.assertEquals(c1.getId(), history.getParentContainerNew().getId());
                Assert.assertEquals(c0.getId(), history.getParentContainerOld().getId());
                Assert.assertEquals(1.5, history.getAmountOld(), 0.001);
                Assert.assertEquals(1.25, history.getAmountNew(), 0.001);
            } else {
                ItemPositionHistoryList history = (ItemPositionHistoryList) d;
                Assert.assertEquals(2, history.getPositionAdds().size() + history.getPositionRemoves().size());
                for (ItemPositionsHistory h : history.getPositionRemoves()) {
                    boolean x = Objects.equals(h.getColNew(), null) && Objects.equals(h.getRowNew(), null) && Objects.equals(h.getColOld(), 1) && Objects.equals(h.getRowOld(), 1);
                    boolean y = Objects.equals(h.getColNew(), null) && Objects.equals(h.getRowNew(), null) && Objects.equals(h.getColOld(), 2) && Objects.equals(h.getRowOld(), 1);
                    Assert.assertTrue(x || y);
                }
                for (ItemPositionsHistory h : history.getPositionAdds()) {
                    boolean z = Objects.equals(h.getColNew(), 1) && Objects.equals(h.getRowNew(), 1) && Objects.equals(h.getColOld(), null) && Objects.equals(h.getRowOld(), null);
                    Assert.assertTrue(z);
                }
            }
        }

        //change to new container without position
        original = edited;
        edited = original.copy();
        edited.setContainer(c2);
        instance.saveEditedItem(edited, original, owner, new HashSet<>());
        loadedItem = instance.loadItemById(original.getId());
        Assert.assertEquals(c2.getId(), loadedItem.getContainer().getId());
        Container loadedContainer = containerService.loadContainerById(c2.getId());
        Assert.assertNull(loadedContainer.getItems());
        diffs = loadedItem.getHistory().get(loadedItem.getHistory().lastKey());
        Assert.assertEquals(1, diffs.size());
        for (ItemDifference d : diffs) {
            if (d instanceof ItemHistory) {
                ItemHistory history = (ItemHistory) d;
                Assert.assertEquals(c2.getId(), history.getParentContainerNew().getId());
                Assert.assertEquals(c1.getId(), history.getParentContainerOld().getId());
            } else {
                ItemPositionHistoryList history = (ItemPositionHistoryList) d;
                Assert.assertEquals(1, history.getPositionRemoves().size());
                for (ItemPositionsHistory h : history.getPositionRemoves()) {
                    boolean x = Objects.equals(h.getColNew(), null) && Objects.equals(h.getRowNew(), null) && Objects.equals(h.getColOld(), 1) && Objects.equals(h.getRowOld(), 1);
                    Assert.assertTrue(x);
                }
            }
        }
        // remove the container
        original = edited;
        edited = original.copy();
        edited.setContainer(null);
        instance.saveEditedItem(edited, original, owner, new HashSet<>());
        loadedItem = instance.loadItemById(original.getId());
        Assert.assertNull(loadedItem.getContainer());
        diffs = loadedItem.getHistory().get(loadedItem.getHistory().lastKey());
        Assert.assertEquals(1, diffs.size());
        ItemHistory history = (ItemHistory) diffs.get(0);
        Assert.assertNull(history.getParentContainerNew());
        Assert.assertEquals(c2.getId(), history.getParentContainerOld().getId());

        //put into container  without positions
        original = edited;
        edited = original.copy();
        edited.setContainer(c1);
        instance.saveEditedItem(edited, original, owner, new HashSet<>());
        loadedItem = instance.loadItemById(original.getId());
        Assert.assertEquals(c1.getId(), loadedItem.getContainer().getId());
        diffs = loadedItem.getHistory().get(loadedItem.getHistory().lastKey());
        Assert.assertEquals(1, diffs.size());
        history = (ItemHistory) diffs.get(0);
        Assert.assertNull(history.getParentContainerOld());
        Assert.assertEquals(c1.getId(), history.getParentContainerNew().getId());
        //stay in the same  container without positions
        original = edited;
        edited = original.copy();
        edited.setContainer(c1);
        instance.saveEditedItem(edited, original, owner, new HashSet<>());
        loadedItem = instance.loadItemById(original.getId());
        Assert.assertEquals(c1.getId(), loadedItem.getContainer().getId());
        Assert.assertTrue(loadedItem.getHistory().size() > 0);
        //change container to a new one with positions
        original = edited;
        edited = original.copy();
        edited.setContainer(c0);
        positions.clear();
        positions.add(new int[]{0, 0});
       
        instance.saveEditedItem(edited, original, owner, positions);
        loadedItem = instance.loadItemById(original.getId());
        Assert.assertEquals(c0.getId(), loadedItem.getContainer().getId());
        positions = loadedItem.getContainer().getPositionsOfItem(original.getId());
        for (int[] pos : positions) {
            Assert.assertTrue(pos[0] == 0 && pos[1] == 0 || pos[0] == 1 && pos[1] == 0);
        }
        diffs = loadedItem.getHistory().get(loadedItem.getHistory().lastKey());
        Assert.assertEquals(2, diffs.size());
        for (ItemDifference d : diffs) {
            if (d instanceof ItemHistory) {
                history = (ItemHistory) d;
                Assert.assertEquals(c0.getId(), history.getParentContainerNew().getId());
                Assert.assertEquals(c1.getId(), history.getParentContainerOld().getId());
            } else {
                ItemPositionHistoryList l = (ItemPositionHistoryList) d;
                Assert.assertEquals(1, l.getPositionAdds().size());
                for (ItemPositionsHistory h : l.getPositionAdds()) {
                    boolean x = Objects.equals(h.getColNew(), 0) && Objects.equals(h.getRowNew(), 0) && Objects.equals(h.getColOld(), null) && Objects.equals(h.getRowOld(), null);
                    boolean y = Objects.equals(h.getColNew(), 1) && Objects.equals(h.getRowNew(), 0) && Objects.equals(h.getColOld(), null) && Objects.equals(h.getRowOld(), null);
                    Assert.assertTrue(x || y);
                }
            }
        }

        //remove item from container 
        original = edited;
        edited = original.copy();
        edited.setContainer(null);
        instance.saveEditedItem(edited, original, owner, new HashSet<>());
        //put item into container with positions
        original = edited;
        edited = original.copy();
        edited.setContainer(c0);
        positions.clear();
        positions.add(new int[]{0, 0});
        instance.saveEditedItem(edited, original, owner, positions);
        loadedItem = instance.loadItemById(original.getId());
        Assert.assertEquals(c0.getId(), loadedItem.getContainer().getId());
        positions = loadedItem.getContainer().getPositionsOfItem(original.getId());
        for (int[] pos : positions) {
            Assert.assertTrue(pos[0] == 0 && pos[1] == 0 || pos[0] == 1 && pos[1] == 0);
        }
        diffs = loadedItem.getHistory().get(loadedItem.getHistory().lastKey());
        Assert.assertEquals(2, diffs.size());
        for (ItemDifference d : diffs) {
            if (d instanceof ItemHistory) {
                history = (ItemHistory) d;
                Assert.assertEquals(c0.getId(), history.getParentContainerNew().getId());
                Assert.assertNull(history.getParentContainerOld());
            } else {
                ItemPositionHistoryList l = (ItemPositionHistoryList) d;
                Assert.assertEquals(1, l.getPositionAdds().size());
                for (ItemPositionsHistory h : l.getPositionAdds()) {
                    boolean x = Objects.equals(h.getColNew(), 0) && Objects.equals(h.getRowNew(), 0) && Objects.equals(h.getColOld(), null) && Objects.equals(h.getRowOld(), null);
                    boolean y = Objects.equals(h.getColNew(), 1) && Objects.equals(h.getRowNew(), 0) && Objects.equals(h.getColOld(), null) && Objects.equals(h.getRowOld(), null);
                    Assert.assertTrue(x || y);
                }
            }
        }
        //remove item from container 
        original = edited;
        edited = original.copy();
        edited.setContainer(null);
        instance.saveEditedItem(edited, original, owner, new HashSet<>());
        loadedItem = instance.loadItemById(original.getId());
        int oldHistorySize = loadedItem.getHistory().size();
        // save item with no container into no container
        original = edited;
        edited = original.copy();
        edited.setContainer(null);
        instance.saveEditedItem(edited, original, owner, new HashSet<>());
        loadedItem = instance.loadItemById(original.getId());
        Assert.assertEquals(oldHistorySize, loadedItem.getHistory().size());
    }

    @Test
    public void test006_saveAliquot() {
        Item parent = createItem();
        parent.setAmount(1.3);
        parent.setUnit(Unit.getUnit("kg"));
        parent = instance.saveItem(parent);

        Item aliquot = createItem();
        aliquot.setAmount(100.0);
        aliquot.setUnit(Unit.getUnit("ml"));
        aliquot.setParentId(parent.getId());

        // quantity has wrong quality
        assertThrows(EJBException.class, () -> instance.saveAliquot(aliquot, null, Quantity.create(10.0, Unit.getUnit("ml")), owner));

        // amount in parent would become negative 
        assertThrows(EJBException.class, () -> instance.saveAliquot(aliquot, null, Quantity.create(1.4, Unit.getUnit("kg")), owner));

        // can save aliquot successfully
        Item savedAliquot = instance.saveAliquot(aliquot, null, Quantity.create(250.0, Unit.getUnit("g")), owner);

        /*
         * assertions on aliquot
         */
        assertNotNull(savedAliquot.getId());
        assertEquals(100.0, savedAliquot.getAmount(), 0);
        assertEquals("ml", savedAliquot.getUnit().toString());

        /*
         * assertions on parent
         */
        parent = instance.loadItemById(parent.getId());
        assertEquals(1.05, parent.getAmount(), 0.0001);
        assertEquals("kg", parent.getUnit().toString());

        /*
         * assertions on parent history
         */
        SortedMap<Date, List<ItemDifference>> history = instance.loadHistoryOfItem(parent);
        assertEquals(1, history.size());

        List<ItemDifference> histEntry = history.values().iterator().next();
        assertEquals(1, histEntry.size());

        ItemHistory expectedHistory = new ItemHistory();
        expectedHistory.setAmountNew(1.05);
        expectedHistory.setAmountOld(1.3);
        expectedHistory.setAction("EDIT");
        expectedHistory.setActor(owner);
        expectedHistory.setItem(parent);
        assertTrue(compareHistories(expectedHistory, histEntry));
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("ItemServiceTest.war");

        return ItemDeployment.add(UserBeanDeployment.add(deployment));
    }

    private void createAndSaveMaterial() {
        owner = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        //Preparing project and material
        project = creationTools.createProject();
        userGroups = project.getUserGroups().getId();
        ownerid = owner.getId();

        entityManagerService.doSqlUpdate(String.format(INSERT_MATERIAL_SQL, userGroups, ownerid, project.getId()));
        entityManagerService.doSqlUpdate("INSERT INTO structures  VALUES(1,'',0,0,null)");
        entityManagerService.doSqlUpdate("INSERT INTO storages VALUES(1,1,'')");
        entityManagerService.doSqlUpdate("INSERT INTO material_indices VALUES(1,1,1,'TESTMATERIAL','de',0)");
        entityManagerService.doSqlUpdate("INSERT INTO material_indices VALUES(2,1,1,'TESTMATERIA2','en',0)");
    }

    private Item createItem() {

        Structure s = new Structure("", 0d, 0d, 1, new ArrayList<>(), project.getId(), new HazardInformation(), new StorageInformation(), null);
        Item item = new Item();
        item.setAmount(23d);

        item.setACList(GlobalAdmissionContext.getPublicReadACL());
        item.setUnit(Unit.getUnit("kg"));
        item.setArticle(null);
        item.setConcentration(32d);
        item.setContainer(c2);
        item.setContainerSize(100d);
        item.setDescription("description");
        item.setMaterial(s);
        item.setOwner(owner);
        item.setProject(project);
        item.setPurity("rein");
        item.setcTime(new Date());
        item.setSolvent(null);
        item.setLabel(UUID.randomUUID().toString());
        return item;
    }

    private boolean compareHistories(ItemHistory orig, List<ItemDifference> diffList) {

        ItemHistory loaded = (ItemHistory) diffList.get(0);
        boolean equal = orig.getAction().equals(loaded.getAction())
                && orig.getActor().getId().equals(loaded.getActor().getId())
                && Objects.equals(orig.getAmountNew(), loaded.getAmountNew())
                && Objects.equals(orig.getAmountOld(), loaded.getAmountOld())
                && Objects.equals(orig.getConcentrationNew(), loaded.getConcentrationNew())
                && Objects.equals(orig.getConcentrationOld(), loaded.getConcentrationOld())
                && Objects.equals(orig.getDescriptionNew(), loaded.getDescriptionNew())
                && Objects.equals(orig.getDescriptionOld(), loaded.getDescriptionOld())
                && Objects.equals(orig.getPurityNew(), loaded.getPurityNew())
                && Objects.equals(orig.getPurityOld(), loaded.getPurityOld())
                && Objects.equals(orig.getItem().getId(), loaded.getItem().getId());
        if (orig.getProjectNew() != null && orig.getProjectOld() != null) {
            equal = equal && orig.getProjectNew().getId() == loaded.getProjectNew().getId();
        }
        if (orig.getProjectOld() != null && loaded.getProjectOld() != null) {
            equal = equal && orig.getProjectOld().getId() == loaded.getProjectOld().getId();
        }
        if (orig.getOwnerNew() != null && loaded.getOwnerNew() != null) {
            equal = equal && orig.getOwnerNew().getId().equals(loaded.getOwnerNew().getId());
        }
        if (orig.getOwnerOld() != null && loaded.getOwnerOld() != null) {
            equal = equal && orig.getOwnerOld().getId().equals(loaded.getOwnerOld().getId());
        }
        return equal;
    }

    private void checkItem(Item loadedItem) {
        Assert.assertEquals("Testcase 001: Amount must be 23", 23d, (double) loadedItem.getAmount(), 0);
        Assert.assertEquals("Testcase 001: Unit must be kg", "kg", loadedItem.getUnit().getUnit());
        Assert.assertNull("Testcase 001: Article must be null", loadedItem.getArticle());
        Assert.assertEquals("Testcase 001: Concentration must be 32", 32d, (double) loadedItem.getConcentration(), 0);
        Assert.assertNotNull("Testcase 001: Parent container must be not null", loadedItem.getContainer());
        Assert.assertEquals("Testcase 001: containersize must be 100", 100d, loadedItem.getContainerSize(), 0);
        Assert.assertEquals("Testcase 001: Description must be 'description'", "description", loadedItem.getDescription());
        Assert.assertEquals("Testcase 001: Material id must be 1", 1, (int) loadedItem.getMaterial().getId());
        Assert.assertEquals("Testcase 001: Owner-id must be " + ownerid, owner.getId(), loadedItem.getOwner().getId());
        Assert.assertEquals("Testcase 001: Project-id must be " + project.getId(), project.getId(), loadedItem.getProject().getId());
        Assert.assertEquals("Testcase 001: Purity must be 'rein'", "rein", loadedItem.getPurity());
        Assert.assertEquals("Testcase 001: The direct Container must be found", 1, loadedItem.getNestedContainer().size());
        Assert.assertNull("Testcase 001: Solvent must be null", loadedItem.getSolvent());
        Assert.assertNotNull(loadedItem.getcTime());

        Assert.assertNotNull("Testcase 001: Material must not be null", loadedItem.getMaterial());
    }
}
