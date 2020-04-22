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
package de.ipb_halle.lbac.material.common;

import de.ipb_halle.lbac.entity.ACEntry;
import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.material.common.MaterialDetailType;
import de.ipb_halle.lbac.material.entity.MaterialDetailRightEntity;

/**
 * Java represantation of a materialdetailtype.
 *
 * @author fmauz
 */
public class MaterialDetailRight {

    protected ACList acList;
    protected MaterialDetailType type;
    protected int id;
    protected int materialId;

    public MaterialDetailRight() {

    }

    public MaterialDetailRight(MaterialDetailRightEntity dbEntity, ACList acList) {
        this.acList = acList;
        this.id = dbEntity.getId();
        this.type = MaterialDetailType.getTypeById(dbEntity.getMaterialtypeid());
        this.materialId = dbEntity.getMaterialid();

    }

    public MaterialDetailRight copy() {
        MaterialDetailRight copy = new MaterialDetailRight();
        ACList aclCopy = new ACList();
        for (ACEntry ace : acList.getACEntries().values()) {
            aclCopy.addACE(ace.getMember(), ace.getAcPermissionArray());
        }
        copy.acList = aclCopy;
        copy.type = type;
        copy.materialId = materialId;
        return copy;
    }

    public ACList getAcList() {
        return acList;
    }

    public void setAcList(ACList acList) {
        this.acList = acList;
    }

    public MaterialDetailType getType() {
        return type;
    }

    public void setType(MaterialDetailType type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMaterialId() {
        return materialId;
    }

    public void setMaterialId(int materialId) {
        this.materialId = materialId;
    }

}
