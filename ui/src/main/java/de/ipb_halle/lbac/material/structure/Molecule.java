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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class Molecule  implements Serializable{

    protected final Logger logger = LogManager.getLogger(this.getClass().getName());

    protected String structureModel;
    private int id;
  

    public Molecule() {
    }

    public Molecule(String structureModel, int id) {
        this.structureModel = structureModel;
        this.id = id;
       
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

    //ToDo: implement method
    public boolean isEmptyMolecule() {
       return structureModel==null || structureModel.isEmpty();
    }

}
