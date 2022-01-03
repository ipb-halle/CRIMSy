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
package de.ipb_halle.lbac.material.common.entity;

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
 @Table(name = "materials_hist")
public class MaterialHistoryEntity implements Serializable {

    @EmbeddedId
    private HistoryEntityId id;

    @Column
    private String digest;

    @Column
    private String action;

    @Column
    private Integer aclistid_old;

    @Column
    private Integer aclistid_new;

    @Column
    private Integer projectid_old;

    @Column
    private Integer projectid_new;

    @Column
    private Integer ownerid_old;

    @Column
    private Integer ownerid_new;

    public HistoryEntityId getId() {
        return id;
    }

    public void setId(HistoryEntityId id) {
        this.id = id;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getAclistid_old() {
        return aclistid_old;
    }

    public void setAclistid_old(Integer aclistid_old) {
        this.aclistid_old = aclistid_old;
    }

    public Integer getAclistid_new() {
        return aclistid_new;
    }

    public void setAclistid_new(Integer aclistid_new) {
        this.aclistid_new = aclistid_new;
    }

    public Integer getProjectid_old() {
        return projectid_old;
    }

    public void setProjectid_old(Integer projectid_old) {
        this.projectid_old = projectid_old;
    }

    public Integer getProjectid_new() {
        return projectid_new;
    }

    public void setProjectid_new(Integer projectid_new) {
        this.projectid_new = projectid_new;
    }

    public Integer getOwnerid_old() {
        return ownerid_old;
    }

    public void setOwnerid_old(Integer ownerid_old) {
        this.ownerid_old = ownerid_old;
    }

    public Integer getOwnerid_new() {
        return ownerid_new;
    }

    public void setOwnerid_new(Integer ownerid_new) {
        this.ownerid_new = ownerid_new;
    }

}
