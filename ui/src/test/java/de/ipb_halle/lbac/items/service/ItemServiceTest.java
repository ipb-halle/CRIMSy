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
package de.ipb_halle.lbac.items.service;

import de.ipb_halle.lbac.EntityManagerService;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.LdapProperties;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.announcement.membership.MembershipOrchestrator;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.items.Container;
import de.ipb_halle.lbac.items.ContainerType;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.ItemHistory;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.StorageClassInformation;
import de.ipb_halle.lbac.material.service.MaterialService;
import de.ipb_halle.lbac.material.service.MoleculeService;
import de.ipb_halle.lbac.material.subtype.structure.Structure;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.service.MemberService;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
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
public class ItemServiceTest extends TestBase {

    private Container c0, c1,c2;
    @Inject
    private ItemService instance;

    @Inject
    private EntityManagerService emService;

    @Inject
    private ContainerService containerService;

    @Inject
    private ProjectService projectService;
    private User owner;
    private Project project;
    private String ownerid;
    private String userGroups;
    private CreationTools creationTools;
    String INSERT_MATERIAL_SQL = "INSERT INTO MATERIALS VALUES("
            + "1,"
            + "1,"
            + "now(),"
            + "cast('%s' as UUID),"
            + "cast('%s' as UUID),"
            + "false,%d)";

    @Before
    @Override
    public void setUp() {
        super.setUp();
        creationTools = new CreationTools("", "", "", memberService, projectService);

        cleanItemsFromDb();
        cleanMaterialsFromDB();
        createAndSaveMaterial();

        c0 = new Container();
        c0.setBarCode(null);
        c0.setDimension("3;3;1");
        c0.setFireSection("F1");
        c0.setGvoClass("S0");
        c0.setLabel("R302");
        c0.setType(new ContainerType("ROOM", 90));

        c1 = new Container();
        c1.setBarCode("9845893457");
        c1.setDimension("2;2;1");
        c1.setFireSection(c0.getFireSection());
        c1.setGvoClass(c0.getGvoClass());
        c1.setLabel("Schrank1");
        c1.setParentContainer(c0);
        c1.setType(new ContainerType("CUPBOARD", 90));
        
         c2 = new Container();
        c2.setBarCode("43753456");
        c2.setDimension(null);
        c2.setFireSection(c1.getFireSection());
        c2.setGvoClass(c1.getGvoClass());
        c2.setLabel("Karton3");
        c2.setParentContainer(c1);
        c2.setType(new ContainerType("CARTON", 90));

        containerService.saveContainer(c0);
        containerService.saveContainer(c1);
        containerService.saveContainer(c2);

    }

    @After
    public void finish() {
        cleanItemsFromDb();
        cleanMaterialsFromDB();
        entityManagerService.doSqlUpdate("DELETE FROM usersgroups where login='itemServiceTestUser'");
        entityManagerService.doSqlUpdate("DELETE FROM nested_containers");
        entityManagerService.doSqlUpdate("DELETE FROM containers");
    }

    @Test
    public void test001_saveAndLoadItem() {
        instance.saveItem(createItem());
        Assert.assertEquals("Testcase 001: One Item must be found after save (native Query)", 1, emService.doSqlQuery("select * from items").size());

        Assert.assertEquals(1, instance.getItemAmount(owner, new HashMap<>()));
        List<Item> items = instance.loadItems(owner, new HashMap<>(), 0, 25);

        Assert.assertEquals("Testcase 001: One Item must be found after load", 1, items.size());
        Item loadedItem = items.get(0);
        Assert.assertEquals("Testcase 001: Amount must be 23", 23d, (double) loadedItem.getAmount(), 0);
        Assert.assertEquals("Testcase 001: Unit must be kg", "kg", loadedItem.getUnit());
        Assert.assertNull("Testcase 001: Article must be null", loadedItem.getArticle());
        Assert.assertEquals("Testcase 001: Concentration must be 32", 32d, (double) loadedItem.getConcentration(), 0);
        Assert.assertNotNull("Testcase 001: Parent container must be not null", loadedItem.getContainer());
        Assert.assertEquals("Testcase 001: containersize must be 100", 100d, loadedItem.getContainerSize(), 0);
        Assert.assertEquals("Testcase 001: Description must be 'description'", "description", loadedItem.getDescription());
        Assert.assertEquals("Testcase 001: Material id must be 1", 1, loadedItem.getMaterial().getId());
        Assert.assertEquals("Testcase 001: Owner-id must be " + ownerid, owner.getId(), loadedItem.getOwner().getId());
        Assert.assertEquals("Testcase 001: Project-id must be " + project.getId(), project.getId(), loadedItem.getProject().getId());
        Assert.assertEquals("Testcase 001: Purity must be 'rein'", "rein", loadedItem.getPurity());
        Assert.assertEquals("Testcase 001: One nested Container must be found", 2, items.get(0).getNestedContainer().size());
        Assert.assertNull("Testcase 001: Solvent must be null", loadedItem.getSolvent());
        Assert.assertNotNull(loadedItem.getcTime());

        Assert.assertNotNull("Testcase 001: Material must not be null", loadedItem.getMaterial());
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
        instance.saveItemHistory(history2);
        Thread.sleep(100);
        Project project2 = creationTools.createProject();
        ItemHistory history3 = new ItemHistory();
        history3.setActor(owner);
        history3.setItem(item);
        Date date3 = new Date();
        history3.setMdate(date3);
        history3.setProjectNew(project2);
        history3.setProjectOld(project);
        history3.setAction("EDIT");
        instance.saveItemHistory(history3);
        Thread.sleep(100);
        User user2 = createUser("itemServiceTestUser", "itemServiceTestUser", nodeService.getLocalNode(), memberService, membershipService);
        ItemHistory history4 = new ItemHistory();
        history4.setActor(owner);
        history4.setItem(item);
        Date date4 = new Date();
        history4.setMdate(date4);
        history4.setOwnerNew(user2);
        history4.setOwnerOld(owner);
        history4.setAction("EDIT");
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

        SortedMap<Date, ItemHistory> histories = instance.loadHistoryOfItem(item);

        Assert.assertEquals("4 Histories must be found", 4, histories.size());
        Iterator i = histories.keySet().iterator();

        Assert.assertTrue("test002: history1 differs", compareHistories(history1, histories.get(i.next())));
        Assert.assertTrue("test002: history2 differs", compareHistories(history2, histories.get(i.next())));
        Assert.assertTrue("test002: history3 differs", compareHistories(history3, histories.get(i.next())));
        Assert.assertTrue("test002: history4 differs", compareHistories(history4, histories.get(i.next())));
    }

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("ItemServiceTest.war")
                .addClass(EntityManagerService.class)
                .addClass(MemberService.class)
                .addClass(ProjectService.class)
                .addClass(MaterialService.class)
                .addClass(ContainerService.class)
                .addClass(KeyManager.class)
                .addClass(LdapProperties.class)
                .addClass(MoleculeService.class)
                .addClass(ArticleService.class)
                .addClass(MembershipOrchestrator.class)
                .addClass(UserBean.class)
                .addClass(ItemService.class);
    }

    private void createAndSaveMaterial() {
        owner = memberService.loadUserById(UUID.fromString(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID));
        //Preparing project and material
        project = creationTools.createProject();
        userGroups = project.getUserGroups().getId().toString();
        ownerid = owner.getId().toString();

        entityManagerService.doSqlUpdate(String.format(INSERT_MATERIAL_SQL, userGroups, ownerid, project.getId()));
        entityManagerService.doSqlUpdate("INSERT INTO structures  VALUES(1,'',0,0,null)");
        entityManagerService.doSqlUpdate("INSERT INTO storages VALUES(1,1,'')");
        entityManagerService.doSqlUpdate("INSERT INTO material_indices VALUES(1,1,1,'TESTMATERIAL','de',0)");
        entityManagerService.doSqlUpdate("INSERT INTO material_indices VALUES(2,1,1,'TESTMATERIA2','en',0)");
    }

    private Item createItem() {
        Structure s = new Structure("", 0, 0, 1, new ArrayList<>(), project.getId(), new HazardInformation(), new StorageClassInformation(), null);
        Item item = new Item();
        item.setAmount(23d);
        item.setUnit("kg");
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
        return item;
    }

    private boolean compareHistories(ItemHistory orig, ItemHistory loaded) {
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
}
