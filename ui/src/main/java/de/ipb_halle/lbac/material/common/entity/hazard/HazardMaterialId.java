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
package de.ipb_halle.lbac.material.common.entity.hazard;

import java.io.Serializable;
import jakarta.persistence.Embeddable;

/**
 *
 * @author fmauz
 */
@Embeddable
public class HazardMaterialId implements Serializable {

    private final static long serialVersionUID = 1L;

    private int typeID;
    private int materialID;

    public HazardMaterialId() {
    }

    public HazardMaterialId(int typeID, int materialID) {
        this.typeID = typeID;
        this.materialID = materialID;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null) {
            if (this == o) {
                return true;
            }

            if (o instanceof HazardMaterialId) {
                HazardMaterialId otherId = (HazardMaterialId) o;
                return (otherId.materialID == this.materialID)
                    && (otherId.typeID == this.typeID);
            }
        }
        return false;
    }

    public int getTypeID() {
        return typeID;
    }

    @Override
    public int hashCode() {
        return this.typeID + this.materialID;
    }

    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }

    public int getMaterialID() {
        return materialID;
    }

    public void setMaterialID(int materialID) {
        this.materialID = materialID;
    }

}
