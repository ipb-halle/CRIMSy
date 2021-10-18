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
package de.ipb_halle.lbac.material.composition;

import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.bean.MaterialBean;
import de.ipb_halle.lbac.material.common.history.HistoryController;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.inaccessible.InaccessibleMaterial;
import de.ipb_halle.lbac.util.Unit;

/**
 *
 * @author fmauz
 */
public class CompositionHistoryController implements HistoryController<CompositionDifference> {

    MaterialCompositionBean compositionBean;
    User user;
    ACListService aclistService;
    MaterialService materialService;
    MaterialBean materialBean;

    public CompositionHistoryController(
            MaterialBean materialBean) {
        this.compositionBean = materialBean.getCompositionBean();
        this.user = materialBean.getUserBean().getCurrentAccount();
        this.aclistService = materialBean.getAcListService();
        this.materialService = materialBean.getMaterialService();
    }

    /**
     *
     * @param diff
     */
    @Override
    public void applyPositiveDifference(CompositionDifference diff) {
        for (int i = 0; i < diff.getMaterialIds_new().size(); i++) {
            Double concentrationNew = diff.getConcentrations_new().get(i);
            handleDelta(
                    diff.getMaterialIds_new().get(i),
                    concentrationNew,
                    diff.getMaterialIds_old().get(i),
                    getUnitFromString(diff.getUnits_new().get(i)));
        }
    }

    /**
     *
     * @param diff
     */
    @Override
    public void applyNegativeDifference(CompositionDifference diff) {
        for (int i = 0; i < diff.getMaterialIds_old().size(); i++) {
            Double concentrationOld = diff.getConcentrations_old().get(i);
            handleDelta(
                    diff.getMaterialIds_old().get(i),
                    concentrationOld,
                    diff.getMaterialIds_new().get(i),
                    getUnitFromString(diff.getUnits_old().get(i)));
        }
    }

    private Unit getUnitFromString(String unitString) {
        Unit unit = null;
        if (unitString != null) {
            unit = Unit.getUnit(unitString);
        }
        return unit;
    }

    private void handleDelta(Integer materialIdToInsert, Double concentrationOld, Integer materialIdToRemove, Unit unit) {
        if (materialIdToInsert != null) {
            addConcentration(materialIdToInsert, concentrationOld, unit);
        } else {
            removeConcentration(materialIdToRemove);
        }
    }

    private void removeConcentration(int materialId) {
        compositionBean.getConcentrationsInComposition().remove(compositionBean.getConcentrationWithMaterial(materialId));
    }

    private void addConcentration(int materialId, Double oldConcentration, Unit unit) {
        if (!compositionBean.isMaterialAlreadyInComposition(materialId)) {
            addNewMaterialToComposition(materialId, oldConcentration, unit);
        } else {
            compositionBean.getConcentrationWithMaterial(materialId).setConcentration(oldConcentration);
        }
    }

    private void addNewMaterialToComposition(int materialId, Double concentration, Unit unit) {
        Material material = getMaterialWithPermissionCheck(materialId);
        compositionBean.getConcentrationsInComposition().add(new Concentration(material, concentration, unit));
    }

    private Material getMaterialWithPermissionCheck(int materialId) {
        Material material = materialService.loadMaterialById(materialId);
        if (!aclistService.isPermitted(ACPermission.permREAD, material, user)) {
            material = InaccessibleMaterial.createNewInstance(GlobalAdmissionContext.getPublicReadACL());
        }
        return material;
    }

}
