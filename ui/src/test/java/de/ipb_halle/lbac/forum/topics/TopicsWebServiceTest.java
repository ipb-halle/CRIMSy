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

import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.entity.Cloud;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.forum.ForumService;
import de.ipb_halle.lbac.forum.Topic;
import de.ipb_halle.lbac.forum.TopicsList;
import de.ipb_halle.lbac.forum.postings.PostingWebClient;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.admission.MembershipService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.webclient.LbacWebClient;
import de.ipb_halle.lbac.webservice.service.WebRequestAuthenticator;
import java.util.Base64;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import org.jboss.arquillian.container.test.api.Deployment;
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
public class TopicsWebServiceTest extends TestBase {

    @Inject
    private KeyManager keyManager;

    @Inject
    protected WebRequestAuthenticator authenticator;

    @Inject
    protected MemberService memberService;

    @Inject
    protected MembershipService membershipService;

    @Inject
    protected TopicsWebService forumWebService;

    @Inject
    protected ForumService forumService;

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("TopicsWebServiceTest.war")
                .addClass(ForumService.class)
                .addClass(TopicsWebClient.class)
                .addClass(PostingWebClient.class)
                .addClass(WebRequestAuthenticator.class)
                .addClass(TopicsWebService.class)
                .addClass(KeyManager.class);
    }

    @BeforeEach
    public void init() {
        initializeBaseUrl();
        initializeKeyStoreFactory();
    }

    @Test
    public void getReadableTopicsTest() throws Exception {

        User u = createUser(
                "test",
                "testName");

        TopicsWebRequest webRequest = createWebRequest(u);
        CloudNode cn = cloudNodeService.loadCloudNode(TESTCLOUD, TEST_NODE_ID);
        cn.setPublicKey(Base64.getEncoder().encodeToString(keyManager.getLocalPublicKey(TESTCLOUD).getEncoded()));
        cn = cloudNodeService.save(cn);

        // Usecase not authenticated
        Assert.assertEquals(403, forumWebService.getReadableTopics(null).getStatus());
        Cloud cloud = cloudService.load().get(0);
        webRequest.setCloud(cloud);
        // UseCase one readable topic
        forumService.createNewTopic("TestTopic", TopicCategory.OTHER, u, cloud.getName());

        Response response = forumWebService.getReadableTopics(webRequest);
        Assert.assertEquals(
                200,
                response.getStatus());

        List<Topic> readableTopics = response.readEntity(TopicsList.class).getTopics();
        Assert.assertEquals(1, readableTopics.size());

    }

    private TopicsWebRequest createWebRequest(User u) throws Exception {
        TopicsWebRequest webRequest = new TopicsWebRequest();
        webRequest.setCloudName(TESTCLOUD);
        webRequest.setNodeIdOfRequest(TEST_NODE_ID);
        webRequest.setUser(u);

        LbacWebClient client = new LbacWebClient();
        webRequest.setSignature(
                client.createWebRequestSignature(
                        keyManager.getLocalPrivateKey(TESTCLOUD)
                ));
        return webRequest;

    }
}
