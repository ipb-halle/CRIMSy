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
package de.ipb_halle.lbac.material.common.service;

import de.ipb_halle.lbac.material.structure.StructureInformationSaver;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.material.common.history.MaterialDifference;
import de.ipb_halle.lbac.material.common.history.MaterialComparator;
import de.ipb_halle.lbac.material.common.history.MaterialHistory;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.entity.MaterialEntity;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.globals.SqlStringWrapper;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.MaterialDetailRight;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.bean.MaterialEditSaver;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.MaterialDetailType;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.common.entity.hazard.HazardsMaterialsEntity;
import de.ipb_halle.lbac.material.common.entity.MaterialDetailRightEntity;
import de.ipb_halle.lbac.material.common.entity.MaterialHistoryEntity;
import de.ipb_halle.lbac.material.common.entity.MaterialHistoryId;
import de.ipb_halle.lbac.material.biomaterial.BioMaterialEntity;
import de.ipb_halle.lbac.material.common.entity.index.MaterialIndexEntryEntity;
import de.ipb_halle.lbac.material.common.entity.storage.StorageConditionMaterialEntity;
import de.ipb_halle.lbac.material.common.entity.storage.StorageEntity;
import de.ipb_halle.lbac.material.structure.StructureEntity;
import de.ipb_halle.lbac.material.biomaterial.TissueEntity;
import de.ipb_halle.lbac.material.biomaterial.BioMaterial;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.material.biomaterial.Taxonomy;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyNestingService;
import de.ipb_halle.lbac.material.biomaterial.Tissue;
import de.ipb_halle.lbac.material.common.StorageClass;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.material.common.search.MaterialSearchConditionBuilder;
import de.ipb_halle.lbac.material.consumable.Consumable;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.search.SearchResultImpl;
import de.ipb_halle.lbac.search.lang.Attribute;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.DbField;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import de.ipb_halle.lbac.search.lang.OrderDirection;
import de.ipb_halle.lbac.search.lang.SqlBuilder;
import de.ipb_halle.lbac.search.lang.SqlCountBuilder;
import de.ipb_halle.lbac.search.lang.Value;
import de.ipb_halle.lbac.service.NodeService;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

    private static final long serialVersionUID = 1L;

    private List<StorageClass> storageClasses = new ArrayList<>();
    private MaterialEntityGraphBuilder graphBuilder;
    protected StructureInformationSaver structureInformationSaver;

    private final String SQL_GET_STORAGE = "SELECT materialid,storageClass,description FROM storages WHERE materialid=:mid";
    private final String SQL_GET_STORAGE_CONDITION = "SELECT conditionId,materialid FROM storageconditions_material WHERE materialid=:mid";
    private final String SQL_GET_HAZARDS = "SELECT typeid,materialid,remarks FROM material_hazards WHERE materialid=:mid";
    private final String SQL_GET_INDICES = "SELECT id,materialid,typeid,value,language,rank FROM material_indices WHERE materialid=:mid order by rank";
    private final String SQL_GET_STRUCTURE_INFOS = "SELECT id,sumformula,molarmass,exactmolarmass,moleculeid FROM structures WHERE id=:mid";
    private final String SQL_GET_MOLECULE = "SELECT id,molecule FROM molecules WHERE id=:mid";
    private final String SQL_DEACTIVATE_MATERIAL = "UPDATE materials SET deactivated=true WHERE materialid=:mid";
    private final String SQL_GET_SIMILAR_NAMES
            = "SELECT DISTINCT(mi.value) "
            + "FROM material_indices mi "
            + "JOIN materials m ON m.materialid=mi.materialid "
            + SqlStringWrapper.JOIN_KEYWORD + " "
            + "WHERE mi.value ILIKE :name "
            + "AND " + SqlStringWrapper.WHERE_KEYWORD + " "
            + "AND mi.typeid=1 "
            + "AND m.materialtypeid NOT IN(6,7)";
    private final String SQL_SAVE_EFFECTIVE_TAXONOMY = "INSERT INTO effective_taxonomy (taxoid,parentid) VALUES(:tid,:pid)";
    private final String SQL_GET_STORAGE_CLASSES = "SELECT id,name FROM storageclasses";

    private final String SQL_UPDATE_MATERIAL_ACL
            = "UPDATE materials "
            + "SET aclist_id =:aclid "
            + "WHERE materialid=:mid";

    protected MaterialHistoryService materialHistoryService;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    protected EntityManager em;

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
    protected HazardService hazardService;

    @Inject
    protected TissueService tissueService;

    @Inject
    protected NodeService nodeService;

    @Inject
    protected TaxonomyNestingService taxonomyNestingService;

    @Inject
    protected MemberService memberService;

    @PostConstruct
    public void init() {
        comparator = new MaterialComparator();
        materialHistoryService = new MaterialHistoryService(this);
        editedMaterialSaver = new MaterialEditSaver(this, taxonomyNestingService);
        structureInformationSaver = new StructureInformationSaver(em);
        storageClasses = loadStorageClasses();
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

    public int loadMaterialAmount(SearchRequest request) {
        EntityGraph graph = createEntityGraph();
        SqlBuilder sqlBuilder = new SqlCountBuilder(graph,
                new Attribute("materials", AttributeType.BARCODE));

        MaterialSearchConditionBuilder materialBuilder = new MaterialSearchConditionBuilder(graph, "materials");
        Condition con = materialBuilder.convertRequestToCondition(request, ACPermission.permREAD);

        String sql = sqlBuilder.query(con);

        Query q = em.createNativeQuery(sql);
        for (Value param : sqlBuilder.getValueList()) {
            q.setParameter(param.getArgumentKey(), param.getValue());
        }
        BigInteger bi = (BigInteger) q.getResultList().get(0);
        return bi.intValue();
    }

    public SearchResult getReadableMaterials(SearchRequest request) {
        try {
            EntityGraph graph = createEntityGraph();
            SearchResult result = new SearchResultImpl(nodeService.getLocalNode());
            SqlBuilder sqlBuilder = new SqlBuilder(graph);

            MaterialSearchConditionBuilder materialBuilder = new MaterialSearchConditionBuilder(graph, "materials");
            Condition con = materialBuilder.convertRequestToCondition(request, ACPermission.permREAD);

            String sql = sqlBuilder.query(con,
                    createOrderList());
            logger.info(sql);
            Query q = em.createNativeQuery(sql, MaterialEntity.class);
            q.setFirstResult(request.getFirstResult());
            q.setMaxResults(request.getMaxResults());
            for (Value param : sqlBuilder.getValueList()) {
                q.setParameter(param.getArgumentKey(), param.getValue());
                logger.info("  "+param.getArgumentKey()+" : "+ param.getValue());
            }
            @SuppressWarnings("unchecked")
            List<MaterialEntity> entities =q.getResultList();
            for (MaterialEntity me : entities) {
                Material material = loadMaterialById(me.getMaterialid());
                result.addResult(material);
            }
            return result;
        } catch (Exception e) {
            logger.error(e);
            return new SearchResultImpl(nodeService.getLocalNode());
        }
    }

    private List<DbField> createOrderList() {
        DbField labelField = new DbField()
                .setColumnName("materialid")
                .setTableName("materials")
                .setOrderDirection(OrderDirection.ASC);

        List<DbField> orderList = new ArrayList<>();
        orderList.add(labelField);
        return orderList;
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
        b.setCreationTime(me.getCtime());
        b.setACList(aclService.loadById(me.getACList()));
        b.setOwner(memberService.loadUserById(me.getOwner()));
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
        return this.em.createNativeQuery(SqlStringWrapper.aclWrapper(SQL_GET_SIMILAR_NAMES, "m.aclist_id", "m.owner_id", ACPermission.permREAD))
                .setParameter("name", "%" + name + "%")
                .setParameter("userid", user.getId())
                .getResultList();
    }

    @SuppressWarnings("unchecked")
    private StorageInformation loadStorageClassInformation(int materialId) {

        Query q = em.createNativeQuery(SQL_GET_STORAGE, StorageEntity.class);
        Query q2 = em.createNativeQuery(SQL_GET_STORAGE_CONDITION, StorageConditionMaterialEntity.class);
        q2.setParameter("mid", materialId);
        q.setParameter("mid", materialId);
        List<StorageEntity> storageEntities = q.getResultList();
        StorageInformation storageInfos = null;
        if (storageEntities.isEmpty()) {
            storageInfos = StorageInformation.createObjectByDbEntity(
                    q2.getResultList());
        } else {
            String remarks = storageEntities.get(0).getDescription();
            StorageClass storageClass = loadStorageClassById(storageEntities.get(0).getStorageClass());
            storageInfos = StorageInformation.createObjectByDbEntity(
                    remarks, storageClass, q2.getResultList());
        }

        return storageInfos;
    }

    @SuppressWarnings("unchecked")
    private HazardInformation loadHazardInformation(int materialId) {
        Query q3 = em.createNativeQuery(SQL_GET_HAZARDS, HazardsMaterialsEntity.class);
        q3.setParameter("mid", materialId);
        return new HazardInformation(q3.getResultList(), hazardService.getAllHazardTypes());
    }

    /**
     *
     * @param me
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Structure getStructure(MaterialEntity me) {
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
            molecule = (String) result[1];

        }

        Structure s = Structure.createInstanceFromDB(
                me,
                loadHazardInformation(me.getMaterialid()),
                loadStorageClassInformation(me.getMaterialid()),
                q4.getResultList(),
                sE,
                molecule,
                moleculeId,
                moleculeFormat);
        s.setOwner(memberService.loadUserById(me.getOwner()));
        return s;
    }

    @SuppressWarnings("unchecked")
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
        if (MaterialType.getTypeById(entity.getMaterialtypeid()) == MaterialType.CONSUMABLE) {
            material = loadConsumable(entity);
        }
        material.setACList(aclService.loadById(entity.getACList()));
        material.setStorageInformation(loadStorageClassInformation(id));
        material.setOwner(memberService.loadUserById(entity.getOwner()));
        material.getDetailRights().addAll(loadDetailRightsOfMaterial(material.getId()));
        material.setHistory(materialHistoryService.loadHistoryOfMaterial(material.getId()));
        material.setCreationTime(entity.getCtime());
        return material;
    }

    private Consumable loadConsumable(MaterialEntity me) {
        Consumable c = new Consumable(
                me.getMaterialid(),
                loadMaterialNamesById(me.getMaterialid()),
                me.getProjectid(),
                loadHazardInformation(me.getMaterialid()),
                loadStorageClassInformation(me.getMaterialid()));
        c.setCreationTime(me.getCtime());
        return c;
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
            Integer projectAcl,
            Integer actorId) throws Exception {
        Date modDate = new Date();

        List<MaterialDifference> diffs = comparator.compareMaterial(oldMaterial, newMaterial);
        for (MaterialDifference md : diffs) {
            md.initialise(newMaterial.getId(), actorId, modDate);
        }
        editedMaterialSaver.init(comparator, diffs, newMaterial, oldMaterial,
                projectAcl, actorId);
        editedMaterialSaver.saveEditedMaterialOverview();
        editedMaterialSaver.saveEditedMaterialIndices();
        if (newMaterial.getType() == MaterialType.STRUCTURE) {
            editedMaterialSaver.saveEditedMaterialStructure();
        }
        editedMaterialSaver.saveEditedMaterialHazards();
        editedMaterialSaver.saveEditedMaterialStorage();
        editedMaterialSaver.saveEditedTaxonomy();
        editedMaterialSaver.saveEditedBiomaterial();
    }

    /**
     *
     * @param m
     * @param projectAclId
     * @param detailTemplates
     * @param onwerId
     */
    public void saveMaterialToDB(
            Material m,
            Integer projectAclId,
            Map<MaterialDetailType, ACList> detailTemplates,
            Integer onwerId) {
        saveMaterialOverview(m, projectAclId, onwerId);
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

    /**
     *
     * @param m
     * @param projectAclId
     * @param detailTemplates
     */
    public void saveMaterialToDB(
            Material m,
            Integer projectAclId,
            Map<MaterialDetailType, ACList> detailTemplates,
            User owner) {
        saveMaterialToDB(
                m,
                projectAclId,
                detailTemplates,
                owner.getId());

    }

    public void saveBioMaterial(BioMaterial b) {
        this.em.persist(b.createEntity());
    }

    /**
     *
     * @param m
     * @param projectAclId
     * @param ownerId
     * @return
     */
    protected Material saveMaterialOverview(Material m, Integer projectAclId, Integer ownerId) {
        MaterialEntity mE = new MaterialEntity();
        mE.setCtime(new Date());
        mE.setMaterialtypeid(m.getType().getId());
        mE.setOwner(ownerId);
        mE.setProjectid(m.getProjectId());
        mE.setACList(projectAclId);
        mE.setDeactivated(false);
        em.persist(mE);
        m.setId(mE.getMaterialid());
        m.setACList(aclService.loadById(projectAclId));
        m.setOwner(memberService.loadUserById(ownerId));
        m.setCreationTime(mE.getCtime());
        return m;
    }

    public Material updateMaterialAcList(Material m) {
        m.setACList(aclService.save(m.getACList()));
        em.createNativeQuery(SQL_UPDATE_MATERIAL_ACL)
                .setParameter("mid", m.getId())
                .setParameter("aclid", m.getACList().getId())
                .executeUpdate();
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
        for (HazardsMaterialsEntity haMaEn : m.getHazards().createEntity(m.getId())) {
            em.persist(haMaEn);
        }
    }

    /**
     *
     * @param m
     */
    protected void saveStorageConditions(Material m) {
        if (m.getStorageInformation().getStorageClass() != null) {
            em.persist(m.getStorageInformation().createStorageDBInstance(m.getId()));
        }
        for (StorageConditionMaterialEntity scsE : m.getStorageInformation().createDBInstances(m.getId())) {
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

    public void setStructureInformationSaver(StructureInformationSaver structureInformationSaver) {
        this.structureInformationSaver = structureInformationSaver;
        this.editedMaterialSaver.setStructureSaver(structureInformationSaver);
    }

    /**
     *
     * @param m
     * @param projectAclId
     * @param userId
     */
    protected void updateMaterialOverview(
            Material m,
            Integer projectAclId,
            Integer userId) {
        MaterialEntity mE = new MaterialEntity();
        mE.setMaterialid(m.getId());
        mE.setCtime(m.getCreationTime());
        mE.setMaterialtypeid(m.getType().getId());
        mE.setOwner(userId);
        mE.setProjectid(m.getProjectId());
        mE.setACList(projectAclId);
        em.merge(mE);
    }

    @SuppressWarnings("unchecked")
    public List<StorageClass> loadStorageClasses() {
        storageClasses.clear();
        List<Object> objects = this.em.createNativeQuery(SQL_GET_STORAGE_CLASSES).getResultList();
        for (Object o : objects) {
            Object[] oo = (Object[]) o;
            storageClasses.add(new StorageClass((Integer) oo[0], (String) oo[1]));
        }

        return storageClasses;
    }

    private StorageClass loadStorageClassById(int id) {
        for (StorageClass sc : storageClasses) {
            if (sc.getId() == id) {
                return sc;
            }
        }
        throw new IllegalArgumentException("Could not load storage class with id: " + id);

    }

    private EntityGraph createEntityGraph() {
        graphBuilder = new MaterialEntityGraphBuilder();
        return graphBuilder.buildEntityGraph(true);
    }
}
