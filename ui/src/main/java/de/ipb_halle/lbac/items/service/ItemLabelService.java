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
package de.ipb_halle.lbac.items.service;

import de.ipb_halle.lbac.items.Code25LabelGenerator;
import java.io.Serializable;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

/**
 *
 * @author fmauz
 */
@Stateless
public class ItemLabelService implements Serializable{

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;
    private String SQL_UPDATE_ITEM_LABEL
            = "UPDATE items SET label=:label WHERE id=:id";
    private String SQL_LOAD_ITEMS_BY_LABEL
            = "SELECT COUNT(id) FROM items WHERE label=:label";

    private Code25LabelGenerator labelGenerator;

    @PostConstruct
    public void init() {
        labelGenerator = new Code25LabelGenerator();
    }

    public String createLabel(int id, Class clazz) {
        return labelGenerator.generateLabel(id);
    }

    public void saveItemLabel(String label, int id) {
        Query q = em.createNativeQuery(SQL_UPDATE_ITEM_LABEL);
        q.setParameter("label", label);
        q.setParameter("id", id);
        q.executeUpdate();
    }

    public boolean isLabelAvailable(String labelToCheck) {
        Query q = em.createNativeQuery(SQL_LOAD_ITEMS_BY_LABEL);
        q.setParameter("label", labelToCheck);
        return ((long) q.getResultList().get(0)) == 0L;
    }

}
