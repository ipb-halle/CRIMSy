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
package de.ipb_halle.lbac.forum.topics;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.entity.Cloud;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.forum.ForumSearchState;
import de.ipb_halle.lbac.forum.topics.mock.TopicsWebServiceMock;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.service.MemberService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.util.ssl.SecureWebClientBuilder;
import java.util.UUID;
import javax.inject.Inject;
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
public class TopicsWebClientTest extends TestBase {

    @Inject
    private TopicsWebClient client;

    @Inject
    private KeyManager keymanager;

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("TopicsWebClientTest.war")
                .addClass(KeyManager.class)
                .addClass(TopicsWebServiceMock.class)
                .addClass(TopicsWebClient.class);
    }

    @Before
    public void setUp() {
        super.setUp();
        initializeBaseUrl();
        initializeKeyStoreFactory();
    }

    @Test
    @RunAsClient
    public void getTopicsFromRemoteNodeTest() throws Exception {

        Node n = nodeService.getLocalNode();
        Cloud c = cloudService.loadByName(TESTCLOUD);
        CloudNode cn = cloudNodeService.loadCloudNode(c,n);

        User publicUser = memberService.loadUserById(
                UUID.fromString(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID));

        ForumSearchState state = new ForumSearchState();

        state.getReadableTopics().addAll(client.getTopicsFromRemoteNode(
                cn,
                publicUser));

        Assert.assertEquals(1, state.getReadableTopics().size());
    }
}
