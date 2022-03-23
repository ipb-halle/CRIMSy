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
import de.ipb_halle.lbac.util.units.Unit;

import java.util.Objects;

/**
 * Moves the state of the compositionBean forward and backward in time driven by
 * a given difference
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
     * Calculates the new state of the compositionBean driven by the given
     * CompositionDifference
     *
     * @param diff
     */
    @Override
    public void applyPositiveDifference(CompositionDifference diff) {
        for (int i = 0; i < diff.getMaterialIds_new().size(); i++) {
            Double concentrationNew = diff.getConcentrations_new().get(i);
            Double concentrationOld = diff.getConcentrations_old().get(i);

            handleDelta(
                    diff.getMaterialIds_old().get(i),
                    diff.getMaterialIds_new().get(i),
                    concentrationOld, concentrationNew,
                    getUnitFromString(diff.getUnits_old().get(i)),
                    getUnitFromString(diff.getUnits_new().get(i))
            );

        }
    }

    /**
     * Calculates the new state of the compositionBean driven by the given
     * CompositionDifference
     *
     * @param diff
     */
    @Override
    public void applyNegativeDifference(CompositionDifference diff) {
        for (int i = 0; i < diff.getMaterialIds_old().size(); i++) {
            Double concentrationOld = diff.getConcentrations_old().get(i);
            Double concentrationNew = diff.getConcentrations_new().get(i);

            handleDelta(
                    diff.getMaterialIds_new().get(i),
                    diff.getMaterialIds_old().get(i),
                    concentrationNew, concentrationOld,
                    getUnitFromString(diff.getUnits_new().get(i)),
                    getUnitFromString(diff.getUnits_old().get(i))
            );
        }
    }

    private Unit getUnitFromString(String unitString) {
        Unit unit = null;
        if (unitString != null) {
            unit = Unit.getUnit(unitString);
        }
        return unit;
    }

    private void handleDelta(
            Integer materialIdToRemove,
            Integer materialIdToInsert,
            Double concentrationToRemove,
            Double concentrationToInsert,
            Unit unitToRemove,
            Unit unitToInsert) {
        if (materialIdToInsert != null) {
            addOrChangeConcentration(materialIdToRemove, materialIdToInsert, concentrationToRemove, concentrationToInsert, unitToRemove, unitToInsert);
        } else {
            removeConcentrationFromComposition(materialIdToRemove);
        }
    }

    private void addOrChangeConcentration(
            Integer materialIdToRemove,
            Integer materialIdToInsert,
            Double concentrationToRemove,
            Double concentrationToInsert,
            Unit unitToRemove,
            Unit unitToInsert) {
        Material loadedMaterialToInsert = getMaterialWithPermissionCheck(materialIdToInsert);

        if (areMaterialsEqual(materialIdToInsert, materialIdToRemove)
                && isMaterialInaccessible(loadedMaterialToInsert.getId())) {
            changeConcentration(concentrationToRemove, concentrationToInsert, loadedMaterialToInsert.getId());
            changeUnit(unitToRemove, unitToInsert, loadedMaterialToInsert.getId());
        } else if (!compositionBean.isMaterialAlreadyInComposition(materialIdToInsert)) {
            addNewConcentration(loadedMaterialToInsert, concentrationToInsert, unitToInsert);
        } else {
            changeConcentration(concentrationToRemove, concentrationToInsert, materialIdToInsert);
            changeUnit(unitToRemove, unitToInsert, loadedMaterialToInsert.getId());
        }
    }

    private void addNewConcentration(
            Material loadedMaterialToInsert,
            Double concentrationToInsert,
            Unit unitToInsert) {
        compositionBean.getConcentrationsInComposition()
                .add(new Concentration(loadedMaterialToInsert, concentrationToInsert, unitToInsert));
    }

    private boolean isMaterialInaccessible(int materialId) {
        return materialId == -1;
    }

    private void removeConcentrationFromComposition(Integer materialIdToRemove) {
        Material m = getMaterialWithPermissionCheck(materialIdToRemove);
        removeConcentration(m.getId());
    }

    private boolean areMaterialsEqual(Integer materialId, Integer otherMaterialId) {
        return Objects.equals(materialId, otherMaterialId);
    }

    private void changeConcentration(
            Double concentrationToRemove,
            Double concentrationToInsert,
            Integer materialIdToInsert) {
        if (!areConcentrationsEqual(concentrationToInsert, concentrationToRemove)) {
            compositionBean.getConcentrationWithMaterial(materialIdToInsert)
                    .setConcentration(concentrationToInsert);
        }
    }

    private void changeUnit(
            Unit unitToRemove,
            Unit unitToInsert,
            int materialIdToInsert) {
        if (!Objects.equals(unitToInsert, unitToRemove)) {
            compositionBean.getConcentrationWithMaterial(materialIdToInsert)
                    .setUnit(unitToInsert);
        }
    }

    private boolean areConcentrationsEqual(Double concentration1, Double concentration2) {
        if (concentration1 == null && concentration2 == null) {
            return true;
        }
        if (concentration1 != null && concentration2 != null) {
            return (Math.abs(concentration1 - concentration2) <= Double.MIN_VALUE);
        }
        return false;

    }

    private void removeConcentration(int materialId) {
        compositionBean.getConcentrationsInComposition()
                .remove(compositionBean
                        .getConcentrationWithMaterial(materialId));
    }

    private Material getMaterialWithPermissionCheck(Integer materialId) {
        if (materialId == null) {
            return null;
        }
        Material material = materialService.loadMaterialById(materialId);
        if (!aclistService.isPermitted(ACPermission.permREAD, material, user)) {
            material = InaccessibleMaterial.createNewInstance(GlobalAdmissionContext.getPublicReadACL());
        }
        return material;
    }

}
