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
package de.ipb_halle.lbac.base;

import de.ipb_halle.lbac.admission.AdmissionSubSystemType;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.ACPermission;
import de.ipb_halle.lbac.entity.Collection;
import de.ipb_halle.lbac.entity.FileObject;
import de.ipb_halle.lbac.entity.Group;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.globals.GlobalVersions;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.globals.KeyStoreFactory;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.service.ACListService;
import de.ipb_halle.lbac.service.CloudService;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.CollectionService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.service.InfoObjectService;
import de.ipb_halle.lbac.service.MemberService;
import de.ipb_halle.lbac.service.MembershipService;
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
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;
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

    // per convention (defined in init.sql) test node == local node
    public final static UUID TEST_NODE_ID = UUID.fromString("986ad1be-9a3b-4a70-8600-c489c2a00da4");
    public final static String TESTCLOUD = "TESTCLOUD";


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

    ACList acListReadable, acListNonReadable;

    public static WebArchive prepareDeployment(String archiveName) {
        WebArchive archive = ShrinkWrap.create(WebArchive.class, archiveName)
                .addClass(GlobalAdmissionContext.class)
                .addClass(GlobalVersions.class)
                .addClass(ACListService.class)
                .addClass(CloudService.class)
                .addClass(CloudNodeService.class)
                .addClass(EntityManager.class)
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
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    /**
     * Creates a Testuser which will be saved in the local database.
     *
     * @param login
     * @param name
     * @param localNode
     * @param memberService
     * @param memberShipService
     * @return
     */
    protected User createUser(
            String login,
            String name,
            Node localNode,
            MemberService memberService,
            MembershipService memberShipService) {
        User u = new User();
        u.setLogin(login);
        u.setName(name);
        u.setNode(localNode);
        u.setSubSystemType(AdmissionSubSystemType.LOCAL);
        memberService.save(u);

        Group g = new Group();
        g.setId(UUID.randomUUID());
        g.setName("Group of user " + u.getLogin());
        g.setNode(localNode);
        g.setSubSystemData("L");
        g.setSubSystemType(AdmissionSubSystemType.LOCAL);
        memberService.save(g);

        memberShipService.addMembership(u, u);
        memberShipService.addMembership(g, u);

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
        memberService.save(g);
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
        col.setId(UUID.randomUUID());
        col.setOwner(owner);
        collections.add(col);
        collectionService.save(col);
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
        }).filter((u) -> (!u.getName().equals("Public Account") && !u.getName().equals("Admin") && !u.getId().equals(UUID.fromString(GlobalAdmissionContext.OWNER_ACCOUNT_ID)))).forEachOrdered((u) -> {
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
}
