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
package de.ipb_halle.lbac.exp;

import de.ipb_halle.lbac.exp.ExpRecord;
import de.ipb_halle.lbac.exp.LinkedData;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.structure.Molecule;

import java.io.Serializable;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Bean for interacting with the ui to present and manipulate a experiments
 *
 * @author fbroda
 */
@Dependent
public class LinkedDataAgent implements Serializable {

    private final static long serialVersionUID = 1L;

    private LinkedData linkedData;

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    public LinkedData getLinkedData() { 
        return this.linkedData;
    }

    public boolean getHasStructure() { 
        if ((this.linkedData != null) 
                && (this.linkedData.getMaterial() != null)
                && (this.linkedData.getMaterial().getType() == MaterialType.STRUCTURE)) {
            return true;
        }
        return false;
    }

    public Item getItem() {
        if (this.linkedData != null) {
            return this.linkedData.getItem();
        }
        return null;
    }

    public Material getMaterial() {
        if (this.linkedData != null)  {
            return this.linkedData.getMaterial();
        } 
        return null;
    }

    public void setLinkedData(LinkedData data) {
        this.linkedData = data;
    }
}
