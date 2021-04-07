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
package de.ipb_halle.lbac.material;

import de.ipb_halle.lbac.material.biomaterial.BioMaterial;
import de.ipb_halle.lbac.material.biomaterial.Taxonomy;
import de.ipb_halle.lbac.material.biomaterial.Tissue;
import de.ipb_halle.lbac.material.common.MaterialDetailType;
import de.ipb_halle.lbac.material.structure.Structure;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.toMap;
import java.util.stream.Stream;

/**
 * Represents a materialtpye from the database. Every type has some detail
 * informations definitions which are present in the specific type
 *
 * @author fmauz
 */
public enum MaterialType implements Serializable {
    STRUCTURE(
            1,
            MaterialDetailType.COMMON_INFORMATION,
            MaterialDetailType.INDEX,
            MaterialDetailType.STORAGE_CLASSES,
            MaterialDetailType.STRUCTURE_INFORMATION,
            MaterialDetailType.HAZARD_INFORMATION),
    COMPOSITION(
            2,
            MaterialDetailType.COMMON_INFORMATION),
    BIOMATERIAL(
            3,
            MaterialDetailType.COMMON_INFORMATION,
            MaterialDetailType.TAXONOMY),
    CONSUMABLE(
            4,
            MaterialDetailType.COMMON_INFORMATION),
    SEQUENCE(
            5,
            MaterialDetailType.COMMON_INFORMATION),
    TISSUE(
            6, MaterialDetailType.COMMON_INFORMATION),
    TAXONOMY(
            7, MaterialDetailType.COMMON_INFORMATION),
    INACCESSIBLE(8);

    private final List<MaterialDetailType> types;
    private static final Map<String, MaterialType>  string2Enum = Stream.of(values()).collect(toMap(Object::toString, e -> e));
    private final int id;

    /**
     *
     * @param id
     * @param t
     */
    MaterialType(int id, MaterialDetailType... t) {
        types = Arrays.asList(t);
        this.id = id;
    }

    public static MaterialType fromString(String type) {
        return string2Enum.get(type);
    }
    
    /**
     * Returns all detail informations of the specific type
     *
     * @return
     */
    public List<MaterialDetailType> getPossibleDetailTypes() {
        return types;
    }

    /**
     *
     * @return
     */
    public int getId() {
        return id;
    }

    /**
     * Gets the materialtype with the given id
     *
     * @param id id of type to look for
     * @return the found materialtype. Null if no type was found.
     */
    public static MaterialType getTypeById(int id) {
        for (MaterialType t : MaterialType.values()) {
            if (t.getId() == id) {
                return t;
            }
        }
        return null;
    }

    public Class getClassOfDto() {
        if (this == STRUCTURE) {
            return Structure.class;
        }
        if (this == BIOMATERIAL) {
            return BioMaterial.class;
        }
        if (this == TISSUE) {
            return Tissue.class;
        }
        if (this == TAXONOMY) {
            return Taxonomy.class;
        }
        throw new RuntimeException("Could not resolve class of DTO for" + this);
    }
}
