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
package de.ipb_halle.job;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * JobService loads, stores and deletes job entities.
 */
public abstract class JobService<T extends Job> {

    public final static String SETTING_JOB_SECRET = "SETTING_JOB_SECRET";
    public final static String JOB_SECRET_QUERY = "SELECT value FROM info WHERE key='SETTING_JOB_SECRET'";

    public final static String CONDITION_JOBTYPE = "JOBTYPE";
    public final static String CONDITION_QUEUE = "QUEUE";
    public final static String CONDITION_STATUS = "STATUS";
    public final static String CONDITION_OWNERID = "OWNERID";

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    private Logger logger = LoggerFactory.getLogger(this.getClass());;

    @PostConstruct
    private void JobServiceInit() {
        if (em == null) {
            logger.error("Injection failed for EntityManager. @PersistenceContext(name = \"de.ipb_halle.lbac\")");
        }
    }

    /**
     * Obtain the job secret from the database 
     */
    public String obtainJobSecret() {
        try {
            return (String) em.createNativeQuery(JOB_SECRET_QUERY).getSingleResult();
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    /**
     * load all jobs
     */
    public List<T> loadAllJobs() {
        return loadJobs(new HashMap<String, Object>());
    }

    /**
     * Loads jobs from the database according to the given conditions.
     * 
     * @param cmap map of conditions
     * @return selected jobs
     */
    public List<T> loadJobs(Map<String, Object> cmap) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<JobEntity> criteriaQuery = builder.createQuery(JobEntity.class);
        Root<JobEntity> jobRoot = criteriaQuery.from(JobEntity.class);
        criteriaQuery.select(jobRoot);

        List<Predicate> predicates = buildPredicatesFromConditions(cmap, builder, jobRoot);
        criteriaQuery.where(builder.and(predicates.toArray(new Predicate[0])));
        return getResultsFromQuery(criteriaQuery);
    }

    /**
     * Builds a Job object from a JobEntity. Can be overwritten by subclasses
     * to better handle the specific needs of the subclass (i.e. to load the owner).
     *
     * @param the JobEntity
     * @return the Job object
     */

    protected abstract T buildJob(JobEntity entity);
/* {
        if (entity != null) {
            return new Job(entity);
        }
        return null;
    }
*/
    protected List<Predicate> buildPredicatesFromConditions(Map<String, Object> cmap, CriteriaBuilder builder,
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

    private List<T> getResultsFromQuery(CriteriaQuery<JobEntity> criteriaQuery) {
        List<T> result = new ArrayList<>();
        for (JobEntity e : em.createQuery(criteriaQuery).getResultList()) {
            result.add(buildJob(e));
        }
        return result;
    }

    /**
     * Loads jobs from the database with a jobdate older than the given date and
     * according to the given conditions.
     * 
     * @param date
     * @param cmap map of conditions
     * @return selected jobs
     */
    public List<T> loadJobsOlderThan(Date date, Map<String, Object> cmap) {
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
     * @param id JobEntity Id
     * @return the Job object or null in case the entity does not exist
     */
    public T loadJobById(Integer id) {
        return buildJob(em.find(JobEntity.class, id));
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
     * @param id the job's id
     */
    public void removeJob(Integer id) {
        if (id == null) {
            return;
        }

        JobEntity entity = em.find(JobEntity.class, id);
        if (entity != null) {
            em.remove(entity);
        }
    }

    /**
     * save a single job object
     *
     * @param job the JobEntity to save
     * @return the Job
     */
    public T saveJob(T job) {
        return buildJob(saveEntity(job));
    }

    protected JobEntity saveEntity(Job job) {
        return em.merge(job.createEntity());
    }
}
