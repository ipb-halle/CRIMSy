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
package de.ipb_halle.lbac.material.structure;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author fmauz
 */
@Entity
@Table(name = "structures")
public class StructureEntity implements Serializable {

    @Id
    private Integer id;

    @Column
    private String sumformula;

    @Column
    private Double molarmass;

    @Column
    private Double exactmolarmass;

    @Column
    private Integer moleculeid;
    
    

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSumformula() {
        return sumformula;
    }

    public void setSumformula(String sumformula) {
        this.sumformula = sumformula;
    }

    public Double getMolarmass() {
        return molarmass;
    }

    public void setMolarmass(Double molarmass) {
        this.molarmass = molarmass;
    }

    public Double getExactmolarmass() {
        return exactmolarmass;
    }

    public void setExactmolarmass(Double exactmolarmass) {
        this.exactmolarmass = exactmolarmass;
    }

    public Integer getMoleculeid() {
        return moleculeid;
    }

    public void setMoleculeid(Integer moleculeid) {
        this.moleculeid = moleculeid;
    }

}
