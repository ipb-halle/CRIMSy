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
package de.ipb_halle.lbac.datalink;

import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.Hazard;
import de.ipb_halle.lbac.material.common.IndexEntry;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.Dependent;

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
    private Map<Hazard, String> hazardImageLocs = new HashMap<>();
    private Material material;

    @PostConstruct
    public void init() {
        hazardImageLocs.put(Hazard.explosive, "/resources/img/hazards/GHS01.png");
        hazardImageLocs.put(Hazard.highlyFlammable, "/resources/img/hazards/GHS02.png");
        hazardImageLocs.put(Hazard.oxidizing, "/resources/img/hazards/GHS03.png");
        hazardImageLocs.put(Hazard.compressedGas, "/resources/img/hazards/GHS04.png");
        hazardImageLocs.put(Hazard.corrosive, "/resources/img/hazards/GHS05.png");
        hazardImageLocs.put(Hazard.poisonous, "/resources/img/hazards/GHS06.png");
        hazardImageLocs.put(Hazard.irritant, "/resources/img/hazards/GHS07.png");
        hazardImageLocs.put(Hazard.unhealthy, "/resources/img/hazards/GHS08.png");
        hazardImageLocs.put(Hazard.environmentallyHazardous, "/resources/img/hazards/GHS09.png");
        hazardImageLocs.put(Hazard.danger, "/resources/img/hazards/GHS07.png");
        hazardImageLocs.put(Hazard.attention, "/resources/img/hazards/GHS07.png");
    }

    public LinkedData getLinkedData() {
        return this.linkedData;
    }

    public boolean getHasStructure() {
        if ((this.linkedData != null)
                && (material != null)
                && (material.getType() == MaterialType.STRUCTURE)) {
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
        return material;
    }

    public void setLinkedData(LinkedData data) {
        if (data.getMaterial() != null) {
            material = data.getMaterial();
        }
        if (data.getItem() != null) {
            material = data.getItem().getMaterial();
        }

        this.linkedData = data;
    }

    public Set<Hazard> getHazards() {
        if (material == null) {
            return new HashSet<>();
        }
        return material.getHazards().getHazards();
    }

    public String getImageIconOf(Hazard hazard) {
        return hazardImageLocs.get(hazard);
    }

    public List<String> getNamesOfMaterial(int maxNames) {
        if (material == null) {
            return new ArrayList<>();
        }
        List<String> names = new ArrayList<>();
        int nameCount = Math.min(material.getNames().size(), maxNames);
        for (int i = 0; i < nameCount; i++) {
            names.add(material.getNames().get(i).getValue());
        }
        return names;
    }

    public List<String[]> getIndices() {
        List<String[]> indices = new ArrayList<>();
        if (material == null) {
            return new ArrayList<>();
        }
        addIndex(indices, 3, "CAS");
        addIndex(indices, 4, "SMILES");
        addIndex(indices, 5, "InChI");
        return indices;
    }

    private void addIndex(List<String[]> indices, int typeid, String indexName) {
        boolean indexFound = false;
        for (IndexEntry ie : material.getIndices()) {
            if (ie.getTypeId() == typeid) {
                indices.add(new String[]{indexName, ie.getValue()});
                indexFound = true;
            }
        }
        if (!indexFound) {
            indices.add(new String[]{indexName, "not available"});
        }
    }

    public String getAmountOfItem() {
        if (linkedData == null || linkedData.getItem() == null) {
            return "";
        }

        double amount = linkedData.getItem().getAmount();

        String unit = linkedData.getItem().getUnit() == null ? "" : linkedData.getItem().getUnit();

        if (linkedData.getItem().getContainerSize() == null) {

            return String.format("%.2f %s", amount, unit);

        }

        return String.format("%.2f of %.2f %s", amount, linkedData.getItem().getContainerSize(), unit);
    }

    public String getItemLabel() {
        if (linkedData == null || linkedData.getItem() == null) {
            return "";
        }
        return linkedData.getItem().getLabel();
    }

    public String getLocationOfItem() {
        if (linkedData == null || linkedData.getItem() == null) {
            return "";
        }
        if (linkedData.getItem().getContainer() != null) {
            return linkedData.getItem().getContainer().getNameToDisplay() +" -> "+linkedData.getItem().getContainer().getLocation(true, false);
        }
        return "";
    }
}
