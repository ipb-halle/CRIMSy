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
 * SOPService provides service to load, save, update experiment entities.
 * 
 * The current implementation is rather a mock implementation as many 
 * important aspects (permissions, history, filtering, ...) are missing.
 */
import de.ipb_halle.lbac.entity.FileObject;
import de.ipb_halle.lbac.file.FileEntityService;

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
public class SOPService implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    @Inject
    private FileEntityService fileEntityService;

    private Logger logger;

    public SOPService() {
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    @PostConstruct
    public void SOPServiceInit() {
        if (em == null) {
            logger.error("Injection failed for EntityManager. @PersistenceContext(name = \"de.ipb_halle.lbac\")");
        }
    }

    /**
     * @return the the complete list of standard operating procedures
     */
    @SuppressWarnings("unchecked")
    public List<SOP> load() {

        CriteriaBuilder builder = this.em.getCriteriaBuilder();
        CriteriaQuery<SOPEntity> criteriaQuery = builder.createQuery(SOPEntity.class);
        Root<SOPEntity> sopRoot = criteriaQuery.from(SOPEntity.class);
        criteriaQuery.select(sopRoot);

        List<SOP> result = new ArrayList<SOP> ();
        for(SOPEntity e :  this.em.createQuery(criteriaQuery).getResultList()) {
            FileObject document = this.fileEntityService.getFileEntity(e.getDocumentId());
            result.add(new SOP(e, document));
        }
        return result;
    }

    /**
     * load an experiment by id
     *
     * @param id experiment Id
     * @return the SOP object
     */
    public SOP loadById(Integer id) {
        SOPEntity entity = this.em.find(SOPEntity.class, id);
        FileObject document = this.fileEntityService.getFileEntity(entity.getDocumentId());
        return new SOP(entity, document);
    }

    /**
     * save a single experiment object
     *
     * @param c the experiment to save
     * @return the persisted SOP DTO
     */
    public SOP save(SOP c) {
        SOPEntity entity = this.em.merge(c.createEntity());
        FileObject document = this.fileEntityService.getFileEntity(entity.getDocumentId());
        return new SOP(entity, document);
    }
}
