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

import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.entity.index.IndexTypeEntity;

import java.io.Serializable;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.ipb_halle.lbac.material.common.entity.index.MaterialIndexEntryEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

/**
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

    public Map<Integer, List<MaterialName>> createMaterialNamesMapFromTaxonomyIds(List<Integer> taxonomieIdsList) {
        String queryForGettingMaterialNames = "select id, materialid, typeid, value, language, rank " +
                "from material_indices " +
                "where typeid =1 " +
                "and materialid in(:taxonomieIdsList) ;";
        //we send the query with a list of Ids using MaterialIndexEntryEntity with given typeid of 1 ()
        Query query = em.createNativeQuery(queryForGettingMaterialNames, MaterialIndexEntryEntity.class);

        //setting of TaxonomyIds List as query variable
        query.setParameter("taxonomieIdsList", taxonomieIdsList);
        //getting results
        List<MaterialIndexEntryEntity> materialNameEntires = (List<MaterialIndexEntryEntity>) query.getResultList();

        Map<Integer, List<MaterialName>> resultMap = new HashMap<>();

        //building a resulting HashMap with if condition(if the id of material is already in the map, then the materialName should be added to the List<MaterialName> materialNames)
        for (MaterialIndexEntryEntity materialIndexEntry : materialNameEntires) {
            MaterialName materialName = new MaterialName(materialIndexEntry.getValue(), materialIndexEntry.getLanguage(), materialIndexEntry.getRank());

            if (!resultMap.containsKey(materialIndexEntry.getId())) {
                resultMap.put(materialIndexEntry.getId(), new ArrayList<>());
            }
            resultMap.get(materialIndexEntry.getId()).add(materialName);
        }

        return resultMap;
    }
}
