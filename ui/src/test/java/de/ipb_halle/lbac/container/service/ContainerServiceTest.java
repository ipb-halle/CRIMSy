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
package de.ipb_halle.lbac.container.service;

import de.ipb_halle.lbac.EntityManagerService;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.project.ProjectType;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.mocks.StructureInformationSaverMock;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.util.Unit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class ContainerServiceTest extends TestBase {

    Container c0;
    Container c1;
    Container c2;
    Container c3;

    private CreationTools creationTools;
    private User publicUser;

    @Inject
    private ContainerService instance;

    @Inject
    private ACListService acListService;

    @Inject
    private EntityManagerService entityService;

    @Inject
    private ProjectService projectService;

    @Inject
    private GlobalAdmissionContext globalContext;

    @Inject
    private ContainerPositionService positionService;

    @Inject
    private MaterialService materialService;

    @Inject
    private ItemService itemService;

    @Before
    public void init() {
        cleanItemsFromDb();
        cleanMaterialsFromDB();
        materialService.setStructureInformationSaver(new StructureInformationSaverMock(materialService.getEm()));

        creationTools = new CreationTools("", "", "", memberService, projectService);

        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);

        c0 = new Container();
        c0.setBarCode(null);
        c0.setColumns(3);
        c0.setRows(3);
        c0.setFireArea("F1");
        c0.setGmoSafetyLevel("S0");
        c0.setLabel("R302");
        c0.setType(new ContainerType("ROOM", 90, false, true));

        c1 = new Container();
        c1.setBarCode("9845893457");
        c1.setColumns(2);
        c1.setRows(2);
        c1.setFireArea(c0.getFireArea());
        c1.setGmoSafetyLevel(c0.getGmoSafetyLevel());
        c1.setLabel("Schrank1");
        c1.setParentContainer(c0);
        c1.setType(new ContainerType("CUPBOARD", 90, true, false));

        c2 = new Container();
        c2.setBarCode("43753456");
        c2.setFireArea(c1.getFireArea());
        c2.setGmoSafetyLevel(c1.getGmoSafetyLevel());
        c2.setLabel("Karton3");
        c2.setParentContainer(c1);
        c2.setType(new ContainerType("CARTON", 90, true, false));

        c3 = new Container();
        c3.setBarCode("43753456");
        c3.setFireArea(c1.getFireArea());
        c3.setGmoSafetyLevel(c1.getGmoSafetyLevel());
        c3.setLabel("Karton5");
        c3.setParentContainer(c1);
        c3.setType(new ContainerType("CARTON", 90, true, false));
    }

    @After
    public void finish() {

        super.cleanItemsFromDb();

    }

    @Test
    public void test001_saveContainer() {

        instance.saveContainer(c0);
        instance.saveContainer(c1);
        instance.saveContainer(c2);

        Assert.assertNotNull(c0.getId());
        Assert.assertNotNull(c1.getId());
        Assert.assertNotNull(c2.getId());
        Assert.assertEquals(3, (int) entityService.doSqlQuery("select * from containers").size());

        List<Object> nestedContainer = entityService.doSqlQuery("select sourceid,targetid,nested from nested_containers order by sourceid,targetid");
        Assert.assertEquals(3, (int) nestedContainer.size());
        int[] targetSources = new int[]{c1.getId(), c2.getId(), c2.getId()};
        int[] targetTargets = new int[]{c0.getId(), c0.getId(), c1.getId()};
        boolean[] targetNested = new boolean[]{false, true, false};
        for (int i = 0; i < 3; i++) {
            Object[] nc = (Object[]) nestedContainer.get(i);
            Assert.assertEquals(targetSources[i], (int) nc[0]);
            Assert.assertEquals(targetTargets[i], (int) nc[1]);
            Assert.assertEquals(targetNested[i], (boolean) nc[2]);
        }
    }

    @Test
    public void test002_loadContainer() {

        User secondUser = createUser("testUser", "testUser");
        Project p = new Project(ProjectType.BIOCHEMICAL_PROJECT, "testproject");
        p.setOwner(secondUser);
        ACList priviligedAcl = new ACList();
        priviligedAcl.addACE(secondUser, new ACPermission[]{ACPermission.permREAD});
        acListService.save(priviligedAcl);

        p.setACList(priviligedAcl);
        projectService.saveProjectToDb(p);

        instance.saveContainer(c0);
        instance.saveContainer(c1);
        instance.saveContainer(c2);

        // This container is not readable for the user because he has no 
        // READ right in the project
        Container c4 = new Container();
        c4.setBarCode("3840955");
        c4.setFireArea(c1.getFireArea());
        c4.setGmoSafetyLevel(c1.getGmoSafetyLevel());
        c4.setLabel("Karton3");
        c4.setProject(p);
        c4.setParentContainer(null);
        c4.setType(new ContainerType("CARTON", 190, true, false));
        instance.saveContainer(c4);

        List<Container> result = instance.loadContainersWithoutItems(publicUser);
        Assert.assertEquals("Three containers must be found", 3, result.size());
        Assert.assertNull("testcase 002: First container must have no parent", result.get(0).getParentContainer());
        Assert.assertNotNull("testcase 002: Second container must have a parent", result.get(1).getParentContainer());
        Assert.assertEquals("testcase 002: Second container must be container 1", c0.getId(), result.get(1).getParentContainer().getId());
        Assert.assertNotNull("testcase 003: Third container must have a parent", result.get(2).getParentContainer());
        Assert.assertEquals("testcase 003: Third container must be container 2", c1.getId(), result.get(2).getParentContainer().getId());
        Assert.assertNull("testcase 003: Third container must be have a container hirarchy of max 2 level", result.get(2).getParentContainer().getParentContainer().getParentContainer());
    }

    /**
     * Adds items into containers at specified positions
     */
    @Test
    public void test003_saveLoadItemsInContainer() {
        /**
         * Initialising the neccessary components for adding:
         * <ol>
         * <li>a project</li>
         * <li> a material</li>
         * <li> three different items</li>
         * <li>2 different container</li>
         * </ol>
         */
        Project p = creationTools.createProject();
        User owner = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        String userGroups = p.getUserGroups().getId().toString();
        String ownerid = owner.getId().toString();
        createMaterial(userGroups, ownerid, p.getId());

        c0 = instance.saveContainer(c0);
        c1 = instance.saveContainer(c1);

        createItems(ownerid);

        //Add the items to the container
        positionService.saveItemInContainer(1, c0.getId(), 0, 0);
        positionService.saveItemInContainer(2, c0.getId(), 1, 2);
        positionService.saveItemInContainer(3, c1.getId(), 0, 1);

        List<Object> o = entityManagerService.doSqlQuery("Select * from item_positions");
        Assert.assertEquals("Three items must be in container one and two", 3, o.size());

        //Check container one
        Item[][] loadedItems = instance.loadItemsOfContainer(c0);
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (x == 0 && (y == 0)) {
                    Assert.assertEquals(1, loadedItems[x][y].getId(), 0);
                } else if (x == 1 && (y == 2)) {
                    Assert.assertEquals(2, loadedItems[x][y].getId(), 0);
                } else {
                    Assert.assertNull(loadedItems[x][y]);
                }
            }
        }
        //Check container two
        loadedItems = instance.loadItemsOfContainer(c1);
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 2; x++) {
                if (x == 0 && (y == 1)) {
                    Assert.assertEquals(3, loadedItems[x][y].getId(), 0);
                } else {
                    Assert.assertNull(loadedItems[x][y]);
                }
            }
        }
        Assert.assertTrue(3 == positionService.getItemIdAtPosition(c1.getId(), 0, 1));
        Assert.assertTrue(positionService.saveItemInContainer(3, c1.getId(), 0, 1));
        Assert.assertFalse(positionService.saveItemInContainer(4, c1.getId(), 0, 1));

    }

    @Test
    public void test005_similarContainerLabels() {
        instance.saveContainer(c0);
        instance.saveContainer(c1);
        instance.saveContainer(c2);
//        Set<Container> names = instance.getSimilarContainerNames("kart", publicUser);
//        Assert.assertEquals(1, names.size());
//        Assert.assertEquals("Karton3", names.iterator().next());

    }

    @Test
    public void test006_loadContainersWithCmap() {
        Project project = new Project();
        User user = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        ACList oneUserAcl = new ACList();
        oneUserAcl.addACE(user, new ACPermission[]{ACPermission.permREAD});
        oneUserAcl = acListService.save(oneUserAcl);

        User testUser = createUser("testUser", "testUser");

        project.setName("Container Test Project");
        project.setOwner(user);
        project.setACList(oneUserAcl);
        project.setProjectType(ProjectType.IT_PROJECT);

        projectService.saveProjectToDb(project);
        c1.setProject(project);
        instance.saveContainer(c0);
        instance.saveContainer(c1);
        instance.saveContainer(c2);
        instance.saveContainer(c3);
        instance.deactivateContainer(c0);

        List<Object> en = entityManagerService.doSqlQuery("SELECT * from containers");

        List<Container> loadedContainer = instance.loadContainersWithoutItems(testUser);

        entityManagerService.doSqlUpdate("UPDATE  containers SET deactivated=false");
        Assert.assertEquals(2, loadedContainer.size());
        Map<String, Object> cmap = new HashMap<>();
        cmap.put("id", c0.getId());
        loadedContainer = instance.loadContainersWithoutItems(testUser, cmap);
        Assert.assertEquals(1, loadedContainer.size());

        cmap.clear();
        cmap.put("project", "Container Test Project");
        loadedContainer = instance.loadContainersWithoutItems(testUser, cmap);
        Assert.assertEquals(0, loadedContainer.size());
        loadedContainer = instance.loadContainersWithoutItems(user, cmap);
        Assert.assertEquals(1, loadedContainer.size());

        cmap.clear();
        cmap.put("label", "R302");
        loadedContainer = instance.loadContainersWithoutItems(testUser, cmap);
        Assert.assertEquals(1, loadedContainer.size());

        cmap.clear();
        cmap.put("location", "R302");
        loadedContainer = instance.loadContainersWithoutItems(testUser, cmap);
        Assert.assertEquals(2, loadedContainer.size());
        this.entityManagerService.doSqlUpdate("Delete from nested_containers");
        cleanItemsFromDb();
        this.entityManagerService.doSqlUpdate("Delete from containers");
        cleanProjectFromDB(project, true);
    }

    @Test
    public void test007_loadContainerByName() {
        instance.saveContainer(c0);
        Assert.assertNotNull(instance.loadContainerByName("R302"));
        Assert.assertNotNull(instance.loadContainerByName("r302"));
        Assert.assertNull(instance.loadContainerByName("R30"));
        Assert.assertNull(instance.loadContainerByName(""));
    }

    @Test
    public void test008_editContainer() {
        Container[] container = test008_initializeContainer();
        test008_testPrecondition(container);
        container[2].setParentContainer(container[3]);
        instance.saveEditedContainer(container[2]);
        int[][] expectation = new int[][]{
            {container[0].getId(), container[1].getId()},
            {container[0].getId(), container[2].getId()},
            {container[0].getId(), container[3].getId()},
            {container[1].getId(), container[2].getId()},
            {container[1].getId(), container[3].getId()},
            {container[2].getId(), container[3].getId()},
            {container[4].getId(), container[5].getId()}};
        test008_testPostcondition(expectation);

        container[2].setParentContainer(null);
        instance.saveEditedContainer(container[2]);
        expectation = new int[][]{
            {container[0].getId(), container[1].getId()},
            {container[0].getId(), container[2].getId()},
            {container[1].getId(), container[2].getId()},
            {container[4].getId(), container[5].getId()}};
        test008_testPostcondition(expectation);
    }

    /**
     * The test ensures that when the container hierarchy is broken open, the
     * orphaned nenesting entries are completely removed
     *
     */
    // BEFORE
    // *          |- c3
    // * c1 - c2 -
    // *          |- c4
    // * AFTER
    // * c1
    // *
    // *    |- c3
    // * c2 -
    // *    |- c4
    // */
    @Test
    public void test009_removeParent() {
        String CHECK_SQL = "SELECT * FROM nested_containers order by sourceid";

        instance.saveContainer(c0);
        instance.saveContainer(c1);
        instance.saveContainer(c2);
        instance.saveContainer(c3);

        ArrayList<Object[]> i = (ArrayList) entityManagerService.doSqlQuery(CHECK_SQL);
        Assert.assertEquals(5, i.size());
        // Container c1 is in c0
        Assert.assertEquals(c1.getId(), (int) i.get(0)[0],0);
        Assert.assertEquals(c0.getId(), (int) i.get(0)[1],0);
        // Container c2 is in c1 and indirect c0
        Assert.assertEquals(c2.getId(), (int) i.get(1)[0],0);
        Assert.assertEquals(c1.getId(), (int) i.get(1)[1],0);
        Assert.assertEquals(c2.getId(), (int) i.get(2)[0],0);
        Assert.assertEquals(c0.getId(), (int) i.get(2)[1],0);
        // Container c3 is in c1 and indirect in c0
        Assert.assertEquals(c3.getId(), (int) i.get(3)[0],0);
        Assert.assertEquals(c1.getId(), (int) i.get(3)[1],0);
        Assert.assertEquals(c3.getId(), (int) i.get(4)[0],0);
        Assert.assertEquals(c0.getId(), (int) i.get(4)[1],0);

        //remove the link between c0 and c1
        c1.setParentContainer(null);
        instance.saveEditedContainer(c1);
        i = (ArrayList) entityManagerService.doSqlQuery(CHECK_SQL);
        Assert.assertEquals(2, i.size());
        // Container c2 is in c1 and no more in c0
        Assert.assertEquals(c2.getId(), (int) i.get(0)[0],0);
        Assert.assertEquals(c1.getId(), (int) i.get(0)[1],0);
        // Container c3 is in c1 and no more in c0
        Assert.assertEquals(c3.getId(), (int) i.get(1)[0],0);
        Assert.assertEquals(c1.getId(), (int) i.get(1)[1],0);
    }

    @Test
    public void test010_loadContainersWithManyItems() {
        Project project = creationTools.createAndSaveProject("ContainerServiceTest_test010_loadContainersWithManyItems_project");
        instance.saveContainer(c0);

        for (int i = 0; i < 4; i++) {
            Container wellPlate = createWellPlate(String.format("wp%d", i + 1));
            instance.saveContainer(wellPlate);
            for (int j = 0; j < 70; j++) {
                Structure s = creationTools.createStructure(project);
                s.setMolecule(null);
                materialService.saveMaterialToDB(s, project.getACList().getId(), new HashMap(), publicUser);
                Item item = createAndSaveItem(wellPlate, project, s);
                positionService.saveItemInContainer(item.getId(), wellPlate.getId(), i % 8, (i / 8));
            }
        }

        List<Container> loadedContainers = instance.loadContainersWithoutItems(publicUser);
        Assert.assertEquals(5, loadedContainers.size());
    }

    private Item createAndSaveItem(Container c, Project p, Structure s) {
        Item item = new Item();
        item.setACList(p.getACList());
        item.setOwner(publicUser);
        item.setAmount(0d);
        item.setConcentration(0d);
        item.setConcentrationUnit(Unit.getUnit("%"));
        item.setContainer(c);
        item.setMaterial(s);
        item.setProject(p);
        item.setUnit(Unit.getUnit("g"));
        item.setcTime(new Date());
        itemService.saveItem(item);
        return item;

    }

    private Container createWellPlate(String name) {
        Container wellPlate = new Container();
        wellPlate.setBarCode(name);
        wellPlate.setColumns(12);
        wellPlate.setRows(8);
        wellPlate.setFireArea(c0.getFireArea());
        wellPlate.setGmoSafetyLevel(c0.getGmoSafetyLevel());
        wellPlate.setLabel(name);
        wellPlate.setParentContainer(c0);
        wellPlate.setType(new ContainerType("WELLPLATE", 90, true, false));
        return wellPlate;
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("ContainerServiceTest.war");
        return ItemDeployment.add(UserBeanDeployment.add(deployment));
    } 

    private void createMaterial(String userGroups, String ownerid, int projectid) {
        String sql = "INSERT INTO MATERIALS VALUES("
                + "1,"
                + "1,"
                + "now(),"
                + " " + userGroups + ", "
                + " " + ownerid + ", "
                + "false,"
                + projectid + ")";

        entityManagerService.doSqlUpdate(sql);
        entityManagerService.doSqlUpdate("INSERT INTO structures  VALUES(1,'',0,0,null)");
        entityManagerService.doSqlUpdate("INSERT INTO storages VALUES(1,1,'')");
        entityManagerService.doSqlUpdate("INSERT INTO material_indices VALUES(1,1,1,'TESTMATERIAL','de',0)");
        entityManagerService.doSqlUpdate("INSERT INTO material_indices VALUES(2,1,1,'TESTMATERIA2','en',0)");
    }

    private void createItems(String ownerid) {
        String sql = "INSERT INTO items "
                + " (id,materialid,amount, articleid,projectid,concentration, "
                + "unit,purity,solventid,description,owner_id,containersize,containertype, "
                + "containerid,ctime,aclist_id) "
                + "values("
                + "1,1,10,null,null,0,'kg','unknown',null,'item 1', "
                + " " + ownerid + " ,null,null," + c0.getId() + ",now(), "
                + " " + globalContext.getAdminOnlyACL().getId() + "  )";

        entityManagerService
                .doSqlUpdate(sql);
        sql = "INSERT INTO items "
                + " (id,materialid,amount, articleid,projectid,concentration, "
                + "unit,purity,solventid,description,owner_id,containersize,containertype, "
                + "containerid,ctime,aclist_id) "
                + "values(2,1,5,null,null,0,'g','pure',null,'item 2', " + ownerid + " ,null,null," + c0.getId() + ",now(), "
                + " " + globalContext.getAdminOnlyACL().getId() + " )";

        entityManagerService.doSqlUpdate(sql);
        sql = "INSERT INTO items "
                + " (id,materialid,amount, articleid,projectid,concentration, "
                + "unit,purity,solventid,description,owner_id,containersize,containertype, "
                + "containerid,ctime,aclist_id) "
                + "values(3,1,11,null,null,0,'mg','xxx',null,'item 3', " + ownerid + " ,null,null," + c1.getId() + ",now(), "
                + " " + globalContext.getAdminOnlyACL().getId() + " )";
        entityManagerService.doSqlUpdate(sql);
    }

    @SuppressWarnings("unchecked")
    private void test008_testPostcondition(int[][] expectation) {
        List<Object[]> nested = (List) entityManagerService.doSqlQuery("SELECT CAST(sourceid AS INTEGER),CAST(targetid AS INTEGER) from nested_containers order by sourceid DESC,targetid DESC");
        Assert.assertEquals(expectation.length, nested.size());
        for (int i = 0; i < expectation.length; i++) {
            int sourceid = (Integer) nested.get(i)[0];
            int targetid = (Integer) nested.get(i)[1];
            Assert.assertTrue("test008(postcondition): unexpected source at index " + i, expectation[i][0] == sourceid);
            Assert.assertTrue("test008(postcondition): unexpected target at index " + i, expectation[i][1] == targetid);
        }
    }

    @SuppressWarnings("unchecked")
    private void test008_testPrecondition(Container[] container) {

        int[][] expectation = new int[][]{
            {container[0].getId(), container[1].getId()},
            {container[0].getId(), container[2].getId()},
            {container[0].getId(), container[4].getId()},
            {container[0].getId(), container[5].getId()},
            {container[1].getId(), container[2].getId()},
            {container[1].getId(), container[4].getId()},
            {container[1].getId(), container[5].getId()},
            {container[2].getId(), container[4].getId()},
            {container[2].getId(), container[5].getId()},
            {container[4].getId(), container[5].getId()}};
        List<Object[]> nested = (List) entityManagerService.doSqlQuery("SELECT CAST(sourceid AS INTEGER),CAST(targetid AS INTEGER) from nested_containers order by sourceid DESC,targetid DESC");
        Assert.assertEquals(10, nested.size());
        for (int i = 0; i < 10; i++) {
            int sourceid = (Integer) nested.get(i)[0];
            int targetid = (Integer) nested.get(i)[1];
            Assert.assertTrue("test008(precondition): unexpected source at index " + i, expectation[i][0] == sourceid);
            Assert.assertTrue("test008(precondition): unexpected target at index " + i, expectation[i][1] == targetid);
        }
    }

    private Container[] test008_initializeContainer() {
        Container c0 = new Container();
        c0.setType(new ContainerType("ROOM", 100, false, true));
        c0.setLabel("C0");
        instance.saveContainer(c0);
        Container c1 = new Container();
        c1.setType(new ContainerType("ROOM", 99, false, true));
        c1.setLabel("C1");
        c1.setParentContainer(c0);
        instance.saveContainer(c1);
        Container c2 = new Container();
        c2.setType(new ContainerType("ROOM", 99, false, true));
        c2.setLabel("C2");
        instance.saveContainer(c2);
        Container c3 = new Container();
        c3.setType(new ContainerType("ROOM", 98, false, true));
        c3.setLabel("C3");
        c3.setParentContainer(c1);
        instance.saveContainer(c3);
        Container c4 = new Container();
        c4.setType(new ContainerType("ROOM", 97, false, true));
        c4.setLabel("C4");
        c4.setParentContainer(c3);
        instance.saveContainer(c4);
        Container c5 = new Container();
        c5.setType(new ContainerType("ROOM", 96, false, true));
        c5.setLabel("C5");
        c5.setParentContainer(c4);
        instance.saveContainer(c5);

        return new Container[]{c5, c4, c3, c2, c1, c0};
    }
}
