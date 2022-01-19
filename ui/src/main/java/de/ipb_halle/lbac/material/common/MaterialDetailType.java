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
package de.ipb_halle.lbac.material.common;

import java.io.Serializable;

/**
 *
 * @author fmauz
 */
public enum MaterialDetailType implements Serializable {

    COMMON_INFORMATION(1),
    STRUCTURE_INFORMATION(2),
    INDEX(3),
    HAZARD_INFORMATION(4),
    STORAGE_CLASSES(5),
    TAXONOMY(6),
    COMPOSITION(7),
    SEQUENCE_INFORMATION(8);

    private int id;

    MaterialDetailType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    /**
     * Returns the MaterialDetailType with the given id. Returns null if id is
     * unknown.
     *
     * @param id
     * @return MaterialDetailType or null
     */
    public static MaterialDetailType getTypeById(int id) {
        for (MaterialDetailType mdt : values()) {
            if (mdt.getId() == id) {
                return mdt;
            }
        }
        return null;
    }

}
