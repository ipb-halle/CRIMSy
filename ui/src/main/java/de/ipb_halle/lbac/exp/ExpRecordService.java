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
 * ExpRecordService provides service to load, save, update experiment 
 * record entities. As <code>ExpRecord</code> is abstract, it also 
 * works as a one stop shop for all other record types and delegates 
 * work to specific other service classes.
 * 
 * The current implementation is rather a mock implementation as many 
 * important aspects (permissions, history, filtering, ...) are missing.
 */
import de.ipb_halle.lbac.exp.assay.AssayService;
import de.ipb_halle.lbac.exp.text.TextService;

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
public class ExpRecordService implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    @Inject
    private ExperimentService experimentService;

    @Inject
    private AssayService assayService;

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
     * NOTE: This method must be enhanced to filter the list of results. This 
     * will change the method signature!
     *
     * @return the list of ExpRecords
     */
    @SuppressWarnings("unchecked")
    public List<ExpRecord> load() {

        CriteriaBuilder builder = this.em.getCriteriaBuilder();
        CriteriaQuery<ExpRecordEntity> criteriaQuery = builder.createQuery(ExpRecordEntity.class);
        Root<ExpRecordEntity> expRecordRoot = criteriaQuery.from(ExpRecordEntity.class);
        criteriaQuery.select(expRecordRoot);

        /*
         * add filter criteria 
         */

        List<ExpRecord> result = new ArrayList<ExpRecord> ();
        for(ExpRecordEntity e :  this.em.createQuery(criteriaQuery).getResultList()) {
            Experiment experiment = this.experimentService.loadById(e.getExperimentId());
            switch(e.getType()) {
                case ASSAY :
                    result.add(this.assayService.loadAssayById(experiment, e)); 
                    break;
                case TEXT :
                    result.add(this.textService.loadTextById(experiment, e));
                    break;
                default : 
                    throw new UnsupportedOperationException("load(): invalid ExpRecord.type");
            }
        }
        return result;
    }

    /**
     * load an experiment record by id
     *
     * @param id experiment record Id
     * @return the ExpRecord object
     */
    public ExpRecord loadById(Long id) {
        ExpRecordEntity e = this.em.find(ExpRecordEntity.class, id);
        if (e == null) {
            return null;
        }
        Experiment experiment = this.experimentService.loadById(e.getExperimentId());

        switch(e.getType()) {
            case ASSAY :
                return this.assayService.loadAssayById(experiment, e); 
            case TEXT :
                return this.textService.loadTextById(experiment, e);
        }
        throw new UnsupportedOperationException("loadById(): invalid ExpRecord.type");
    }

    /**
     * save a single experiment object
     *
     * @param r the experiment to save
     * @return the persisted Experiment DTO
     */
    public ExpRecord save(ExpRecord record) {
        ExpRecordEntity e = this.em.merge(record.createExpRecordEntity());
        record.setExpRecordEntity(e);
        record.setExperiment(this.experimentService.save(record.getExperiment()));
        switch(e.getType()) {
            case ASSAY :
                return this.assayService.saveAssay(record);
            case TEXT :
                return this.textService.saveText(record);
        } 
        throw new UnsupportedOperationException("save(): invalid ExpRecord.type");
    }
}
