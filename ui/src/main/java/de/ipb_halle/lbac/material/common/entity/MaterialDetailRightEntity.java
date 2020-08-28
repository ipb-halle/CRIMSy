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

import de.ipb_halle.lbac.message.LocalUUIDConverter;
import java.io.Serializable;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import org.apache.johnzon.mapper.JohnzonConverter;

/**
 *
 * @author fmauz
 */
@Entity
@Table(name = "materialdetailrights")
public class MaterialDetailRightEntity implements Serializable {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Integer id;

    @Column
    private int materialid;

    @Column
    private Integer aclistid;

    @Column
    private int materialtypeid;

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

    public Integer getAclistid() {
        return aclistid;
    }

    public void setAclistid(Integer aclistid) {
        this.aclistid = aclistid;
    }

   

    public int getMaterialtypeid() {
        return materialtypeid;
    }

    public void setMaterialtypeid(int materialtypeid) {
        this.materialtypeid = materialtypeid;
    }

}
