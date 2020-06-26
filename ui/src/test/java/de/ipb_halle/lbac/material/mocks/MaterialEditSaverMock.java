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

import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.structure.Molecule;
import de.ipb_halle.lbac.material.common.bean.MaterialEditSaver;
import de.ipb_halle.lbac.material.structure.MaterialStructureDifference;
import de.ipb_halle.lbac.material.structure.Structure;
import javax.persistence.Query;

/**
 * Mocks the usage of pgchem plugin functionalities from the postgres database
 *
 * @author fmauz
 */
public class MaterialEditSaverMock extends MaterialEditSaver {

    String SQL_ID_MOLECULE = "select max(id) from molecules";

    public MaterialEditSaverMock(MaterialService materialService) {
        super(materialService, null);
        SQL_INSERT_MOLECULE = "INSERT INTO molecules (molecule,format) VALUES(:molecule,:format)";
    }

    @Override
    public void saveEditedMaterialStructure() {
        Structure structure = (Structure) newMaterial;
        MaterialStructureDifference strucDiff = comparator.getDifferenceOfType(diffs, MaterialStructureDifference.class);
        if (strucDiff != null) {

            Molecule newMol = strucDiff.getMoleculeId_new();
            Molecule oldMol = strucDiff.getMoleculeId_old();
            if (!(oldMol == null && newMol == null)) {
                if (newMol != null) {
                    Query q = materialService.getEm().createNativeQuery(SQL_INSERT_MOLECULE)
                            .setParameter("molecule", newMol.getStructureModel())
                            .setParameter("format", newMol.getModelType().toString());

                    q.executeUpdate();
                    int id = (Integer) materialService.getEm().createNativeQuery(SQL_ID_MOLECULE).getSingleResult();
                    newMol.setId(id);
                }

            }

            saveMaterialStrcutureDifferences(strucDiff);
            updateStructureOverview(structure);
        }
    }
}
