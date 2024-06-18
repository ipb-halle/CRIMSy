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

import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 *
 * @author fmauz
 */
@Entity
@Table(name = "material_hazards")
public class HazardsMaterialsEntity implements Serializable {

    @EmbeddedId
    private HazardMaterialId id;

    @Column
    private String remarks;

    public HazardsMaterialsEntity() {
    }

    public HazardsMaterialsEntity(HazardMaterialId id, String remarks) {
        this.id = id;
        this.remarks = remarks;
    }

    public HazardMaterialId getId() {
        return id;
    }

    public void setId(HazardMaterialId id) {
        this.id = id;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

}
