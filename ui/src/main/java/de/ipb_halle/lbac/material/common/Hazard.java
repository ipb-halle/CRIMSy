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
public enum Hazard  implements Serializable {

    explosive(1),
    highlyFlammable(2),
    oxidizing(3),
    compressedGas(4),
    corrosive(5),
    poisonous(6),
    irritant(7),
    unhealthy(8),
    environmentallyHazardous(9),
    danger(10),
    attention(11);

    private int typeId;

    private Hazard(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public static Hazard getHazardById(int id) {
        for (Hazard h : Hazard.values()) {
            if (h.getTypeId() == id) {
                return h;
            }
        }
        return null;
    }

}
