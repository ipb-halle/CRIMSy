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
package de.ipb_halle.lbac.material.common.entity.hazard;

import de.ipb_halle.lbac.message.LocalUUIDConverter;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import org.apache.johnzon.mapper.JohnzonConverter;

/**
 *
 * @author fmauz
 */
@Entity
@Table(name = "hazards_materials_hist")
public class HazardsMaterialHistEntity implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;

    @Column
    private int materialid;

    @Column
    @Temporal(javax.persistence.TemporalType.TIMESTAMP)
    private Date mDate;

    @Column
    private Integer actorid;

    @Column
    private String digest;

    @Column
    private Integer typeid_old;

    @Column
    private Integer typeid_new;

    @Column
    private String remarks_old;

    @Column
    private String remarks_new;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getMaterialid() {
        return materialid;
    }

    public void setMaterialid(int materialid) {
        this.materialid = materialid;
    }

    public Date getmDate() {
        return mDate;
    }

    public void setmDate(Date mDate) {
        this.mDate = mDate;
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

    public Integer getTypeid_old() {
        return typeid_old;
    }

    public void setTypeid_old(Integer typeid_old) {
        this.typeid_old = typeid_old;
    }

    public Integer getTypeid_new() {
        return typeid_new;
    }

    public void setTypeid_new(Integer typeid_new) {
        this.typeid_new = typeid_new;
    }

    public String getRemarks_old() {
        return remarks_old;
    }

    public void setRemarks_old(String remarks_old) {
        this.remarks_old = remarks_old;
    }

    public String getRemarks_new() {
        return remarks_new;
    }

    public void setRemarks_new(String remarks_new) {
        this.remarks_new = remarks_new;
    }

}
