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
package de.ipb_halle.lbac.material.structure;

import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.IndexEntry;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author fmauz
 */
public class StructureInformationSaver {

    protected String SQL_INSERT_MOLECULE = "INSERT INTO molecules (molecule,format) VALUES(CAST ((:molecule) AS molecule),:format) RETURNING id";
    protected EntityManager em;

    public StructureInformationSaver(EntityManager em) {
        this.em = em;
    }

    /**
     *
     * @param m
     */
    public void saveStructureInformation(Material m) {
        Structure s = (Structure) m;
        for (IndexEntry ie : s.getIndices()) {
            em.persist(ie.toDbEntity(m.getId(), 0));
        }
        if (s.getMolecule().getStructureModel() != null) {
            Query q = em.createNativeQuery(SQL_INSERT_MOLECULE)
                    .setParameter("molecule", s.getMolecule().getStructureModel())
                    .setParameter("format", s.getMolecule().getModelType().toString());

            int molId = (int) q.getSingleResult();
            em.persist(s.createDbEntity(m.getId(), molId));
        } else {
            em.persist(s.createDbEntity(m.getId(), null));
        }
    }
}
