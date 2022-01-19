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
package de.ipb_halle.lbac.material.sequence.search.service;

import java.util.UUID;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author fmauz
 */
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@Stateless
public class SearchParameterService {
    private final String SQL_DELETE_PARAMETER = "DELETE from temp_search_parameter WHERE processid=:id";
    private final String SQL_INSERT_PARAMETER = "INSERT INTO temp_search_parameter(processid,parameter) VALUES(cast(:processid as UUID),cast(:parameter as jsonb))";

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    /**
     * Saves a search parameter in the database table "temp_search_parameter".
     * 
     * @param processid
     * @param parameterAsJson
     * @throws Exception
     */
    public void saveParameter(String processid, String parameterAsJson) throws Exception {
        Query q = em.createNativeQuery(SQL_INSERT_PARAMETER);
        q.setParameter("processid", processid);
        q.setParameter("parameter", parameterAsJson);
        q.executeUpdate();
    }

    /**
     * Removes a search parameter from the database table "temp_search_parameter".
     * 
     * @param processid
     */
    public void removeParameter(UUID processid) {
        this.em.createNativeQuery(SQL_DELETE_PARAMETER).setParameter("id", processid).executeUpdate();
    }
}
