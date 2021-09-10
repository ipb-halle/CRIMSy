/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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

import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.MaterialType;

/**
 *
 * @author fmauz
 */
public class Concentration {

    private final Material material;
    private Double concentration;

    public Concentration(Material material) {
        this.material = material;
    }

    public boolean isSameMaterial(Material materialToCheck) {
        return materialToCheck.getId() == material.getId();
    }

    public Double getConcentration() {
        return concentration;
    }

    public void setConcentration(Double concentration) {
        this.concentration = concentration;
    }

    public int getMaterialId() {
        return material.getId();
    }

    public String getMaterialName() {
        return material.getFirstName();
    }

    public MaterialType getMaterialType() {
        return material.getType();
    }

    public Material getMaterial() {
        return material;
    }

}
