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
package de.ipb_halle.lbac.material.common;

import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.entity.storage.StorageConditionMaterialEntity;
import de.ipb_halle.lbac.material.common.entity.storage.StorageConditionStorageId;
import de.ipb_halle.lbac.material.common.entity.storage.StorageEntity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class StorageInformation implements Serializable {

    private static final long serialVersionUID = 1L;

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    private String remarks;
    private StorageClass storageClass;
    private Set<StorageCondition> storageConditions = new HashSet<>();

    public StorageInformation() {
        storageClass = null;
    }

    public StorageInformation(Material m) {
        storageConditions.addAll(m.getStorageInformation().getStorageConditions());
        if (m.getStorageInformation().getStorageClass() != null) {
            storageClass = new StorageClass(
                    m.getStorageInformation().getStorageClass().id,
                    m.getStorageInformation().getStorageClass().getName());
        }
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public StorageClass getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(StorageClass storageClass) {
        this.storageClass = storageClass;
    }

    public StorageEntity createStorageDBInstance(int materialId) {
        StorageEntity entity = new StorageEntity();
        entity.setDescription(remarks);
        entity.setMaterialId(materialId);
        if (storageClass != null) {
            entity.setStorageClass(storageClass.getId());
        }

        return entity;
    }

    public List<StorageConditionMaterialEntity> createDBInstances(int materialId) {
        List<StorageConditionMaterialEntity> entities = new ArrayList<>();

        for (StorageCondition sc : storageConditions) {

            entities.add(new StorageConditionMaterialEntity(new StorageConditionStorageId(sc.getId(), materialId)));

        }

        return entities;
    }

    public static StorageInformation createObjectByDbEntity(
            List<StorageConditionMaterialEntity> storageParameter) {
        StorageInformation sci = new StorageInformation();
        for (StorageConditionMaterialEntity scse : storageParameter) {
            sci.getStorageConditions().add(StorageCondition.getStorageConditionById(scse.getId().getConditionid()));
        }
        return sci;
    }

    public static StorageInformation createObjectByDbEntity(
            String remarks,
            StorageClass storageClass,
            List<StorageConditionMaterialEntity> storageParameter) {
        StorageInformation sci = new StorageInformation();
        sci.setRemarks(remarks);
        sci.setStorageClass(storageClass);
        for (StorageConditionMaterialEntity scse : storageParameter) {
            sci.getStorageConditions().add(StorageCondition.getStorageConditionById(scse.getId().getConditionid()));
        }
        return sci;
    }

    public Set<StorageCondition> getStorageConditions() {
        return storageConditions;
    }

    public void setStorageConditions(Set<StorageCondition> storageConditions) {
        this.storageConditions = storageConditions;
    }

    public StorageInformation copy() {
        StorageInformation copy = new StorageInformation();
        if (storageClass != null) {
            copy.storageClass = new StorageClass(storageClass.getId(), storageClass.getName());
        } else {
            copy.storageClass = null;
        }
        copy.storageConditions.addAll(storageConditions);
        copy.remarks = remarks;

        return copy;
    }
}
