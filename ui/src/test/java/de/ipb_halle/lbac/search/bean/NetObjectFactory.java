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
package de.ipb_halle.lbac.search.bean;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.search.document.Document;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.exp.Experiment;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageClassInformation;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.search.NetObject;
import de.ipb_halle.lbac.search.NetObjectImpl;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author fmauz
 */
public class NetObjectFactory {
    
    private Node localNode, remoteNode;
    private Document localDocument, remoteDocument;
    private Structure localStruc, remoteStruc;
    private Project localProject, remoteProject;
    private User localUser, remoteUser;
    private Item localItem, remoteItem;
    private Experiment localExp, remoteExp;
    private UUID localNodeId, remoteNodeId;
    
    public NetObjectFactory() {
        localNodeId = UUID.randomUUID();
        remoteNodeId = UUID.randomUUID();
    }
    
    public List<NetObject> createNetObjects() {
        List<NetObject> netObjects = new ArrayList<>();
        localNode = createLocalNode();
        remoteNode = createRemoteNode();
        localDocument = createDocument("localCol", localNode, "localDoc");
        remoteDocument = createDocument("remoteCol", remoteNode, "remoteDoc");
        localUser = createUser(localNode, "local User");
        remoteUser = createUser(remoteNode, "remote User");
        localProject = createProject(localUser, "localProject");
        remoteProject = createProject(remoteUser, "remoteProject");
        localStruc = createStructure(localUser, "localStructure", localProject);
        remoteStruc = createStructure(remoteUser, "remoteStructure", remoteProject);
        localItem = createItem(localUser, localStruc, "localItem");
        remoteItem = createItem(remoteUser, remoteStruc, "remoteItem");
        localExp = createExperiment(localUser, "localExp");
        remoteExp = createExperiment(remoteUser, "remoteExp");
        
        netObjects.add(new NetObjectImpl(localDocument, localNode));
        netObjects.add(new NetObjectImpl(remoteDocument, remoteNode));
        netObjects.add(new NetObjectImpl(localUser, localNode));
        netObjects.add(new NetObjectImpl(remoteUser, remoteNode));
        netObjects.add(new NetObjectImpl(localProject, localNode));
        netObjects.add(new NetObjectImpl(remoteProject, remoteNode));
        netObjects.add(new NetObjectImpl(localStruc, localNode));
        netObjects.add(new NetObjectImpl(remoteStruc, remoteNode));
        netObjects.add(new NetObjectImpl(localItem, localNode));
        netObjects.add(new NetObjectImpl(remoteItem, remoteNode));
        netObjects.add(new NetObjectImpl(localExp, localNode));
        netObjects.add(new NetObjectImpl(remoteExp, remoteNode));
        
        return netObjects;
    }
    
    public Node createLocalNode() {
        Node localNode = new Node();
        localNode.setInstitution("local");
        localNode.setLocal(true);
        localNode.setId(localNodeId);
        localNode.setBaseUrl("http://local");
        return localNode;
    }
    
    public Node createRemoteNode() {
        Node localNode = new Node();
        localNode.setInstitution("remote");
        localNode.setLocal(true);
        localNode.setId(remoteNodeId);
        localNode.setBaseUrl("http://remote");
        return localNode;
    }
    
    public Document createDocument(String collectionName, Node node, String documentName) {
        Document d = new Document();
        Collection col = new Collection();
        col.setNode(node);
        col.setName(collectionName);
        d.setCollection(col);
        d.setNode(node);
        d.setContentType("pdf");
        d.setPath("/path");
        d.setCollectionId(1);
        d.setNodeId(node.getId());
        d.setOriginalName(documentName);
        return d;
    }
    
    public Structure createStructure(User u, String name, Project project) {
        List<MaterialName> names = new ArrayList<>();
        names.add(new MaterialName(name, "de", 0));
        return new Structure("",
                0d,
                0d,
                1,
                names,
                project.getId(),
                new HazardInformation(),
                new StorageClassInformation(),
                null);
        
    }
    
    public User createUser(Node n, String name) {
        User u = new User();
        u.setName(name);
        u.setNode(n);
        return u;
    }
    
    public Project createProject(User user, String name) {
        Project p = new Project();
        p.setId(1);
        p.setName(name);
        p.setOwner(user);
        return p;
    }
    
    public Item createItem(User u, Material m, String name) {
        Item i = new Item();
        i.setMaterial(m);
        i.setDescription(name);
        i.setId(12014);
        i.setOwner(u);
        return i;
    }
    
    public Experiment createExperiment(User u, String code) {
        return new Experiment(1, code, code, false, null, u, new Date());
    }
}
