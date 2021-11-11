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
package de.ipb_halle.lbac.material.biomaterial;

import de.ipb_halle.lbac.material.common.bean.MaterialBean;
import de.ipb_halle.lbac.material.common.history.HistoryController;

/**
 *
 * @author fmauz
 */
public class BioMaterialHistoryController implements HistoryController<BioMaterialDifference> {

    private MaterialBean materialBean;

    public BioMaterialHistoryController(MaterialBean materialBean) {
        this.materialBean = materialBean;
    }

    @Override
    public void applyPositiveDifference(BioMaterialDifference bioMatDiff) {
        if (bioMatDiff.getTaxonomyid_new() != null) {
            materialBean.getTaxonomyController().setSelectedTaxonomyById(bioMatDiff.getTaxonomyid_new());
        }
        if (materialBean.getTaxonomyController() != null) {
            if (!materialBean.getMaterialEditState().isMostRecentVersion()) {
                materialBean.getTaxonomyController().deactivateTree();
            } else {
                materialBean.getTaxonomyController().activateTree();
            }
        }
    }

    @Override
    public void applyNegativeDifference(BioMaterialDifference bioMatDiff) {
        if (materialBean.getTaxonomyController() != null && bioMatDiff.getTaxonomyid_old() != null) {
            materialBean.getTaxonomyController().setSelectedTaxonomyById(bioMatDiff.getTaxonomyid_old());
            materialBean.getTaxonomyController().deactivateTree();
            
        }
    }

}
