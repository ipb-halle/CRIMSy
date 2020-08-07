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

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "files")
public class FileObjectEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    @Id
    private UUID id;

    @Size(min = 1, max = 255)
    private String name;

    @Size(min = 1, max = 255)
    private String filename;

    @Size(min = 1, max = 255)
    private String hash;

    @Column
    private Timestamp created;

    @Column(name = "user_id")
    private UUID user;

    @Column(name = "collection_id")
    private UUID collection;

    private String document_language;

    /**
     * default constructor
     */
    public FileObjectEntity() {
    }

    /*
     * getter & setter
     */
    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getFilename() {
        return filename;
    }

    public String getHash() {
        return hash;
    }

    public Timestamp getCreated() {
        return created;
    }

    public UUID getUser() {
        return user;
    }

    public UUID getCollection() {
        return collection;
    }

    public String getDocument_language() {
        return document_language;
    }

    public FileObjectEntity setCollection(UUID collection) {
        this.collection = collection;
        return this;
    }

    public FileObjectEntity setCreated(Timestamp created) {
        this.created = created;
        return this;
    }
    
    public FileObjectEntity setCreatedFromDate(Date d){
        this.created=new Timestamp(d.getTime());
        return this;
    }

    public FileObjectEntity setFilename(String filename) {
        this.filename = filename;
        return this;
    }

    public FileObjectEntity setHash(String hash) {
        this.hash = hash;
        return this;
    }
    
     public FileObjectEntity setId(UUID id) {
        this.id = id;
        return this;
    }

    public FileObjectEntity setName(String name) {
        this.name = name;
        return this;
    }

    public FileObjectEntity setDocument_language(String document_language) {
        this.document_language = document_language;
        return this;
    }

    public FileObjectEntity setUser(UUID user) {
        this.user = user;
        return this;
    }

}
