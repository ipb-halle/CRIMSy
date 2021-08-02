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
package de.ipb_halle.lbac.project;

import de.ipb_halle.lbac.material.MaterialType;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author fmauz
 */
public enum ProjectType implements Serializable {
    DUMMY_PROJECT(0),
    CHEMICAL_PROJECT(1,
            MaterialType.STRUCTURE,
            MaterialType.CONSUMABLE,
            MaterialType.COMPOSITION
    //            MaterialType.SEQUENCE,

    ),
    IT_PROJECT(2,
            MaterialType.CONSUMABLE
    ),
    FINANCE_PROJECT(3,
            MaterialType.CONSUMABLE
    ),
    BIOLOGICAL_PROJECT(4,
            MaterialType.BIOMATERIAL,
            MaterialType.CONSUMABLE
    //            MaterialType.COMPOSITION
    //            MaterialType.SEQUENCE,
    ),
    BIOCHEMICAL_PROJECT(5,
            MaterialType.STRUCTURE,
            MaterialType.BIOMATERIAL,
            MaterialType.CONSUMABLE,
            MaterialType.COMPOSITION
    //            MaterialType.SEQUENCE,
    //            MaterialType.COMPOSITION
    );

    private final int id;
    private final List<MaterialType> materialTypes = new ArrayList<>();

    ProjectType(int id, MaterialType... materialType) {
        this.id = id;
        materialTypes.addAll(Arrays.asList(materialType));
    }

    public List<MaterialType> getMaterialTypes() {
        return materialTypes;
    }

    public int getId() {
        return id;
    }

    public static ProjectType getProjectTypeById(int id) {
        for (ProjectType pt : ProjectType.values()) {
            if (pt.getId() == id) {
                return pt;
            }
        }
        return null;
    }

}
