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
import de.ipb_halle.lbac.material.composition.MaterialComposition;
import java.util.List;
import java.util.Set;

/**
 *
 * @author fmauz
 */
public class CompositionComparator extends IMaterialComparator {

    @Override
    public void compareDifferences(List<MaterialDifference> differences, Material originalMat, Material editedMat) throws Exception {
        addMaterialNameDifference(differences, originalMat, editedMat);
        addOverviewDifference(differences, originalMat, editedMat);
        addIndexDifference(differences, originalMat, editedMat);
        addHazardDifference(differences, originalMat, editedMat);
        addStorageDifference(differences, originalMat, editedMat);

        MaterialComposition originalComposition = (MaterialComposition) originalMat;
        MaterialComposition editedComposition = (MaterialComposition) editedMat;
        CompositionDifference diff = new CompositionDifference("EDIT");
        //Add new component
        for (Material m : editedComposition.getComponents().keySet()) {
            if (!originalComposition.getComponents().keySet().contains(m)) {
                diff.addDifference(null, m.getId(), null, editedComposition.getComponents().get(m));
            }
        }
        //Remove component
        for (Material m : originalComposition.getComponents().keySet()) {
            if (!editedComposition.getComponents().keySet().contains(m)) {
                diff.addDifference(m.getId(), null, originalComposition.getComponents().get(m), null);
            }
        }
        //Change concentration
        for (Material originalComponent : originalComposition.getComponents().keySet()) {
            int id = originalComponent.getId();
            Material newComponent = getMaterialWithId(originalComponent.getId(), editedComposition.getComponents().keySet());
            if (newComponent != null) {
                Double concentrationOld = originalComposition.getComponents().get(originalComponent);
                Double concentrationNew = editedComposition.getComponents().get(newComponent);
                if (concentrationNew == null && concentrationOld != null) {
                    diff.addDifference(id, id, concentrationOld, null);
                }
                if (concentrationNew != null && concentrationOld == null) {
                    diff.addDifference(id, id, null, concentrationNew);
                }
                if (concentrationNew != null && concentrationOld != null) {
                    if (Math.abs(concentrationNew - concentrationOld) > Double.MIN_NORMAL) {
                        diff.addDifference(id, id, concentrationOld, concentrationNew);
                    }
                }
            }
        }

        if (diff.hasDifferences()) {
            differences.add(diff);
        }
    }

    private Material getMaterialWithId(int id, Set<Material> set) {
        for (Material m : set) {
            if (m.getId() == id) {
                return m;
            }
        }
        return null;

    }

}
