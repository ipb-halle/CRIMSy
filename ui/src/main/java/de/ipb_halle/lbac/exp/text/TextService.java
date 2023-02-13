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
package de.ipb_halle.lbac.exp.text;

/**
 * TextService handles the specific demands for 
 * storing and retrieving text data.
 */
import de.ipb_halle.lbac.exp.Experiment;
import de.ipb_halle.lbac.exp.ExpRecord;
import de.ipb_halle.lbac.exp.ExpRecordEntity;

import java.io.Serializable;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class TextService implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

        
    /**
     * load an Text record by id
     *
     * @param id Text Id
     * @return the Text object
     */
    public Text loadTextById(Experiment experiment, ExpRecordEntity expRecordEntity) {
        TextEntity e = this.em.find(TextEntity.class, expRecordEntity.getExpRecordId());
        Text text = new Text();
        text.setExperiment(experiment);
        text.setExpRecordEntity(expRecordEntity);
        text.setText(e.getText());
        return text;
    }

    /**
     * save a single experiment object
     *
     * @param rec the experiment record to save
     * @return the persisted Experiment DTO
     */
    public Text saveText(ExpRecord rec) {
        Text text = (Text) rec;
        this.em.merge(text.createEntity());
        return text;
    }
}
