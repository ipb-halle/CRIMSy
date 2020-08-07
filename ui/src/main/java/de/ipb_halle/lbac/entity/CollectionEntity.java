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
package de.ipb_halle.lbac.entity;

/**
 * Collection class represents a collection in the Bioactives Cloud. Model for
 * collection managment
 */

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.UUID;

@Entity
@Table(name = "collections")
public class CollectionEntity extends ACObjectEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    @Id
    private UUID id;

    @Column
    @Size(min = 1, max = 255)
    private String description;

    @Column
    @Size(min = 3, max = 100)
    private String name;

    @Column(name = "node_id")
    private UUID node;

    @Column
    @Size(min = 0, max = 255)
    private String indexPath;

    @Column
    @Size(min = 0, max = 255)
    private String storagePath;

    /**
     * default constructor
     */
    public CollectionEntity() {
    }

    //*** getter and setter ***
    public UUID getId() {
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

    public UUID getNode() {
        return this.node;
    }

    public CollectionEntity setId(UUID n) {
        this.id = n;
        return this;
    }

    public CollectionEntity setDescription(String d) {
        this.description = d;
        return this;
    }

    public CollectionEntity setName(String n) {
        this.name = n;
        return this;
    }

    public CollectionEntity setIndexPath(String n) {
        this.indexPath = n;
        return this;
    }

    public CollectionEntity setStoragePath(String n) {
        this.storagePath = n;
        return this;
    }

    public CollectionEntity setNode(UUID n) {
        this.node = n;
        return this;
    }
}
