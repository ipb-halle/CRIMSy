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
package de.ipb_halle.lbac.material.component;

/**
 *
 * @author fmauz
 */
public class Molecule {

    public enum MoleculeFormat {
        V2000,
        V3000
    }
    protected String structureModel;
    private int id;
    private MoleculeFormat modelType;

    public Molecule() {
    }

    public Molecule(String structureModel, int id) {
        this.structureModel = structureModel;
        this.id = id;
        this.modelType=MoleculeFormat.V2000;
    }

    public Molecule(String structureModel, int id, String format) {
        this.structureModel = structureModel;
        this.id = id;
        modelType = MoleculeFormat.valueOf(format);
    }

    public String getStructureModel() {
        return structureModel;
    }

    public void setStructureModel(String structureModel) {
        this.structureModel = structureModel;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public MoleculeFormat getModelType() {
        return modelType;
    }

    public void setModelType(MoleculeFormat modelType) {
        this.modelType = modelType;
    }

}
