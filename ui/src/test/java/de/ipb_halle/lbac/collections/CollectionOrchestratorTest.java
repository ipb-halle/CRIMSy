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
import static de.ipb_halle.lbac.base.TestBase.TESTCLOUD;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.collections.mock.CollectionWebClientMock;
import de.ipb_halle.lbac.collections.mock.CollectionWebServiceMock;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.webservice.service.WebRequestAuthenticator;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author fmauz
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class CollectionOrchestratorTest extends TestBase {

    @Inject
    protected CollectionService collectionService;

    @Inject
    protected CollectionOrchestrator collectionOrc;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("CollectionOrchestratorTest.war")
                .addClass(Navigator.class)
                .addClass(WebRequestAuthenticator.class)
                .addClass(FileService.class)
                .addClass(FileEntityService.class)
                .addClass(CollectionOrchestrator.class)
                .addClass(CollectionWebClient.class)
                .addClass(TermVectorEntityService.class)
                .addClass(CollectionWebServiceMock.class);
        return UserBeanDeployment.add(deployment);
    }

    @Test
    public void test_getCollectionsOfUser() throws InterruptedException {
        resetCollectionsInDb(collectionService);
        final int DELAY_IN_MILLISEC = 1500;
        final int AMOUNT_OF_REMOTE_COLLS = 3;
        final int AMOUNT_OF_LOCAL_COLLS = 1;

        Node remoteNode = createNode(nodeService, "dummyKey");
        CloudNode cn = new CloudNode(cloudService.loadByName(TESTCLOUD), remoteNode);

        cloudNodeService.save(cn);
        // Mocks the CollectionWebClient 
        collectionOrc.setCollectionWebClient(
                new CollectionWebClientMock(DELAY_IN_MILLISEC, AMOUNT_OF_REMOTE_COLLS)
        );

        //Aktuelle Krücke bis @Inject Problematik behoben wurde
        collectionOrc.setNodeService(nodeService);
        collectionOrc.setCollectionService(this.collectionService);
        collectionOrc.setManagedExecutorService(this.executor);

        User u = createUser(
                "test",
                "testName");
        User u2 = createUser(
                "test2",
                "testName2");

        createLocalCollections(
                createAcList(u, true), nodeService.getLocalNode(),
                u, "READ-COL1", "Readable Collection for User " + u.getName(),
                collectionService
        );

        createLocalCollections(
                createAcList(u, false), nodeService.getLocalNode(),
                u2, "NOREAD-COL1", "Non-Readable Collection for User " + u.getName(),
                collectionService
        );
        CollectionSearchState status = new CollectionSearchState();
        collectionOrc.setMembershipService(membershipService);
        collectionOrc.startCollectionSearch(status, u);

        assert (status.getCollections().size() == AMOUNT_OF_LOCAL_COLLS);

        Thread.sleep(DELAY_IN_MILLISEC + 2000);

        Assert.assertTrue(
                String.format(
                        "Found collection %d instead of %d",
                        status.getCollections().size(),
                        AMOUNT_OF_REMOTE_COLLS + AMOUNT_OF_LOCAL_COLLS),
                status.getCollections().size() >= (AMOUNT_OF_REMOTE_COLLS + AMOUNT_OF_LOCAL_COLLS));
    }

}
