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
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 *
 * @author fmauz
 */
@Entity
@Table(name = "material_compositions")
public class MaterialCompositionEntity implements Serializable {

    private final static long serialVersionUID = 1L;

    @EmbeddedId
    private MaterialCompositionId id;

    @Column
    private Double concentration;

    @Column
    private String unit;

    public MaterialCompositionId getId() {
        return id;
    }

    public MaterialCompositionEntity setId(MaterialCompositionId id) {
        this.id = id;
        return this;
    }

    public Double getConcentration() {
        return concentration;
    }

    public MaterialCompositionEntity setConcentration(Double concentration) {
        this.concentration = concentration;
        return this;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

}
