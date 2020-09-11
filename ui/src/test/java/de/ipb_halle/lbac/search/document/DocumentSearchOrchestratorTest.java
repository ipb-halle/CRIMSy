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
package de.ipb_halle.lbac.search.document;

import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.entity.Cloud;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.search.mocks.DocumentSearchEndpointMock;
import de.ipb_halle.lbac.service.CloudService;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.NodeService;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.junit.Test;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class DocumentSearchOrchestratorTest extends TestBase {

    @Inject
    private DocumentSearchOrchestrator searchOrch;

    private DocumentSearchState searchState;

    @Deployment
    public static WebArchive createDeployment() {

        return prepareDeployment("DocumentSearchOrchestratorTest.war")
                .addClass(CloudService.class)
                .addClass(CloudNodeService.class)
                .addClass(NodeService.class)
                .addClass(KeyManager.class)
                .addClass(DocumentSearchState.class)
                .addClass(DocumentSearchEndpointMock.class)
                .addClass(DocumentSearchQuery.class)
                .addClass(DocumentSearchOrchestrator.class);
    }

    @Before
    @Override
    public void setUp() {
        super.setUp();
        initializeBaseUrl();
        initializeKeyStoreFactory();
    }

    @Test
    public void orchestrateTest()
            throws IOException, Exception {

        searchState = new DocumentSearchState();
        DocumentSearchRequest searchReq = new DocumentSearchRequest();
        searchReq.setSearchQuery(new DocumentSearchQuery("java"));

        List<Collection> collList = new ArrayList<>();

        // remote node; no RSA key because REST-API gets mocked.
        Cloud cloud = cloudService.loadByName(TESTCLOUD);
        Node node = createNode(nodeService, "remove this argument");
        node.setBaseUrl(baseUrl.toString());
        cloudNodeService.save(new CloudNode(cloud, node));
        nodeService.save(node);

        Collection col = new Collection();
        col.setId(-100000);

        col.setNode(node);
        collList.add(col);

        searchOrch.orchestrate(collList, "java", searchState);

        Assert.assertTrue(searchState.getFoundDocuments().isEmpty());

        Thread.sleep(DocumentSearchEndpointMock.SLEEPTIME_BETWEEN_REQUESTS + 3000);
        Assert.assertEquals(1, searchState.getFoundDocuments().size());

    }
}
