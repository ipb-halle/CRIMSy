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
package de.ipb_halle.lbac.material;

import de.ipb_halle.lbac.material.biomaterial.BioMaterial;
import de.ipb_halle.lbac.material.biomaterial.BioMaterialFactory;
import de.ipb_halle.lbac.material.biomaterial.Taxonomy;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyFactory;
import de.ipb_halle.lbac.material.biomaterial.Tissue;
import de.ipb_halle.lbac.material.biomaterial.TissueFactory;
import de.ipb_halle.lbac.material.common.MaterialDetailType;
import de.ipb_halle.lbac.material.common.service.MaterialFactory;
import de.ipb_halle.lbac.material.composition.CompositionFactory;
import de.ipb_halle.lbac.material.composition.MaterialComposition;
import de.ipb_halle.lbac.material.consumable.Consumable;
import de.ipb_halle.lbac.material.consumable.ConsumableFactory;
import de.ipb_halle.lbac.material.inaccessible.InaccessibleMaterial;
import de.ipb_halle.lbac.material.sequence.Sequence;
import de.ipb_halle.lbac.material.sequence.SequenceFactory;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.material.structure.StructureFactory;
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
            Structure.class,
            new StructureFactory(),
            MaterialDetailType.COMMON_INFORMATION,
            MaterialDetailType.INDEX,
            MaterialDetailType.STORAGE_CLASSES,
            MaterialDetailType.STRUCTURE_INFORMATION,
            MaterialDetailType.HAZARD_INFORMATION),
    COMPOSITION(
            2,
            MaterialComposition.class,
            new CompositionFactory(),
            MaterialDetailType.COMMON_INFORMATION,
            MaterialDetailType.INDEX,
            MaterialDetailType.HAZARD_INFORMATION,
            MaterialDetailType.STORAGE_CLASSES,
            MaterialDetailType.COMPOSITION),
    BIOMATERIAL(
            3,
            BioMaterial.class,
            new BioMaterialFactory(),
            MaterialDetailType.COMMON_INFORMATION,
            MaterialDetailType.HAZARD_INFORMATION,
            MaterialDetailType.TAXONOMY),
    CONSUMABLE(
            4,
            Consumable.class,
            new ConsumableFactory(),
            MaterialDetailType.HAZARD_INFORMATION,
            MaterialDetailType.COMMON_INFORMATION),
    SEQUENCE(
            5,
            Sequence.class,
            new SequenceFactory(),
            MaterialDetailType.COMMON_INFORMATION,
            MaterialDetailType.INDEX,
            MaterialDetailType.SEQUENCE_INFORMATION),
    TISSUE(
            6, Tissue.class, new TissueFactory(), MaterialDetailType.COMMON_INFORMATION),
    TAXONOMY(
            7, Taxonomy.class, new TaxonomyFactory(), MaterialDetailType.COMMON_INFORMATION),
    INACCESSIBLE(8, InaccessibleMaterial.class, null);

    private final List<MaterialDetailType> types;
    private static final Map<String, MaterialType> string2Enum = Stream.of(values()).collect(toMap(Object::toString, e -> e));
    private final int id;
    private final Class clazz;
    private final MaterialFactory factory;

    /**
     *
     * @param id
     * @param t
     * @param clazz
     * @param factory
     */
    MaterialType(int id, Class clazz, MaterialFactory factory, MaterialDetailType... t) {
        this.types = Arrays.asList(t);
        this.clazz = clazz;
        this.id = id;
        this.factory = factory;
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
        return clazz;
    }

    public MaterialFactory getFactory() {
        return factory;
    }

}
