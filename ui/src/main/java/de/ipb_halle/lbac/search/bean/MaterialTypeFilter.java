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
package de.ipb_halle.lbac.search.bean;

import de.ipb_halle.lbac.material.MaterialType;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author fmauz
 */
public class MaterialTypeFilter {

    private boolean structures;
    private boolean biomaterial;
    private boolean sequences;
    private boolean compositions;

    public boolean isStructures() {
        return structures;
    }

    public void setStructures(boolean structures) {
        this.structures = structures;
    }

    public boolean isBiomaterial() {
        return biomaterial;
    }

    public void setBiomaterial(boolean biomaterial) {
        this.biomaterial = biomaterial;
    }

    public boolean isSequences() {
        return sequences;
    }

    public void setSequences(boolean sequences) {
        this.sequences = sequences;
    }

    public boolean isCompositions() {
        return compositions;
    }

    public void setCompositions(boolean compositions) {
        this.compositions = compositions;
    }

    public Set<MaterialType> getTypes() {
        Set<MaterialType> types = new HashSet<>();
        if (structures) {
            types.add(MaterialType.STRUCTURE);
        }
        if (sequences) {
            types.add(MaterialType.SEQUENCE);
        }
        if (biomaterial) {
            types.add(MaterialType.BIOMATERIAL);
        }
        if (compositions) {
            types.add(MaterialType.COMPOSITION);
        }
        return types;
    }

}
