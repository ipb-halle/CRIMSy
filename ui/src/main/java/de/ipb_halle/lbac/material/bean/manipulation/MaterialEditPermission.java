/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.bean.manipulation;

import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.ACPermission;
import de.ipb_halle.lbac.material.bean.MaterialBean;
import static de.ipb_halle.lbac.entity.ACPermission.permEDIT;
import de.ipb_halle.lbac.material.component.MaterialDetailRight;
import de.ipb_halle.lbac.material.component.MaterialDetailType;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class MaterialEditPermission {

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    private MaterialBean bean;

    public MaterialEditPermission(MaterialBean bean) {
        this.bean = bean;
    }

    /**
     * Checks if the current user has the right to edit a detailinformation of a
     * material. If the current state of the materialEditBean is in CREATE than
     * always true, if it is in HITORY mode then always false.
     *
     * @param typeString
     * @return
     */
    public boolean isDetailInformationEditable(String typeString) {
        MaterialDetailType type = MaterialDetailType.valueOf(typeString);
        boolean isCreate = bean.getMode() == MaterialBean.Mode.CREATE;
        boolean isHistory = bean.getMode() == MaterialBean.Mode.HISTORY;
        return (isCreate||isOwnerOrPermitted(type, permEDIT)) && !isHistory;
    }

    public boolean isForwardButtonEnabled() {
        return !bean.getMaterialEditState().getMaterialBeforeEdit().getHistory().isMostRecentVersion(bean.getMaterialEditState().getCurrentVersiondate());
    }

    public boolean isBackwardButtonEnabled() {
        return bean.getMaterialEditState().getCurrentVersiondate() != null;
    }

    public boolean isFormulaAndMassesInputsEnabled() {
        return !bean.isCalculateFormulaAndMassesByDb() && bean.getMode() != MaterialBean.Mode.HISTORY;
    }

    private boolean isOwnerOrPermitted(MaterialDetailType type, ACPermission permission) {
        ACList aclist = bean.getMaterialEditState().getMaterialToEdit().getDetailRight(type);
        boolean userHasEditRight = aclist != null && bean.getAcListService().isPermitted(permission, aclist, bean.getUserBean().getCurrentAccount());
        boolean userIsOwner = bean.getMaterialEditState().getMaterialToEdit().getOwnerID().equals(bean.getUserBean().getCurrentAccount().getId());
        return userIsOwner || userHasEditRight;
    }

    public boolean isDetailPanelVisible(String typeName) {

        if (bean.getCurrentMaterialType() == null) {
            return false;
        }

        try {
            MaterialDetailType type = MaterialDetailType.valueOf(typeName);
            boolean materialGotDetail = bean.getCurrentMaterialType().getPossibleDetailTypes().contains(type);

            if (bean.getMode() == MaterialBean.Mode.CREATE) {
                return materialGotDetail;
            }

            ACList acl = getAcListOfDetailRight(type, bean.getMaterialEditState());
            if (bean.getMaterialEditState().getMaterialToEdit().getOwnerID().equals(bean.getUserBean().getCurrentAccount().getId())) {
                return true;
            }

            if (acl == null) {
                return false;
            }

            boolean userHasRight = bean.getAcListService().isPermitted(
                    ACPermission.permREAD,
                    acl,
                    bean.getUserBean().getCurrentAccount());
            return materialGotDetail && userHasRight;
        } catch (Exception e) {
            logger.info("Error in isVisible(): " + typeName);
            logger.info("Current MaterialType: " + bean.getCurrentMaterialType());
            logger.error(e);

        }
        return false;
    }

    protected ACList getAcListOfDetailRight(MaterialDetailType type, MaterialEditState state) {
        List<MaterialDetailRight> rights = bean.getMaterialEditState().getMaterialToEdit().getDetailRights();
        for (MaterialDetailRight mdr : rights) {
            if (mdr.getType() == type) {
                return mdr.getAcList();
            }
        }
        return null;
    }

}
