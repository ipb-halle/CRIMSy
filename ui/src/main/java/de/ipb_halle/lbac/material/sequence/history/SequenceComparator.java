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
package de.ipb_halle.lbac.material.sequence.history;

import java.util.List;
import java.util.Objects;

import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.history.IMaterialComparator;
import de.ipb_halle.lbac.material.common.history.MaterialDifference;
import de.ipb_halle.lbac.material.sequence.Sequence;
import de.ipb_halle.lbac.material.sequence.SequenceData;

/**
 * 
 * @author flange
 */
public class SequenceComparator extends IMaterialComparator {
    @Override
    public void compareDifferences(List<MaterialDifference> differences,
            Material originalMat, Material editedMat) throws Exception {
        addMaterialNameDifference(differences, originalMat, editedMat);
        addOverviewDifference(differences, originalMat, editedMat);
        addIndexDifference(differences, originalMat, editedMat);
        //addHazardDifference(differences, originalMat, editedMat);
        //addStorageDifference(differences, originalMat, editedMat);

        SequenceData originalSequenceData = ((Sequence) originalMat).getSequenceData();
        SequenceData editedSequenceData = ((Sequence) editedMat).getSequenceData();

        if (Objects.equals(originalSequenceData, editedSequenceData)) {
            // nothing changed
            return;
        }

        differences.add(new SequenceDifference(originalSequenceData, editedSequenceData));
    }
}
