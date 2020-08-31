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
package de.ipb_halle.lbac.material.common.bean;

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACPermission;
import static de.ipb_halle.lbac.admission.ACPermission.permEDIT;
import de.ipb_halle.lbac.material.common.MaterialDetailRight;
import de.ipb_halle.lbac.material.common.MaterialDetailType;
import java.io.Serializable;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class MaterialEditPermission implements Serializable{

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
        return (isCreate || isOwnerOrPermitted(type, permEDIT)) && !isHistory;
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
        boolean userIsOwner = bean.getMaterialEditState().getMaterialToEdit().getOwner().getId().equals(bean.getUserBean().getCurrentAccount().getId());
        return userIsOwner || userHasEditRight;
    }

    /**
     * Checks if a panel with some informationsub types (e.g. names, taxonomy,
     * ...) is visible to the user logged in .
     *
     * @param typeName name of the enum
     * @return
     */
    public boolean isDetailPanelVisible(String typeName) {
        if (bean.getCurrentMaterialType() == null) {
            return false;
        }

        try {
            MaterialDetailType type = MaterialDetailType.valueOf(typeName);
            boolean materialGotDetail = bean.getCurrentMaterialType().getPossibleDetailTypes().contains(type);

            boolean userHasRight = bean.getAcListService().isPermitted(
                    ACPermission.permREAD,
                    getAcListOfDetailRight(type, bean.getMaterialEditState()),
                    bean.getUserBean().getCurrentAccount());

            return materialGotDetail
                    && (isOwner()
                    || userHasRight
                    || bean.getMode() == MaterialBean.Mode.CREATE);

        } catch (Exception e) {
            logger.info("Error in isVisible(): " + typeName);
            logger.info("Current MaterialType: " + bean.getCurrentMaterialType());
            logger.error(e);
        }
        return false;
    }

    protected ACList getAcListOfDetailRight(MaterialDetailType type, MaterialEditState state) {
        if (bean.getMaterialEditState() == null
                || bean.getMaterialEditState().getMaterialToEdit() == null) {
            return null;
        }
        try {
            List<MaterialDetailRight> rights = bean.getMaterialEditState().getMaterialToEdit().getDetailRights();
            for (MaterialDetailRight mdr : rights) {
                if (mdr.getType() == type) {
                    return mdr.getAcList();
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
        return null;
    }

    private boolean isOwner() {
        if (bean.getMaterialEditState() == null
                || bean.getMaterialEditState().getMaterialToEdit() == null
                || bean.getMaterialEditState().getMaterialToEdit().getOwner().getId() == null) {
            return false;
        }
        try {
            return bean.getMaterialEditState() != null
                    && bean.getMaterialEditState()
                            .getMaterialToEdit()
                            .getOwner().getId()
                            .equals(bean.getUserBean()
                                    .getCurrentAccount()
                                    .getId());
        } catch (Exception e) {
            logger.error(e);
            return false;
        }
    }
}
