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
 * ExperimentService provides service to load, save, update experiment entities.
 *
 * The current implementation is rather a mock implementation as many important
 * aspects (permissions, history, filtering, ...) are missing.
 */
import de.ipb_halle.lbac.service.ACListService;
import de.ipb_halle.lbac.service.MemberService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

// import javax.persistence.Persistence;
@Stateless
public class ExperimentService implements Serializable {

    private static final long serialVersionUID = 1L;

    public final static String TEMPLATE_FLAG = "TEMPLATE_FLAG";

    private final static String SQL_LOAD = "SELECT "
            + "e.experimentid, "
            + "e.code, "
            + "e.description, "
            + "e.template, "
            + "e.aclist_id, "
            + "e.ownerid, "
            + "e.ctime "
            + "FROM experiments AS e "
            + "WHERE (e.template = :TEMPLATE_FLAG OR :TEMPLATE_FLAG IS NULL) "
            + "ORDER BY e.code";

    @Inject
    private ACListService aclistService;

    @Inject
    private MemberService memberService;

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
     * build
     */
    public Query createExperimentQuery(String rawSql, Map<String, Object> cmap, Class targetClass) {
        Query q;
        if (targetClass == null) {
            q = this.em.createNativeQuery(rawSql);
        } else {
            q = this.em.createNativeQuery(rawSql, targetClass);
        }

        return q.setParameter(TEMPLATE_FLAG, cmap.getOrDefault(TEMPLATE_FLAG, null));
    }

    /**
     * @param cmap map of query criteria
     * @return the list of experiments
     */
    @SuppressWarnings("unchecked")
    public List<Experiment> load(Map<String, Object> cmap) {
        List<Experiment> result = new ArrayList<>();
        Query q = createExperimentQuery(SQL_LOAD,
                (cmap == null) ? new HashMap<String, Object>() : cmap,
                ExperimentEntity.class);
        // q.setParameter("USERID", xxxxx);
        // q.setFirstResult();
        // q.setMaxResults();

        for (ExperimentEntity e : (List<ExperimentEntity>) q.getResultList()) {
            result.add(new Experiment(
                    e,
                    aclistService.loadById(e.getACListId()),
                    memberService.loadUserById(e.getOwnerId())));
        }
        return result;
    }

    /**
     * load an experiment by id
     *
     * NOTE: the Experiment DTO does NOT include its experiment records. Records
     * MUST be handled separately.
     *
     * @param id experiment Id
     * @return the Experiment object
     */
    public Experiment loadById(Integer id) {
        ExperimentEntity entity = this.em.find(ExperimentEntity.class, id);
        return new Experiment(
                entity, aclistService.loadById(entity.getACListId()),
                memberService.loadUserById(entity.getOwnerId()));
    }

    /**
     * save a single experiment object
     *
     * @param e the experiment to save
     * @return the persisted Experiment DTO
     */
    public Experiment save(Experiment e) {
        return new Experiment(
                this.em.merge(e.createEntity()),
                e.getACList(),
                e.getOwner());
    }
}
