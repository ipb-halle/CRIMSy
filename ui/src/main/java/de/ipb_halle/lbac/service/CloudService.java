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
package de.ipb_halle.lbac.service;

/**
 * CloudService provides service to load, save, update cloud entities.
 */
import de.ipb_halle.lbac.entity.Cloud;
import de.ipb_halle.lbac.entity.CloudEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Stateless
public class CloudService implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    private Logger logger;

    public CloudService() {
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    @PostConstruct
    public void CloudServiceInit() {
        if (em == null) {
            logger.error("Injection failed for EntityManager. @PersistenceContext(name = \"de.ipb_halle.lbac\")");
        }
    }

    /**
     * Load a list of nodes from the local database.
     *
     * @return the list of clouds, this node knows about
     */
    @SuppressWarnings("unchecked")

    public List<Cloud> load() {

        CriteriaBuilder builder = this.em.getCriteriaBuilder();
        CriteriaQuery<CloudEntity> criteriaQuery = builder.createQuery(CloudEntity.class);
        Root<CloudEntity> cloudRoot = criteriaQuery.from(CloudEntity.class);
        criteriaQuery.select(cloudRoot);
        List<Cloud> result = new ArrayList<Cloud> ();
        for(CloudEntity e :  this.em.createQuery(criteriaQuery).getResultList()) {
            result.add(new Cloud(e));
        }
        return result;
    }

    /**
     * load a cloud by id
     *
     * @param id cloud Id
     * @return the Cloud object
     */
    public Cloud loadById(Long id) {
        return new Cloud(this.em.find(CloudEntity.class, id));
    }

    /**
     * load a cloud by name (returns the first object found)
     * 
     * @param n name of the cloud
     * @return the Cloud object
     */
    public Cloud loadByName(String n) {
        CriteriaBuilder builder = this.em.getCriteriaBuilder();
        CriteriaQuery<CloudEntity> criteriaQuery = builder.createQuery(CloudEntity.class);
        Root<CloudEntity> cloudRoot = criteriaQuery.from(CloudEntity.class);
        criteriaQuery.select(cloudRoot);
        criteriaQuery.where(builder.equal(cloudRoot.get("name"), n)); 
        try {
            return new Cloud(this.em.createQuery(criteriaQuery).getSingleResult());
        } catch(NoResultException e) {
            // ignore
        }
        return null;
    }

    /**
     * save a single cloud object
     *
     * @param c the code to save
     * @return the managed Cloud object
     */
    public Cloud save(Cloud c) {
        return new Cloud(this.em.merge(c.createEntity()));
    }
}
