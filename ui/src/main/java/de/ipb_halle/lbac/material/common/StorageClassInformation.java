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

import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.entity.storage.StorageConditionStorageEntity;
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
public class StorageClassInformation implements Serializable {

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    private String remarks;

    private List<StorageClass> possibleStorageClasses = new ArrayList<>();
    private StorageClass storageClass;
    private Set<StorageCondition> storageConditions = new HashSet<>();

    public StorageClassInformation() {
        init();
        storageClass = possibleStorageClasses.get(0);
    }

    public StorageClassInformation(List<StorageClass> possibleClasses) {
        possibleStorageClasses = possibleClasses;
    }

    public StorageClassInformation(Material m, List<StorageClass> possibleClasses) {
        possibleStorageClasses.clear();
        this.possibleStorageClasses = possibleClasses;
        storageConditions.addAll(m.getStorageInformation().getStorageConditions());

        storageClass = new StorageClass(
                m.getStorageInformation().getStorageClass().id,
                m.getStorageInformation().getStorageClass().getName());
    }

    public boolean isMoistureSensitive() {
        return storageConditions.contains(StorageCondition.moistureSensitive);

    }

    public void setMoistureSensitive(boolean moistureSensitive) {
        if (moistureSensitive) {
            storageConditions.add(StorageCondition.moistureSensitive);
        } else {
            storageConditions.remove(StorageCondition.moistureSensitive);
        }
    }

    public boolean isKeepMoist() {
        return storageConditions.contains(StorageCondition.keepMoist);

    }

    public void setKeepMoist(boolean keepMoist) {
        if (keepMoist) {
            storageConditions.add(StorageCondition.keepMoist);
        } else {
            storageConditions.remove(StorageCondition.keepMoist);
        }
    }

    public boolean isLightSensitive() {
        return storageConditions.contains(StorageCondition.lightSensitive);
    }

    public void setLightSensitive(boolean lightSensitive) {
        if (lightSensitive) {
            storageConditions.add(StorageCondition.lightSensitive);
        } else {
            storageConditions.remove(StorageCondition.lightSensitive);
        }
    }

    public boolean isStoreUnderProtectiveGas() {
        return storageConditions.contains(StorageCondition.storeUnderProtectiveGas);
    }

    public void setStoreUnderProtectiveGas(boolean storeUnderProtectiveGas) {
        if (storeUnderProtectiveGas) {
            storageConditions.add(StorageCondition.storeUnderProtectiveGas);
        } else {
            storageConditions.remove(StorageCondition.storeUnderProtectiveGas);
        }
    }

    public boolean isAcidSensitive() {
        return storageConditions.contains(StorageCondition.acidSensitive);

    }

    public void setAcidSensitive(boolean acidSensitive) {
        if (acidSensitive) {
            storageConditions.add(StorageCondition.acidSensitive);
        } else {
            storageConditions.remove(StorageCondition.acidSensitive);
        }
    }

    public boolean isAlkaliSensitive() {
        return storageConditions.contains(StorageCondition.alkaliSensitive);
    }

    public void setAlkaliSensitive(boolean alkaliSensitive) {
        if (alkaliSensitive) {
            storageConditions.add(StorageCondition.alkaliSensitive);
        } else {
            storageConditions.remove(StorageCondition.alkaliSensitive);
        }
    }

    public boolean isAwayFromOxidants() {
        return storageConditions.contains(StorageCondition.awayFromOxidants);

    }

    public void setAwayFromOxidants(boolean awayFromOxidants) {
        if (awayFromOxidants) {
            storageConditions.add(StorageCondition.awayFromOxidants);
        } else {
            storageConditions.remove(StorageCondition.awayFromOxidants);
        }

    }

    public boolean isFrostSensitive() {
        return storageConditions.contains(StorageCondition.frostSensitive);

    }

    public void setFrostSensitive(boolean frostSensitive) {
        if (frostSensitive) {
            storageConditions.add(StorageCondition.frostSensitive);
        } else {
            storageConditions.remove(StorageCondition.frostSensitive);
        }
    }

    public boolean isKeepCool() {
        return storageConditions.contains(StorageCondition.keepCool);

    }

    public void setKeepCool(boolean keepCool) {
        if (keepCool) {
            storageConditions.add(StorageCondition.keepCool);
        } else {
            storageConditions.remove(StorageCondition.keepCool);
        }
    }

    public boolean isKeepFrozen() {
        return storageConditions.contains(StorageCondition.keepFrozen);
    }

    public void setKeepFrozen(boolean keepFrozen) {
        if (keepFrozen) {
            storageConditions.add(StorageCondition.keepFrozen);
        } else {
            storageConditions.remove(StorageCondition.keepFrozen);
        }
    }

    public boolean isKeepTempBelowMinus40Celsius() {
        return storageConditions.contains(StorageCondition.keepTempBelowMinus40Celsius);
    }

    public void setKeepTempBelowMinus40Celsius(boolean keepTempBelowMinus40Celsius) {
        if (keepTempBelowMinus40Celsius) {
            storageConditions.add(StorageCondition.keepTempBelowMinus40Celsius);
        } else {
            storageConditions.remove(StorageCondition.keepTempBelowMinus40Celsius);
        }
    }

    public boolean isKeepTempBelowMinus80Celsius() {
        return storageConditions.contains(StorageCondition.keepTempBelowMinus80Celsius);

    }

    public void setKeepTempBelowMinus80Celsius(boolean keepTempBelowMinus80Celsius) {
        if (keepTempBelowMinus80Celsius) {
            storageConditions.add(StorageCondition.keepTempBelowMinus80Celsius);
        } else {
            storageConditions.remove(StorageCondition.keepTempBelowMinus80Celsius);
        }
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public List<StorageClass> getPossibleStorageClasses() {
        return possibleStorageClasses;
    }

    public StorageClass getPossibleStorageClassById(int id) {
        for (StorageClass sc : getPossibleStorageClasses()) {
            if (sc.getId() == id) {
                return sc;
            }
        }
        return null;
    }

    public void setPossibleStorageClasses(List<StorageClass> possibleStorageClasses) {
        this.possibleStorageClasses = possibleStorageClasses;
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
        entity.setStorageClass(storageClass.getId());

        return entity;
    }

    public List<StorageConditionStorageEntity> createDBInstances(int materialId) {
        List<StorageConditionStorageEntity> entities = new ArrayList<>();

        for (StorageCondition sc : storageConditions) {

            entities.add(new StorageConditionStorageEntity(new StorageConditionStorageId(sc.getId(), materialId)));

        }

        return entities;
    }

    public static StorageClassInformation createObjectByDbEntity(StorageEntity sE, List<StorageConditionStorageEntity> storageParameter) {
        StorageClassInformation sci = new StorageClassInformation();
        sci.setRemarks(sE.getDescription());
        sci.setStorageClass(sci.getPossibleStorageClassById(sE.getStorageClass()));
        for (StorageConditionStorageEntity scse : storageParameter) {
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

    public StorageClassInformation copy() {
        StorageClassInformation copy = new StorageClassInformation();
        copy.possibleStorageClasses = possibleStorageClasses;
        copy.storageClass = new StorageClass(storageClass.getId(), storageClass.getName());
        copy.storageConditions.addAll(storageConditions);
        copy.remarks = remarks;

        return copy;
    }

    private void init() {
        possibleStorageClasses = new ArrayList<>();
        possibleStorageClasses.add(new StorageClass(1, "1"));
        possibleStorageClasses.add(new StorageClass(2, "2A"));
        possibleStorageClasses.add(new StorageClass(3, "2B"));
        possibleStorageClasses.add(new StorageClass(4, "3"));
        possibleStorageClasses.add(new StorageClass(5, "4.1A"));
        possibleStorageClasses.add(new StorageClass(6, "4.1B"));
        possibleStorageClasses.add(new StorageClass(7, "4.2"));
        possibleStorageClasses.add(new StorageClass(8, "4.3"));
        possibleStorageClasses.add(new StorageClass(9, "5.1A"));
        possibleStorageClasses.add(new StorageClass(10, "5.1B"));
        possibleStorageClasses.add(new StorageClass(11, "5.1C"));
        possibleStorageClasses.add(new StorageClass(12, "5.2"));
        possibleStorageClasses.add(new StorageClass(13, "6.1A"));
        possibleStorageClasses.add(new StorageClass(14, "6.1B"));
        possibleStorageClasses.add(new StorageClass(15, "6.1C"));
        possibleStorageClasses.add(new StorageClass(16, "6.1D"));
        possibleStorageClasses.add(new StorageClass(17, "6.2"));
        possibleStorageClasses.add(new StorageClass(18, "7"));
        possibleStorageClasses.add(new StorageClass(19, "8A"));
        possibleStorageClasses.add(new StorageClass(20, "8B"));
        possibleStorageClasses.add(new StorageClass(21, "10"));
        possibleStorageClasses.add(new StorageClass(22, "11"));
        possibleStorageClasses.add(new StorageClass(23, "12"));
        possibleStorageClasses.add(new StorageClass(24, "13"));

    }

}
