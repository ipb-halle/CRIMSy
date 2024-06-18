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
import de.ipb_halle.lbac.admission.ACObjectEntity;

import jakarta.validation.constraints.Size;
import de.ipb_halle.crimsy_api.AttributeTag;
import de.ipb_halle.crimsy_api.AttributeType;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "collections")
public class CollectionEntity extends ACObjectEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    @AttributeTag(type = AttributeType.COLLECTION)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;

    @Column
    @Size(min = 1, max = 255)
    private String description;

    @Column
    @Size(min = 3, max = 100)
    private String name;

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

    public CollectionEntity setId(Integer n) {
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

}
