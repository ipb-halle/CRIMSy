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
package de.ipb_halle.lbac.base;

import de.ipb_halle.lbac.EntityManagerService;
import de.ipb_halle.lbac.admission.AdmissionSubSystemType;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACListEntity;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.admission.Group;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.globals.GlobalVersions;
import de.ipb_halle.lbac.globals.KeyStoreFactory;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.service.CloudService;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.service.InfoObjectService;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.admission.MembershipService;
import de.ipb_halle.lbac.service.NodeService;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;

/**
 * Basic Functionality to support specialized tests.
 *
 * @author fmauz
 */
public class TestBase implements Serializable {

    protected Logger logger;
    protected String LBAC_PROPERTIES_PATH = "target/test-classes/keystore/lbac_properties.xml";
    protected String TEST_ROOT = "target/test-classes/";
    protected CreationTools creationTools;
    protected MaterialCreator materialCreator;
    protected ItemCreator itemCreator;

    // per convention (defined in init.sql) test node == local node
    public final static UUID TEST_NODE_ID = UUID.fromString("986ad1be-9a3b-4a70-8600-c489c2a00da4");
    public final static String TESTCLOUD = "TESTCLOUD";

    protected String INSERT_TAXONOMY_MATERIAL_SQL = "INSERT INTO MATERIALS VALUES("
            + "%d,"
            + "7,"
            + "now(),"
            + "%d, "
            + "%d, "
            + "false,%d)";

    @ArquillianResource
    protected URL baseUrl;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    protected EntityManager em;

    @Resource(name = "DefaultManagedExecutorService")
    protected ManagedExecutorService executor;

    @Inject
    protected CloudService cloudService;

    @Inject
    protected CloudNodeService cloudNodeService;

    @Inject
    protected EntityManagerService entityManagerService;

    @Inject
    protected NodeService nodeService;

    @Inject
    protected TermVectorEntityService termVectorEntityService;

    @Inject
    protected FileEntityService fileEntityService;

    @Inject
    protected CollectionService collectionService;

    @Inject
    protected MemberService memberService;

    @Inject
    protected MembershipService membershipService;

    @Inject
    protected GlobalAdmissionContext context;

    protected ACList acListReadable, acListNonReadable;
    protected User publicUser;

    public static WebArchive prepareDeployment(String archiveName) {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, archiveName)
                .addClass(GlobalAdmissionContext.class)
                .addClass(GlobalVersions.class)
                .addClass(ACListService.class)
                .addClass(CloudService.class)
                .addClass(CloudNodeService.class)
                .addClass(EntityManager.class)
                .addClass(EntityManagerService.class)
                .addClass(InfoObjectService.class)
                .addClass(MemberService.class)
                .addClass(MembershipService.class)
                .addClass(NodeService.class)
                .addClass(FileEntityService.class)
                .addClass(FileService.class)
                .addClass(TermVectorEntityService.class)
                .addClass(CollectionService.class)
                .addClass(KeyStoreFactory.class)
                .addAsWebInfResource("test-persistence.xml", "persistence.xml")
                .addAsResource("init.sql", "init.sql")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        return archive;
    }

    @Before
    public void setUp() {
        System.setProperty("log4j.configurationFile", "log4j2-test.xml");

        this.entityManagerService.doSqlUpdate("Delete from nested_containers");
        cleanItemsFromDb();
        this.entityManagerService.doSqlUpdate("Delete from containers");
        cleanMaterialsFromDB();
        materialCreator = new MaterialCreator(entityManagerService);
        itemCreator = new ItemCreator(entityManagerService);
        acListReadable = GlobalAdmissionContext.getPublicReadACL();
        this.logger = LogManager.getLogger(this.getClass().getName());

        entityManagerService.doSqlUpdate("DELETE FROM PROJECTTEMPLATES");
        entityManagerService.doSqlUpdate("DELETE FROM projects");

        entityManagerService.doSqlUpdate("DELETE FROM unstemmed_words");
        entityManagerService.doSqlUpdate("DELETE FROM termvectors");
        entityManagerService.doSqlUpdate("DELETE FROM files");

        cleanExperimentsFromDB();
        publicUser=memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
    }

    protected void createTaxanomy(int id, String name, int level, Integer userGroups, Integer ownerId, Integer... parents) {
        entityManagerService.doSqlUpdate(String.format(INSERT_TAXONOMY_MATERIAL_SQL, id, userGroups, ownerId, null));
        entityManagerService.doSqlUpdate(String.format("INSERT INTO taxonomy  VALUES(%d ,%d)", id, level));
        entityManagerService.doSqlUpdate(String.format("INSERT INTO storages VALUES(%d,1,'')", id));
        entityManagerService.doSqlUpdate(String.format("INSERT INTO material_indices(materialid, typeid,value,language,rank) VALUES(%d,1,'" + name + "_de','de',0)", id));
        for (Integer parent : parents) {
            entityManagerService.doSqlUpdate(String.format("INSERT INTO effective_taxonomy(taxoid,parentid) VALUES(%d,%d)", id, parent));
        }
    }

    /**
     * Creates a Testuser which will be saved in the local database.
     *
     * @param login
     * @param name
     * @return
     */
    protected User createUser(
            String login,
            String name) {
        User u = new User();
        u.setLogin(login);
        u.setName(name);
        u.setNode(nodeService.getLocalNode());
        u.setSubSystemType(AdmissionSubSystemType.LOCAL);
        u = memberService.save(u);

        Group g = new Group();
        g.setName("Group of user " + u.getLogin());
        g.setNode(nodeService.getLocalNode());
        g.setSubSystemData("L");
        g.setSubSystemType(AdmissionSubSystemType.LOCAL);
        g = memberService.save(g);

        membershipService.addMembership(u, u);
        membershipService.addMembership(g, u);

        return u;
    }

    /**
     * Creates a local group which will be saved in the database.
     *
     * @param name
     * @param localNode
     * @param memberService
     * @param memberShipService
     * @return
     */
    protected Group createGroup(
            String name,
            Node localNode,
            MemberService memberService,
            MembershipService memberShipService) {
        Group g = new Group();
        g.setName(name);
        g.setNode(localNode);
        g.setSubSystemType(AdmissionSubSystemType.LOCAL);
        g = memberService.save(g);
        memberShipService.addMembership(g, g);
        return g;
    }

    /**
     * Creates an empty collection which will be saved in the database.
     *
     * @param acList
     * @param node
     * @param owner
     * @param name
     * @param description
     * @param collectionService
     * @return
     */
    protected List<Collection> createLocalCollections(
            ACList acList,
            Node node,
            User owner,
            String name,
            String description,
            CollectionService collectionService) {

        List<Collection> collections = new ArrayList<>();
        Collection col = new Collection();
        col.setACList(acList);
        col.setDescription(description);
        col.setIndexPath("/");
        col.setName(name);
        col.setNode(node);
        col.setOwner(owner);
        col = collectionService.save(col);
        collections.add(col);
        return collections;
    }

    /**
     * Creates an access controll list.
     *
     * @param u user
     * @param readable can user u read the object
     * @return
     */
    public ACList createAcList(User u, boolean readable) {
        ACList acList = new ACList();
        if (readable) {
            acList.addACE(u, new ACPermission[]{ACPermission.permREAD});
        } else {
            acList.addACE(u, new ACPermission[]{});
        }
        return acList;
    }

    /**
     * Creates an access controll list.
     *
     * @param g Group
     * @param readable can user u read the object
     * @return
     */
    public ACList createAcList(Group g, boolean readable) {
        ACList acList = new ACList();
        if (readable) {
            acList.addACE(g, new ACPermission[]{ACPermission.permREAD});
        } else {
            acList.addACE(g, new ACPermission[]{});
        }
        return acList;
    }

    /**
     * Creates an access controll list with given permissions.
     *
     * @param u user
     * @param permissions
     * @return
     */
    public ACList createAcList(User u, ACPermission[] permissions) {
        ACList acList = new ACList();
        acList.addACE(u, permissions);
        return acList;
    }

    protected void createTaxonomyTreeInDB(Integer userGroups, Integer ownerId) {
        createTaxanomy(1, "Leben", 1, userGroups, ownerId);
        createTaxanomy(2, "Pilze", 2, userGroups, ownerId, 1);
        createTaxanomy(3, "Agaricomycetes", 4, userGroups, ownerId, 1, 2);
        createTaxanomy(4, "Champignonartige", 5, userGroups, ownerId, 1, 2, 3);
        createTaxanomy(5, "Wulstlingsverwandte", 6, userGroups, ownerId, 1, 2, 3, 4);
        createTaxanomy(6, "Wulstlinge", 7, userGroups, ownerId, 1, 2, 3, 4, 5);
        createTaxanomy(7, "Schleimschirmlinge", 7, userGroups, ownerId, 1, 2, 3, 4, 5);
        createTaxanomy(8, "Dacrymycetes", 4, userGroups, ownerId, 1, 2);
        createTaxanomy(9, "Ohrlappenpilzverwandte", 5, userGroups, ownerId, 1, 2, 3);
        createTaxanomy(10, "Ohrlappenpilze", 7, userGroups, ownerId, 1, 2, 3, 9);
        createTaxanomy(11, "Gallerttränenverwandte", 6, userGroups, ownerId, 1, 2, 8);
        createTaxanomy(12, "Hörnlinge ", 7, userGroups, ownerId, 1, 2, 8, 11);
        createTaxanomy(13, "Gallerttränen", 7, userGroups, ownerId, 1, 2, 8, 11);
        createTaxanomy(14, "Bakterien", 2, userGroups, ownerId, 1);
        createTaxanomy(15, "Escherichia", 7, userGroups, ownerId, 1,14);
        createTaxanomy(16, "Pflanzen", 2, userGroups, ownerId, 1);
        createTaxanomy(17, "Seerosenartige", 5, userGroups, ownerId, 1, 16);
        createTaxanomy(18, "Seerosengewächse", 6, userGroups, ownerId, 1, 16, 17);
        createTaxanomy(19, "Victoria", 7, userGroups, ownerId, 1, 16, 17, 18);
        createTaxanomy(20, "Euryale", 7, userGroups, ownerId, 1, 16, 17, 18);
        createTaxanomy(21, "Haarnixen", 7, userGroups, ownerId, 1, 16, 17);
    }

    public void resetDB(MemberService memberService) {
        List<Collection> colls = collectionService.load(new HashMap<>());
        termVectorEntityService.deleteTermVectors();
        for (Collection c : colls) {
            fileEntityService.delete(c);
        }

        List<Group> groups = memberService.loadGroups(new HashMap<>());

        groups.stream().map((g) -> {
            return g;
        }).filter((g) -> (!g.getName().equals("Public Group") && !g.getName().equals("Admin Group"))).forEachOrdered((g) -> {
            //  memberService.deleteGroup(g.getId());
        });
        List<User> users = memberService.loadUsers(new HashMap<>());
        users.stream().map((u) -> {
            return u;
        }).filter((u) -> (!u.getName().equals("Public Account") && !u.getName().equals("Admin") && !u.getId().equals(GlobalAdmissionContext.OWNER_ACCOUNT_ID))).forEachOrdered((u) -> {
            // memberService.deleteUser(u.getId());
        });
    }

    public void resetCollectionsInDb(CollectionService collectionService) {
        List<Collection> colls = collectionService.load(null);
        for (Collection c : colls) {
            if (!c.getName().equals("public")) {
                termVectorEntityService.deleteTermVectorOfCollection(c);
                fileEntityService.delete(c);
                collectionService.delete(c);
            }
        }
    }

    protected Map<String, Object> nameCmap(String name) {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put("name", name);
        return cmap;
    }

    protected Node createNode(
            NodeService nodeService,
            String publicKey
    ) {
        Node newNode = new Node();
        newNode.setBaseUrl(this.baseUrl.toString());

        newNode.setInstitution("Fake Institution");
        newNode.setLocal(false);
        return nodeService.save(newNode);
    }

    protected void initializeBaseUrl() {
        Node n = this.nodeService.getLocalNode();
        n.setBaseUrl(this.baseUrl.toString());
        this.nodeService.save(n);
    }

    protected void initializeKeyStoreFactory() {
        KeyStoreFactory.setLBAC_PROPERTIES_PATH(LBAC_PROPERTIES_PATH);
        KeyStoreFactory ksf = KeyStoreFactory
                .getInstance()
                .setLOCAL_KEY_ALIAS("test")
                .init();
    }

    protected void cleanAllProjectsFromDb() {
        entityManagerService.doSqlUpdate("delete from projecttemplates");
        entityManagerService.doSqlUpdate("delete from budgetreservations");
        entityManagerService.doSqlUpdate("delete from projects");
    }

    protected void cleanProjectFromDB(Project p, boolean deleteAcl) {

        entityManagerService.doSqlUpdate("delete from projecttemplates");
        entityManagerService.doSqlUpdate("delete from budgetreservations");
        entityManagerService.doSqlUpdate("delete from projects where id=" + p.getId());
        if (deleteAcl) {
            entityManagerService.doSqlUpdate("delete from acentries where aclist_id='" + p.getUserGroups().getId().toString() + "'");
            entityManagerService.doSqlUpdate("delete from aclists where id='" + p.getUserGroups().getId().toString() + "'");
        }

    }

    public void cleanItemsFromDb() {
        entityManagerService.doSqlUpdate("delete from item_positions_history");
        entityManagerService.doSqlUpdate("delete from item_positions");
        entityManagerService.doSqlUpdate("delete from items_history");
        entityManagerService.doSqlUpdate("delete from items");
    }

    protected void cleanMaterialsFromDB() {
        entityManagerService.doSqlUpdate("delete from biomaterial");
        entityManagerService.doSqlUpdate("DELETE FROM tissues");
        entityManagerService.doSqlUpdate("DELETE FROM EFFECTIVE_TAXONOMY");
        entityManagerService.doSqlUpdate("DELETE FROM taxonomy_history");
        entityManagerService.doSqlUpdate("DELETE FROM taxonomy");

        entityManagerService.doSqlUpdate("delete from storagesconditions_storages_hist");
        entityManagerService.doSqlUpdate("delete from material_hazards_hist");
        entityManagerService.doSqlUpdate("delete from storages_hist");
        entityManagerService.doSqlUpdate("delete from material_indices_hist");
        entityManagerService.doSqlUpdate("delete from storageconditions_storages");
        entityManagerService.doSqlUpdate("delete from storages");
        entityManagerService.doSqlUpdate("delete from structures_hist");
        entityManagerService.doSqlUpdate("delete from materials_hist");
        entityManagerService.doSqlUpdate("delete from material_indices");
        entityManagerService.doSqlUpdate("delete from materialdetailrights");
        entityManagerService.doSqlUpdate("delete from structures");
        entityManagerService.doSqlUpdate("delete from storageconditions_storages");
        entityManagerService.doSqlUpdate("delete from storages");
        entityManagerService.doSqlUpdate("delete from material_hazards");
        entityManagerService.doSqlUpdate("delete from materials");
    }

    protected void cleanAcListFromDB(ACList acl) {
        entityManagerService.removeEntity(ACListEntity.class, acl.getId());
    }

    protected void cleanExperimentsFromDB() {
        entityManagerService.doSqlUpdate("delete from experiments");
    }
}
