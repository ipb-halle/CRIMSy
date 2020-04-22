/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.mocks;

import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.service.MaterialHistoryService;
import de.ipb_halle.lbac.material.service.MaterialService;
import de.ipb_halle.lbac.material.bean.save.MaterialEditSaver;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.difference.MaterialComparator;
import de.ipb_halle.lbac.material.subtype.structure.Structure;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author fmauz
 */
@Stateless
public class MaterialServiceMock extends MaterialService {

    @PersistenceContext(name = "de.ipb_halle.lbac")
    protected EntityManager em;

    protected String SQL_INSERT_MOLECULE = "INSERT INTO molecules (id,molecule,format) VALUES(?,?,'V2000')";

    @PostConstruct
    @Override
    public void init() {
        comparator = new MaterialComparator();
        materialHistoryService = new MaterialHistoryService(this);
        editedMaterialSaver = new MaterialEditSaverMock(this);
    }

    @Override
    protected void saveStructureInformation(Material m) {

        int molId = (int) (Math.random() * 100000);
        Structure s = (Structure) m;
        for (IndexEntry ie : s.getIndices()) {
            em.persist(ie.toDbEntity(m.getId(), 0));
        }
        if (s.getMolecule().getStructureModel() != null) {
            Query q = em.createNativeQuery(SQL_INSERT_MOLECULE)
                    .setParameter(1, molId)
                    .setParameter(2, s.getMolecule().getStructureModel());

            q.executeUpdate();

            em.persist(s.createDbEntity(m.getId(), molId));
            s.getMolecule().setId(molId);
        } else {
            em.persist(s.createDbEntity(m.getId(), null));
        }
    }

}
