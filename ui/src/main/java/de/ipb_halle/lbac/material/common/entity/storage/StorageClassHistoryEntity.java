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
package de.ipb_halle.lbac.material.common.entity.storage;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *
 * @author fmauz
 */
@Entity
@Table(name = "storages_hist")
public class StorageClassHistoryEntity implements Serializable {

    @EmbeddedId
    private StorageClassHistoryId id;

    @Column
    private Integer actorid;

    @Column
    private String digest;

    @Column
    private String description_old;

    @Column
    private String description_new;

    @Column
    private Integer storageclass_old;

    @Column
    private Integer storageclass_new;

    public StorageClassHistoryId getId() {
        return id;
    }

    public void setId(StorageClassHistoryId id) {
        this.id = id;
    }

    public Integer getActorid() {
        return actorid;
    }

    public void setActorid(Integer actorid) {
        this.actorid = actorid;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getDescription_old() {
        return description_old;
    }

    public void setDescription_old(String description_old) {
        this.description_old = description_old;
    }

    public String getDescription_new() {
        return description_new;
    }

    public void setDescription_new(String description_new) {
        this.description_new = description_new;
    }

    public Integer getStorageclass_old() {
        return storageclass_old;
    }

    public void setStorageclass_old(Integer storageclass_old) {
        this.storageclass_old = storageclass_old;
    }

    public Integer getStorageclass_new() {
        return storageclass_new;
    }

    public void setStorageclass_new(Integer storageclass_new) {
        this.storageclass_new = storageclass_new;
    }

}
