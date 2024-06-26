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
import de.ipb_halle.lbac.material.common.history.HistoryEntityId;
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
import de.ipb_halle.lbac.material.common.entity.index.MaterialIndexEntryEntity;
import de.ipb_halle.lbac.material.common.entity.storage.StorageConditionMaterialEntity;
import de.ipb_halle.lbac.material.common.entity.storage.StorageEntity;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyNestingService;
import de.ipb_halle.lbac.material.common.StorageClass;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.composition.MaterialCompositionEntity;
import de.ipb_halle.lbac.material.common.search.MaterialSearchConditionBuilder;
import de.ipb_halle.lbac.material.structure.StructureFactory;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.search.SearchResultImpl;
import de.ipb_halle.lbac.search.lang.Attribute;
import de.ipb_halle.crimsy_api.AttributeType;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.DbField;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import de.ipb_halle.lbac.search.lang.OrderDirection;
import de.ipb_halle.lbac.search.lang.SqlBuilder;
import de.ipb_halle.lbac.search.lang.SqlCountBuilder;
import de.ipb_halle.lbac.search.lang.Value;
import de.ipb_halle.lbac.service.NodeService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
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

    private final String SQL_GET_STORAGE = "SELECT materialid,storageClass,description FROM storages WHERE materialid=:mid";
    private final String SQL_GET_STORAGE_CONDITION = "SELECT conditionId,materialid FROM storageconditions_material WHERE materialid=:mid";
    private final String SQL_GET_HAZARDS = "SELECT typeid,materialid,remarks FROM material_hazards WHERE materialid=:mid";
    private final String SQL_GET_INDICES = "SELECT id,materialid,typeid,value,language,rank FROM material_indices WHERE materialid=:mid order by rank";
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
    private final String SQL_GET_STORAGE_CLASSES = "SELECT id,name FROM storageclasses";

    private final String SQL_UPDATE_MATERIAL_ACL
            = "UPDATE materials "
            + "SET aclist_id =:aclid "
            + "WHERE materialid=:mid";

    private final String SQL_DELETE_COMPONENTS
            = "DELETE FROM material_compositions "
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
        storageClasses = loadStorageClasses();
    }

    /**
     * Deactivates a material by setting a flag 'deactivated=true'. The material
     * will still remain in the database and can be reactivated. The operation
     * will be saved in the history
     *
     * @param materialID
     * @param actor
     */
    public void deactivateMaterial(int materialID, User actor) {
        em.createNativeQuery(SQL_DEACTIVATE_MATERIAL)
                .setParameter("mid", materialID)
                .executeUpdate();

        MaterialHistoryEntity histEntity = new MaterialHistoryEntity();
        histEntity.setAction("DELETE");
        histEntity.setId(new HistoryEntityId(materialID, new Date(), actor.getId()));
        this.em.persist(histEntity);
    }

    /**
     * loads the amount of found readable materials for the given search
     * request.
     *
     * @param request
     * @return
     */
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
        Long bi = (Long) q.getResultList().get(0);
        return bi.intValue();
    }

    /**
     * Loads the materials which are readable and matches the criteria of the
     * request. It is advised to limit the result to prevent heavy load.
     *
     * @param request
     * @return
     */
    public SearchResult loadReadableMaterials(SearchRequest request) {
        try {
            EntityGraph graph = createEntityGraph();
            SearchResult result = new SearchResultImpl(nodeService.getLocalNode());
            SqlBuilder sqlBuilder = new SqlBuilder(graph);

            MaterialSearchConditionBuilder materialBuilder = new MaterialSearchConditionBuilder(graph, "materials");
            Condition con = materialBuilder.convertRequestToCondition(request, ACPermission.permREAD);
            String sql = sqlBuilder.query(con, createOrderList());
            Query q = em.createNativeQuery(sql, MaterialEntity.class);
            q.setFirstResult(request.getFirstResult());
            q.setMaxResults(request.getMaxResults());
            for (Value param : sqlBuilder.getValueList()) {
                q.setParameter(param.getArgumentKey(), param.getValue());

            }
            @SuppressWarnings("unchecked")
            List<MaterialEntity> entities = q.getResultList();
            for (MaterialEntity me : entities) {
                Material material = loadMaterial(me, request.getUser());
                result.addResult(material);
            }
            return result;
        } catch (Exception e) {
            logger.error("loadReadableMaterials() caught an exception:", (Throwable) e);
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

    /**
     * Gets all materialnames which matches the pattern %name% and are readable
     * by the user
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
     * Loads all indices of a material (indextype == 1)
     *
     * @param materialid
     * @return
     */
    @SuppressWarnings("unchecked")
    public List<MaterialName> loadMaterialNamesById(int materialid) {
        List<MaterialName> names = new ArrayList<>();
        Query query = em.createNativeQuery(SQL_GET_INDICES, MaterialIndexEntryEntity.class);
        query.setParameter("mid", materialid);
        List<MaterialIndexEntryEntity> entities = query.getResultList();
        for (MaterialIndexEntryEntity entity : entities) {
            if (entity.getTypeid() == 1) {
                names.add(new MaterialName(entity.getValue(), entity.getLanguage(), entity.getRank()));
            }
        }
        return names;
    }

    /**
     * Loads all indices of a material (indextype > 1)
     *
     * @param materialid
     * @return
     */
     @SuppressWarnings("unchecked")
    private List<IndexEntry> loadMaterialIndicesById(int materialid) {
        List<IndexEntry> indices = new ArrayList<>();
        Query query = em.createNativeQuery(SQL_GET_INDICES, MaterialIndexEntryEntity.class);
        query.setParameter("mid", materialid);
        List<MaterialIndexEntryEntity> entities = query.getResultList();
        for (MaterialIndexEntryEntity entity : entities) {
            if (entity.getTypeid() > 1) {
                indices.add(new IndexEntry(entity.getTypeid(), entity.getValue(), entity.getLanguage()));
            }
        }
        return indices;
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
            logger.error("loadHistoryOfMaterial() caught an exception:", (Throwable) e);
            return new MaterialHistory();
        }
    }

    private Material loadMaterial(MaterialEntity entity, User user) {
        Material material = MaterialType.getTypeById(entity.getMaterialtypeid())
                .getFactory()
                .createLoader()
                .loadMaterial(entity, em, this, taxonomyService, tissueService, aclService, user);

        material.setACList(aclService.loadById(entity.getACList()));
        material.setOwner(memberService.loadUserById(entity.getOwner()));
        material.getDetailRights().addAll(loadDetailRightsOfMaterial(material.getId()));
        material.setNames(loadMaterialNamesById(material.getId()));
        material.setIndices(loadMaterialIndicesById(material.getId()));
        material.setHistory(materialHistoryService.loadHistoryOfMaterial(material.getId()));
        material.setCreationTime(entity.getCtime());

        material.setStorageInformation(loadStorageClassInformation(entity.getMaterialid()));
        material.setHazards(loadHazardInformation(entity.getMaterialid()));
        return material;
    }

    /**
     *
     * @param id
     * @return
     */
    public Material loadMaterialById(int id) {
        MaterialEntity entity = em.find(MaterialEntity.class, id);
        return loadMaterial(entity, null);
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
        editedMaterialSaver.saveComponentDifferences();
        editedMaterialSaver.saveEditedSequence();

        deleteExistingComponents(newMaterial);
        saveComponents(newMaterial);

    }

    private void deleteExistingComponents(Material m) {
        Query q = em.createNativeQuery(SQL_DELETE_COMPONENTS);
        q.setParameter("mid", m.getId());
        q.executeUpdate();
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
        saveIndices(m);
        saveMaterialHazards(m);
        saveStorageConditions(m);
        saveDetailRightsFromTemplate(m, detailTemplates);
        saveComponents(m);

        //Save specific attributes for the subtype
        m.getType().getFactory().createSaver().saveMaterial(m, em);

    }

    private void saveComponents(Material m) {
        for (MaterialCompositionEntity mce : m.createCompositionEntities()) {
            em.merge(mce);
        }
    }

    private void saveIndices(Material m) {
        for (IndexEntry ie : m.getIndices()) {
            em.persist(ie.toDbEntity(m.getId(), 0));
        }

    }

    /**
     *
     * @param m
     * @param projectAclId
     * @param detailTemplates
     * @param owner
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

    public void setEditedMaterialSaver(MaterialEditSaver editedMaterialSaver) {
        this.editedMaterialSaver = editedMaterialSaver;
    }

    public void setStructureInformationSaver(StructureInformationSaver structureInformationSaver) {
        StructureFactory factory = (StructureFactory) MaterialType.STRUCTURE.getFactory();
        factory.setSaver(structureInformationSaver);
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

    public ACListService getAcListService() {
        return aclService;
    }

    public MaterialEditSaver getEditedMaterialSaver() {
        return editedMaterialSaver;
    }

    public EntityManager getEm() {
        return em;
    }
}
