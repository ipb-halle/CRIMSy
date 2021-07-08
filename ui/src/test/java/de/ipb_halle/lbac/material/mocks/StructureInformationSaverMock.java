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
package de.ipb_halle.lbac.material.mocks;

import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.material.structure.StructureInformationSaver;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author fmauz
 */
public class StructureInformationSaverMock extends StructureInformationSaver {

    private static final long serialVersionUID = 1L;

    protected String SQL_INSERT_MOLECULE = "INSERT INTO molecules (id,molecule) VALUES(?,?)";
    private static int molId = 0;

    public StructureInformationSaverMock(EntityManager em) {
        super(em);
    }

    @Override
    public void saveMaterial(Material m) {

        Structure s = (Structure) m;
        for (IndexEntry ie : s.getIndices()) {
            em.persist(ie.toDbEntity(m.getId(), 0));
        }
        if (s.getMolecule() != null && s.getMolecule().getStructureModel() != null) {
            Query q = em.createNativeQuery(SQL_INSERT_MOLECULE)
                    .setParameter(1, molId)
                    .setParameter(2, s.getMolecule().getStructureModel());

            q.executeUpdate();
            s.getMolecule().setId(molId);
            em.persist(s.createEntity());
            molId++;

        } else {
            em.persist(s.createEntity());
        }
    }

    @Override
    public int saveMolecule(String moleculeString) {
        int molId = (int) (Math.random() * 100000);
        Query q = em.createNativeQuery(SQL_INSERT_MOLECULE)
                .setParameter(1, molId)
                .setParameter(2, moleculeString);
        q.executeUpdate();
        return molId;
    }
}
