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
package de.ipb_halle.lbac.exp;

/**
 * ExpRecordService provides service to load, save, update experiment record
 * entities. As <code>ExpRecord</code> is abstract, it also works as a one stop
 * shop for all other record types and delegates work to specific other service
 * classes.
 *
 * The current implementation is rather a mock implementation as many important
 * aspects (permissions, history, filtering, ...) are missing.
 */
import de.ipb_halle.lbac.datalink.LinkedData;
import de.ipb_halle.lbac.datalink.LinkedDataEntity;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.exp.assay.AssayService;
import de.ipb_halle.lbac.exp.text.TextService;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.UnknownItemFactory;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.unknown.UnknownMaterial;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Stateless
public class ExpRecordService implements Serializable {

    private static final long serialVersionUID = 1L;

    public final static String EXPERIMENT_ID = "EXPERIMENT_ID";

    private final static String SQL_LOAD = "SELECT DISTINCT "
            + "r.exprecordid, r.experimentid, r.changetime, r.creationtime, r.next, r.revision, r.type "
            + "FROM exp_records AS r WHERE "
            + "(r.experimentid = :EXPERIMENT_ID OR :EXPERIMENT_ID = -1) "
            + "ORDER BY r.exprecordid";

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    @Inject
    private ACListService aclistService;

    @Inject
    private ExperimentService experimentService;

    @Inject
    private AssayService assayService;

    @Inject
    private ItemService itemService;

    @Inject
    private MaterialService materialService;

    @Inject
    private TextService textService;

    private Logger logger;

    public ExpRecordService() {
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    @PostConstruct
    public void ExpRecordServiceInit() {
        if (em == null) {
            logger.error("Injection failed for EntityManager. @PersistenceContext(name = \"de.ipb_halle.lbac\")");
        }
    }

    /**
     * build
     */
    public Query createExpRecordQuery(String rawSql, Map<String, Object> cmap, Class targetClass) {
        Query q;
        if (targetClass == null) {
            q = this.em.createNativeQuery(rawSql);
        } else {
            q = this.em.createNativeQuery(rawSql, targetClass);
        }

        return q.setParameter(EXPERIMENT_ID, cmap.getOrDefault(EXPERIMENT_ID, Integer.valueOf(-1)));
    }

    public void deleteAssayRecord(long recordId) {
        em.createNativeQuery(
                String.format("DELETE FROM linked_data WHERE recordid = %d", recordId))
                .executeUpdate();
    }

    /**
     * load a list of records according to the query criteria
     *
     * @param cmap map of query criteria
     * @return the list of ExpRecords
     */
    @SuppressWarnings("unchecked")
    public List<ExpRecord> load(Map<String, Object> cmap, User user) {
        List<ExpRecord> result = new ArrayList<ExpRecord>();

        Query q = createExpRecordQuery(SQL_LOAD,
                (cmap == null) ? new HashMap<String, Object>() : cmap,
                ExpRecordEntity.class);
        // q.setParameter("USERID", xxxxx);
        // q.setFirstResult();
        // q.setMaxResults();

        for (ExpRecordEntity e : (List<ExpRecordEntity>) q.getResultList()) {
            Experiment experiment = this.experimentService.loadById(e.getExperimentId());
            ExpRecord record;
            switch (e.getType()) {
                case ASSAY:
                    record = this.assayService.loadAssayById(experiment, e, user);
                    break;
                case TEXT:
                    record = this.textService.loadTextById(experiment, e);
                    break;
                default:
                    throw new UnsupportedOperationException("load(): invalid ExpRecord.type");
            }
            record.setLinkedData(loadLinkedData(record, user));
            result.add(record);
        }
        return result;
    }

    /**
     * load an experiment record by id
     *
     * @param id experiment record Id
     * @return the ExpRecord object
     */
    public ExpRecord loadById(Long id, User user) {
        ExpRecordEntity e = this.em.find(ExpRecordEntity.class, id);
        if (e == null) {
            return null;
        }
        Experiment experiment = this.experimentService.loadById(e.getExperimentId());

        ExpRecord record;
        switch (e.getType()) {
            case ASSAY:
                record = this.assayService.loadAssayById(experiment, e, user);
                break;
            case TEXT:
                record = this.textService.loadTextById(experiment, e);
                break;
            default:
                throw new UnsupportedOperationException("loadById(): invalid ExpRecord.type");
        }
        record.setLinkedData(loadLinkedData(record, user));
        return record;
    }

    /**
     * Load a list of LinkRecords for a given Assay.
     *
     * @param expRecord
     * @param user
     * @return the list of AssayRecords
     */
    @SuppressWarnings("unchecked")
    public List<LinkedData> loadLinkedData(ExpRecord expRecord, User user) {

        // this.logger.info("loadAssayRecords() called");
        CriteriaBuilder builder = this.em.getCriteriaBuilder();
        CriteriaQuery<LinkedDataEntity> criteriaQuery = builder.createQuery(LinkedDataEntity.class);
        Root<LinkedDataEntity> root = criteriaQuery.from(LinkedDataEntity.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get("exprecordid"), expRecord.getExpRecordId()));
        criteriaQuery.orderBy(builder.asc(root.get("rank")));

        List<LinkedData> result = new ArrayList<>();
        for (LinkedDataEntity e : this.em.createQuery(criteriaQuery).getResultList()) {

            Material material = null;
            Item item = null;

            if (e.getItemId() != null) {
                  item = this.itemService.loadItemById(e.getItemId());
                if (!aclistService.isPermitted(ACPermission.permREAD, item, user)) {
                    item = UnknownItemFactory.getInstance(user, GlobalAdmissionContext.getPublicReadACL());
                } 
                material = item.getMaterial();
            } else {
                if (e.getMaterialId() != null) {
                    material = this.materialService.loadMaterialById(e.getMaterialId());
                    if (!aclistService.isPermitted(ACPermission.permREAD, material, user)) {
                        material = UnknownMaterial.createNewInstance(GlobalAdmissionContext.getPublicReadACL());

                    }
                }
            }
            expRecord.setLinkedDataMaxRank(e.getRank());

            if (material != null) {
                result.add(new LinkedData(e, expRecord, material, item));
            }
        }
        expRecord.reIndexLinkedData();
        return result;
    }

    /**
     * order the list of records according to their next field
     */
    public List<ExpRecord> orderList(List<ExpRecord> records) {

        if (records.size() < 2) {
            return records;
        }

        ArrayList<ExpRecord> result = new ArrayList<ExpRecord>();
        ExpRecord first = null;
        ExpRecord last = null;

        boolean changed = true;
        while (changed && (records.size() > 0)) {
            ListIterator<ExpRecord> iter = records.listIterator();
            changed = false;
            while (iter.hasNext()) {
                ExpRecord record = iter.next();
                if ((last == null)
                        || (record.getExpRecordId().equals(last.getNext()))) {
                    // append to the top of the list
                    result.add(record);
                    last = record;
                    iter.remove();
                    changed = true;
                    if (first == null) {
                        first = record;
                    }
                } else {
                    if (first.getExpRecordId().equals(record.getNext())) {
                        // append to the bottom of the list
                        result.add(0, record);
                        first = record;
                        iter.remove();
                        changed = true;
                    }
                }
            }
        }
        if (!changed) {
            this.logger.info("orderList() contains multiple record chains; adding remaining ExpRecords");
            result.addAll(records);
        }
        return result;
    }

    /**
     * save a single experiment record object
     *
     * @param record the experiment record to save
     * @param user
     * @return the persisted Experiment DTO
     */
    public ExpRecord save(ExpRecord record, User user) {
        record = saveOnly(record);
        record.setLinkedData(saveLinkedData(record, user));
        switch (record.getType()) {
            case ASSAY:
                return this.assayService.saveAssay(record);
            case TEXT:
                return this.textService.saveText(record);
        }
        throw new UnsupportedOperationException("save(): invalid ExpRecord.type");
    }

    private List<LinkedData> saveLinkedData(ExpRecord record, User user) {
        List<LinkedData> records = new ArrayList<>();
        Set<Long> idsOfAssayRecords = findAssayRecordsToDelete(record, user);

        for (LinkedData data : record.getLinkedData()) {
            LinkedDataEntity entity = this.em.merge(data.createEntity());
            records.add(new LinkedData(entity, record, data.getMaterial(), data.getItem()));
            idsOfAssayRecords.remove(data.getRecordId());
        }
        for (Long id : idsOfAssayRecords) {
            deleteAssayRecord(id);
        }
        return records;
    }

    private Set<Long> findAssayRecordsToDelete(ExpRecord record, User user) {
        Set<Long> ids = new HashSet<>();
        for (LinkedData ld : loadLinkedData(record, user)) {
            ids.add(ld.getRecordId());
        }
        return ids;

    }

    /**
     * save a only single experiment object (i.e.do not update the type specific
     * data.Used i.e. for updating record ordering.
     *
     * @param record
     * @return the persisted Experiment DTO
     */
    public ExpRecord saveOnly(ExpRecord record) {
        record.incrementRevision();
        record.setExperiment(this.experimentService.save(record.getExperiment()));
        ExpRecordEntity e = this.em.merge(record.createExpRecordEntity());
        record.setExpRecordEntity(e);
        return record;
    }

}
