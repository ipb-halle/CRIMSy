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
package de.ipb_halle.lbac.announcement.membership;

import de.ipb_halle.lbac.admission.AdmissionSubSystemType;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.announcement.membership.mock.MembershipWebServiceMock;
import de.ipb_halle.lbac.admission.MembershipOrchestrator;
import de.ipb_halle.lbac.admission.MembershipWebClient;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.cloud.solr.SolrAdminService;
import de.ipb_halle.lbac.collections.CollectionBean;
import de.ipb_halle.lbac.collections.CollectionOrchestrator;
import de.ipb_halle.lbac.collections.CollectionSearchState;
import de.ipb_halle.lbac.collections.CollectionWebClient;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.Collection;
import de.ipb_halle.lbac.entity.Group;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.search.SolrSearcher;
import de.ipb_halle.lbac.search.document.DocumentSearchBean;
import de.ipb_halle.lbac.search.termvector.SolrTermVectorSearch;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.search.wordcloud.WordCloudBean;
import de.ipb_halle.lbac.service.CollectionService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.service.MemberService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.util.ssl.SecureWebClientBuilder;
import de.ipb_halle.lbac.webservice.Updater;
import de.ipb_halle.lbac.webservice.service.WebRequestAuthenticator;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class MembershipWebClientTest extends TestBase {

    @Deployment
    public static WebArchive createDeployment() {

        return prepareDeployment("MembershipWebServiceTest.war")
                .addPackage(MemberService.class.getPackage())
                .addPackage(NodeService.class.getPackage())
                .addClass(GlobalAdmissionContext.class)
                .addClass(GlobalAdmissionContext.class)
                .addClass(WebRequestAuthenticator.class)
                .addClass(MembershipWebServiceMock.class)
                .addClass(Updater.class)
                .addClass(KeyManager.class)
                .addClass(FileService.class)
                .addClass(FileEntityService.class);

    }

    @Inject
    KeyManager keymanager;

    @Before
    public void setUp() {
        super.setUp();
        initializeBaseUrl();
        initializeKeyStoreFactory();
    }

    @Test
    public void announceUserToRemoteNodesTest() throws Exception {
        MembershipWebClient client = new MembershipWebClient();
        Set<Group> groups = new HashSet<>();
        Group g = new Group();
        g.setId(UUID.randomUUID());
        g.setName("testGroup");
        g.setNode(nodeService.getLocalNode());
        g.setSubSystemData("G");
        g.setSubSystemType(AdmissionSubSystemType.LOCAL);
        User u = new User();
        u.setId(UUID.randomUUID());
        u.setName("testUser");
        u.setLogin("testUserLogIn");

        CloudNode cn = cloudNodeService.loadCloudNode(TESTCLOUD, TEST_NODE_ID);

        client.announceUserToRemoteNodes(
                u,
                cn,
                groups,
                nodeService.getLocalNodeId(),
                keymanager.getLocalPrivateKey(TESTCLOUD)
        );
        MembershipWebServiceMock.SUCCESS = false;

        client.announceUserToRemoteNodes(
                u,
                cn,
                groups,
                nodeService.getLocalNodeId(),
                keymanager.getLocalPrivateKey(TESTCLOUD)
        );

    }
}
