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
package de.ipb_halle.lbac;

import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author fmauz
 */
@Stateless
public class EntityManagerService {

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    public void flush() {
        this.em.flush();
    } 

    public void doSqlUpdate(String query) {
        em.createNativeQuery(query).executeUpdate();
    }

    @SuppressWarnings("unchecked")
    public List<Object> doSqlQuery(String query) {
        return em.createNativeQuery(query).getResultList();
    }

    public void deleteUserWithAllMemberships(String userId) {
        this.em.createNativeQuery("delete from memberships where member_id='" + userId + "'").executeUpdate();
    }

    @SuppressWarnings("unchecked")
    public void removeEntity(Class clazz, Object id) {
        this.em.remove(this.em.find(clazz, id));

    }

    public EntityManager getEntityManager() {
        return em;
    }
}
