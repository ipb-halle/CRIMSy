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

import de.ipb_halle.lbac.material.common.HazardType;
import de.ipb_halle.lbac.material.common.bean.MaterialBean;

/**
 *
 * @author fmauz
 */
public class HazardHistoryController implements HistoryController<MaterialHazardDifference> {

    private MaterialBean materialBean;

    public HazardHistoryController(MaterialBean materialBean) {
        this.materialBean = materialBean;
    }

    @Override
    public void applyPositiveDifference(MaterialHazardDifference diff) {
        for (int i = 0; i < diff.getEntries(); i++) {
            Integer newTypeId = diff.getTypeIdsNew().get(i);
            Integer oldTypeId = diff.getTypeIdsOld().get(i);
            String newValue = diff.getRemarksNew().get(i);
            if (newTypeId != null) {
                materialBean.getMaterialEditState().getHazardController().addHazardType(getHazardById(newTypeId), newValue);
            } else {
                materialBean.getMaterialEditState().getHazardController().removeHazard(getHazardById(oldTypeId));
            }
        }
    }

    @Override
    public void applyNegativeDifference(MaterialHazardDifference diff) {
        materialBean.getMaterialEditState().getHazardController().setEditable(false);
        for (int i = 0; i < diff.getEntries(); i++) {
            Integer newTypeId = diff.getTypeIdsNew().get(i);
            Integer oldTypeId = diff.getTypeIdsOld().get(i);
            String oldValue = diff.getRemarksOld().get(i);
            if (oldTypeId != null) {
                materialBean.getMaterialEditState().getHazardController().addHazardType(getHazardById(oldTypeId), oldValue);
            } else {
                materialBean.getMaterialEditState().getHazardController().removeHazard(getHazardById(newTypeId));
            }
        }
    }

    private HazardType getHazardById(int id) {
        for (HazardType hazard : materialBean.getAllPossibleHazards()) {
            if (hazard.getId() == id) {
                return hazard;
            }
        }
        return null;
    }

}
