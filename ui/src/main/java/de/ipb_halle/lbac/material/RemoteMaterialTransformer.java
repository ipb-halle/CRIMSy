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
package de.ipb_halle.lbac.material;

import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.search.RemoteTransformer;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.bean.Type;

/**
 *
 * @author fmauz
 */
public class RemoteMaterialTransformer implements RemoteTransformer {

    private RemoteMaterial remoteMat;
    private Material originalMat;

    public RemoteMaterialTransformer(Material material) {
        remoteMat = new RemoteMaterial();
        originalMat = material;
        remoteMat.setId(originalMat.getId());
        remoteMat.setType(new Type(SearchTarget.MATERIAL, originalMat.getType()));
    }

    @Override
    public RemoteMaterial transformToRemote() {
        setNames();
        setStructure();
        setIndices();
        return remoteMat;
    }

    private void setNames() {
        for (MaterialName name : originalMat.getNames()) {
            remoteMat.addName(name.getValue());
        }
    }

    private void setStructure() {
        if (originalMat instanceof Structure) {
            Structure struc = (Structure) originalMat;
            if (struc.getMolecule() != null) {
                remoteMat.setMoleculeString(struc.getMolecule().getStructureModel());
            }
            remoteMat.setSumFormula(struc.getSumFormula());
        }
    }

    private void setIndices() {
        for (IndexEntry ie : originalMat.getIndices()) {
            remoteMat.getIndices().put(ie.getTypeId(), ie.getValue());
        }
    }

}
