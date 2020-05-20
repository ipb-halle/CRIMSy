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
package de.ipb_halle.lbac.material.difference;

import de.ipb_halle.lbac.material.common.StorageCondition;
import de.ipb_halle.lbac.material.entity.storage.StorageClassHistoryEntity;
import de.ipb_halle.lbac.material.entity.storage.StorageClassHistoryId;
import de.ipb_halle.lbac.material.entity.storage.StorageConditionHistoryEntity;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class MaterialStorageDifference implements MaterialDifference {

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    private int materialID;
    private UUID actorID;
    private Date mDate;
    private String descriptionOld;
    private String descriptionNew;
    private Integer storageclassOld;
    private Integer storageclassNew;
    private List<StorageCondition> storageConditionsOld = new ArrayList<>();
    private List<StorageCondition> storageConditionsNew = new ArrayList<>();

    public MaterialStorageDifference() {

    }

    public MaterialStorageDifference(StorageClassHistoryEntity classEntity, List< StorageConditionHistoryEntity> entities) {
        if (classEntity != null) {
            materialID = classEntity.getId().getMaterialid();
            actorID = classEntity.getActorid();
            mDate = classEntity.getId().getMdate();
            if (!Objects.equals(classEntity.getDescription_new(), classEntity.getDescription_old())) {
                descriptionOld = classEntity.getDescription_old();
                descriptionNew = classEntity.getDescription_new();
            }
            if (!Objects.equals(classEntity.getStorageclass_new(), classEntity.getStorageclass_old())) {
                storageclassOld = classEntity.getStorageclass_old();
                storageclassNew = classEntity.getStorageclass_new();
            }
        }
        if (entities != null && entities.size() > 0) {
            if (mDate == null) {
                materialID = entities.get(0).getMaterialid();
                actorID = entities.get(0).getActorid();
                mDate = entities.get(0).getmDate();
            }
            for (StorageConditionHistoryEntity sce : entities) {
                if (sce.getConditionId_new() != null) {
                    addCondition(StorageCondition.getStorageConditionById(sce.getConditionId_new()));
                } else {
                    removeCondition(StorageCondition.getStorageConditionById(sce.getConditionId_old()));
                }
            }

        }
    }

    @Override
    public UUID getUserId() {
        return actorID;
    }

    @Override
    public void initialise(int materialId, UUID actorID, Date mDate) {
        this.materialID = materialId;
        this.actorID = actorID;
        this.mDate = mDate;
    }

    @Override
    public Date getModificationDate() {
        return mDate;
    }

    public void removeCondition(StorageCondition sc) {
        storageConditionsNew.add(null);
        storageConditionsOld.add(sc);
    }

    public void addCondition(StorageCondition sc) {
        storageConditionsNew.add(sc);
        storageConditionsOld.add(null);
    }

    public Integer getStorageclassOld() {
        return storageclassOld;
    }

    public Integer getStorageclassNew() {
        return storageclassNew;
    }

    public List<StorageCondition> getStorageConditionsOld() {
        return storageConditionsOld;
    }

    public List<StorageCondition> getStorageConditionsNew() {
        return storageConditionsNew;
    }

    public boolean diffFound() {
        return storageConditionsOld.size() > 0 || storageClassDiffFound();
    }

    public boolean storageClassDiffFound() {
        if (!Objects.equals(storageclassNew, storageclassOld)) {
            return true;
        }
        if (!Objects.equals(descriptionOld, descriptionNew)) {
            return true;
        }
        return false;
    }

    public void changeStorageClass(Integer oldValue, Integer newValue) {
        this.storageclassOld = oldValue;
        this.storageclassNew = newValue;
    }

    public void changeStorageDescription(String oldValue, String newValue) {
        this.descriptionNew = newValue;
        this.descriptionOld = oldValue;
    }

    public StorageClassHistoryEntity createStorageClassHistEntity() {
        StorageClassHistoryEntity entity = new StorageClassHistoryEntity();
        entity.setActorid(actorID);
        entity.setId(new StorageClassHistoryId(materialID, mDate));
        entity.setDescription_new(descriptionNew);
        entity.setDescription_old(descriptionOld);
        entity.setStorageclass_new(storageclassNew);
        entity.setStorageclass_old(storageclassOld);
        return entity;
    }

    public List<StorageConditionHistoryEntity> createStorageConditionHistEntities() {
        List<StorageConditionHistoryEntity> entities = new ArrayList<>();
        for (int i = 0; i < storageConditionsOld.size(); i++) {
            StorageConditionHistoryEntity entity = new StorageConditionHistoryEntity();
            entity.setActorid(actorID);
            entity.setmDate(mDate);
            entity.setMaterialid(materialID);
            entity.setConditionId_new(storageConditionsOld.get(i).getId());
            entity.setConditionId_new(storageConditionsNew.get(i).getId());
            entities.add(entity);
        }
        return entities;
    }

    public int getMaterialID() {
        return materialID;
    }

    public String getDescriptionOld() {
        return descriptionOld;
    }

    public void setDescriptionOld(String descriptionOld) {
        this.descriptionOld = descriptionOld;
    }

    public String getDescriptionNew() {
        return descriptionNew;
    }

    public void setDescriptionNew(String descriptionNew) {
        this.descriptionNew = descriptionNew;
    }

}
