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
package de.ipb_halle.lbac.material.structure;

import de.ipb_halle.lbac.material.common.bean.MaterialBean;
import de.ipb_halle.lbac.material.common.history.HistoryController;
import java.util.Objects;

/**
 *
 * @author fmauz
 */
public class StructureHistoryController implements HistoryController<MaterialStructureDifference> {

    private MaterialBean materialBean;

    public StructureHistoryController(MaterialBean materialBean) {
        this.materialBean = materialBean;
    }

    @Override
    public void applyPositiveDifference(MaterialStructureDifference structureDiff) {

        if (!Objects.equals(structureDiff.getMoleculeId_old(), structureDiff.getMoleculeId_new())) {
            if (structureDiff.getMoleculeId_old() != null && structureDiff.getMoleculeId_new() == null) {
                materialBean.getStructureInfos().setStructureModel(null);
            } else {
                materialBean.getStructureInfos().setStructureModel(structureDiff.getMoleculeId_new().getStructureModel());
            }
        }
        if (!Objects.equals(structureDiff.getMolarMass_old(), structureDiff.getMolarMass_new())) {
            materialBean.getStructureInfos().setAverageMolarMass(structureDiff.getMolarMass_new());
        }
        if (!Objects.equals(structureDiff.getExactMolarMass_old(), structureDiff.getExactMolarMass_new())) {
            materialBean.getStructureInfos().setExactMolarMass(structureDiff.getExactMolarMass_new());
        }
        if (!Objects.equals(structureDiff.getSumFormula_old(), structureDiff.getSumFormula_new())) {
            materialBean.getStructureInfos().setSumFormula(structureDiff.getSumFormula_new());
        }
    }

    @Override
    public void applyNegativeDifference(MaterialStructureDifference structureDiff) {
        if (!Objects.equals(structureDiff.getMoleculeId_old(), structureDiff.getMoleculeId_new())) {
            if (structureDiff.getMoleculeId_old() == null && structureDiff.getMoleculeId_new() != null) {
                materialBean.getStructureInfos().setStructureModel(null);
            } else {
                materialBean.getStructureInfos().setStructureModel(structureDiff.getMoleculeId_old().getStructureModel());
            }
        }
        if (!Objects.equals(structureDiff.getMolarMass_old(), structureDiff.getMolarMass_new())) {
            materialBean.getStructureInfos().setAverageMolarMass(structureDiff.getMolarMass_old());
        }
        if (!Objects.equals(structureDiff.getExactMolarMass_old(), structureDiff.getExactMolarMass_new())) {
            materialBean.getStructureInfos().setExactMolarMass(structureDiff.getExactMolarMass_old());
        }
        if (!Objects.equals(structureDiff.getSumFormula_old(), structureDiff.getSumFormula_new())) {
            materialBean.getStructureInfos().setSumFormula(structureDiff.getSumFormula_old());
        }
    }

}
