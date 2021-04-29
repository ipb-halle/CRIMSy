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
import java.io.Serializable;
import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 *
 * @author fmauz
 */
public class StructureInformationSaver implements Serializable {

    protected String SQL_INSERT_MOLECULE = "INSERT INTO molecules (molecule) "
            + "VALUES (:molecule) "
            + "RETURNING id";
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
        if (s.getMolecule().getStructureModel() != null 
                && !s.getMolecule().getStructureModel().isEmpty()) {
            s.getMolecule().setId(saveMolecule(s.getMolecule().getStructureModel()));
        }else{
            s.getMolecule().setId(0);
        }
        em.persist(s.createEntity());
    }
    
    public int saveMolecule(String moleculeString){
         Query q = em.createNativeQuery(SQL_INSERT_MOLECULE)
                    .setParameter("molecule", moleculeString);
          int molId = (int) q.getSingleResult();
           return molId;
    }
}
