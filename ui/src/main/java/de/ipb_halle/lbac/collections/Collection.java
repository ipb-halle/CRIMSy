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
package de.ipb_halle.lbac.collections;

/**
 * Collection class represents a collection in the Bioactives Cloud. Model for
 * collection managment
 */
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACObject;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.entity.Obfuscatable;
import java.io.Serializable;
import java.nio.file.Paths;
import de.ipb_halle.lbac.file.save.AttachmentHolder;

public class Collection extends ACObject implements Serializable, Obfuscatable, DTO, AttachmentHolder {

    protected final static long serialVersionUID = 1L;

    protected Integer id;

    public String COLLECTIONS_BASE_FOLDER = "/data/ui/collections";

    protected String description;

    protected String name;

    protected Node node;

    protected String indexPath;

    protected String storagePath;

    protected Long countDocs = -1L;

    /**
     * default constructor
     */
    public Collection() {

    }

    /**
     * Constructor
     *
     * @param entity
     * @param n
     * @param acl
     * @param owner
     * @param countDocs
     */
    public Collection(
            CollectionEntity entity,
            Node n,
            ACList acl,
            User owner,
            Long countDocs) {
        this.id = entity.getId();
        this.description = entity.getDescription();
        this.indexPath = entity.getIndexPath();
        this.name = entity.getName();
        this.storagePath = entity.getStoragePath();
        this.node = n;
        setOwner(owner);
        setACList(acl);
        this.countDocs = countDocs;

    }

    @Override
    public CollectionEntity createEntity() {
        CollectionEntity entity = new CollectionEntity();
        entity.setACList(getACList().getId());
        entity.setOwner(getOwner().getId());
        return entity
                .setId(id)
                .setDescription(description)
                .setIndexPath(indexPath)
                .setName(name)
                .setStoragePath(storagePath);
    }

    //*** getter and setter ***
    @Override
    public Integer getId() {
        return this.id;
    }

    public String getDescription() {
        return this.description;
    }

    public String getName() {
        return this.name;
    }

    public String getIndexPath() {
        return this.indexPath;
    }

    public String getStoragePath() {
        return this.storagePath;
    }

    public Node getNode() {
        return this.node;
    }

    public Long getCountDocs() {
        return countDocs;
    }

    /**
     * set indexPath and storagePath to null as this information should not be
     * forwarded to remote sites.
     */
    @Override
    public void obfuscate() {
        super.obfuscate();
        this.indexPath = null;
        this.storagePath = null;

    }

    public void setId(Integer n) {
        this.id = n;
    }

    public void setDescription(String d) {
        this.description = d;
    }

    public void setName(String n) {
        this.name = n;
    }

    public void setIndexPath(String n) {
        this.indexPath = n;
    }

    public void setStoragePath(String n) {
        this.storagePath = n;
    }

    public void setNode(Node n) {
        this.node = n;
    }

    public void setCountDocs(Long countDocs) {
        this.countDocs = countDocs;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("COLLECTION DUMP");
        sb.append("\n  id=");
        sb.append(this.id);
        sb.append("\n  name=");
        sb.append(this.name);
        sb.append("\n  indexPath=");
        sb.append(this.indexPath);
        sb.append("\n  storagePath=");
        sb.append(this.storagePath);
        sb.append("\n  nodeId=");
        sb.append(this.node.getId());
        sb.append("\n  countDocs=");
        sb.append(this.countDocs);
        sb.append("\n");
        return sb.toString();
    }

    @Override
    public String getBaseFolder() {
        return Paths.get(COLLECTIONS_BASE_FOLDER, getName()).toString();
    }

}
