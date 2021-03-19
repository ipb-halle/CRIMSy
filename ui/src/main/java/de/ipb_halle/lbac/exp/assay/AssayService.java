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
 * AssayService handles the specific demands for storing and retrieving assay
 * data.
 */
import de.ipb_halle.lbac.datalink.LinkedDataEntity;
import de.ipb_halle.lbac.datalink.LinkedData;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.exp.Experiment;
import de.ipb_halle.lbac.exp.ExpRecord;
import de.ipb_halle.lbac.exp.ExpRecordEntity;
import de.ipb_halle.lbac.items.service.ItemService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Stateless
public class AssayService implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    /**
     * load an Assay record by id
     *
     * @param id Assay Id
     * @return the Assay object
     */
    public Assay loadAssayById(
            Experiment experiment,
            ExpRecordEntity expRecordEntity,
            User user) {
        AssayEntity e = this.em.find(AssayEntity.class, expRecordEntity.getExpRecordId());
        Assay assay = new Assay(e);
        assay.setExperiment(experiment);
        assay.setExpRecordEntity(expRecordEntity);

        return assay;
    }

    /**
     * save a single experiment object
     *
     * @param r the experiment to save
     * @return the persisted Experiment DTO
     */
    public Assay saveAssay(ExpRecord rec) {
        Assay assay = (Assay) rec;
        this.em.merge(assay.createEntity());
        return assay;
    }

}
