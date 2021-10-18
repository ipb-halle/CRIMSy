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
package de.ipb_halle.lbac.material.composition;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author fmauz
 */
@Entity
@Table(name = "components_history")
public class CompositionHistoryEntity implements Serializable {

    private static final long serialVersionUID = 1L;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;
    @Column
    private Integer materialid;
    @Column
    private Date mdate;
    @Column
    private Integer actorid;

    @Column
    private String digest;
    @Column
    private String action;
    @Column
    private Integer materialid_old;
    @Column
    private Integer materialid_new;
    @Column
    private Double concentration_old;
    @Column
    private Double concentration_new;
    @Column
    private String unit_old;
    @Column
    private String unit_new;

    public String getDigest() {
        return digest;
    }

    public CompositionHistoryEntity setDigest(String digest) {
        this.digest = digest;
        return this;
    }

    public String getAction() {
        return action;
    }

    public CompositionHistoryEntity setAction(String action) {
        this.action = action;
        return this;
    }

    public Integer getMaterialid_old() {
        return materialid_old;
    }

    public void setMaterialid_old(Integer materialid_old) {
        this.materialid_old = materialid_old;
    }

    public Integer getMaterialid_new() {
        return materialid_new;
    }

    public void setMaterialid_new(Integer materialid_new) {
        this.materialid_new = materialid_new;
    }

    public Double getConcentration_old() {
        return concentration_old;
    }

    public void setConcentration_old(Double concentration_old) {
        this.concentration_old = concentration_old;
    }

    public Double getConcentration_new() {
        return concentration_new;
    }

    public void setConcentration_new(Double concentration_new) {
        this.concentration_new = concentration_new;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getMaterialid() {
        return materialid;
    }

    public void setMaterialid(Integer materialid) {
        this.materialid = materialid;
    }

    public Date getMdate() {
        return mdate;
    }

    public void setMdate(Date mdate) {
        this.mdate = mdate;
    }

    public Integer getActorid() {
        return actorid;
    }

    public void setActorid(Integer actorid) {
        this.actorid = actorid;
    }

    public String getUnit_old() {
        return unit_old;
    }

    public void setUnit_old(String unit_old) {
        this.unit_old = unit_old;
    }

    public String getUnit_new() {
        return unit_new;
    }

    public void setUnit_new(String unit_new) {
        this.unit_new = unit_new;
    }

}
