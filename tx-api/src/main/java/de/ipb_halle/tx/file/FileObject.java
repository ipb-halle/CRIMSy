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
package de.ipb_halle.tx.file;

import de.ipb_halle.crimsy_api.DTO;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

public class FileObject implements Serializable, DTO {

    private final static long serialVersionUID = 1L;
    private Integer id;
    private String name;
    private String fileLocation;
    private String hash;
    private Date created;
    private Integer userId;
    private Integer collectionId;
    private String document_language;

    /**
     * default constructor
     */
    public FileObject() {
        created = new Date();
        document_language = "en";
    }

    /**
     * Constructor
     *
     * @param entity
     */
    public FileObject(FileObjectEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.fileLocation = entity.getFilename();
        this.hash = entity.getHash();
        this.created = entity.getCreated();
        this.document_language = entity.getDocument_language();
        this.collectionId = entity.getCollectionId();
        this.userId = entity.getUserId();
    }

    @Override
    public FileObjectEntity createEntity() {
        return new FileObjectEntity()
                .setCollectionId(collectionId)
                .setCreatedFromDate(created)
                .setDocument_language(document_language)
                .setFilename(fileLocation)
                .setHash(hash)
                .setName(name)
                .setUserId(userId)
                .setId(id);
    }

    /*
     * getter & setter
     */
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date d) {
        this.created = d;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public Integer getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(Integer colId) {
        this.collectionId = colId;
    }

    public Integer getUserId() {
        return userId;
    }
    
    public void setUserId(Integer uid) {
        this.userId = uid;
    }
    
    public String getDocument_language() {
        return document_language;
    }

    public void setDocument_language(String document_language) {
        this.document_language = document_language;
    }

}
