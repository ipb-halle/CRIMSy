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
import de.ipb_halle.lbac.material.biomaterial.Taxonomy;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyDifference;
import java.util.List;

/**
 *
 * @author fmauz
 */
public class TaxonomyComparator extends IMaterialComparator {

    @Override
    public void compareDifferences(List<MaterialDifference> differences, Material originalMat, Material editedMat) {
        addMaterialNameDifference(differences, originalMat, editedMat);
        Taxonomy originalTaxonomy = (Taxonomy) originalMat;
        Taxonomy editedTaxonomy = (Taxonomy) editedMat;
        boolean differenceFound = false;
        TaxonomyDifference td = new TaxonomyDifference();
        if (originalTaxonomy.getLevel().getId() != editedTaxonomy.getLevel().getId()) {
            differenceFound = true;
            td.setOldLevelId(originalTaxonomy.getLevel().getId());
            td.setNewLevelId(editedTaxonomy.getLevel().getId());

        }
        if (originalTaxonomy.getTaxHierarchy().get(0).getId() != editedTaxonomy.getTaxHierarchy().get(0).getId()) {
            differenceFound = true;
            for (Taxonomy t : editedTaxonomy.getTaxHierarchy()) {
                td.getNewHierarchy().add(t.getId());
            }

        }
        if (differenceFound) {
            differences.add(td);
        }

    }

}
