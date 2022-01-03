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

import de.ipb_halle.lbac.material.Material;
import java.io.Serializable;

/**
 *
 * @author fmauz
 */
public class StructureInformation implements Serializable {

    private static final long serialVersionUID = 1L;

    protected Double exactMolarMass;
    protected Double averageMolarMass;
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

    public Double getAverageMolarMass() {
        return averageMolarMass;
    }

    public void setAverageMolarMass(Double molarMass) {
        this.averageMolarMass = molarMass;
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
