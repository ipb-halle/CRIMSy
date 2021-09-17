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
import de.ipb_halle.lbac.material.common.MaterialIndexSaver;
import de.ipb_halle.lbac.material.common.MaterialSaver;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author fmauz
 */
public class StructureInformationSaver implements MaterialSaver {

    private static final long serialVersionUID = 1L;
    protected String SQL_INSERT_MOLECULE = "INSERT INTO molecules (molecule) "
            + "VALUES (:molecule) "
            + "RETURNING id";

    public StructureInformationSaver() {

    }

    /**
     * Saves the indices and if present the molecule of the structure
     *
     * @param m Structure to save
     * @param em
     */
    @Override
    public void saveMaterial(Material m, EntityManager em) {
        MaterialIndexSaver indexSaver = new MaterialIndexSaver(em);
        Structure s = (Structure) m;
        indexSaver.saveIndices(s);
        if (s.getMolecule() != null) {
            saveMoleculeOf(s, em);
        }
        em.persist(s.createEntity());
    }

    /**
     * Saves a molecule in the database and returns the generated id
     *
     * @param moleculeString SMILES,V2000 or V3000
     * @param em
     * @return generated id
     */
    public int saveMolecule(String moleculeString, EntityManager em) {
        Query q = em.createNativeQuery(SQL_INSERT_MOLECULE)
                .setParameter("molecule", moleculeString);
        int molId = (int) q.getSingleResult();
        return molId;
    }

    private void saveMoleculeOf(Structure s, EntityManager em) {
        if (s.getMolecule().getStructureModel() != null
                && !s.getMolecule().getStructureModel().isEmpty()) {
            s.getMolecule().setId(saveMolecule(s.getMolecule().getStructureModel(), em));
        } else {
            s.getMolecule().setId(0);
        }
    }

}
