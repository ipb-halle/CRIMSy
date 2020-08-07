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

import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.cloud.solr.SolrAdminService;
import de.ipb_halle.lbac.collections.mock.CollectionWebServiceMock;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.CollectionList;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.service.CollectionService;
import de.ipb_halle.lbac.service.MemberService;
import de.ipb_halle.lbac.service.MembershipService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.webclient.LbacWebClient;
import de.ipb_halle.lbac.webclient.WebRequestSignature;
import de.ipb_halle.lbac.search.SolrSearcher;
import de.ipb_halle.lbac.search.document.DocumentSearchBean;
import de.ipb_halle.lbac.search.termvector.SolrTermVectorSearch;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.search.wordcloud.WordCloudWebService;
import de.ipb_halle.lbac.search.wordcloud.WordCloudBean;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.webservice.Updater;
import de.ipb_halle.lbac.webservice.service.WebRequestAuthenticator;
import java.util.Base64;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
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
public class CollectionWebServiceTest
        extends TestBase {

    @Inject
    CollectionWebService collectionWebService;

    @Inject
    protected NodeService nodeService;

    @Inject
    protected CollectionService collectionService;

    @Inject
    protected MemberService memberService;

    @Inject
    protected MembershipService membershipService;

    @Inject
    protected KeyManager keyManager;

    @Inject
    protected WebRequestAuthenticator authenticator;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("CollectionWebServiceTest.war")
                .addClass(CollectionWebService.class)
                .addPackage(CollectionService.class.getPackage())
                .addClass(NodeService.class)
                .addClass(KeyManager.class)
                .addClass(WebRequestAuthenticator.class)
                .addPackage(DocumentSearchBean.class.getPackage())
                .addPackage(WordCloudBean.class.getPackage())
                .addPackage(WordCloudWebService.class.getPackage())
                .addPackage(SolrSearcher.class.getPackage())
                .addPackage(CollectionBean.class.getPackage())
                .addPackage(SolrAdminService.class.getPackage())
                .addClass(CollectionWebServiceMock.class)
                .addPackage(Navigator.class.getPackage())
                .addPackage(Updater.class.getPackage())
                .addClass(SolrTermVectorSearch.class)
                .addClass(FileService.class)
                .addClass(TermVectorEntityService.class)
                .addClass(FileEntityService.class);
        return UserBeanDeployment.add(deployment);
    }

    @Before
    public void setUp() {
        super.setUp();
        initializeKeyStoreFactory();
    }

    @Test
    public void testCollectionWebService() throws Exception {

        Assert.assertNotNull("Could not initialize WebService", collectionWebService);

        User u = createUser("test", "testName");
        User u2 = createUser("test2", "testName2");

        CloudNode cn = cloudNodeService.loadCloudNode(TESTCLOUD, TEST_NODE_ID);
        cn.setPublicKey(Base64.getEncoder().encodeToString(keyManager.getLocalPublicKey(TESTCLOUD).getEncoded()));
        cn = cloudNodeService.save(cn);

        createLocalCollections(
                createAcList(u, true), nodeService.getLocalNode(),
                u2, "READ-COL1", "Readable Collection for User " + u2.getName(),
                collectionService
        );

        createLocalCollections(
                createAcList(u, false), nodeService.getLocalNode(),
                u2, "NONREAD-COL1", "Non-Readable Collection for User " + u2.getName(),
                collectionService
        );

        LbacWebClient client = new LbacWebClient();
        WebRequestSignature wrs = client.createWebRequestSignature(
                keyManager.getLocalPrivateKey(TESTCLOUD)
        );

        CollectionWebRequest wr = new CollectionWebRequest();
        wr.setUser(u);
        wr.setCloudName(TESTCLOUD);
        wr.setNodeIdOfRequest(TEST_NODE_ID);
        wr.setSignature(wrs);

        collectionWebService.setAuthenticator(authenticator);
        Response resp = collectionWebService.getReadableCollections(wr);

        Assert.assertEquals("Found Collectionssize does not match", 1, resp.readEntity(CollectionList.class).getCollectionList().size());

    }

}
