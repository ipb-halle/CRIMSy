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
package de.ipb_halle.lbac.material.bean.save;

import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.service.MaterialService;
import de.ipb_halle.lbac.material.subtype.structure.Molecule;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageCondition;
import de.ipb_halle.lbac.material.difference.MaterialComparator;
import de.ipb_halle.lbac.material.difference.MaterialDifference;
import de.ipb_halle.lbac.material.difference.MaterialHazardDifference;
import de.ipb_halle.lbac.material.difference.MaterialIndexDifference;
import de.ipb_halle.lbac.material.difference.MaterialOverviewDifference;
import de.ipb_halle.lbac.material.difference.MaterialStorageDifference;
import de.ipb_halle.lbac.material.difference.MaterialStructureDifference;
import de.ipb_halle.lbac.material.entity.hazard.HazardsMaterialHistEntity;
import de.ipb_halle.lbac.material.entity.hazard.HazardsMaterialsEntity;
import de.ipb_halle.lbac.material.entity.MaterialEntity;
import de.ipb_halle.lbac.material.entity.MaterialHistoryEntity;
import de.ipb_halle.lbac.material.entity.MaterialHistoryId;
import de.ipb_halle.lbac.material.entity.index.MaterialIndexHistoryEntity;
import de.ipb_halle.lbac.material.entity.storage.StorageClassHistoryEntity;
import de.ipb_halle.lbac.material.entity.storage.StorageClassHistoryId;
import de.ipb_halle.lbac.material.entity.storage.StorageConditionHistoryEntity;
import de.ipb_halle.lbac.material.entity.storage.StorageConditionStorageEntity;
import de.ipb_halle.lbac.material.entity.storage.StorageConditionStorageId;
import de.ipb_halle.lbac.material.entity.storage.StorageEntity;
import de.ipb_halle.lbac.material.entity.structure.StructureEntity;
import de.ipb_halle.lbac.material.subtype.structure.Structure;
import java.util.List;
import java.util.UUID;
import javax.persistence.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author fmauz
 */
public class MaterialEditSaver {
    
    protected String SQL_INSERT_MOLECULE = "INSERT INTO molecules (molecule,format) VALUES(CAST ((:molecule) AS molecule),:format) RETURNING id";
    protected String SQL_DELETE_STORAGE_CONDITIONS = "DELETE FROM storageconditions_storages WHERE materialid=:mid";
    
    protected MaterialService materialService;
    
    protected MaterialComparator comparator;
    protected List<MaterialDifference> diffs;
    protected Material newMaterial;
    protected Material oldMaterial;
    protected UUID projectAcl;
    protected UUID actorId;
    private Logger logger = LogManager.getLogger(this.getClass().getName());
    
    public void init(
            MaterialComparator comparator,
            List<MaterialDifference> diffs,
            Material newMaterial,
            Material oldMaterial,
            UUID projectAcl,
            UUID actorId) {
        this.comparator = comparator;
        this.diffs = diffs;
        this.newMaterial = newMaterial;
        this.oldMaterial = oldMaterial;
        this.projectAcl = projectAcl;
        this.actorId = actorId;
    }
    
    public MaterialEditSaver(MaterialService materialService) {
        this.materialService = materialService;
    }

    /**
     * Saves the differences of the storage class and conditions to the history
     * tables in the db.
     */
    public void saveEditedMaterialStorage() {
        MaterialStorageDifference diff = comparator.getDifferenceOfType(diffs, MaterialStorageDifference.class);
        if (diff != null) {
            updateStorageClass(diff);
            updateStorageConditions(diff);
            saveStorageHistoryEntries(diff);
        }
    }
    
    protected void updateStorageConditions(MaterialStorageDifference diff) {
        if (!diff.getStorageConditionsNew().isEmpty()) {
            this.materialService.getEm()
                    .createNativeQuery(SQL_DELETE_STORAGE_CONDITIONS)
                    .setParameter("mid", diff.getMaterialID())
                    .executeUpdate();
            
            for (StorageCondition sc : newMaterial.getStorageInformation().getStorageConditions()) {
                StorageConditionStorageEntity dbEntity = new StorageConditionStorageEntity();
                dbEntity.setId(new StorageConditionStorageId(sc.getId(), newMaterial.getId()));
                this.materialService.getEm().persist(dbEntity);
            }
        }
        
    }
    
    protected void updateStorageClass(MaterialStorageDifference diff) {
        StorageEntity entity = this.materialService.getEm().find(StorageEntity.class, diff.getMaterialID());
        if (entity != null) {
            entity.setDescription(newMaterial.getStorageInformation().getRemarks());
            entity.setStorageClass(newMaterial.getStorageInformation().getStorageClass().getId());
            materialService.getEm().merge(entity);
        }
    }
    
    protected void saveStorageHistoryEntries(MaterialStorageDifference diff) {
        if (diff.storageClassDiffFound()) {
            StorageClassHistoryEntity storageClassEntity = new StorageClassHistoryEntity();
            storageClassEntity.setActorid(actorId);
            storageClassEntity.setId(new StorageClassHistoryId(diff.getMaterialID(), diff.getModificationDate()));
            storageClassEntity.setStorageclass_new(diff.getStorageclassNew());
            storageClassEntity.setStorageclass_old(diff.getStorageclassOld());
            storageClassEntity.setDescription_new(diff.getDescriptionNew());
            storageClassEntity.setDescription_old(diff.getDescriptionOld());
            this.materialService.getEm().persist(storageClassEntity);
        }
        
        for (int i = 0; i < diff.getStorageConditionsNew().size(); i++) {
            StorageConditionHistoryEntity storageConditionEntity = new StorageConditionHistoryEntity();
            storageConditionEntity.setActorid(actorId);
            StorageCondition newCondition = diff.getStorageConditionsNew().get(i);
            StorageCondition oldCondition = diff.getStorageConditionsOld().get(i);
            storageConditionEntity.setConditionId_new(newCondition == null ? null : newCondition.getId());
            storageConditionEntity.setConditionId_old(oldCondition == null ? null : oldCondition.getId());
            storageConditionEntity.setMaterialid(diff.getMaterialID());
            storageConditionEntity.setmDate(diff.getModificationDate());
            this.materialService.getEm().persist(storageConditionEntity);
        }
    }
    
    public void saveEditedMaterialIndices() {
        MaterialIndexDifference diff = comparator.getDifferenceOfType(diffs, MaterialIndexDifference.class);
        if (diff != null) {
            saveMaterialIndexDifferences(diff);
            deleteOldMaterialIndices(newMaterial);
            saveMaterialNames(newMaterial);
            saveNewMaterialIndices(newMaterial);
        }
    }
    
    public void saveEditedMaterialHazards() {
        MaterialHazardDifference hazardDiff = comparator.getDifferenceOfType(diffs, MaterialHazardDifference.class);
        if (hazardDiff != null) {
            List<HazardsMaterialHistEntity> dbEntities = hazardDiff.createDbInstances();
            for (HazardsMaterialHistEntity dbEntity : dbEntities) {
                this.materialService.getEm().persist(dbEntity);
            }
        }
        deleteOldHazards(newMaterial);
        List<HazardsMaterialsEntity> dbEntities = newMaterial.getHazards().createDBInstances(newMaterial.getId());
        for (HazardsMaterialsEntity dbEntity : dbEntities) {
            this.materialService.getEm().persist(dbEntity);
        }
    }
    
    public void saveEditedMaterialStructure() {
        Structure structure = (Structure) newMaterial;
        MaterialStructureDifference strucDiff = comparator.getDifferenceOfType(diffs, MaterialStructureDifference.class);
        if (strucDiff != null) {
            
            Molecule newMol = strucDiff.getMoleculeId_new();
            Molecule oldMol = strucDiff.getMoleculeId_old();
           
            if (!(oldMol == null && newMol == null)) {
                if (newMol != null) {
                    Query q = materialService.getEm().createNativeQuery(SQL_INSERT_MOLECULE)
                            .setParameter("molecule", newMol.getStructureModel())
                            .setParameter("format", newMol.getModelType().toString());
                    
                    int molId = (int) q.getSingleResult();
                    newMol.setId(molId);
                }
                
            }
            
            saveMaterialStrcutureDifferences(strucDiff);
            updateStructureOverview(structure);
        }
    }
    
    protected void updateStructureOverview(Structure structure) {
        StructureEntity dbentity;
        
        if (structure.getMolecule() != null) {
            dbentity = structure.createDbEntity(structure.getId(), structure.getMolecule().getId());
        } else {
            dbentity = structure.createDbEntity(structure.getId(), null);
        }
        materialService.getEm().merge(dbentity);
    }
    
    public void saveEditedMaterialOverview() {
        if (comparator.getDifferenceOfType(diffs, MaterialOverviewDifference.class) != null) {
            saveMaterialOverviewDifference(comparator.getDifferenceOfType(diffs, MaterialOverviewDifference.class));
            updateMaterialOverview(newMaterial, projectAcl, newMaterial.getOwnerID());
        }
    }
    
    protected void saveMaterialOverviewDifference(MaterialOverviewDifference diff) {
        MaterialHistoryEntity entity = new MaterialHistoryEntity();
        ACList acl = null;
        if (diff.getAcListNew() != null) {
            acl = materialService.getAcListService().save(diff.getAcListNew());
            entity.setAclistid_new(acl.getId());
            entity.setAclistid_old(diff.getAcListOld().getId());
        }
        
        entity.setAction(diff.getAction().toString());
        entity.setActorid(diff.getActorID());
        entity.setId(new MaterialHistoryId(diff.getMaterialID(), diff.getmDate()));
        entity.setOwnerid_new(diff.getOwnerIdNew());
        entity.setOwnerid_old(diff.getOwnerIdOld());
        entity.setProjectid_new(diff.getProjectIdNew());
        entity.setProjectid_old(diff.getProjectIdOld());
        materialService.getEm().persist(entity);
    }
    
    protected void updateMaterialOverview(
            Material m,
            UUID projectAclId,
            UUID userId) {
        MaterialEntity mE = new MaterialEntity();
        mE.setMaterialid(m.getId());
        mE.setCtime(m.getCreationTime());
        mE.setMaterialtypeid(m.getType().getId());
        mE.setOwnerid(userId);
        mE.setProjectid(m.getProjectId());
        mE.setUsergroups(projectAclId);
        materialService.getEm().merge(mE);
    }
    
    protected void saveMaterialIndexDifferences(
            MaterialIndexDifference diffs) {
        for (MaterialIndexHistoryEntity mihe : diffs.createDbEntities(
                diffs.getMaterialId(),
                diffs.getmDate(),
                diffs.getActorID())) {
            this.materialService.getEm().persist(mihe);
        }
    }
    
    protected void saveNewMaterialIndices(Material newMaterial) {
        for (IndexEntry ie : newMaterial.getIndices()) {
            this.materialService.getEm().persist(ie.toDbEntity(newMaterial.getId(), 0));
        }
    }
    
    protected void deleteOldMaterialIndices(Material m) {
        this.materialService.getEm().createNativeQuery("Delete from material_indices where materialid=:mid").setParameter("mid", m.getId()).executeUpdate();
    }
    
    protected void deleteOldHazards(Material m) {
        this.materialService.getEm().createNativeQuery("Delete from hazards_materials where materialid=:mid").setParameter("mid", m.getId()).executeUpdate();
    }
    
    protected void saveMaterialNames(Material m) {
        int rank = 0;
        for (MaterialName mn : m.getNames()) {
            this.materialService.getEm().persist(mn.toDbEntity(m.getId(), rank));
            rank++;
        }
    }
    
    protected void saveMaterialStrcutureDifferences(MaterialStructureDifference diff) {
        this.materialService.getEm().persist(diff.createDbInstance());
    }
    
}
