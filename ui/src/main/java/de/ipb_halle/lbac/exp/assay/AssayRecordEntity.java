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
package de.ipb_halle.lbac.exp.assay;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

/**
 * @author fbroda
 */
@Entity
@Table(name = "exp_assay_records")
public class AssayRecordEntity implements Serializable {

    private final static long serialVersionUID = 1L;
   
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long recordid;

    @Column
    private Long exprecordid;

    @Column
    private Integer itemid;

    @Column
    private Integer materialid;

    @Column
    private AssayOutcomeType type;

    @Type(type = "RawJsonb")
    private String outcome;

    public Long getExpRecordId() {
        return this.exprecordid;
    }

    public Integer getItemId() {
        return this.itemid;
    }

    public Integer getMaterialId() {
        return this.materialid;
    }

    public String getOutcome() {
        return this.outcome;
    }

    public Long getRecordId() {
        return this.recordid;
    }

    public AssayOutcomeType getType() {
        return this.type;
    }

    public AssayRecordEntity setExpRecordId(Long exprecordid) {
        this.exprecordid = exprecordid;
        return this;
    }

    public AssayRecordEntity setItemId(Integer itemid) {
        this.itemid = itemid;
        return this;
    }

    public AssayRecordEntity setMaterialId(Integer materialid) {
        this.materialid = materialid;
        return this;
    }

    public AssayRecordEntity setOutcome(String outcome) {
        this.outcome = outcome;
        return this;
    }

    public AssayRecordEntity setRecordId(Long recordid) {
        this.recordid = recordid;
        return this;
    }

    public AssayRecordEntity setType(AssayOutcomeType type) {
        this.type = type;
        return this;
    }

}
