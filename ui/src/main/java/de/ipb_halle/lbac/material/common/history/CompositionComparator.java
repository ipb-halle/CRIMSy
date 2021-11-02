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
import de.ipb_halle.lbac.material.composition.CompositionDifference;
import de.ipb_halle.lbac.material.composition.Concentration;
import de.ipb_halle.lbac.material.composition.MaterialComposition;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class CompositionComparator extends IMaterialComparator {

    private MaterialComposition originalComposition, editedComposition;
    private CompositionDifference diff;
    protected Logger logger = LogManager.getLogger(this.getClass().getName());

    @Override
    public void compareDifferences(List<MaterialDifference> differences, Material originalMat, Material editedMat) throws Exception {
        originalComposition = (MaterialComposition) originalMat;
        editedComposition = (MaterialComposition) editedMat;
        diff = new CompositionDifference("EDIT");

        addMaterialNameDifference(differences, originalMat, editedMat);
        addOverviewDifference(differences, originalMat, editedMat);
        addIndexDifference(differences, originalMat, editedMat);
        addHazardDifference(differences, originalMat, editedMat);
        addStorageDifference(differences, originalMat, editedMat);

        addComponents();
        removeComponents();
        changeConcentrations();
        changeUnits();

        if (diff.hasDifferences()) {
            differences.add(diff);
        }
    }

    private void removeComponents() {
        for (Concentration m : originalComposition.getComponents()) {
            if (!hasConcentration(editedComposition, m)) {
                diff.addDifference(
                        m.getMaterialId(), null,
                        m.getConcentration(), null,
                        m.getUnitString(), null);
            }
        }
    }

    private void addComponents() {
        for (Concentration editedConc : editedComposition.getComponents()) {
            if (!hasConcentration(originalComposition, editedConc)) {
                diff.addDifference(null, editedConc.getMaterialId(), null, editedConc.getConcentration(), null, editedConc.getUnitString());
            }
        }
    }

    private void changeConcentrations() {
        for (Concentration originalComponent : originalComposition.getComponents()) {
            int id = originalComponent.getMaterialId();
            Concentration newComponent = getConcentrationWithId(id, editedComposition.getComponents());
            if (newComponent != null) {
                Double concentrationOld = originalComponent.getConcentration();
                Double concentrationNew = newComponent.getConcentration();
                if (concentrationNew == null && concentrationOld != null) {
                    diff.addConcentrationDifference(id, concentrationOld, null);
                }
                if (concentrationNew != null && concentrationOld == null) {
                    diff.addConcentrationDifference(id, null, concentrationNew);
                }
                if (concentrationNew != null && concentrationOld != null) {                    
                    if (Math.abs(concentrationNew - concentrationOld) > Double.MIN_NORMAL) {
                        diff.addConcentrationDifference(id, concentrationOld, concentrationNew);
                    }
                }
            }
        }

    }

    private void changeUnits() {
        for (Concentration originalComponent : originalComposition.getComponents()) {
            int id = originalComponent.getMaterialId();
            Concentration newComponent = getConcentrationWithId(id, editedComposition.getComponents());
            if (newComponent != null) {
                String unitOld = originalComponent.getUnitString();
                String unitNew = newComponent.getUnitString();
                if (unitNew == null && unitOld != null) {
                    diff.addUnitDifference(id, unitOld, null);
                }
                if (unitNew != null && unitOld == null) {
                    diff.addUnitDifference(id, null, unitNew);
                }
                if (unitNew != null && unitOld != null&&!unitNew.equals(unitOld)) {
                    diff.addUnitDifference(id, unitOld, unitNew);
                }
            }
        }

    }

    private Concentration getConcentrationWithId(int id, List<Concentration> concentrations) {
        for (Concentration m : concentrations) {
            if (m.getMaterialId() == id) {
                return m;
            }
        }
        return null;
    }

    private boolean hasConcentration(MaterialComposition compo, Concentration concToCheck) {
        for (Concentration c : compo.getComponents()) {
            if (c.getMaterialId() == concToCheck.getMaterialId()) {
                return true;
            }
        }
        return false;
    }

}
