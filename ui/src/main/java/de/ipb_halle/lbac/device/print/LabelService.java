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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.Query;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Stateless
public class LabelService implements Serializable {

    private static final long serialVersionUID = 1L;

    public final static String LABEL_TYPE = "LABEL_TYPE";
    public final static String PRINTER_MODEL = "PRINTER_MODEL";

    private final static String SQL_LOAD = "SELECT DISTINCT "
        + "l.id, l.name, l.description, l.labeltype, l.printermodel, l.config "
        + "FROM labels AS l WHERE "
        + "(l.labeltype = :LABEL_TYPE OR :LABEL_TYPE IS NULL) "
        + "(l.printermodel = :PRINTER_MODEL OR :PRINTER_MODEL IS NULL) "
        + "ORDER BY l.name";

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
     * build query
     */
    public Query createLabelQuery(String rawSql, Map<String, Object> cmap, Class targetClass) {
        Query q;
        if (targetClass == null) {
            q = this.em.createNativeQuery(rawSql);
        } else {
            q = this.em.createNativeQuery(rawSql, targetClass);
        }

        q.setParameter(LABEL_TYPE, cmap.getOrDefault(LABEL_TYPE, null));
        q.setParameter(PRINTER_MODEL, cmap.getOrDefault(PRINTER_MODEL, null));
        return q;
    }

    /**
     * @param cmap the map of selection criteria
     * @return the list label configurations matching search criteria 
     */
    @SuppressWarnings("unchecked")
    public List<Label> load(Map<String, Object> cmap) {

        Query q = createLabelQuery(SQL_LOAD,
            (cmap == null) ? new HashMap<String, Object> () : cmap,
            LabelEntity.class);
        // q.setFirstResult();
        // q.setMaxResults();

        List<Label> result = new ArrayList<>();
        for (LabelEntity e :  (List<LabelEntity>) q.getResultList()) {
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
