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

import de.ipb_halle.lbac.admission.MemberService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
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

/**
 * JobService loads, stores and deletes jobs.
 */
@Stateless
public class JobService implements Serializable {
    private static final long serialVersionUID = 1L;

    public final static String CONDITION_JOBTYPE = "JOBTYPE";
    public final static String CONDITION_QUEUE = "QUEUE";
    public final static String CONDITION_STATUS = "STATUS";
    public final static String CONDITION_OWNERID = "OWNERID";

    @Inject
    private MemberService memberService;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    private Logger logger = LogManager.getLogger(this.getClass().getName());;

    @PostConstruct
    public void JobServiceInit() {
        if (em == null) {
            logger.error("Injection failed for EntityManager. @PersistenceContext(name = \"de.ipb_halle.lbac\")");
        }
    }

    /**
     * load all jobs
     */
    public List<Job> loadAllJobs() {
        return loadJobs(new HashMap<String, Object>());
    }

    /**
     * Loads jobs from the database according to the given conditions.
     * 
     * @param cmap map of conditions
     * @return selected jobs
     */
    public List<Job> loadJobs(Map<String, Object> cmap) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<JobEntity> criteriaQuery = builder.createQuery(JobEntity.class);
        Root<JobEntity> jobRoot = criteriaQuery.from(JobEntity.class);
        criteriaQuery.select(jobRoot);

        List<Predicate> predicates = buildPredicatesFromConditions(cmap, builder, jobRoot);
        criteriaQuery.where(builder.and(predicates.toArray(new Predicate[0])));
        return getResultsFromQuery(criteriaQuery);
    }

    private List<Predicate> buildPredicatesFromConditions(Map<String, Object> cmap, CriteriaBuilder builder,
            Root<JobEntity> jobRoot) {
        List<Predicate> predicates = new ArrayList<>();
        if (cmap.get(CONDITION_JOBTYPE) != null) {
            predicates.add(builder.equal(jobRoot.get("jobtype"), cmap.get(CONDITION_JOBTYPE)));
        }

        if (cmap.get(CONDITION_QUEUE) != null) {
            predicates.add(builder.equal(jobRoot.get("queue"), cmap.get(CONDITION_QUEUE)));
        }

        if (cmap.get(CONDITION_STATUS) != null) {
            predicates.add(builder.equal(jobRoot.get("status"), cmap.get(CONDITION_STATUS)));
        }

        if (cmap.get(CONDITION_OWNERID) != null) {
            predicates.add(builder.equal(jobRoot.get("ownerid"), cmap.get(CONDITION_OWNERID)));
        }
        return predicates;
    }

    private List<Job> getResultsFromQuery(CriteriaQuery<JobEntity> criteriaQuery) {
        List<Job> result = new ArrayList<>();
        for (JobEntity e : em.createQuery(criteriaQuery).getResultList()) {
            result.add(new Job(e, memberService.loadUserById(e.getOwnerId())));
        }
        return result;
    }

    /**
     * Loads jobs from the database with a jobdate older than the given date and according to the given conditions.
     * 
     * @param date
     * @param cmap map of conditions
     * @return selected jobs
     */
    public List<Job> loadJobsOlderThan(Date date, Map<String, Object> cmap) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<JobEntity> criteriaQuery = builder.createQuery(JobEntity.class);
        Root<JobEntity> jobRoot = criteriaQuery.from(JobEntity.class);
        criteriaQuery.select(jobRoot);

        List<Predicate> predicates = buildPredicatesFromConditions(cmap, builder, jobRoot);
        predicates.add(builder.lessThan(jobRoot.get("jobdate"), date));

        criteriaQuery.where(builder.and(predicates.toArray(new Predicate[0])));
        return getResultsFromQuery(criteriaQuery);
    }

    /**
     * load a Job by id
     *
     * @param id Job Id
     * @return the Job object or null in case the entity does not exist
     */
    public Job loadJobById(Integer id) {
        JobEntity entity = em.find(JobEntity.class, id);
        if (entity == null) {
            return null;
        }
        return new Job(entity, memberService.loadUserById(entity.getOwnerId()));
    }

    /**
     * remove a job from the database
     * 
     * @param job the job DTO
     */
    public void removeJob(Job job) {
        removeJob(job.getJobId());
    }

    /**
     * remove a job from the database
     * 
     * @param jobId the job's id
     */
    public void removeJob(Integer jobId) {
        if (jobId != null) {
            em.remove(em.find(JobEntity.class, jobId));
        }
    }

    /**
     * save a single job object
     *
     * @param job the Job to save
     * @return the persisted Job DTO
     */
    public Job saveJob(Job job) {
        return new Job(em.merge(job.createEntity()), job.getOwner());
    }
}
