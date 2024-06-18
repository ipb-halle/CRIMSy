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
package de.ipb_halle.lbac.forum.topics.mock;

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.forum.Posting;
import de.ipb_halle.lbac.forum.Topic;
import de.ipb_halle.lbac.forum.TopicsList;
import de.ipb_halle.lbac.forum.topics.TopicCategory;
import de.ipb_halle.lbac.forum.topics.TopicsWebRequest;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import jakarta.ejb.Stateless;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Mocks the functionality of the Topics WebService with a default response of
 * one topic.
 *
 * @author fmauz
 */
@Path("rest/topics")
@Stateless
public class TopicsWebServiceMock {
    
    @POST
    @Produces(MediaType.APPLICATION_XML)
    public Response getReadableTopics(TopicsWebRequest request) {
        Topic t = new Topic("TestTopic", TopicCategory.OTHER);
        ACList acList = new ACList();
        acList.addACE(request.getUser(), new ACPermission[]{ACPermission.permREAD, ACPermission.permEDIT});
        t.setACList(acList);
        Node n = new Node();
        n.setBaseUrl("htttp://XXX");
        n.setId(UUID.randomUUID());
        n.setInstitution("remote Institution");
        n.setLocal(true);
        n.setPublicNode(false);
        t.setNode(n);
        
        Posting p = new Posting();
        p.setCreated(new Date());
        p.setId(100000);
        p.setOwner(request.getUser());
        p.setText("TestBeitrag");
        p.setTopic(t);
        t.setPostings(Arrays.asList(p));
        t.setOwner(request.getUser());
        TopicsList tl = new TopicsList();
        tl.setTopics(Arrays.asList(t));
        return Response.ok(tl).build();
    }
    
}
