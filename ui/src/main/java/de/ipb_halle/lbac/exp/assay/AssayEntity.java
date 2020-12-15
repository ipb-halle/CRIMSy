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

import de.ipb_halle.lbac.exp.LinkedDataType;
import de.ipb_halle.lbac.search.lang.AttributeTag;
import de.ipb_halle.lbac.search.lang.AttributeType;
import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author fbroda
 */
@Entity
@Table(name = "exp_assays")
public class AssayEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    @Id
    private Long exprecordid;

    @Column
    private LinkedDataType outcomeType;

    @AttributeTag(type = AttributeType.TEXT)
    @Column
    private String remarks;

    @Column
    private Integer targetid;

    @Column
    private String units;

    public Long getExpRecordId() {
        return this.exprecordid;
    }

    public LinkedDataType getOutcomeType() {
        return this.outcomeType;
    }

    public String getRemarks() {
        return this.remarks;
    }

    public Integer getTargetId() {
        return this.targetid;
    }

    public String getUnits() {
        return this.units;
    }

    public AssayEntity setExpRecordId(Long exprecordid) {
        this.exprecordid = exprecordid;
        return this;
    }

    public AssayEntity setOutcomeType(LinkedDataType outcomeType) {
        this.outcomeType = outcomeType;
        return this;
    }

    public AssayEntity setRemarks(String remarks) {
        this.remarks = remarks;
        return this;
    }

    public AssayEntity setTargetId(Integer targetid) {
        this.targetid = targetid;
        return this;
    }

    public AssayEntity setUnits(String units) {
        this.units = units;
        return this;
    }
}
