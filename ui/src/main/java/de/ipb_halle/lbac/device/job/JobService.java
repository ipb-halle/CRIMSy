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
package de.ipb_halle.lbac.device.job;

/**
 * JobService loads, stores and deletes jobs.
 */
import de.ipb_halle.lbac.admission.MemberService;

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
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Stateless
public class JobService implements Serializable {

    private static final long serialVersionUID = 1L;

    public final static String CONDITION_JOBTYPE = "JOBTYPE";
    public final static String CONDITION_QUEUE = "QUEUE";
    public final static String CONDITION_STATUS = "STATUS";

    @Inject
    private MemberService memberService;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    private Logger logger;

    public JobService() {
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    @PostConstruct
    public void JobServiceInit() {
        if (em == null) {
            logger.error("Injection failed for EntityManager. @PersistenceContext(name = \"de.ipb_halle.lbac\")");
        }
    }

    /**
     * simply load all jobs
     */
    public List<Job> load() {
        return load(new HashMap<String, Object> ());
    }

    /**
     * This needs to be complemented by means to select:
     * <ul>
     * <li>jobs by state (PENDING, FAILED, ...)</li>
     * <li>jobs by queue</li>
     * <li>jobs by owner?</li>
     * <li>jobs by date (for job exipiration)</li>
     * </ul>
     * @return the the complete list of jobs
     */
    @SuppressWarnings("unchecked")
    public List<Job> load(Map<String, Object> cmap) {

        CriteriaBuilder builder = this.em.getCriteriaBuilder();
        CriteriaQuery<JobEntity> criteriaQuery = builder.createQuery(JobEntity.class);
        Root<JobEntity> jobRoot = criteriaQuery.from(JobEntity.class);
        criteriaQuery.select(jobRoot);

        List<Predicate> predicates = new ArrayList<Predicate> ();

        if (cmap.get(CONDITION_JOBTYPE) != null) {
            predicates.add(builder.equal(jobRoot.get("jobtype"), cmap.get(CONDITION_JOBTYPE)));
        }

        if (cmap.get(CONDITION_QUEUE) != null) {
            predicates.add(builder.equal(jobRoot.get("queue"), cmap.get(CONDITION_QUEUE)));
        }

        if (cmap.get(CONDITION_STATUS) != null) {
            predicates.add(builder.equal(jobRoot.get("status"), cmap.get(CONDITION_STATUS)));
        }

        criteriaQuery.where(builder.and(predicates.toArray(new Predicate[0])));
        List<Job> result = new ArrayList<>();
        for (JobEntity e : this.em.createQuery(criteriaQuery).getResultList()) {
            result.add(new Job(
                    e,
                    memberService.loadUserById(e.getOwnerId())));
        }
        return result;
    }

    /**
     * load a Job by id 
     *
     * @param id Job Id 
     * @return the Job object
     */
    public Job loadById(Integer id) {
        JobEntity entity = this.em.find(JobEntity.class, id);
        return new Job(
                entity, 
                memberService.loadUserById(entity.getOwnerId()));
    }

    /**
     * remove a job from the database
     * @param p the job DTO
     */
    public void remove(Job j) {
        this.em.remove(this.em.find(JobEntity.class, j.getJobId()));
    }

    /**
     * save a single job object
     *
     * @param j the Job  to save
     * @return the persisted Job DTO
     */
    public Job save(Job j) {
        return new Job(
                this.em.merge(j.createEntity()),
                j.getOwner());
    }
}
