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
package de.ipb_halle.lbac.collections;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.cloud.solr.SolrAdminService;
import de.ipb_halle.lbac.collections.mock.CollectionWebServiceMock;
import de.ipb_halle.lbac.collections.mock.FileEntityServiceMock;
import de.ipb_halle.lbac.collections.mock.FileServiceMock;
import de.ipb_halle.lbac.collections.mock.SolarAdminServiceMock;

import de.ipb_halle.lbac.entity.Collection;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.search.SolrSearcher;
import de.ipb_halle.lbac.search.document.DocumentSearchBean;
import de.ipb_halle.lbac.search.termvector.SolrTermVectorSearch;
import de.ipb_halle.lbac.search.wordcloud.WordCloudBean;
import de.ipb_halle.lbac.service.CollectionService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.webservice.Updater;
import de.ipb_halle.lbac.webservice.service.WebRequestAuthenticator;
import java.util.List;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import javax.persistence.PersistenceContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class CollectionOperationTest extends TestBase {

    private CollectionOperation instance;
    private final String PUBLIC_COLL_NAME = "public";

    @Inject
    private TermVectorEntityService termVectorEntityService;

    @Inject
    CollectionService collectionService;

    @Inject
    GlobalAdmissionContext globalAdmissionContext;

    @Inject
    SolrTermVectorSearch solrTermVectorSearch;

    @Before
    public void init() {

        Assert.assertNotNull("NodeService not injected", nodeService);
        Assert.assertNotNull("CollectionService not injected", collectionService);

        SolarAdminServiceMock solarAdminMock = new SolarAdminServiceMock();
        FileServiceMock fileServiceMock = new FileServiceMock();
        FileEntityServiceMock fileEntityMock = new FileEntityServiceMock();

        instance = new CollectionOperation(
                fileServiceMock,
                fileEntityMock,
                globalAdmissionContext,
                solarAdminMock,
                nodeService,
                collectionService,
                PUBLIC_COLL_NAME,
                solrTermVectorSearch,
                termVectorEntityService,
                null);

        resetDB(memberService);
    }

    /**
     * Creating a new collection.
     */
    @Test
    public void createCollectionTest() {
        String collName = "test001_testColl";
        User u = createUser(
                "testuser",
                "testuser");

        //Try to create a collection with reserved name
        Collection coll = new Collection();
        coll.setOwner(u);

        coll.setName("configsets");
        coll.setDescription("testColl-description");
        Assert.assertTrue("A collection with a reserved name would be saved",
                CollectionOperation.OperationState.CREATION_RESERVED_NAME == instance.createCollection(coll, u)
        );

        // Create a new Collection
        coll.setName(collName);
        CollectionOperation.OperationState state = instance.createCollection(coll, u);
        System.out.println("STATE " + state);
        Assert.assertTrue(
                "Failed to create a new collection",
                CollectionOperation.OperationState.OPERATION_SUCCESS == state
        );
        List<Collection> loadedColls = collectionService.load(nameCmap(collName));
        Assert.assertTrue(!loadedColls.isEmpty());
        Collection col = loadedColls.get(0);
        Assert.assertTrue("Owner not correct", col.getOwner().equals(u));

        // Try to create a new collection with a name that already exists
        Assert.assertTrue(
                "A second public collection would be saved",
                CollectionOperation.OperationState.CREATION_DUPLICATE_NAME == instance.createCollection(coll, u)
        );
    }

    @Test
    public void clearCollectionTest() {

    }

    @Test
    public void deleteCollectionTest() {
        String collName = "deleteCollectionTest_collection";
        User u = createUser(
                "testuser",
                "testuser");

        Collection coll = new Collection();
        coll.setOwner(u);
        coll.setName(collName);
        coll.setDescription("testColl-description");
        CollectionOperation.OperationState state = instance.createCollection(coll, u);
        Assert.assertTrue("Collection was not created", CollectionOperation.OperationState.OPERATION_SUCCESS == state);
        List<Collection> loadedColls = collectionService.load(nameCmap(collName));
        Assert.assertTrue(!loadedColls.isEmpty());

        instance.deleteCollection(coll, u);
        loadedColls = collectionService.load(nameCmap(collName));
        Assert.assertTrue(loadedColls.isEmpty());
    }

    @Test
    public void reindexCollectionTest() {

    }

    @Test
    public void updateCollectionTest() {
        String NEW_DESCRIPTION = "Changed Description by test";
        String collName = "testColl";
        User u = createUser(
                "testuser",
                "testuser");

        Collection coll = new Collection();

        coll.setOwner(u);
        coll.setName(collName);
        coll.setDescription("testColl-description");
        instance.createCollection(coll, u);

        coll.setDescription(NEW_DESCRIPTION);

        instance.updateCollection(coll, u);
        List<Collection> loadedColls = collectionService.load(nameCmap(collName));
        Assert.assertTrue(!loadedColls.isEmpty());
        Assert.assertTrue(loadedColls.get(0).getDescription().equals(NEW_DESCRIPTION));
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("PermissionEditBeanTest.war")
                .addPackage(Collection.class.getPackage())
                .addClass(CollectionOrchestrator.class)
                .addClass(CollectionWebClient.class)
                .addPackage(CollectionBean.class.getPackage())
                .addPackage(WebRequestAuthenticator.class.getPackage())
                .addClass(Updater.class)
                .addClass(Navigator.class)
                .addClass(CollectionWebServiceMock.class)
                .addClass(CollectionSearchState.class)
                .addPackage(SolrAdminService.class.getPackage())
                .addPackage(SolrSearcher.class.getPackage())
                .addPackage(DocumentSearchBean.class.getPackage())
                .addPackage(WordCloudBean.class.getPackage())
                .addClass(TermVectorEntityService.class)
                .addClass(EntityManager.class)
                .addClass(FileService.class)
                .addClass(FileEntityService.class)
                .addClass(CollectionService.class)
                .addClass(TermVectorEntityService.class)
                .addClass(SolrTermVectorSearch.class);
        return UserBeanDeployment.add(deployment);
    }

}
