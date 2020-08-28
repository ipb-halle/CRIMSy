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

import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.forum.topics.TopicCategory;
import de.ipb_halle.lbac.entity.ACObject;
import de.ipb_halle.lbac.entity.Cloud;
import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.entity.Obfuscatable;
import de.ipb_halle.lbac.admission.User;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author fmauz
 */
public class Topic extends ACObject implements Serializable, Comparable<Topic>, DTO, Obfuscatable {

    private final static long serialVersionUID = 1L;

    private TopicCategory category;

    private String cloudName;

    private boolean editable;

    private Integer id;

    private String name;

    private Node node;

    private List<Posting> postings = new ArrayList<>();

    /**
     * Default constructor
     */
    public Topic() {
        
    }

    /**
     * Constructor
     *
     * @param name
     * @param category
     */
    public Topic(String name, TopicCategory category) {
       
        this.name = name;
        this.category = category;
    }

    /**
     * Constructor
     *
     * @param entity
     * @param acl
     * @param owner
     * @param n
     * @param cloudName
     * @param c
     */
    public Topic(
            TopicEntity entity,
            ACList acl,
            User owner,
            Node n,
            String cloudName) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.category = entity.getCategory();
        setACList(acl);
        setOwner(owner);
        this.node = n;
        this.cloudName = cloudName;
    }

    @Override
    public int compareTo(Topic o) {
        return name.compareTo(o.name);
    }

    @Override
    public TopicEntity createEntity() {
        TopicEntity entity = new TopicEntity();
        entity.setACList(getACList().getId());
        entity.setOwner(getOwner().getId());
        return entity
                .setCloudName(cloudName)
                .setCategory(category)
                .setId(id)
                .setName(name)
                .setNode(node.getId());

    }

    public String getCloudName() {
        return cloudName;
    }

    public TopicCategory getCategory() {
        return category;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Node getNode() {
        return node;
    }

    @Override
    public void obfuscate() {
        super.obfuscate();
        for (Posting p : postings) {
            p.obfuscate();
        }
    }

    public List<Posting> getPostings() {
        return postings;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setCategory(TopicCategory category) {
        this.category = category;
    }

    public void setCloudName(String cloudName) {
        this.cloudName = cloudName;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public void setPostings(List<Posting> postings) {
        this.postings = postings;
    }

}
