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
package de.ipb_halle.lbac.exp.assay;

/**
 * AssayService handles the specific demands for 
 * storing and retrieving assay data.
 */
import de.ipb_halle.lbac.exp.Experiment;
import de.ipb_halle.lbac.exp.ExpRecord;
import de.ipb_halle.lbac.exp.ExpRecordEntity;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.service.MaterialService;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.service.ItemService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Stateless
public class AssayService implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    @Inject
    private ItemService itemService;

    @Inject
    private MaterialService materialService;

    /**
     * Load a list of AssayRecords for a given Assay. 
     *
     * @return the list of AssayRecords
     */
    @SuppressWarnings("unchecked")
    public List<AssayRecord> loadAssayRecords(Assay assay) {

        CriteriaBuilder builder = this.em.getCriteriaBuilder();
        CriteriaQuery<AssayRecordEntity> criteriaQuery = builder.createQuery(AssayRecordEntity.class);
        Root<AssayRecordEntity> root = criteriaQuery.from(AssayRecordEntity.class);
        criteriaQuery.select(root);
        criteriaQuery.where(builder.equal(root.get("exprecordid"), assay.getExpRecordId()));

        List<AssayRecord> result = new ArrayList<AssayRecord> ();
        for(AssayRecordEntity e :  this.em.createQuery(criteriaQuery).getResultList()) {

            Material material = null;
            Item item = null;
            if (e.getMaterialId() != null) {
                material = this.materialService.loadMaterialById(e.getMaterialId()); 
            }
            if (e.getItemId() != null) {
                item = this.itemService.loadItemById(e.getItemId());
            }
            result.add(new AssayRecord(e, assay,  material, item)); 
        }
        return result;
    }
        
    /**
     * load an Assay record by id
     *
     * @param id Assay Id
     * @return the Assay object
     */
    public Assay loadAssayById(Experiment experiment, ExpRecordEntity expRecordEntity) {
        AssayEntity e = this.em.find(AssayEntity.class, expRecordEntity.getExpRecordId());
        Material material = null;
        if ( e.getTargetId() != null) {
            material = materialService.loadMaterialById(e.getTargetId());
        }
        Assay assay = new Assay(e, material);
        assay.setExperiment(experiment);
        assay.setExpRecordEntity(expRecordEntity);

        return assay.setRecords(loadAssayRecords(assay));
    }

    /**
     * save a single experiment object
     *
     * @param r the experiment to save
     * @return the persisted Experiment DTO
     */
    public Assay saveAssay(ExpRecord rec) {
        Assay assay = (Assay) rec;
        AssayEntity e = this.em.merge(assay.createEntity());
        assay.setRecords(saveAssayRecords(assay));
        return assay;
    }

    private List<AssayRecord> saveAssayRecords(Assay assay) {
        List <AssayRecord> records = new ArrayList<AssayRecord> ();
        for (AssayRecord ar : assay.getRecords()) {
            AssayRecordEntity e = this.em.merge(ar.createEntity());
            records.add(new AssayRecord(e, assay, ar.getMaterial(), ar.getItem())); 
        }
        return records;
    }
}
