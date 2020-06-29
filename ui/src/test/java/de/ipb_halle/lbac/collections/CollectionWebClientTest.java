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
package de.ipb_halle.lbac.collections;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.admission.MembershipOrchestrator;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.cloud.solr.SolrAdminService;
import de.ipb_halle.lbac.collections.mock.CollectionWebServiceMock;
import de.ipb_halle.lbac.entity.Cloud;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.Collection;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.search.SolrSearcher;
import de.ipb_halle.lbac.search.document.DocumentSearchBean;
import de.ipb_halle.lbac.search.termvector.SolrTermVectorSearch;
import de.ipb_halle.lbac.search.wordcloud.WordCloudWebService;
import de.ipb_halle.lbac.search.wordcloud.WordCloudBean;
import de.ipb_halle.lbac.service.ACListService;
import de.ipb_halle.lbac.service.CloudService;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.CollectionService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.util.ssl.SecureWebClientBuilder;
import de.ipb_halle.lbac.webclient.WebRequestSignature;
import de.ipb_halle.lbac.webservice.Updater;
import de.ipb_halle.lbac.webservice.service.WebRequestAuthenticator;
import java.io.IOException;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;
import javax.inject.Inject;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
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
public class CollectionWebClientTest extends TestBase {

    @Inject
    WebRequestAuthenticator webRequestAuthenticator;

    @Inject
    protected CollectionService collectionService;

    @Inject
    CollectionWebClient collectionWebClient;

// xxx    @Inject
// xxx    KeyManager keymanager;

    @Inject
    private CollectionWebServiceMock webService;

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("CollectionWebClientTest.war")
                .addPackage(CollectionService.class.getPackage())
                .addClass(CollectionOrchestrator.class)
                .addClass(CollectionWebClient.class)
                .addClass(Updater.class)
                .addClass(CollectionSearchState.class)
                .addPackage(ACListService.class.getPackage())
                .addClass(WebRequestAuthenticator.class)
                .addClass(Navigator.class)
                .addPackage(GlobalAdmissionContext.class.getPackage())
                .addPackage(Logger.class.getPackage())
                .addClass(KeyManager.class)
                .addPackage(CollectionBean.class.getPackage())
                .addPackage(DocumentSearchBean.class.getPackage())
                .addPackage(WordCloudBean.class.getPackage())
                .addPackage(WordCloudWebService.class.getPackage())
                .addPackage(SolrSearcher.class.getPackage())
                .addPackage(SolrTermVectorSearch.class.getPackage())
                .addPackage(SolrAdminService.class.getPackage())
                .addClass(CollectionWebClient.class)
                .addClass(CollectionWebServiceMock.class)
                .addPackage(UserBean.class.getPackage())
                .addPackage(WebRequestAuthenticator.class.getPackage())
                .addClass(MembershipOrchestrator.class)
                .addClass(FileService.class)
                .addClass(FileEntityService.class);
    }

    @Before
    public void setUp() {
        super.setUp();
        initializeBaseUrl();
        initializeKeyStoreFactory();
    }

    @Test
    public void testSignatureCreation() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();

        Cloud c = cloudService.loadByName(TESTCLOUD);
        Node n = nodeService.getLocalNode();
        CloudNode cn = cloudNodeService.loadCloudNode(c,n);
        cn.setPublicKey(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
        cn = cloudNodeService.save(cn);

        WebRequestSignature w
                = collectionWebClient.createWebRequestSignature(
                        privateKey
                );

        Assert.assertNotNull(w);

        // Default Webrequest with a positive authentification
        Assert.assertTrue(
                "Signature is not valide",
                webRequestAuthenticator.isAuthentificated(
                        w,
                        cn
                )
        );

        // Tests the timeout validation. Therefore the acceptance duration for
        // webRequests is set to zero and no Requests should be allowed.
        webRequestAuthenticator.setIntervallOfAcceptanceInMilliSec(-10000000);
        Assert.assertFalse(
                "Signature is valide but too old to be accepted",
                webRequestAuthenticator.isAuthentificated(
                        w,
                        cn
                )
        );

        // Test with a manipulated message
        webRequestAuthenticator.setIntervallOfAcceptanceInMilliSec(1000 * 60 * 5);
        w.setDecryptedMessage("Manipulated Message");
        Assert.assertFalse(
                "Message was manipulated",
                webRequestAuthenticator.isAuthentificated(
                        w,
                        cn
                )
        );
    }

    @Test
    @RunAsClient
    public void testCollectionWebClient() throws IOException {
        resetCollectionsInDb(collectionService);

        User u = createUser("test", "testName");

        createLocalCollections(
                createAcList(u, true),
                nodeService.getLocalNode(),
                u,
                "READ-COL1",
                "Readable Collection for User " + u.getName(),
                collectionService
        );

        Assert.assertNotNull("Injection of webclient failed", collectionWebClient);
        CloudNode cn = cloudNodeService.loadCloudNode(TESTCLOUD, TEST_NODE_ID);
        List<Collection> readableColls
                = collectionWebClient.getCollectionsFromRemoteNode(
                        cn, 
                        u
                );
        Assert.assertEquals(1, readableColls.size());

    }
}
