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

import de.ipb_halle.lbac.admission.ACObjectEntity;
import de.ipb_halle.crimsy_api.AttributeTag;
import de.ipb_halle.crimsy_api.AttributeType;
import java.io.Serializable;
import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 *
 * @author fmauz
 */
@Entity
@Table(name = "materials")
public class MaterialEntity extends ACObjectEntity implements Serializable{

    private final static long serialVersionUID = 1L;

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @AttributeTag(type = AttributeType.BARCODE)
    private Integer materialid;

    @Column
    @AttributeTag(type = AttributeType.MATERIAL_TYPE)
    private int materialtypeid;

    @Temporal(TemporalType.TIMESTAMP)
    @Column
    private Date ctime;

    @Column
    @AttributeTag(type = AttributeType.DEACTIVATED)
    private boolean deactivated;
    @Column
    private Integer projectid;

    public Integer getMaterialid() {
        return materialid;
    }

    public void setMaterialid(Integer materialid) {
        this.materialid = materialid;
    }

    public int getMaterialtypeid() {
        return materialtypeid;
    }

    public void setMaterialtypeid(int materialtypeid) {
        this.materialtypeid = materialtypeid;
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }

    public Integer getProjectid() {
        return projectid;
    }

    public void setProjectid(Integer projectid) {
        this.projectid = projectid;
    }

    public boolean isDeactivated() {
        return deactivated;
    }

    public void setDeactivated(boolean deactivated) {
        this.deactivated = deactivated;
    }

}
