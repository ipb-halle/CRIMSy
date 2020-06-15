/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
import de.ipb_halle.lbac.admission.LdapProperties;
import de.ipb_halle.lbac.admission.SystemSettings;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.announcement.membership.MembershipOrchestrator;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.ACPermission;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.service.ArticleService;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.service.MaterialService;
import de.ipb_halle.lbac.material.service.MoleculeService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.project.ProjectType;
import de.ipb_halle.lbac.service.ACListService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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

    @Inject
    private ContainerNestingService nestingController;

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

    @Before
    @Override
    public void setUp() {
        super.setUp();
        cleanItemsFromDb();
        cleanMaterialsFromDB();

        creationTools = new CreationTools("", "", "", memberService, projectService);

        publicUser = memberService.loadUserById(UUID.fromString(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID));
        entityService.doSqlUpdate("delete from item_positions");
        entityService.doSqlUpdate("delete from items");
        entityService.doSqlUpdate("delete from containers");

        c0 = new Container();
        c0.setBarCode(null);
        c0.setDimension("3;3;1");
        c0.setFireSection("F1");
        c0.setGmosavety("S0");
        c0.setLabel("R302");
        c0.setType(new ContainerType("ROOM", 90));

        c1 = new Container();
        c1.setBarCode("9845893457");
        c1.setDimension("2;2;1");
        c1.setFireSection(c0.getFireSection());
        c1.setGmosavety(c0.getGmosavety());
        c1.setLabel("Schrank1");
        c1.setParentContainer(c0);
        c1.setType(new ContainerType("CUPBOARD", 90));

        c2 = new Container();
        c2.setBarCode("43753456");
        c2.setDimension(null);
        c2.setFireSection(c1.getFireSection());
        c2.setGmosavety(c1.getGmosavety());
        c2.setLabel("Karton3");
        c2.setParentContainer(c1);
        c2.setType(new ContainerType("CARTON", 90));

        c3 = new Container();
        c3.setBarCode("43753456");
        c3.setDimension(null);
        c3.setFireSection(c1.getFireSection());
        c3.setGmosavety(c1.getGmosavety());
        c3.setLabel("Karton5");
        c3.setParentContainer(c1);
        c3.setType(new ContainerType("CARTON", 90));
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

        p.setUserGroups(priviligedAcl);
        projectService.saveProjectToDb(p);

        instance.saveContainer(c0);
        instance.saveContainer(c1);
        instance.saveContainer(c2);

        // This container is not readable for the user because he has no 
        // READ right in the project
        Container c4 = new Container();
        c4.setBarCode("3840955");
        c4.setDimension(null);
        c4.setFireSection(c1.getFireSection());
        c4.setGmosavety(c1.getGmosavety());
        c4.setLabel("Karton3");
        c4.setProject(p);
        c4.setParentContainer(null);
        c4.setType(new ContainerType("CARTON", 190));
        instance.saveContainer(c4);

        List<Container> result = instance.loadContainers(publicUser);
        Assert.assertEquals("Three containers must be found", 3, result.size());
        Assert.assertNull("testcase 002: First container must have no parent", result.get(0).getParentContainer());
        Assert.assertNotNull("testcase 002: Second container must have a parent", result.get(1).getParentContainer());
        Assert.assertEquals("testcase 002: Second container must be container 1", c0.getId(), result.get(1).getParentContainer().getId());
        Assert.assertNotNull("testcase 003: Third container must have a parent", result.get(2).getParentContainer());
        Assert.assertEquals("testcase 003: Third container must be container 2", c1.getId(), result.get(2).getParentContainer().getId());
        Assert.assertNull("testcase 003: Third container must be have a container hirarchy of max 1 level", result.get(2).getParentContainer().getParentContainer());
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
        User owner = memberService.loadUserById(UUID.fromString(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID));
        String userGroups = p.getUserGroups().getId().toString();
        String ownerid = owner.getId().toString();
        createMaterial(userGroups, ownerid, p.getId());

        c0 = instance.saveContainer(c0);
        c1 = instance.saveContainer(c1);

        createItems(ownerid);

        //Add the items to the container
        instance.saveItemInContainer(1, c0.getId(), 0, 0);
        instance.saveItemInContainer(2, c0.getId(), 1, 2);
        instance.saveItemInContainer(3, c1.getId(), 0, 1);

        List<Object> o = entityManagerService.doSqlQuery("Select * from item_positions");
        Assert.assertEquals("Three items must be in container one and two", 3, o.size());

        //Check container one
        Item[][][] loadedItems = instance.loadItemsOfContainer(c0);
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (x == 0 && (y == 0)) {
                    checkItem1(loadedItems[x][y][0], "Container 1-Item-1: ");
                } else if (x == 1 && (y == 2)) {
                    checkItem2(loadedItems[x][y][0], "Container 1-Item-2: ");
                } else {
                    Assert.assertNull(loadedItems[x][y][0]);
                }
            }
        }
        //Check container two
        loadedItems = instance.loadItemsOfContainer(c1);
        for (int y = 0; y < 2; y++) {
            for (int x = 0; x < 2; x++) {
                if (x == 0 && (y == 1)) {
                    checkItem3(loadedItems[x][y][0], "Container 2-Item-3: ");
                } else {
                    Assert.assertNull(loadedItems[x][y][0]);
                }
            }
        }
    }

    @Test
    public void test005_similarContainerLabels() {
        instance.saveContainer(c0);
        instance.saveContainer(c1);
        instance.saveContainer(c2);
        Set<String> names = instance.getSimilarContainerNames("kart", publicUser);
        Assert.assertEquals(1, names.size());
        Assert.assertEquals("Karton3", names.iterator().next());

    }

    @Test
    public void test006_loadContainersWithCmap() {
        Project project = new Project();
        User user = memberService.loadUserById(UUID.fromString(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID));
        ACList oneUserAcl = new ACList();
        oneUserAcl.addACE(user, new ACPermission[]{ACPermission.permREAD});
        oneUserAcl = acListService.save(oneUserAcl);

        User testUser = createUser("testUser", "testUser");

        project.setName("Container Test Project");
        project.setOwner(user);
        project.setUserGroups(oneUserAcl);
        project.setProjectType(ProjectType.IT_PROJECT);

        projectService.saveProjectToDb(project);
        c1.setProject(project);
        instance.saveContainer(c0);
        instance.saveContainer(c1);
        instance.saveContainer(c2);
        instance.saveContainer(c3);
        instance.deactivateContainer(c0);

        List<Object> en = entityManagerService.doSqlQuery("SELECT * from containers");

        List<Container> loadedContainer = instance.loadContainers(testUser);

        entityManagerService.doSqlUpdate("UPDATE  containers SET deactivated=false");
        Assert.assertEquals(2, loadedContainer.size());
        Map<String, Object> cmap = new HashMap<>();
        cmap.put("id", c0.getId());
        loadedContainer = instance.loadContainers(testUser, cmap);
        Assert.assertEquals(1, loadedContainer.size());

        cmap.clear();
        cmap.put("project", "Container Test Project");
        loadedContainer = instance.loadContainers(testUser, cmap);
        Assert.assertEquals(0, loadedContainer.size());
        loadedContainer = instance.loadContainers(user, cmap);
        Assert.assertEquals(1, loadedContainer.size());

        cmap.clear();
        cmap.put("label", "R302");
        loadedContainer = instance.loadContainers(testUser, cmap);
        Assert.assertEquals(1, loadedContainer.size());

        cmap.clear();
        cmap.put("location", "R302");
        loadedContainer = instance.loadContainers(testUser, cmap);
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

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("ContainerServiceTest.war")
                .addClass(ContainerService.class)
                .addClass(ACListService.class)
                .addClass(SystemSettings.class)
                .addClass(ItemService.class)
                .addClass(MaterialService.class)
                .addClass(TaxonomyService.class)
                .addClass(TissueService.class)
                .addClass(ArticleService.class)
                .addClass(KeyManager.class)
                .addClass(UserBean.class)
                .addClass(MembershipOrchestrator.class)
                .addClass(MoleculeService.class)
                .addClass(LdapProperties.class)
                .addClass(ContainerNestingService.class)
                .addClass(ProjectService.class);
    }

    private void checkItem1(Item i, String testDesc) {
        Assert.assertNotNull(i);
        Assert.assertEquals(testDesc + "ID must be 1", 1d, i.getId(), 0);
        Assert.assertEquals(testDesc + "Amount must be 10", 10d, i.getAmount(), 0);
        Assert.assertNull(testDesc + "Artice must be null", i.getArticle());
        Assert.assertNull(testDesc + "Project must be null", i.getProject());
        Assert.assertEquals(testDesc + "Concenttration must be zero", 0d, i.getConcentration(), 0);
        Assert.assertEquals(testDesc + "Unit must be kg", "kg", i.getUnit());
        Assert.assertEquals(testDesc + "Purity must be 'unknown'", "unknown", i.getPurity());
        Assert.assertNull(testDesc + "Solvent must be null", i.getSolvent());
        Assert.assertEquals(testDesc + "Description must be item 1", "item 1", i.getDescription());
        Assert.assertNull(testDesc + "Solvent must be null", i.getSolvent());
        Assert.assertEquals(testDesc + "Owner must be PUBLIC USER", GlobalAdmissionContext.PUBLIC_ACCOUNT_ID, i.getOwner().getId().toString());
        Assert.assertNull(testDesc + "Direct containersize must be null", i.getContainerSize());
        Assert.assertNull(testDesc + "Direct containertype must be null", i.getContainerType());
        Assert.assertNotNull(testDesc + "Creationtime must be not null", i.getcTime());
    }

    private void checkItem2(Item i, String testDesc) {
        Assert.assertNotNull(i);
        Assert.assertEquals(testDesc + "ID must be 2", 2d, i.getId(), 0);
        Assert.assertEquals(testDesc + "Amount must be 5", 5d, i.getAmount(), 0);
        Assert.assertNull(testDesc + "Artice must be null", i.getArticle());
        Assert.assertNull(testDesc + "Project must be null", i.getProject());
        Assert.assertEquals(testDesc + "Concenttration must be zero", 0d, i.getConcentration(), 0);
        Assert.assertEquals(testDesc + "Unit must be g", "g", i.getUnit());
        Assert.assertEquals(testDesc + "Purity must be 'pure'", "pure", i.getPurity());
        Assert.assertNull(testDesc + "Solvent must be null", i.getSolvent());
        Assert.assertEquals(testDesc + "Description must be item 2", "item 2", i.getDescription());
        Assert.assertNull(testDesc + "Solvent must be null", i.getSolvent());
        Assert.assertEquals(testDesc + "Owner must be PUBLIC USER", GlobalAdmissionContext.PUBLIC_ACCOUNT_ID, i.getOwner().getId().toString());
        Assert.assertNull(testDesc + "Direct containersize must be null", i.getContainerSize());
        Assert.assertNull(testDesc + "Direct containertype must be null", i.getContainerType());
        Assert.assertNotNull(testDesc + "Creationtime must be not null", i.getcTime());
    }

    private void checkItem3(Item i, String testDesc) {
        Assert.assertNotNull(i);
        Assert.assertEquals(testDesc + "ID must be 3", 3d, i.getId(), 0);
        Assert.assertEquals(testDesc + "Amount must be 11", 11d, i.getAmount(), 0);
        Assert.assertNull(testDesc + "Artice must be null", i.getArticle());
        Assert.assertNull(testDesc + "Project must be null", i.getProject());
        Assert.assertEquals(testDesc + "Concenttration must be zero", 0d, i.getConcentration(), 0);
        Assert.assertEquals(testDesc + "Unit must be mg", "mg", i.getUnit());
        Assert.assertEquals(testDesc + "Purity must be 'xxx'", "xxx", i.getPurity());
        Assert.assertNull(testDesc + "Solvent must be null", i.getSolvent());
        Assert.assertEquals(testDesc + "Description must be item 3", "item 3", i.getDescription());
        Assert.assertNull(testDesc + "Solvent must be null", i.getSolvent());
        Assert.assertEquals(testDesc + "Owner must be PUBLIC USER", GlobalAdmissionContext.PUBLIC_ACCOUNT_ID, i.getOwner().getId().toString());
        Assert.assertNull(testDesc + "Direct containersize must be null", i.getContainerSize());
        Assert.assertNull(testDesc + "Direct containertype must be null", i.getContainerType());
        Assert.assertNotNull(testDesc + "Creationtime must be not null", i.getcTime());
    }

    private void createMaterial(String userGroups, String ownerid, int projectid) {
        String sql = "INSERT INTO MATERIALS VALUES("
                + "1,"
                + "1,"
                + "now(),"
                + "cast('" + userGroups + "' as UUID),"
                + "cast('" + ownerid + "' as UUID),"
                + "false,"
                + projectid + ")";

        entityManagerService.doSqlUpdate(sql);
        entityManagerService.doSqlUpdate("INSERT INTO structures  VALUES(1,'',0,0,null)");
        entityManagerService.doSqlUpdate("INSERT INTO storages VALUES(1,1,'')");
        entityManagerService.doSqlUpdate("INSERT INTO material_indices VALUES(1,1,1,'TESTMATERIAL','de',0)");
        entityManagerService.doSqlUpdate("INSERT INTO material_indices VALUES(2,1,1,'TESTMATERIA2','en',0)");
    }

    private void createItems(String ownerid) {
        String sql = "INSERT INTO items values(1,1,10,null,null,0,'kg','unknown',null,'item 1',cast('" + ownerid + "' as UUID),null,null," + c0.getId() + ",now())";
        entityManagerService.doSqlUpdate(sql);
        sql = "INSERT INTO items values(2,1,5,null,null,0,'g','pure',null,'item 2',cast('" + ownerid + "' as UUID),null,null," + c0.getId() + ",now())";
        entityManagerService.doSqlUpdate(sql);
        sql = "INSERT INTO items values(3,1,11,null,null,0,'mg','xxx',null,'item 3',cast('" + ownerid + "' as UUID),null,null," + c1.getId() + ",now())";
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
        c0.setType(new ContainerType("ROOM", 100));
        c0.setLabel("C0");
        instance.saveContainer(c0);
        Container c1 = new Container();
        c1.setType(new ContainerType("ROOM", 99));
        c1.setLabel("C1");
        c1.setParentContainer(c0);
        instance.saveContainer(c1);
        Container c2 = new Container();
        c2.setType(new ContainerType("ROOM", 99));
        c2.setLabel("C2");
        instance.saveContainer(c2);
        Container c3 = new Container();
        c3.setType(new ContainerType("ROOM", 98));
        c3.setLabel("C3");
        c3.setParentContainer(c1);
        instance.saveContainer(c3);
        Container c4 = new Container();
        c4.setType(new ContainerType("ROOM", 97));
        c4.setLabel("C4");
        c4.setParentContainer(c3);
        instance.saveContainer(c4);
        Container c5 = new Container();
        c5.setType(new ContainerType("ROOM", 96));
        c5.setLabel("C5");
        c5.setParentContainer(c4);
        instance.saveContainer(c5);

        return new Container[]{c5, c4, c3, c2, c1, c0};
    }
}
