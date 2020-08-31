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

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.structure.Molecule;
import de.ipb_halle.lbac.material.common.history.MaterialComparator;
import de.ipb_halle.lbac.material.common.history.MaterialDifference;
import de.ipb_halle.lbac.material.common.history.MaterialHazardDifference;
import de.ipb_halle.lbac.material.common.history.MaterialIndexDifference;
import de.ipb_halle.lbac.material.common.history.MaterialOverviewDifference;
import de.ipb_halle.lbac.material.common.history.MaterialStorageDifference;
import de.ipb_halle.lbac.material.structure.MaterialStructureDifference;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyDifference;
import de.ipb_halle.lbac.material.common.entity.hazard.HazardsMaterialHistEntity;
import de.ipb_halle.lbac.material.common.entity.hazard.HazardsMaterialsEntity;
import de.ipb_halle.lbac.material.common.entity.MaterialEntity;
import de.ipb_halle.lbac.material.common.entity.MaterialHistoryEntity;
import de.ipb_halle.lbac.material.common.entity.MaterialHistoryId;
import de.ipb_halle.lbac.material.common.entity.index.MaterialIndexHistoryEntity;
import de.ipb_halle.lbac.material.common.entity.storage.StorageClassHistoryEntity;
import de.ipb_halle.lbac.material.common.entity.storage.StorageClassHistoryId;
import de.ipb_halle.lbac.material.common.entity.storage.StorageConditionHistoryEntity;
import de.ipb_halle.lbac.material.common.entity.storage.StorageConditionStorageEntity;
import de.ipb_halle.lbac.material.common.entity.storage.StorageConditionStorageId;
import de.ipb_halle.lbac.material.common.entity.storage.StorageEntity;
import de.ipb_halle.lbac.material.structure.StructureEntity;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyHistEntity;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyHistEntityId;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.material.biomaterial.Taxonomy;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyNestingService;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageCondition;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;
import javax.persistence.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class MaterialEditSaver implements Serializable{

    protected String SQL_INSERT_MOLECULE = "INSERT INTO molecules (molecule,format) VALUES(CAST ((:molecule) AS molecule),:format) RETURNING id";
    protected String SQL_DELETE_STORAGE_CONDITIONS = "DELETE FROM storageconditions_storages WHERE materialid=:mid";
    protected String SQL_DELETE_EFFECTIVE_TAXONOMY = "DELETE FROM effective_taxonomy WHERE taxoid=:taxoid";
    protected String SQL_INSERT_EFFECTIVE_TAXONOMY = "INSERT INTO effective_taxonomy(taxoid,parentid) VALUES(:taxoid,:parentid)";
    protected String SQL_UPDATE_TAXONOMY_LEVEL = "UPDATE taxonomy SET level=:level WHERE id=:id";
    protected String SQL_DELETE_INDICES = "DELETE FROM material_indices WHERE materialid=:mid";
    protected String SQL_DELETE_HAZARDS = "DELETE FROM hazards_materials WHERE materialid=:mid";

    protected MaterialService materialService;
    protected TaxonomyNestingService taxonomyNestingService;

    protected MaterialComparator comparator;
    protected List<MaterialDifference> diffs;
    protected Material newMaterial;
    protected Material oldMaterial;
    protected Integer projectAcl;
    protected Integer actorId;
    private Logger logger = LogManager.getLogger(this.getClass().getName());

    public void init(
            MaterialComparator comparator,
            List<MaterialDifference> diffs,
            Material newMaterial,
            Material oldMaterial,
            Integer projectAcl,
            Integer actorId) {
        this.comparator = comparator;
        this.diffs = diffs;
        this.newMaterial = newMaterial;
        this.oldMaterial = oldMaterial;
        this.projectAcl = projectAcl;
        this.actorId = actorId;
    }

    public MaterialEditSaver(
            MaterialService materialService,
            TaxonomyNestingService taxonomyNestingService) {
        this.materialService = materialService;
        this.taxonomyNestingService = taxonomyNestingService;
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

    public void saveEditedTaxonomy() {
        TaxonomyDifference diff = comparator.getDifferenceOfType(diffs, TaxonomyDifference.class);
        if (diff != null) {
            TaxonomyHistEntity the = new TaxonomyHistEntity();
            the.setAction("EDIT");
            the.setId(new TaxonomyHistEntityId(oldMaterial.getId(), diff.getModificationDate(), actorId));
            if (diff.isHierarchyChanged()) {
                updateEffectiveTaxonomy(diff);
                the.setParentid_new(diff.getNewHierarchy().get(0));
                Taxonomy t = (Taxonomy) oldMaterial;
                the.setParentid_old(t.getTaxHierachy().get(0).getId());
            }
            if (diff.isLevelChanged()) {
                materialService.getEm().createNativeQuery(SQL_UPDATE_TAXONOMY_LEVEL)
                        .setParameter("id", diff.getMaterialId())
                        .setParameter("level", diff.getNewLevelId())
                        .executeUpdate();
                the.setLevel_new(diff.getNewLevelId());
                the.setLevel_old(diff.getOldLevelId());
            }

            materialService.getEm().persist(the);

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
        MaterialStructureDifference strucDiff = comparator.getDifferenceOfType(diffs, MaterialStructureDifference.class);
        if (strucDiff != null) {
            Structure structure = (Structure) newMaterial;
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

    protected void updateEffectiveTaxonomy(TaxonomyDifference diff) {
        Taxonomy t = (Taxonomy) newMaterial;
        taxonomyNestingService.updateParentOfTaxonomy(newMaterial.getId(), t.getTaxHierachy().get(0).getId());
    }

    public void saveEditedMaterialOverview() {
        if (comparator.getDifferenceOfType(diffs, MaterialOverviewDifference.class) != null) {
            saveMaterialOverviewDifference(comparator.getDifferenceOfType(diffs, MaterialOverviewDifference.class));
            updateMaterialOverview(newMaterial, projectAcl, newMaterial.getOwner().getId());
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
            Integer projectAclId,
            Integer userId) {
        MaterialEntity mE = new MaterialEntity();
        mE.setMaterialid(m.getId());
        mE.setCtime(m.getCreationTime());
        mE.setMaterialtypeid(m.getType().getId());
        mE.setOwnerid(userId);
        mE.setProjectid(m.getProjectId());
        mE.setAclist_id(projectAclId);
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
        this.materialService.getEm()
                .createNativeQuery(SQL_DELETE_INDICES)
                .setParameter("mid", m.getId())
                .executeUpdate();
    }

    protected void deleteOldHazards(Material m) {
        this.materialService.getEm()
                .createNativeQuery(SQL_DELETE_HAZARDS)
                .setParameter("mid", m.getId())
                .executeUpdate();
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
