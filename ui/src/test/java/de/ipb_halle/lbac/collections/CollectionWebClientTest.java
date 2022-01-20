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
import de.ipb_halle.lbac.collections.mock.CollectionWebServiceMock;
import de.ipb_halle.lbac.entity.Cloud;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.webclient.WebRequestSignature;
import de.ipb_halle.lbac.webservice.Updater;
import de.ipb_halle.lbac.webservice.service.WebRequestAuthenticator;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
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
@ExtendWith(ArquillianExtension.class)
public class CollectionWebClientTest extends TestBase {

    @Inject
    WebRequestAuthenticator webRequestAuthenticator;

    @Inject
    protected CollectionService collectionService;

    @Inject
    CollectionWebClient collectionWebClient;

    @Inject
    private CollectionWebServiceMock webService;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("CollectionWebClientTest.war")
                .addClass(CollectionOrchestrator.class)
                .addClass(CollectionWebClient.class)
                .addClass(Updater.class)
                .addClass(CollectionSearchState.class)
                .addClass(WebRequestAuthenticator.class)
                .addClass(Navigator.class)
                .addClass(CollectionWebClient.class)
                .addClass(CollectionWebServiceMock.class)
                .addClass(FileService.class)
                .addClass(FileEntityService.class);
        return UserBeanDeployment.add(deployment);
    }

    @BeforeEach
    public void init() {
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
        CloudNode cn = cloudNodeService.loadCloudNode(c, n);
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
