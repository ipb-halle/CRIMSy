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
package de.ipb_halle.lbac.material.sequence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@Stateless
public class SearchParameterService implements Serializable {

    private final String SQL_DELETE_PARAMETER = "DELETE from temp_Search_Parameter WHERE processid=:id";
    private static final long serialVersionUID = 1L;

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    public void saveParameter(UUID processid, String[] fields, String[] values) throws JsonProcessingException {
        SearchParameterEntity entity = new SearchParameterEntity();

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();

        for (int i = 0; i < fields.length; i++) {
            root.put(fields[i], values[i]);
        }
        String s = mapper.writeValueAsString(root);
        entity.setProcessid(processid);
        entity.setCdate(new Date());
        entity.setParameter(s);
        em.merge(entity);
    }

    public void removeParameter(UUID processid) {
        this.em.createNativeQuery(SQL_DELETE_PARAMETER)
                .setParameter("id", processid)
                .executeUpdate();
    }
}
