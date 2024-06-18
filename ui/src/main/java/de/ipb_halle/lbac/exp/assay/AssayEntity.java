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

import de.ipb_halle.lbac.datalink.LinkedDataType;
import de.ipb_halle.crimsy_api.AttributeTag;
import de.ipb_halle.crimsy_api.AttributeType;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
    @Enumerated(EnumType.ORDINAL)
    private LinkedDataType outcomeType;

    @AttributeTag(type = AttributeType.TEXT)
    @Column
    private String remarks;

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

    public AssayEntity setUnits(String units) {
        this.units = units;
        return this;
    }
}
