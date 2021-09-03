/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.biomaterial;

import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.MaterialSaver;
import javax.persistence.EntityManager;

/**
 *
 * @author fmauz
 */
public class TaxonomySaver implements MaterialSaver {

    private final String SQL_SAVE_EFFECTIVE_TAXONOMY = "INSERT INTO effective_taxonomy (taxoid,parentid) VALUES(:tid,:pid)";
    private static final long serialVersionUID = 1L;

    @Override
    public void saveMaterial(Material m, EntityManager em) {
        Taxonomy t = (Taxonomy) m;
        em.persist(t.createEntity());
        for (Taxonomy th : t.getTaxHierachy()) {
            em.createNativeQuery(SQL_SAVE_EFFECTIVE_TAXONOMY)
                    .setParameter("tid", t.getId())
                    .setParameter("pid", th.getId())
                    .executeUpdate();
        }

    }

}
