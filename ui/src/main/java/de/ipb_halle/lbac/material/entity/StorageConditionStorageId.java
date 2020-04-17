/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.entity;

import java.io.Serializable;
import javax.persistence.Embeddable;

/**
 *
 * @author fmauz
 */
@Embeddable
public class StorageConditionStorageId implements Serializable {

    private int conditionid;
    private int materialid;

    public StorageConditionStorageId() {
    }

    public StorageConditionStorageId(int typeID, int materialID) {
        this.conditionid = typeID;
        this.materialid = materialID;
    }

    public int getConditionid() {
        return conditionid;
    }

    public void setConditionid(int conditionid) {
        this.conditionid = conditionid;
    }

    public int getMaterialid() {
        return materialid;
    }

    public void setMaterialid(int materialid) {
        this.materialid = materialid;
    }

}
