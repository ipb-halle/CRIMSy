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

import de.ipb_halle.lbac.material.common.StorageCondition;
import de.ipb_halle.lbac.material.common.bean.MaterialBean;
import java.util.Objects;

/**
 *
 * @author fmauz
 */
public class StorageHistoryController implements HistoryController<MaterialStorageDifference> {

    private MaterialBean materialBean;

    public StorageHistoryController(MaterialBean materialBean) {
        this.materialBean = materialBean;
    }

    @Override
    public void applyPositiveDifference(MaterialStorageDifference diff) {
        if (!Objects.equals(diff.getDescriptionNew(), diff.getDescriptionOld())) {
            materialBean.getStorageInformationBuilder().setRemarks(diff.getDescriptionNew());
        }
        if (!Objects.equals(diff.getStorageclassNew(), diff.getStorageclassOld())) {
            if (diff.getStorageclassNew() != null) {
                materialBean.getStorageInformationBuilder().setChoosenStorageClass(materialBean.getStorageInformationBuilder().getStorageClassById(diff.getStorageclassNew()));
                materialBean.getStorageInformationBuilder().setStorageClassActivated(true);

            } else {
                materialBean.getStorageInformationBuilder().setStorageClassActivated(false);
            }
        }
        for (int i = 0; i < diff.getStorageConditionsNew().size(); i++) {
            if (diff.getStorageConditionsOld().get(i) == null) {
                materialBean.getStorageInformationBuilder().addStorageCondition(StorageCondition.getStorageConditionById(diff.getStorageConditionsNew().get(i).getId()));

            } else {
                materialBean.getStorageInformationBuilder().removeStorageCondition(StorageCondition.getStorageConditionById(diff.getStorageConditionsOld().get(i).getId()));
            }
        }
    }

    @Override
    public void applyNegativeDifference(MaterialStorageDifference diff) {
        if (!Objects.equals(diff.getDescriptionNew(), diff.getDescriptionOld())) {
            materialBean.getStorageInformationBuilder().setRemarks(diff.getDescriptionOld());
        }
        if (!Objects.equals(diff.getStorageclassNew(), diff.getStorageclassOld())) {
            if (diff.getStorageclassOld() != null) {
                materialBean.getStorageInformationBuilder().setChoosenStorageClass(materialBean.getStorageInformationBuilder().getStorageClassById(diff.getStorageclassOld()));
                materialBean.getStorageInformationBuilder().setStorageClassActivated(true);
            } else {
                materialBean.getStorageInformationBuilder().setStorageClassActivated(false);
            }
        }
        for (int i = 0; i < diff.getStorageConditionsNew().size(); i++) {
            if (diff.getStorageConditionsNew().get(i) == null) {
                materialBean.getStorageInformationBuilder().addStorageCondition(StorageCondition.getStorageConditionById(diff.getStorageConditionsOld().get(i).getId()));

            } else {
                materialBean.getStorageInformationBuilder().removeStorageCondition(StorageCondition.getStorageConditionById(diff.getStorageConditionsNew().get(i).getId()));
            }
        }
    }

}
