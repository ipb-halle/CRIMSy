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
package de.ipb_halle.lbac.material.common.bean;

import de.ipb_halle.lbac.material.Material;

import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.material.common.StorageClass;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.common.StorageCondition;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class StorageInformationBuilder {

    private boolean storageClassActivated;
    private MessagePresenter messagePresenter;
    private MaterialService materialService;
    private List<StorageClass> possibleStorageClasses;
    private StorageClass choosenStorageClass;
    private String remarks = "";
    private StorageCondition[] selectedConditions;
    private List<StorageCondition> possibleConditions = new ArrayList<>();
    private boolean inHistoryMode;
    protected Logger logger = LogManager.getLogger(this.getClass().getName());
    protected boolean accessRightToEdit = true;

    public StorageInformationBuilder(
            MessagePresenter messagePresenter,
            MaterialService materialService,
            Material material) {
        this.messagePresenter = messagePresenter;
        this.materialService = materialService;
        this.remarks = material.getStorageInformation().getRemarks();
        this.possibleStorageClasses = initStorageClassNames();
        this.selectedConditions = material.getStorageInformation().getStorageConditions().stream().toArray(StorageCondition[]::new);

        this.storageClassActivated = material.getStorageInformation().getStorageClass() != null;
        if (material.getStorageInformation().getStorageClass() != null) {
            this.choosenStorageClass = getStorageClassById(material.getStorageInformation().getStorageClass().id);
        } else {
            this.choosenStorageClass = possibleStorageClasses.get(0);
        }
        possibleConditions.addAll(Arrays.asList(StorageCondition.values()));
    }

    public StorageInformationBuilder(
            MessagePresenter messagePresenter,
            MaterialService materialService) {
        this.messagePresenter = messagePresenter;
        this.materialService = materialService;
        this.possibleStorageClasses = initStorageClassNames();
        this.choosenStorageClass = possibleStorageClasses.get(0);
        this.possibleConditions.addAll(Arrays.asList(StorageCondition.values()));
        this.selectedConditions = new StorageCondition[0];
    }

    public StorageClass getStorageClassById(Integer id) {
        for (StorageClass sc : possibleStorageClasses) {
            if (Objects.equals(sc.id, id)) {
                return sc;
            }
        }
        throw new IllegalArgumentException("No storage class found with id " + id);
    }

    private List<StorageClass> initStorageClassNames() {
        List<StorageClass> classes = materialService.loadStorageClasses();
        for (StorageClass sc : classes) {
            try {
                sc.setName(
                        messagePresenter.presentMessage(
                                "materialCreation_storageclass_" + sc.getName()));
            } catch (Exception e) {
                logger.info("Error in getting names");
            }
        }
        return classes;
    }

    public String getLocalizedConditionName(StorageCondition con) {
        return messagePresenter.presentMessage("materialCreation_panelStorage_" + con.toString());
    }

    public StorageInformation build() {
        StorageInformation storageInfos = new StorageInformation();
        if (storageClassActivated) {
            storageInfos.setStorageClass(choosenStorageClass);
            storageInfos.setRemarks(remarks);
        }
        storageInfos.getStorageConditions().addAll(Arrays.asList(selectedConditions));

        return storageInfos;
    }

    public String getStorageClassMenuStyleClass() {
        if (!isStorageClassDisabled()) {
            logger.info("activated");
            return "storageClassChoosable";
        } else {
            logger.info("deactivated");
            return "invisibleText storageClassChoosable";
        }
    }

    public boolean isStorageClassActivated() {

        return storageClassActivated;
    }

    public boolean isStorageClassDisabled() {
        return !storageClassActivated || inHistoryMode || !accessRightToEdit;
    }

    public void setStorageClassActivated(boolean storageClassActivated) {
        this.storageClassActivated = storageClassActivated;
    }

    public List<StorageClass> getPossibleStorageClasses() {
        return possibleStorageClasses;
    }

    public List<StorageCondition> getPossibleConditions() {
        return possibleConditions;
    }

    public void setPossibleConditions(List<StorageCondition> possibleConditions) {
        this.possibleConditions = possibleConditions;
    }

    public StorageClass getChoosenStorageClass() {
        return choosenStorageClass;
    }

    public void setChoosenStorageClass(StorageClass choosenStorageClass) {
        this.choosenStorageClass = choosenStorageClass;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public StorageCondition[] getSelectedConditions() {
        return selectedConditions;
    }

    public void setSelectedConditions(StorageCondition[] selectedConditions) {
        this.selectedConditions = selectedConditions;
    }

    public boolean isInHistoryMode() {
        return inHistoryMode;
    }

    public void setInHistoryMode(boolean inHistoryMode) {
        this.inHistoryMode = inHistoryMode;
    }

    public void addStorageCondition(StorageCondition c) {
        StorageCondition[] conds = getSelectedConditions();
        StorageCondition[] condsNew = new StorageCondition[conds.length + 1];
        for (int i = 0; i < conds.length; i++) {
            condsNew[i] = conds[i];
        }
        condsNew[condsNew.length - 1] = c;
        setSelectedConditions(condsNew);
    }

    public void removeStorageCondition(StorageCondition c) {
        StorageCondition[] conds = getSelectedConditions();
        StorageCondition[] condsNew = new StorageCondition[conds.length - 1];
        int j = 0;
        for (int i = 0; i < conds.length; i++) {
            if (conds[i] != c) {
                condsNew[j] = conds[i];
                j++;
            }
        }
        setSelectedConditions(condsNew);
    }

    public boolean isConditionEditable() {
        return !inHistoryMode && accessRightToEdit;
    }

    public void setAccessRightToEdit(boolean accessRightToEdit) {
        this.accessRightToEdit = accessRightToEdit;
    }

    public boolean isAccessRightToEdit() {
        return accessRightToEdit;
    }

}
