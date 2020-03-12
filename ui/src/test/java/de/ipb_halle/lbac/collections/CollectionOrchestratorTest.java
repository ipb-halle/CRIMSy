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
import de.ipb_halle.lbac.announcement.membership.MembershipOrchestrator;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.cloud.solr.SolrAdminService;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.collections.mock.CollectionWebClientMock;
import de.ipb_halle.lbac.collections.mock.CollectionWebServiceMock;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.search.SolrSearcher;
import de.ipb_halle.lbac.search.document.DocumentSearchBean;
import de.ipb_halle.lbac.search.termvector.SolrTermVectorSearch;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.search.wordcloud.WordCloudBean;
import de.ipb_halle.lbac.service.ACListService;
import de.ipb_halle.lbac.service.CollectionService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.service.MembershipService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.webservice.Updater;
import de.ipb_halle.lbac.webservice.service.WebRequestAuthenticator;
import java.util.UUID;
import javax.inject.Inject;
import org.apache.log4j.Logger;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class CollectionOrchestratorTest extends TestBase {

    @Inject
    protected CollectionService collectionService;

    @Inject
    protected CollectionOrchestrator collectionOrc;

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("CollectionOrchestratorTest.war")
                .addPackage(CollectionOrchestrator.class.getPackage())
                .addPackage(GlobalAdmissionContext.class.getPackage())
                .addPackage(NodeService.class.getPackage())
                .addPackage(MembershipService.class.getPackage())
                .addPackage(Logger.class.getPackage())
                .addPackage(Updater.class.getPackage())
                .addPackage(SolrAdminService.class.getPackage())
                .addPackage(ACListService.class.getPackage())
                .addClass(Navigator.class)
                .addClass(MembershipOrchestrator.class)
                .addPackage(CollectionBean.class.getPackage())
                .addClass(WebRequestAuthenticator.class)
                .addClass(FileService.class)
                .addClass(FileEntityService.class)
                .addPackage(DocumentSearchBean.class.getPackage())
                .addPackage(WordCloudBean.class.getPackage())
                .addClass(SolrTermVectorSearch.class)
                .addClass(TermVectorEntityService.class)
                .addClass(CollectionWebServiceMock.class)
                .addPackage(SolrSearcher.class.getPackage())
                .addClass(KeyManager.class);
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
                "testName",
                nodeService.getLocalNode(),
                memberService,
                membershipService);
        User u2 = createUser(
                "test2",
                "testName2",
                nodeService.getLocalNode(),
                memberService,
                membershipService);

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
