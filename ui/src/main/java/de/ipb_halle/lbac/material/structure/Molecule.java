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
import java.io.StringReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.MDLV2000Reader;

/**
 *
 * @author fmauz
 */
public class Molecule implements Serializable {

    private static final long serialVersionUID = 1L;

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

    public boolean isEmptyMolecule() {
        boolean empty = structureModel == null || structureModel.isEmpty();
        boolean noAtoms = true;
        try (MDLV2000Reader reader = new MDLV2000Reader(new StringReader(structureModel))) {
            IAtomContainer atom = reader.read(new AtomContainer());
            noAtoms = atom.getAtomCount() == 0;
        } catch (Exception e) {
            return true;
        }
        return empty || noAtoms;

    }

}
