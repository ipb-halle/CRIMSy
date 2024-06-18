/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2023 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.test;

import java.util.List;
import java.util.Map;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.PersistenceContext;

/**
 *
 * @author fbroda
 */
@Stateless
public class EntityManagerService {

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    @SuppressWarnings("unchecked")
    public List<Object> doSqlQuery(String query) {
        return em.createNativeQuery(query).getResultList();
    }

    public void doSqlUpdate(String query) {
        em.createNativeQuery(query).executeUpdate();
    }

    public void doSqlUpdate(String queryString, Map<String, Object> param) {
        Query query = em.createNativeQuery(queryString);
        for (Map.Entry<String,Object> entry : param.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        query.executeUpdate();
    }

    public void flush() {
        this.em.flush();
    }

    public EntityManager getEntityManager() {
        return em;
    }

    @SuppressWarnings("unchecked")
    public void removeEntity(Class clazz, Object id) {
        this.em.remove(this.em.find(clazz, id));

    }
}
