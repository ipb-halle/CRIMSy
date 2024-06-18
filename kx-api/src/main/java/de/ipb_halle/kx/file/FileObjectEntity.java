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
package de.ipb_halle.kx.file;

import de.ipb_halle.crimsy_api.AttributeTag;
import de.ipb_halle.crimsy_api.AttributeType;
import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Table(name = "files")
public class FileObjectEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    @AttributeTag(type = AttributeType.BARCODE)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;

    @Column
    private String name;

    @Column
    private String filename;

    @Column
    private String hash;

    @Column
    private Timestamp created;

    @Column(name = "user_id")
    private Integer userId;

    @AttributeTag(type = AttributeType.COLLECTION)
    @Column(name = "collection_id")
    private Integer collectionId;

    @Column
    private String document_language;

    /**
     * default constructor
     */
    public FileObjectEntity() {
    }

    /*
     * getter & setter
     */
    public Integer getId() {
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

    public Integer getUserId() {
        return userId;
    }

    public Integer getCollectionId() {
        return collectionId;
    }

    public String getDocumentLanguage() {
        return document_language;
    }

    public FileObjectEntity setCollectionId(Integer colId) {
        this.collectionId = colId;
        return this;
    }

    public FileObjectEntity setCreated(Timestamp created) {
        this.created = created;
        return this;
    }

    public FileObjectEntity setCreatedFromDate(Date d) {
        this.created = new Timestamp(d.getTime());
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

    public FileObjectEntity setId(Integer id) {
        this.id = id;
        return this;
    }

    public FileObjectEntity setName(String name) {
        this.name = name;
        return this;
    }

    public FileObjectEntity setDocumentLanguage(String lang) {
        this.document_language = lang;
        return this;
    }

    public FileObjectEntity setUserId(Integer uid) {
        this.userId = uid;
        return this;
    }

}
