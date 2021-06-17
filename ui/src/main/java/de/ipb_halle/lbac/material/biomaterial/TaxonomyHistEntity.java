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

/**
 *
 * @author fmauz
 */
@Entity
@Table(name = "taxonomy_history")
public class TaxonomyHistEntity implements Serializable {

    @EmbeddedId
    private TaxonomyHistEntityId id;

    @Column
    private String digest;

    @Column
    private String action;

    @Column
    private Integer level_old;

    @Column
    private Integer level_new;

    @Column
    private Integer parentid_old;

    @Column
    private Integer parentid_new;

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public TaxonomyHistEntityId getId() {
        return id;
    }

    public void setId(TaxonomyHistEntityId id) {
        this.id = id;
    }

    public Integer getLevel_old() {
        return level_old;
    }

    public void setLevel_old(Integer level_old) {
        this.level_old = level_old;
    }

    public Integer getLevel_new() {
        return level_new;
    }

    public void setLevel_new(Integer level_new) {
        this.level_new = level_new;
    }

    public Integer getParentid_old() {
        return parentid_old;
    }

    public void setParentid_old(Integer parentid_old) {
        this.parentid_old = parentid_old;
    }

    public Integer getParentid_new() {
        return parentid_new;
    }

    public void setParentid_new(Integer parentid_new) {
        this.parentid_new = parentid_new;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

}
