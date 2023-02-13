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
package de.ipb_halle.lbac.forum;

import de.ipb_halle.lbac.EntityManagerService;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.entity.Cloud;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.forum.postings.PostingWebClient;
import de.ipb_halle.lbac.forum.topics.TopicCategory;
import de.ipb_halle.lbac.forum.topics.TopicsWebClient;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.admission.MembershipService;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author fmauz
 */
@TestMethodOrder(MethodName.class)
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class ForumServiceTest extends TestBase {

    @Inject
    private EntityManagerService entityManagerService;

    @Inject
    private ForumService instance;

    @Inject
    private MembershipService memberShipService;

    private User publicUser;

    public ForumServiceTest() {

    }

    @BeforeEach
    public void init() {
        entityManagerService.doSqlUpdate("DELETE FROM postings");
        entityManagerService.doSqlUpdate("DELETE FROM topics");
        entityManagerService.doSqlUpdate("DELETE FROM cloud_nodes WHERE id>1");
        entityManagerService.doSqlUpdate("DELETE FROM clouds WHERE id>1");
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
    }

    @Test
    public void test01_createNewTopic() throws Exception {
        assert (instance != null);
        assert (memberService != null);
        assert (memberShipService != null);

        Cloud cloud = cloudService.load().get(0);

        Integer id = instance.createNewTopic(
                "Test Topic",
                TopicCategory.OTHER,
                publicUser,
                cloud.getName()
        ).getId();

        Assert.assertNotNull("Topic was not saved correctly", id);

        List<Object> o = entityManagerService.doSqlQuery("SELECT "
                + "id , "
                + "name, "
                + "category, "
                + "owner_id , "
                + "aclist_id , "
                + "cloud_name FROM topics");
        Assert.assertEquals("Exact one topic must be found", 1, o.size());
        Object[] z = (Object[]) o.get(0);
        Integer entityId = (Integer) z[0];
        Assert.assertEquals("IDs do not match", id, entityId);
        String name = (String) z[1];
        Assert.assertEquals("name does not match", "Test Topic", name);
        String category = (String) z[2];
        Assert.assertEquals("category does not match", "OTHER", category);
        Integer user_id = (Integer) z[3];
        Assert.assertEquals("Owner-ID does not match", publicUser.getId(), user_id);
        Integer owner_id = (Integer) z[4];
        Assert.assertEquals("ACL-ID does not match", instance.getPublicReadWriteList().getId(), owner_id);
        String cloud_name = (String) z[5];
        Assert.assertEquals("Cloud-ID does not match", cloud.getName(), cloud_name);

    }

    /**
     *
     */
    @Test
    public void test02_addPostingToTopic() throws Exception {
        User idOfUser2 = createUser(
                "forumuser1",
                "forumuser1");

        Cloud cloud = cloudService.load().get(0);
        Topic topic2 = instance.createNewTopic(
                "Test Topic",
                TopicCategory.OTHER,
                publicUser,
                cloud.getName());

        instance.addPostingToTopic(topic2, "Das ist ein TestPost", publicUser, new Date());

        instance.addPostingToTopic(topic2, "Das ist der zweite TestPost", idOfUser2, new Date());

        instance.addPostingToTopic(topic2, "Das ist der letzte TestPost", publicUser, new Date());

    }

    /**
     *
     */
    @SuppressWarnings("unchecked")
    @Test
    public void test03_loadReadableTopics() {
        Cloud cloud1 = cloudService.load().get(0);
        Cloud cloud2 = new Cloud();
        cloud2.setName("Cloud2");
        cloud2 = cloudService.save(cloud2);
        List<Integer> o = (List) entityManagerService.doSqlQuery("SELECT id from clouds");
        o.get(1);
        CloudNode cloudNode = new CloudNode(cloud2, nodeService.getLocalNode());
        cloudNodeService.save(cloudNode);

        instance.createNewTopic(
                "Topic1 in Cloud1",
                TopicCategory.OTHER,
                publicUser,
                cloud1.getName()
        );

        instance.createNewTopic(
                "Topic1 in Cloud2",
                TopicCategory.OTHER,
                publicUser,
                cloud1.getName()
        );

        instance.createNewTopic(
                "Topic2 in Cloud1",
                TopicCategory.OTHER,
                publicUser,
                cloud2.getName()
        );

        List<Topic> topicsOfCloud1 = instance.loadReadableTopics(publicUser, cloud1);
        Assert.assertEquals("2 Topics must be found in cloud 1", 2, topicsOfCloud1.size());
        List<Topic> topicsOfCloud2 = instance.loadReadableTopics(publicUser, cloud2);
        Assert.assertEquals("1 Topic  must be found in cloud 1", 1, topicsOfCloud2.size());

        entityManagerService.doSqlUpdate("DELETE FROM cloud_nodes WHERE id=" + cloudNode.getId());
        entityManagerService.doSqlUpdate("DELETE FROM clouds WHERE name='Cloud2'");
    }

    @Test
    public void test04_upsertTopicList() {
        Node localNode = new Node();
        Node remoteNode = new Node();
        Topic topic_local1 = createTopic(1, localNode);
        Topic topic_local2 = createTopic(2, localNode);
        Topic topic_remote1 = createTopic(1, remoteNode);
        Topic topic_remote2 = createTopic(2, remoteNode);

        List<Topic> upsertedList = instance.upsertTopicList(topic_local1, new ArrayList<>());
        Assert.assertEquals(1, upsertedList.size());
        upsertedList = instance.upsertTopicList(topic_local1, upsertedList);
        Assert.assertEquals(1, upsertedList.size());
        upsertedList = instance.upsertTopicList(topic_local2, upsertedList);
        Assert.assertEquals(2, upsertedList.size());
        upsertedList = instance.upsertTopicList(topic_remote1, upsertedList);
        Assert.assertEquals(3, upsertedList.size());
        upsertedList = instance.upsertTopicList(topic_remote1, upsertedList);
        Assert.assertEquals(3, upsertedList.size());
        upsertedList = instance.upsertTopicList(topic_remote2, upsertedList);
        Assert.assertEquals(4, upsertedList.size());
    }

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("ForumServiceTest.war")
                .addClass(ForumService.class)
                .addClass(TopicsWebClient.class)
                .addClass(PostingWebClient.class)
                .addClass(EntityManagerService.class)
                .addClass(KeyManager.class);
    }

    private Topic createTopic(int id, Node n) {
        Topic topic = new Topic();
        topic.setNode(n);
        topic.setId(id);
        return topic;
    }

}
