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
package de.ipb_halle.lbac.material.common.history;

import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.ModificationType;
import de.ipb_halle.lbac.material.structure.MaterialStructureDifference;
import de.ipb_halle.lbac.material.structure.Molecule;
import de.ipb_halle.lbac.material.structure.Structure;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author fmauz
 */
public class StructureComparator implements IMaterialComparator {

    /**
     * Compares two structures and calculate the differences. The differences
     * are added to the given list.
     *
     * @param differences
     * @param originalMat
     * @param editedMat
     */
    @Override
    public void compareDifferences(
            List<MaterialDifference> differences,
            Material originalMat,
            Material editedMat) {
        Structure originalStruc = (Structure) originalMat;
        Structure editedStruc = (Structure) editedMat;
        MaterialStructureDifference diff = new MaterialStructureDifference();
        if (!Objects.equals(originalStruc.getSumFormula(), editedStruc.getSumFormula())) {
            diff.setSumFormula_new(editedStruc.getSumFormula());
            diff.setSumFormula_old(originalStruc.getSumFormula());
        }
        if (!checkIfBothAreNull(originalStruc.getExactMolarMass(), editedStruc.getExactMolarMass())) {
            if (checkIfOneIsNull(originalStruc.getExactMolarMass(), editedStruc.getExactMolarMass())
                    || Math.abs(originalStruc.getExactMolarMass() - editedStruc.getExactMolarMass()) > Double.MIN_NORMAL) {
                diff.setExactMolarMass_new(editedStruc.getExactMolarMass());
                diff.setExactMolarMass_old(originalStruc.getExactMolarMass());
            }
        }
        if (!checkIfBothAreNull(originalStruc.getAverageMolarMass(), editedStruc.getAverageMolarMass())) {
            if (checkIfOneIsNull(originalStruc.getAverageMolarMass(), editedStruc.getAverageMolarMass())
                    || Math.abs(originalStruc.getAverageMolarMass() - editedStruc.getAverageMolarMass()) > Double.MIN_NORMAL) {
                diff.setMolarMass_new(editedStruc.getAverageMolarMass());
                diff.setMolarMass_old(originalStruc.getAverageMolarMass());
            }
        }
        Molecule origMol = originalStruc.getMolecule();
        Molecule newMol = editedStruc.getMolecule();
        boolean newMoleculeSet = origMol == null && newMol != null;
        boolean newMoleculeRemoved = origMol != null && newMol == null;
        boolean modelEdited = newMol != null
                && origMol != null
                && !newMol.getStructureModel().equals(origMol.getStructureModel());

        if (newMoleculeSet || newMoleculeRemoved || modelEdited) {
            diff.setMoleculeId_new(editedStruc.getMolecule());
            diff.setMoleculeId_old(originalStruc.getMolecule());
        }

        diff.setAction(ModificationType.EDIT);
        if (diff.differenceFound()) {
            differences.add(diff);
        }
    }

    private boolean checkIfOneIsNull(
            Double originalStruc,
            Double editedStruc) {
        return (originalStruc != null && editedStruc == null)
                || (originalStruc == null && editedStruc != null);
    }

    private boolean checkIfBothAreNull(
            Double originalStruc,
            Double editedStruc) {
        return originalStruc == null && editedStruc == null;
    }

}
