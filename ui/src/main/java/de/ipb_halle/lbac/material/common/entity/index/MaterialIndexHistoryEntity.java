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
package de.ipb_halle.lbac.material.common.entity.index;

import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;

/**
 *
 * @author fmauz
 */
@Entity
@Table(name = "material_indices_hist")
public class MaterialIndexHistoryEntity implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;

    @Column
    private int materialid;

    @Column
    private int typeid;

    @Column
    private Integer actorid;

    @Column
    @Temporal(jakarta.persistence.TemporalType.TIMESTAMP)
    private Date mDate;

    @Column
    private String digest;

    @Column
    private String value_old;

    @Column
    private String value_new;

    @Column
    private Integer rank_old;

    @Column
    private Integer rank_new;

    @Column
    private String language_old;

    @Column
    private String language_new;

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

    public int getTypeid() {
        return typeid;
    }

    public void setTypeid(int typeid) {
        this.typeid = typeid;
    }

    public Integer getActorid() {
        return actorid;
    }

    public void setActorid(Integer actorid) {
        this.actorid = actorid;
    }

    public Date getmDate() {
        return mDate;
    }

    public void setmDate(Date mDate) {
        this.mDate = mDate;
    }

    public String getDigest() {
        return digest;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getValue_old() {
        return value_old;
    }

    public void setValue_old(String value_old) {
        this.value_old = value_old;
    }

    public String getValue_new() {
        return value_new;
    }

    public void setValue_new(String value_new) {
        this.value_new = value_new;
    }

    public Integer getRank_old() {
        return rank_old;
    }

    public void setRank_old(Integer rank_old) {
        this.rank_old = rank_old;
    }

    public Integer getRank_new() {
        return rank_new;
    }

    public void setRank_new(Integer rank_new) {
        this.rank_new = rank_new;
    }

    public String getLanguage_old() {
        return language_old;
    }

    public void setLanguage_old(String language_old) {
        this.language_old = language_old;
    }

    public String getLanguage_new() {
        return language_new;
    }

    public void setLanguage_new(String language_new) {
        this.language_new = language_new;
    }

}
