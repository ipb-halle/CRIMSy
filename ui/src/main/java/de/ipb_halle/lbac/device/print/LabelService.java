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
package de.ipb_halle.lbac.device.print;

/**
 * LabelService loads and stores label configuration.
 *
 */
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Stateless
public class LabelService implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    private Logger logger;

    public LabelService() {
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    @PostConstruct
    public void LabelServiceInit() {
        if (em == null) {
            logger.error("Injection failed for EntityManager. @PersistenceContext(name = \"de.ipb_halle.lbac\")");
        }
    }

    /**
     * @param cmap the map of selection criteria
     * @return the list label configurations matching search criteria 
     */
    @SuppressWarnings("unchecked")
    public List<Label> load(Map<String, Object> cmap) {

        CriteriaBuilder builder = this.em.getCriteriaBuilder();
        CriteriaQuery<LabelEntity> criteriaQuery = builder.createQuery(LabelEntity.class);
        Root<LabelEntity> labelRoot = criteriaQuery.from(LabelEntity.class);
        criteriaQuery.select(labelRoot);

        /*
            ToDo: xxxxx really add criteria for Label selection

         */


        List<Label> result = new ArrayList<>();
        for (LabelEntity e : this.em.createQuery(criteriaQuery).getResultList()) {
            result.add(new Label(e));
        }
        return result;
    }

    /**
     * load a Label by id
     *
     * @param id label id
     * @return the Label object
     */
    public Label loadById(Integer id) {
        LabelEntity entity = this.em.find(LabelEntity.class, id);
        return new Label(entity);
    }

    /**
     * delete a Label configuration from the system
     * @param l the label configuration to be deleted
     */
    public void delete(Label l) {
        LabelEntity entity = this.em.find(LabelEntity.class, l.getId());
        this.em.remove(entity);
    }

    /**
     * save a single label configuration object
     *
     * @param l the Label to save
     * @return the persisted Label DTO
     */
    public Label save(Label l) {
        return new Label(
                this.em.merge(l.createEntity()));
    }
}
