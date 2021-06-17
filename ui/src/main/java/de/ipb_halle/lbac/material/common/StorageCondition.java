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
public enum StorageCondition implements Serializable {
    moistureSensitive(1),
    keepMoist(2),
    lightSensitive(3),
    storeUnderProtectiveGas(4),
    acidSensitive(5),
    alkaliSensitive(6),
    awayFromOxidants(7),
    frostSensitive(8),
    keepCool(9),
    keepFrozen(10),
    keepTempBelowMinus40Celsius(11),
    keepTempBelowMinus80Celsius(12);

    private int id;

    private StorageCondition(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static StorageCondition getStorageConditionById(int id) {
        for (StorageCondition h : StorageCondition.values()) {
            if (h.getId() == id) {
                return h;
            }
        }
        return null;
    }

}
