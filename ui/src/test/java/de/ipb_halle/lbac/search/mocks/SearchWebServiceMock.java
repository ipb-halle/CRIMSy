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
package de.ipb_halle.lbac.search.mocks;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.exp.RemoteExperiment;
import de.ipb_halle.lbac.items.RemoteItem;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.RemoteMaterial;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.SearchWebRequest;
import de.ipb_halle.lbac.search.SearchWebResponse;
import de.ipb_halle.lbac.search.bean.Type;
import java.util.Date;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author fmauz
 */
@Path("rest/search/")
public class SearchWebServiceMock {

    public static int SLEEPTIME_BETWEEN_REQUESTS = 500;
    private static int request = 0;
    private User user;
    private Node node;

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response search(SearchWebRequest request) {
        node = createRemoteNode();
        user = createRemoteUser(node);

        SearchWebResponse response = new SearchWebResponse();
        try {
            response.getRemoteItem().add(createItem());
            response.getRemoteMaterials().add(createStructure());
            response.getRemoteExperiments().add(createRemoteExperiment());
        } catch (Exception e) {

        }
        return Response.ok(response).build();
    }

    private RemoteItem createItem() {
        RemoteItem remoteItem = new RemoteItem();
        remoteItem.setAmount(10d);
        remoteItem.setDescription("RemoteItem-desc");
        remoteItem.setId(100);
        remoteItem.setMaterialName("RemoteItemMaterialName");
        remoteItem.setProjectName("remoteItemProjectName");
        remoteItem.setUnit("remoteItemUnit");
        return remoteItem;

    }

    private RemoteMaterial createStructure() {
        RemoteMaterial remoteStructure = new RemoteMaterial();
        remoteStructure.setId(20);
        remoteStructure.setMoleculeString("MOLECULE");
        remoteStructure.setSumFormula("CO2");
        remoteStructure.addName("STRCUTURE-1");
        remoteStructure.addName("STRCUTURE-2");
        remoteStructure.getIndices().put(1, "INDEX-1");
        remoteStructure.setType(new Type(SearchTarget.MATERIAL, MaterialType.STRUCTURE));
        return remoteStructure;
    }

    private RemoteExperiment createRemoteExperiment() {
        RemoteExperiment exp = new RemoteExperiment();
        exp.setCode("REMOTE-EXP");
        exp.setCreationTime(new Date());
        exp.setDescription("REMOTE-EXP-DESC");
        exp.setId(100);
        exp.setOwner(user);
        exp.setProjectId(200);
        return exp;
    }

    private Node createRemoteNode() {
        Node node = new Node();
        node.setBaseUrl("REMOTE-NODE-URL");
        node.setId(UUID.randomUUID());
        node.setInstitution("REMOTE-NODE-INST");
        node.setLocal(false);
        node.setPublicNode(false);
        node.setVersion("1.0");
        return node;
    }

    private User createRemoteUser(Node node) {
        User user = new User();
        user.setEmail("REMOTE-USER-EMAIL");
        user.setPhone("REMOTE-USER-PHONE");
        user.setId(1000);
        user.setName("REMOTE-USER");
        user.setLogin("REMOTE-USER-LOGIN");
        user.setNode(node);
        return user;
    }

}
