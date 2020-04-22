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
package de.ipb_halle.lbac.material.service;

import de.ipb_halle.lbac.material.bean.ModificationType;
import de.ipb_halle.lbac.material.subtype.structure.Molecule;
import de.ipb_halle.lbac.material.bean.history.MaterialHistory;
import de.ipb_halle.lbac.material.difference.MaterialHazardDifference;
import de.ipb_halle.lbac.material.difference.MaterialIndexDifference;
import de.ipb_halle.lbac.material.difference.MaterialOverviewDifference;
import de.ipb_halle.lbac.material.difference.MaterialStorageDifference;
import de.ipb_halle.lbac.material.difference.MaterialStructureDifference;
import de.ipb_halle.lbac.material.entity.hazard.HazardsMaterialHistEntity;
import de.ipb_halle.lbac.material.entity.index.MaterialIndexHistoryEntity;
import de.ipb_halle.lbac.material.entity.storage.StorageClassHistoryEntity;
import de.ipb_halle.lbac.material.entity.storage.StorageConditionHistoryEntity;
import de.ipb_halle.lbac.material.entity.structure.StructureHistEntity;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.persistence.Query;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class MaterialHistoryService {

    private final String SQL_GET_INDEX_HISTORY = "select"
            + " id,"
            + "materialid,"
            + "typeid,"
            + "mdate,"
            + "actorid,"
            + "digest,"
            + "value_old,"
            + "value_new,"
            + "rank_old,"
            + "rank_new,"
            + "language_old,"
            + "language_new "
            + "from material_indices_hist where materialid=:mid";

    private final String SQL_GET_OVERVIEW_HISTORY = "select "
            + "materialid,"
            + "mdate,"
            + "cast(actorid as varchar),"
            + "digest,"
            + "action,"
            + "cast(aclistid_old as varchar),"
            + "cast(aclistid_new as varchar),"
            + "projectid_old,"
            + "projectid_new,"
            + "cast(ownerid_old as varchar),"
            + "cast(ownerid_new as varchar)"
            + " from materials_hist where materialid=:mid";

    private final String SQL_GET_STRUCTURE_HISTORY
            = "select "
            + "id,"
            + "actorid,"
            + "mtime,"
            + "digest, "
            + "sumformula_old,"
            + "sumformula_new,"
            + "molarmass_old,"
            + "molarmass_new,"
            + "exactmolarmass_old,"
            + "exactmolarmass_new,"
            + "moleculeid_old,"
            + "moleculeid_new"
            + " FROM structures_hist"
            + " WHERE id=:mid";

    private final String SQL_GET_HAZARD_HISTORY
            = "select "
            + "id,"
            + "materialid,"
            + "actorid,"
            + "mdate,"
            + "digest, "
            + "typeid_old,"
            + "typeid_new,"
            + "remarks_old,"
            + "remarks_new"
            + " FROM hazards_materials_hist"
            + " WHERE materialid=:mid";

    private final String SQL_GET_MOLECULE = "SELECT "
            + "id,"
            + "format,"
            + "CAST(molecule AS VARCHAR)"
            + " FROM molecules "
            + " WHERE id=:mid";

    private final String SQL_GET_STORAGE_CLASS_HISTORY = "SELECT materialid,mdate,actorid,digest,description_old,description_new,storageclass_old,storageclass_new from storages_hist where materialid=:mid";
    private final String SQL_GET_STORAGE_CONDITION_HISTORY = "SELECT id,materialid,mdate,actorid,digest,conditionId_old,conditionId_new from storagesconditions_storages_hist where materialid=:mid";

    Logger logger = LogManager.getLogger(this.getClass().getName());
    public MaterialService materialService;

    public MaterialHistoryService(MaterialService materialService) {
        this.materialService = materialService;
    }

    public MaterialHistory loadHistoryOfMaterial(
            Integer materialId) {

        MaterialHistory history = new MaterialHistory();
        try {
            loadOverviewHistory(materialId, history);
            loadIndexHistory(materialId, history);
            loadStructureHistory(materialId, history);
            loadHazardHistory(materialId, history);
            loadStorageHistory(materialId, history);
        } catch (Exception e) {
            StackTraceElement t = e.getStackTrace()[0];
            logger.info(t.getClassName() + ":" + t.getMethodName() + ":" + t.getLineNumber());
            logger.error(e);
        }
        return history;
    }

    @SuppressWarnings("unchecked")
    protected void loadIndexHistory(int materialId, MaterialHistory history) {
        List<MaterialIndexHistoryEntity> indexHistory = materialService.getEm().createNativeQuery(SQL_GET_INDEX_HISTORY, MaterialIndexHistoryEntity.class).setParameter("mid", materialId).getResultList();
        Map<Date, ArrayList<MaterialIndexHistoryEntity>> diffsByDate = new HashMap<>();

        for (MaterialIndexHistoryEntity mihe : indexHistory) {
            if (diffsByDate.get(mihe.getmDate()) == null) {
                diffsByDate.put(mihe.getmDate(), new ArrayList<>());
            }
            diffsByDate.get(mihe.getmDate()).add(mihe);
        }
        for (Date d : diffsByDate.keySet()) {
            history.addDifference(new MaterialIndexDifference(diffsByDate.get(d)));
        }
    }

    @SuppressWarnings("unchecked")
    private void loadStructureHistory(
            int materialId,
            MaterialHistory history) {

        try {
            List<StructureHistEntity> dbEntities = materialService.getEm()
                    .createNativeQuery(SQL_GET_STRUCTURE_HISTORY, StructureHistEntity.class)
                    .setParameter("mid", materialId)
                    .getResultList();

            for (StructureHistEntity dbe : dbEntities) {
                history.addDifference(new MaterialStructureDifference(
                        dbe,
                        loadMolecule(dbe.getMoleculeid_old()),
                        loadMolecule(dbe.getMoleculeid_new())));
            }
        } catch (Exception e) {
            logger.error(e);
        }

    }

    @SuppressWarnings("unchecked")
    protected void loadOverviewHistory(int materialId, MaterialHistory history) {
        List<Object[]> hists = materialService.getEm()
                .createNativeQuery(SQL_GET_OVERVIEW_HISTORY)
                .setParameter("mid", materialId)
                .getResultList();

        for (Object[] mhe : hists) {
            MaterialOverviewDifference md = new MaterialOverviewDifference();
            md.setMaterialID((int) mhe[0]);
            md.setmDate((Date) mhe[1]);
            md.setActorID(UUID.fromString((String) mhe[2]));
            md.setDigest((String) mhe[3]);
            md.setAction(ModificationType.valueOf((String) mhe[4]));
            if (mhe[5] != null) {
                md.setAcListOld(materialService.getAcListService().loadById(UUID.fromString((String) mhe[5])));
            }
            if (mhe[6] != null) {
                md.setAcListNew(materialService.getAcListService().loadById(UUID.fromString((String) mhe[6])));
            }
            md.setProjectIdOld((Integer) mhe[7]);
            md.setProjectIdNew((Integer) mhe[8]);
            if (mhe[9] != null) {
                md.setOwnerIdOld(UUID.fromString((String) mhe[9]));
            }
            if (mhe[10] != null) {
                md.setOwnerIdNew(UUID.fromString((String) mhe[10]));
            }
            history.addDifference(md);
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadHazardHistory(int materialId, MaterialHistory history) {

        List<HazardsMaterialHistEntity> dbEntities = materialService.getEm()
                .createNativeQuery(SQL_GET_HAZARD_HISTORY, HazardsMaterialHistEntity.class)
                .setParameter("mid", materialId)
                .getResultList();
        if (dbEntities.size() > 0) {
            history.addDifference(new MaterialHazardDifference(dbEntities));
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadStorageHistory(int materialId, MaterialHistory history) {

        Map<Date, StorageClassHistoryEntity> sortedClassHistories = new HashMap<>();
        Map<Date, ArrayList<StorageConditionHistoryEntity>> sortedConditionHistories = new HashMap<>();
        List<StorageClassHistoryEntity> classHistoryEntities = materialService.getEm()
                .createNativeQuery(SQL_GET_STORAGE_CLASS_HISTORY, StorageClassHistoryEntity.class)
                .setParameter("mid", materialId)
                .getResultList();

        for (StorageClassHistoryEntity entity : classHistoryEntities) {
            Date date = entity.getId().getMdate();
            sortedClassHistories.put(date, entity);
        }

        List<StorageConditionHistoryEntity> classConditionEntities = materialService.getEm()
                .createNativeQuery(SQL_GET_STORAGE_CONDITION_HISTORY, StorageConditionHistoryEntity.class)
                .setParameter("mid", materialId)
                .getResultList();
        for (StorageConditionHistoryEntity entity : classConditionEntities) {
            Date date = entity.getmDate();
            if (sortedConditionHistories.get(date) == null) {
                sortedConditionHistories.put(date, new ArrayList<>());
            }
            sortedConditionHistories.get(date).add(entity);

        }
        for (Date d : sortedClassHistories.keySet()) {
            ArrayList<StorageConditionHistoryEntity> conditions = sortedConditionHistories.get(d);
            history.addDifference(new MaterialStorageDifference(sortedClassHistories.get(d), conditions));
            if (conditions != null) {
                sortedConditionHistories.remove(d);
            }
        }
        for (Date d : sortedConditionHistories.keySet()) {
            history.addDifference(new MaterialStorageDifference(null, sortedConditionHistories.get(d)));
        }
    }

    protected Molecule loadMolecule(Integer moleculeId) {
        if (moleculeId == null) {
            return null;
        }

        Query q6 = materialService.getEm().createNativeQuery(SQL_GET_MOLECULE);
        q6.setParameter("mid", moleculeId);
        Object[] result = (Object[]) q6.getSingleResult();

        String moleculeFormat = (String) result[1];
        String molecule = (String) result[2];
        return new Molecule(molecule, moleculeId, moleculeFormat);

    }

}
