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
package de.ipb_halle.lbac.material;

import de.ipb_halle.lbac.material.common.MaterialDetailRight;
import de.ipb_halle.lbac.material.common.history.MaterialHistory;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACObject;
import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.util.InputConverter;
import de.ipb_halle.lbac.material.common.Hazard;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.common.MaterialDetailType;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageClassInformation;
import de.ipb_halle.lbac.material.common.StorageCondition;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public abstract class Material extends ACObject implements DTO, Serializable {

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    protected int id;
    protected MaterialType type;
    protected List<MaterialName> names;
    protected Integer projectId;
    protected Date creationTime;
    protected HazardInformation hazards;
    protected StorageClassInformation storageInformation;
    protected List<IndexEntry> indices = new ArrayList<>();
    protected List<MaterialDetailRight> detailRights = new ArrayList<>();
    protected MaterialHistory history = new MaterialHistory();
    protected InputConverter vFilter;

    public Material(
            int id,
            List<MaterialName> names,
            Integer projectId,
            HazardInformation hazards,
            StorageClassInformation storageInformation) {
        this.id = id;
        this.names = names;
        this.projectId = projectId;
        this.hazards = hazards;
        this.storageInformation = storageInformation;
        vFilter = new InputConverter();
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setType(MaterialType type) {
        this.type = type;
    }

    public List<MaterialName> getNames() {
        return names;
    }

    public List<MaterialName> getCopiedNames() {
        List<MaterialName> copiedNames = new ArrayList<>();
        int i = 0;
        for (MaterialName mn : names) {
            copiedNames.add(new MaterialName(mn.getValue(), mn.getLanguage(), i));
            i++;
        }
        return copiedNames;
    }

    public void setNames(List<MaterialName> names) {
        this.names = names;
    }

    public String getNumber() {
        return "";
    }

    public int getId() {
        return id;
    }

    public MaterialType getType() {
        return type;
    }

    public String getSecurityInformation() {
        return "danger";
    }

    public Integer getProjectId() {
        return projectId;
    }

    public HazardInformation getHazards() {
        return hazards;
    }

    public void setHazards(HazardInformation hazards) {
        this.hazards = hazards;
    }

    public StorageClassInformation getStorageInformation() {
        return storageInformation;
    }

    public void setStorageInformation(StorageClassInformation storageInformation) {
        this.storageInformation = storageInformation;
    }

    public void print() {
        logger.info("Information about Material " + id);
        logger.info("  Type: " + type);
        logger.info("----- Names ------");
        for (MaterialName mn : names) {
            logger.info("  " + mn.getLanguage() + " -> " + mn.getValue());
        }
        logger.info("----- Indices ------");
        for (IndexEntry mn : indices) {
            logger.info("  " + mn.getTypeId() + " -> " + mn.getValue());
        }
        logger.info("----- Hazards ------");
        for (Hazard h : hazards.getHazards()) {
            logger.info("  " + h.getTypeId() + ": " + h.name());
        }
        logger.info("  H-Statements " + hazards.getHazardStatements());
        logger.info("  P-Statements " + hazards.getPrecautionaryStatements());
        logger.info("----- StorageInformation ------");
        logger.info("  StorageClass: " + storageInformation.getStorageClass().getName());
        logger.info("  Remarks: " + storageInformation.getRemarks());
        for (StorageCondition sc : storageInformation.getStorageConditions()) {
            logger.info("  " + sc.getId() + ": " + sc.name() + " - > ");
        }
    }

    public String getFirstName() {
        return names.get(0).getValue();
    }

    public List<IndexEntry> getIndices() {
        return indices;
    }

    public void setIndices(List<IndexEntry> indices) {
        this.indices = indices;
    }

    public List<IndexEntry> getCopiedIndices() {
        List<IndexEntry> copiedIndices = new ArrayList<>();
        for (IndexEntry ie : indices) {
            copiedIndices.add(new IndexEntry(ie.getTypeId(), ie.getValue(), ie.getLanguage()));
        }
        return copiedIndices;
    }

    public List<MaterialDetailRight> getDetailRights() {
        return detailRights;
    }

    public List<MaterialDetailRight> getCopiedDetailRights() {
        List<MaterialDetailRight> copies = new ArrayList<>();
        for (MaterialDetailRight mdr : detailRights) {
            copies.add(mdr.copy());
        }
        return copies;
    }

    public void setDetailRights(List<MaterialDetailRight> detailRights) {
        this.detailRights = detailRights;
    }

    public abstract Material copyMaterial();

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public MaterialHistory getHistory() {
        return history;
    }

    public void setHistory(MaterialHistory history) {
        this.history = history;
    }

    public ACList getDetailRight(MaterialDetailType type) {
        for (MaterialDetailRight mdr : detailRights) {
            if (mdr.getType() == type) {
                return mdr.getAcList();
            }
        }
        return null;
    }

}
