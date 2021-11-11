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
import de.ipb_halle.lbac.material.biomaterial.BioMaterial;
import de.ipb_halle.lbac.material.biomaterial.BioMaterialDifference;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author fmauz
 */
public class BioMaterialComparator extends IMaterialComparator {

    @Override
    public void compareDifferences(List<MaterialDifference> differences, Material originalMat, Material editedMat) throws Exception {
        addMaterialNameDifference(differences, originalMat, editedMat);
        addOverviewDifference(differences, originalMat, editedMat);
        addIndexDifference(differences, originalMat, editedMat);
        addHazardDifference(differences, originalMat, editedMat);
        addStorageDifference(differences, originalMat, editedMat);

        BioMaterialDifference biomaterialDiff = new BioMaterialDifference();

        BioMaterial biomat_orig = (BioMaterial) originalMat;
        BioMaterial biomat_edited = (BioMaterial) editedMat;

        if (!Objects.equals(
                biomat_orig.getTaxonomyId(),
                biomat_edited.getTaxonomyId())) {
            biomaterialDiff.addTaxonomyDiff(
                    biomat_orig.getTaxonomyId(), biomat_edited.getTaxonomyId());
        }
        if (!Objects.equals(
                biomat_orig.getTissueId(),
                biomat_edited.getTissueId())) {
            biomaterialDiff.addTissueDiff(
                    biomat_orig.getTissueId(), biomat_edited.getTissueId());
        }

        if (biomaterialDiff.differenceFound()) {
            differences.add(biomaterialDiff);
        }
    }

}
