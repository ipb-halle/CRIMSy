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
package de.ipb_halle.lbac.material.common.service;

import de.ipb_halle.lbac.material.common.entity.index.IndexTypeEntity;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author fmauz
 */
@Stateless
public class IndexService implements Serializable {

    private static final long serialVersionUID = 1L;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    @SuppressWarnings("unchecked")
    public Map<Integer, String> loadIndexTypes() {
        Map<Integer, String> back = new HashMap<>();
        List<IndexTypeEntity> ies = em.createNativeQuery("select id,name,javaclass from indextypes", IndexTypeEntity.class).getResultList();
        for (IndexTypeEntity ie : ies) {
            back.put(ie.getId(), ie.getName());
        }
        return back;
    }
}
