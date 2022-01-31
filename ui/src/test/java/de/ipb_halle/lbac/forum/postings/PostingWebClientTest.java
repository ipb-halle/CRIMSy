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
package de.ipb_halle.lbac.forum.postings;

import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.entity.Cloud;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.forum.ForumService;
import de.ipb_halle.lbac.forum.Topic;
import de.ipb_halle.lbac.forum.postings.mock.PostingWebServiceMock;
import de.ipb_halle.lbac.forum.topics.TopicCategory;
import de.ipb_halle.lbac.forum.topics.TopicsWebClient;
import de.ipb_halle.lbac.globals.KeyManager;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author fmauz
 */
@ExtendWith(ArquillianExtension.class)
public class PostingWebClientTest extends TestBase {

    @Inject
    private KeyManager keyManager;

    @Inject
    private ForumService forumService;

    @Inject
    private PostingWebClient client;

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("PostingWebClientTest.war")
                .addClass(KeyManager.class)
                .addClass(PostingWebServiceMock.class)
                .addClass(ForumService.class)
                .addClass(TopicsWebClient.class)
                .addClass(PostingWebClient.class);
    }

    @BeforeEach
    public void init() {
        initializeBaseUrl();
        initializeKeyStoreFactory();
    }

    @Test
    @RunAsClient
    public void announcePostingToRemoteNodeTest() throws Exception {

        Node n = nodeService.getLocalNode();
        nodeService.save(n);
        Cloud cloud = cloudService.load().get(0);
        User u = createUser(
                "test",
                "testName");
        Topic t = forumService.createNewTopic("TestTopic", TopicCategory.OTHER, u, cloud.getName());
        client.announcePostingToRemoteNode(t, u, cloudNodeService.loadCloudNode(TESTCLOUD, TEST_NODE_ID));
    }
}
