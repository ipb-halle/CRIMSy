/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.subtype.structure;

import de.ipb_halle.lbac.material.Material;

/**
 *
 * @author fmauz
 */
public class StructureInformation {

    protected Double exactMolarMass;
    protected Double molarMass;
    protected String sumFormula;
    protected String structureModel = "";

    public StructureInformation() {
    }

    public StructureInformation(Material m) {
        
    }

    public Double getExactMolarMass() {
        return exactMolarMass;
    }

    public void setExactMolarMass(Double exactMolarMass) {
        this.exactMolarMass = exactMolarMass;
    }

    public Double getMolarMass() {
        return molarMass;
    }

    public void setMolarMass(Double molarMass) {
        this.molarMass = molarMass;
    }

    public String getSumFormula() {
        return sumFormula;
    }

    public void setSumFormula(String sumFormula) {
        this.sumFormula = sumFormula;
    }

    public String getStructureModel() {
        return structureModel;
    }

    public void setStructureModel(String structureModel) {
        this.structureModel = structureModel;
    }

}
