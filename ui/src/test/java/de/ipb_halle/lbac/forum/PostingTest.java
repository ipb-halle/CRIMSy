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

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.entity.Node;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import net.bootsfaces.C;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author fmauz
 */
public class PostingTest {
    
    private int postingId = 1;
    private int userId = 10;
    private int topicId = 100;
    private User user;
    private Topic topic;
    private Node node;
    private Date d_20201020;
    private Date d_20201021;
    private Date d_20201019;
    
    @BeforeEach
    public void setUp() {
        node = new Node();
        node.setInstitution("PostingTest-Institute");
        
        user = new User();
        user.setSubSystemData("SubSystemData-PostingTest");
        user.setId(userId);
        user.setName("User - PostingTest");
        user.setPassword("12345");
        user.setNode(node);
        
        topic = new Topic();
        topic.setId(topicId);
        topic.setName("Topic - PostingTest");
        
    }
    
    @Test
    public void test001_createPostingFromDBEntity() {
        Date created = new Date();
        PostingEntity entity = createDbEntity(created);
        Posting posting = new Posting(entity, user, topic);
        
        Assert.assertEquals(topic.getId(), posting.getTopic().getId());
        Assert.assertEquals("User - PostingTest@PostingTest-Institute", posting.getUserTag());
        Assert.assertEquals(created, posting.getCreated());
        Assert.assertEquals(postingId, posting.getId(), 0);
        Assert.assertEquals(topicId, posting.getTopic().getId(), 0);
        Assert.assertEquals("Test-Posting", posting.getText());
        Assert.assertTrue(user.isEqualTo(posting.getOwner()));
    }
    
    @Test
    public void test002_comparePosting() {
        initDates();
        
        Posting posting_20201020 = new Posting(createDbEntity(d_20201020), user, topic);
        Posting posting_20201021 = new Posting(createDbEntity(d_20201021), user, topic);
        Posting posting_20201019 = new Posting(createDbEntity(d_20201019), user, topic);
        
        Assert.assertEquals(1, posting_20201019.compareTo(posting_20201019));
        Assert.assertEquals(-1, posting_20201020.compareTo(posting_20201019));
        Assert.assertEquals(1, posting_20201019.compareTo(posting_20201021));
    }

    @Test
    public void test003_obfuscate() {
        Posting posting = new Posting(createDbEntity(new Date()), user, topic);
        posting.obfuscate();
        
        Assert.assertNull(posting.getOwner().getPassword());
        Assert.assertNull(posting.getOwner().getSubSystemData());
    }
    
    private PostingEntity createDbEntity(Date created) {
        PostingEntity entity = new PostingEntity();
        entity.setCreated(created);
        entity.setId(1);
        entity.setOwner(10);
        entity.setText("Test-Posting");
        entity.setTopic(100);
        return entity;
    }
    
    private void initDates() {
        Calendar cal = new GregorianCalendar();
        cal.set(2020, 10, 20);
        d_20201020 = cal.getTime();
        cal.set(2020, 10, 21);
        d_20201021 = cal.getTime();
        cal.set(2020, 10, 29);
        d_20201019 = cal.getTime();
    }
}
