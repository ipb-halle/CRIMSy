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
package de.ipb_halle.lbac.material.biomaterial;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import de.ipb_halle.lbac.material.common.history.HistoryEntityId;

/**
 *
 * @author fmauz
 */
@Entity
@Table(name = "biomaterial_history")
public class BioMaterialHistoryEntity implements Serializable {

    @EmbeddedId
    private HistoryEntityId id;

    @Column
    private String digest;
    @Column
    private String action;
    @Column
    private Integer tissueid_old;
    @Column
    private Integer tissueid_new;
    @Column
    private Integer taxoid_old;
    @Column
    private Integer taxoid_new;

    public HistoryEntityId getId() {
        return id;
    }

    public BioMaterialHistoryEntity setId(HistoryEntityId id) {
        this.id = id;
        return this;
    }

    public String getDigest() {
        return digest;
    }

    public BioMaterialHistoryEntity setDigest(String digest) {
        this.digest = digest;
        return this;
    }

    public String getAction() {
        return action;
    }

    public BioMaterialHistoryEntity setAction(String action) {
        this.action = action;
        return this;
    }

    public Integer getTissueid_old() {
        return tissueid_old;
    }

    public BioMaterialHistoryEntity setTissueid_old(Integer tissueid_old) {
        this.tissueid_old = tissueid_old;
        return this;
    }

    public Integer getTissueid_new() {
        return tissueid_new;
    }

    public BioMaterialHistoryEntity setTissueid_new(Integer tissueid_new) {
        this.tissueid_new = tissueid_new;
        return this;
    }

    public Integer getTaxoid_old() {
        return taxoid_old;
    }

    public BioMaterialHistoryEntity setTaxoid_old(Integer taxoid_old) {
        this.taxoid_old = taxoid_old;
        return this;
    }

    public Integer getTaxoid_new() {
        return taxoid_new;
    }

    public BioMaterialHistoryEntity setTaxoid_new(Integer taxoid_new) {
        this.taxoid_new = taxoid_new;
        return this;
    }

}
