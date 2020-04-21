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
package de.ipb_halle.lbac.exp.service;

/**
 * ExperimentService provides service to load, save, update experiment entities.
 * 
 * The current implementation is rather a mock implementation as many 
 * important aspects (permissions, history, filtering, ...) are missing.
 */
import de.ipb_halle.lbac.exp.Experiment;
import de.ipb_halle.lbac.exp.entity.ExperimentEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

// import javax.persistence.Persistence;
@Stateless
public class ExperimentService implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    private Logger logger;

    public ExperimentService() {
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    @PostConstruct
    public void ExperimentServiceInit() {
        if (em == null) {
            logger.error("Injection failed for EntityManager. @PersistenceContext(name = \"de.ipb_halle.lbac\")");
        }
    }

    /**
     * @return the the complete list of experiments
     */
    @SuppressWarnings("unchecked")
    public List<Experiment> load() {

        CriteriaBuilder builder = this.em.getCriteriaBuilder();
        CriteriaQuery<ExperimentEntity> criteriaQuery = builder.createQuery(ExperimentEntity.class);
        Root<ExperimentEntity> experimentRoot = criteriaQuery.from(ExperimentEntity.class);
        criteriaQuery.select(experimentRoot);
        List<Experiment> result = new ArrayList<Experiment> ();
        for(ExperimentEntity e :  this.em.createQuery(criteriaQuery).getResultList()) {
            result.add(new Experiment(e));
        }
        return result;
    }

    /**
     * load an experiment by id
     *
     * @param id experiment Id
     * @return the Experiment object
     */
    public Experiment loadById(Integer id) {
        return new Experiment(this.em.find(ExperimentEntity.class, id));
    }

    /**
     * save a single experiment object
     *
     * @param c the experiment to save
     * @return the persisted Experiment DTO
     */
    public Experiment save(Experiment c) {
        return new Experiment(this.em.merge(c.createEntity()));
    }
}
