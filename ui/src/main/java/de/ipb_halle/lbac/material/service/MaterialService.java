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

import de.ipb_halle.lbac.material.difference.MaterialDifference;
import de.ipb_halle.lbac.material.difference.MaterialComparator;
import de.ipb_halle.lbac.material.bean.history.MaterialHistory;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.entity.MaterialEntity;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.MaterialDetailRight;
import de.ipb_halle.lbac.material.subtype.MaterialType;
import de.ipb_halle.lbac.material.bean.save.MaterialEditSaver;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.MaterialDetailType;
import de.ipb_halle.lbac.material.common.StorageClassInformation;
import de.ipb_halle.lbac.material.entity.hazard.HazardsMaterialsEntity;
import de.ipb_halle.lbac.material.entity.MaterialDetailRightEntity;
import de.ipb_halle.lbac.material.entity.MaterialHistoryEntity;
import de.ipb_halle.lbac.material.entity.MaterialHistoryId;
import de.ipb_halle.lbac.material.entity.biomaterial.BioMaterialEntity;
import de.ipb_halle.lbac.material.entity.index.MaterialIndexEntryEntity;
import de.ipb_halle.lbac.material.entity.storage.StorageConditionStorageEntity;
import de.ipb_halle.lbac.material.entity.storage.StorageEntity;
import de.ipb_halle.lbac.material.entity.structure.StructureEntity;
import de.ipb_halle.lbac.material.entity.tissue.TissueEntity;
import de.ipb_halle.lbac.material.subtype.biomaterial.BioMaterial;
import de.ipb_halle.lbac.material.subtype.structure.Structure;
import de.ipb_halle.lbac.material.subtype.taxonomy.Taxonomy;
import de.ipb_halle.lbac.material.subtype.tissue.Tissue;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.service.ACListService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
@Stateless
public class MaterialService implements Serializable {

    protected StructureInformationSaver structureInformationSaver;
    private final String SQL_GET_MATERIAL
            = "SELECT DISTINCT m.materialid, "
            + "m.materialtypeid, "
            + "m.ctime, "
            + "m.usergroups, "
            + "m.ownerid, "
            + "m.projectid, "
            + "m.deactivated "
            + "FROM materials m "
            + "JOIN acentries ace ON ace.aclist_id=m.usergroups "
            + "JOIN memberships me ON ace.member_id=me.group_id "
            + "WHERE deactivated=false "
            + "AND (CAST(:userid AS UUID)=me.member_id "
            + "AND ace.permread=true "
            + "OR m.ownerid=CAST(:userid AS UUID)) "
            + "AND materialtypeid NOT IN (6,7) ";
    private final String SQL_GET_STORAGE = "SELECT materialid,storageClass,description FROM storages WHERE materialid=:mid";
    private final String SQL_GET_STORAGE_CONDITION = "SELECT conditionId,materialid FROM storageconditions_storages WHERE materialid=:mid";
    private final String SQL_GET_HAZARDS = "SELECT typeid,materialid,remarks FROM hazards_materials WHERE materialid=:mid";
    private final String SQL_GET_INDICES = "SELECT id,materialid,typeid,value,language,rank FROM material_indices WHERE materialid=:mid order by rank";
    private final String SQL_GET_STRUCTURE_INFOS = "SELECT id,sumformula,molarmass,exactmolarmass,moleculeid FROM structures WHERE id=:mid";
    private final String SQL_GET_MOLECULE = "SELECT id,format,CAST(molecule AS VARCHAR) FROM molecules WHERE id=:mid";
    private final String SQL_DEACTIVATE_MATERIAL = "UPDATE materials SET deactivated=true WHERE materialid=:mid";
    private final String SQL_GET_SIMILAR_NAMES
            = "SELECT DISTINCT(mi.value) "
            + "FROM material_indices mi "
            + "JOIN materials m ON m.materialid=mi.materialid "
            + "JOIN acentries ace ON ace.aclist_id=m.usergroups "
            + "JOIN memberships me ON ace.member_id=me.group_id "
            + "WHERE LOWER(mi.value) LIKE LOWER(:name) "
            + "AND (CAST(:userid AS UUID)=me.member_id "
            + "AND ace.permread=true "
            + "OR m.ownerid=CAST(:userid AS UUID)) "
            + "AND mi.typeid=1 "
            + "AND m.materialtypeid NOT IN(6,7)";
    private final String SQL_SAVE_EFFECTIVE_TAXONOMY = "INSERT INTO effective_taxonomy (taxoid,parentid) VALUES(:tid,:pid)";

    protected MaterialHistoryService materialHistoryService;

    @Inject
    protected UserBean userBean;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    protected EntityManager em;

    @Inject
    protected MoleculeService moleculeService;

    protected final Logger logger = LogManager.getLogger(this.getClass().getName());

    protected MaterialComparator comparator;

    @Inject
    protected ACListService aclService;

    protected MaterialEditSaver editedMaterialSaver;

    @Inject
    protected ProjectService projectService;

    @Inject
    protected TaxonomyService taxonomyService;

    @Inject
    protected TissueService tissueService;

    @PostConstruct
    public void init() {
        comparator = new MaterialComparator();
        materialHistoryService = new MaterialHistoryService(this);
        editedMaterialSaver = new MaterialEditSaver(this);
        structureInformationSaver = new StructureInformationSaver(em);
    }

    /**
     *
     * @param materialID
     * @param actor
     */
    public void deactivateMaterial(int materialID, User actor) {
        em.createNativeQuery(SQL_DEACTIVATE_MATERIAL)
                .setParameter("mid", materialID)
                .executeUpdate();

        MaterialHistoryEntity histEntity = new MaterialHistoryEntity();
        histEntity.setActorid(actor.getId());
        histEntity.setAction("DELETE");
        histEntity.setId(new MaterialHistoryId(materialID, new Date()));
        this.em.persist(histEntity);
    }

    /**
     *
     * @return
     */
    public ACListService getAcListService() {
        return aclService;
    }

    /**
     *
     * @return
     */
    public MaterialEditSaver getEditedMaterialSaver() {
        return editedMaterialSaver;
    }

    /**
     *
     * @return
     */
    public EntityManager getEm() {
        return em;
    }

    @SuppressWarnings("unchecked")
    public List<Material> getReadableMaterials() {
        Query q = em.createNativeQuery(SQL_GET_MATERIAL, MaterialEntity.class);
        q.setFirstResult(0);
        q.setMaxResults(25);
        q.setParameter("userid", userBean.getCurrentAccount().getId());
        List<MaterialEntity> ies = q.getResultList();
        List<Material> back = new ArrayList<>();
        for (MaterialEntity me : ies) {
            Material m = null;
            if (MaterialType.getTypeById(me.getMaterialtypeid()) == MaterialType.STRUCTURE) {
                m = getStructure(me);

            }
            if (MaterialType.getTypeById(me.getMaterialtypeid()) == MaterialType.BIOMATERIAL) {
                m = getBioMaterial(me);
            }
            m.setAcList(aclService.loadById(me.getUsergroups()));
            m.getDetailRights().addAll(loadDetailRightsOfMaterial(m.getId()));
            back.add(m);
        }

        return back;
    }

    public BioMaterial getBioMaterial(MaterialEntity me) {
        BioMaterialEntity entity = this.em.find(BioMaterialEntity.class, me.getMaterialid());

        Tissue tissue = null;
        if (entity.getTissueid() != null) {
            tissue = tissueService.loadTissueById(entity.getTissueid());
        }
        BioMaterial b = new BioMaterial(
                me.getMaterialid(),
                loadMaterialNamesById(me.getMaterialid()),
                me.getProjectid(),
                loadHazardInformation(me.getMaterialid()),
                loadStorageClassInformation(me.getMaterialid()),
                taxonomyService.loadTaxonomyById(entity.getTaxoid()),
                tissue
        );
        b.setAcList(aclService.loadById(me.getUsergroups()));
        b.setOwnerID(me.getOwnerid());
        return b;

    }

    /**
     * Gets all materialnames which matches the pattern %name%
     *
     * @param name name for searching
     * @param user
     * @return List of matching materialnames
     */
    @SuppressWarnings("unchecked")
    public List<String> getSimilarMaterialNames(String name, User user) {
        return this.em.createNativeQuery(SQL_GET_SIMILAR_NAMES)
                .setParameter("name", "%" + name + "%")
                .setParameter("userid", userBean.getCurrentAccount().getId())
                .getResultList();
    }

    private StorageClassInformation loadStorageClassInformation(int materialId) {
        Query q = em.createNativeQuery(SQL_GET_STORAGE, StorageEntity.class);
        Query q2 = em.createNativeQuery(SQL_GET_STORAGE_CONDITION, StorageConditionStorageEntity.class);
        q2.setParameter("mid", materialId);
        q.setParameter("mid", materialId);

        StorageClassInformation storageInfos = StorageClassInformation.createObjectByDbEntity(
                (StorageEntity) q.getSingleResult(),
                q2.getResultList()
        );
        return storageInfos;
    }

    private HazardInformation loadHazardInformation(int materialId) {
        Query q3 = em.createNativeQuery(SQL_GET_HAZARDS, HazardsMaterialsEntity.class);
        q3.setParameter("mid", materialId);
        return HazardInformation.createObjectFromDbEntity(q3.getResultList());
    }

    /**
     *
     * @param me
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Structure getStructure(MaterialEntity me) {

        StorageClassInformation storageInfos = loadStorageClassInformation(me.getMaterialid());
        HazardInformation hazardIfos = loadHazardInformation(me.getMaterialid());

        Query q4 = em.createNativeQuery(SQL_GET_INDICES, MaterialIndexEntryEntity.class);
        q4.setParameter("mid", me.getMaterialid());

        Query q5 = em.createNativeQuery(SQL_GET_STRUCTURE_INFOS, StructureEntity.class);
        q5.setParameter("mid", me.getMaterialid());

        StructureEntity sE = (StructureEntity) q5.getSingleResult();
        String molecule = "";
        String moleculeFormat = "";
        int moleculeId = 0;
        if (sE.getMoleculeid() != null && sE.getMoleculeid() != 0) {
            Query q6 = em.createNativeQuery(SQL_GET_MOLECULE);
            q6.setParameter("mid", sE.getMoleculeid());
            Object[] result = (Object[]) q6.getSingleResult();
            moleculeId = (int) result[0];
            moleculeFormat = (String) result[1];
            molecule = (String) result[2];

        }

        Structure s = Structure.createInstanceFromDB(
                me,
                hazardIfos,
                storageInfos,
                q4.getResultList(),
                sE,
                molecule,
                moleculeId,
                moleculeFormat);

        return s;
    }

    public List<MaterialName> loadMaterialNamesById(int id) {
        List<MaterialName> names = new ArrayList<>();
        Query query = em.createNativeQuery(SQL_GET_INDICES, MaterialIndexEntryEntity.class);
        query.setParameter("mid", id);
        List<MaterialIndexEntryEntity> entities = query.getResultList();
        for (MaterialIndexEntryEntity entity : entities) {
            if (entity.getTypeid() == 1) {
                names.add(new MaterialName(entity.getValue(), entity.getLanguage(), entity.getRank()));
            }
        }
        return names;
    }

    /**
     *
     * @param id
     * @return
     */
    protected List<MaterialDetailRight> loadDetailRightsOfMaterial(int id) {
        List<MaterialDetailRight> rights = new ArrayList<>();
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<MaterialDetailRightEntity> q = cb.createQuery(MaterialDetailRightEntity.class);
        Root<MaterialDetailRightEntity> c = q.from(MaterialDetailRightEntity.class);
        q.select(c).where(cb.equal(c.get("materialid"), id));
        List<MaterialDetailRightEntity> entities = em.createQuery(q).getResultList();
        for (MaterialDetailRightEntity entity : entities) {
            rights.add(new MaterialDetailRight(entity, aclService.loadById(entity.getAclistid())));
        }
        return rights;
    }

    /**
     *
     * @param materialId
     * @return
     */
    public MaterialHistory loadHistoryOfMaterial(
            Integer materialId) {
        try {
            return materialHistoryService.loadHistoryOfMaterial(materialId);
        } catch (Exception e) {
            StackTraceElement t = e.getStackTrace()[0];
            logger.info(t.getClassName() + ":" + t.getMethodName() + ":" + t.getLineNumber());
            logger.error(e);
            return new MaterialHistory();
        }
    }

    /**
     *
     * @param id
     * @return
     */
    public Material loadMaterialById(int id) {
        Material material = null;
        MaterialEntity entity = em.find(MaterialEntity.class, id);
        if (MaterialType.getTypeById(entity.getMaterialtypeid()) == MaterialType.STRUCTURE) {
            material = getStructure(entity);
        }
        if (MaterialType.getTypeById(entity.getMaterialtypeid()) == MaterialType.BIOMATERIAL) {
            material = getBioMaterial(entity);
        }
        if (MaterialType.getTypeById(entity.getMaterialtypeid()) == MaterialType.TAXONOMY) {
            material = taxonomyService.loadTaxonomyById(id);
        }
        return material;
    }

    /**
     *
     * @param m
     * @param detailTemplates
     */
    protected void saveDetailRightsFromTemplate(
            Material m,
            Map<MaterialDetailType, ACList> detailTemplates) {
        m.getDetailRights().clear();
        for (MaterialDetailType mdt : detailTemplates.keySet()) {
            MaterialDetailRightEntity mdrE = new MaterialDetailRightEntity();
            mdrE.setMaterialid(m.getId());
            mdrE.setMaterialtypeid(mdt.getId());
            mdrE.setAclistid(detailTemplates.get(mdt).getId());
            em.persist(mdrE);

            m.getDetailRights().add(new MaterialDetailRight(mdrE, detailTemplates.get(mdt)));
        }
    }

    /**
     *
     * @param newMaterial
     * @param oldMaterial
     * @param projectAcl
     * @param actorId
     * @throws Exception
     */
    public void saveEditedMaterial(
            Material newMaterial,
            Material oldMaterial,
            UUID projectAcl,
            UUID actorId) throws Exception {
        Date modDate = new Date();

        List<MaterialDifference> diffs = comparator.compareMaterial(oldMaterial, newMaterial);

        for (MaterialDifference md : diffs) {
            md.initialise(newMaterial.getId(), actorId, modDate);
        }
        editedMaterialSaver.init(comparator, diffs, newMaterial, oldMaterial,
                projectAcl, actorId);

        editedMaterialSaver.saveEditedMaterialOverview();
        editedMaterialSaver.saveEditedMaterialIndices();
        editedMaterialSaver.saveEditedMaterialStructure();
        editedMaterialSaver.saveEditedMaterialHazards();
        editedMaterialSaver.saveEditedMaterialStorage();
        editedMaterialSaver.saveEditedTaxonomy();

    }

    /**
     *
     * @param m
     * @param projectAclId
     * @param detailTemplates
     */
    public void saveMaterialToDB(
            Material m,
            UUID projectAclId,
            Map<MaterialDetailType, ACList> detailTemplates) {

        saveMaterialOverview(m, projectAclId);
        saveMaterialNames(m);
        saveMaterialHazards(m);
        saveStorageConditions(m);
        saveDetailRightsFromTemplate(m, detailTemplates);

        if (m.getType() == MaterialType.STRUCTURE) {
            structureInformationSaver.saveStructureInformation(m);
        }
        if (m.getType() == MaterialType.TAXONOMY) {
            saveTaxonomy((Taxonomy) m);
        }
        if (m.getType() == MaterialType.TISSUE) {
            saveTissue((Tissue) m);
        }
        if (m.getType() == MaterialType.BIOMATERIAL) {
            saveBioMaterial((BioMaterial) m);
        }

    }

    public void saveBioMaterial(BioMaterial b) {
        this.em.persist(b.createEntity());
    }

    /**
     *
     * @param m
     * @param projectAclId
     * @return
     */
    protected Material saveMaterialOverview(Material m, UUID projectAclId) {
        MaterialEntity mE = new MaterialEntity();
        mE.setCtime(new Date());
        mE.setMaterialtypeid(m.getType().getId());
        mE.setOwnerid(userBean.getCurrentAccount().getId());
        mE.setProjectid(m.getProjectId());
        mE.setUsergroups(projectAclId);
        mE.setDeactivated(false);
        em.persist(mE);
        m.setId(mE.getMaterialid());
        m.setAcList(aclService.loadById(projectAclId));
        m.setOwnerID(userBean.getCurrentAccount().getId());
        m.setCreationTime(mE.getCtime());
        return m;
    }

    /**
     *
     * @param m
     */
    protected void saveMaterialNames(Material m) {
        int rank = 0;
        for (MaterialName mn : m.getNames()) {
            em.persist(mn.toDbEntity(m.getId(), rank));
            rank++;
        }
    }

    /**
     *
     * @param m
     */
    protected void saveMaterialHazards(Material m) {
        for (HazardsMaterialsEntity haMaEn : m.getHazards().createDBInstances(m.getId())) {
            em.persist(haMaEn);
        }
    }

    /**
     *
     * @param m
     */
    protected void saveStorageConditions(Material m) {
        em.persist(m.getStorageInformation().createStorageDBInstance(m.getId()));
        for (StorageConditionStorageEntity scsE : m.getStorageInformation().createDBInstances(m.getId())) {
            em.persist(scsE);
        }
    }

    public Taxonomy saveTaxonomy(Taxonomy t) {
        this.em.persist(t.createEntity());
        for (Taxonomy th : t.getTaxHierachy()) {
            em.createNativeQuery(SQL_SAVE_EFFECTIVE_TAXONOMY)
                    .setParameter("tid", t.getId())
                    .setParameter("pid", th.getId())
                    .executeUpdate();
        }

        return t;
    }

    public Tissue saveTissue(Tissue t) {
        TissueEntity entity = t.createEntity();
        this.em.persist(entity);
        t.setId(entity.getId());
        return t;
    }

    public void setEditedMaterialSaver(MaterialEditSaver editedMaterialSaver) {
        this.editedMaterialSaver = editedMaterialSaver;
    }

    /**
     *
     * @param userBean
     */
    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public void setStructureInformationSaver(StructureInformationSaver structureInformationSaver) {
        this.structureInformationSaver = structureInformationSaver;
    }

    /**
     *
     * @param m
     * @param projectAclId
     * @param userId
     */
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
        em.merge(mE);
    }

}
