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
package de.ipb_halle.lbac.material.sequence.search;

/**
 * CollectionService provides service to load, save, update collections.
 */
import de.ipb_halle.lbac.EntityManagerService;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@Stateless
public class InnerDatabaseTransactionService {

    private final String SQL_LOAD_PARAMETER
            = "INSERT INTO temp_search_parameter (processid,field,value)"
            + " VALUES ('%s','%s','%s')";

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    public void innerDatabaseAction() {
        em.createNativeQuery(String.format(
                SQL_LOAD_PARAMETER,
                UUID.randomUUID(),
                "field0",
                "value")).executeUpdate();

    }
}
