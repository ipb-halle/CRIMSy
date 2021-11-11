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
import de.ipb_halle.lbac.material.common.MaterialDetailType;
import java.io.Serializable;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class MaterialEditPermission implements Serializable {

    private static final long serialVersionUID = 1L;

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    private MaterialBean bean;

    public MaterialEditPermission(MaterialBean bean) {
        this.bean = bean;
    }

    /**
     * Checks whether the current user has the right to edit the detail
     * information of a material. If the current state of the materialEditBean
     * is in CREATE mode, then always true, if it is in HISTORY mode, then
     * always false.
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

    public boolean isEditAllowed() {
        boolean isCreate = bean.getMode() == MaterialBean.Mode.CREATE;
        boolean isHistory = bean.getMode() == MaterialBean.Mode.HISTORY;
        return (isCreate || isOwnerOrPermitted(null, permEDIT)) && !isHistory;
    }

    public boolean isForwardButtonEnabled() {
        return !bean.getMaterialEditState().getMaterialBeforeEdit().getHistory().isMostRecentVersion(bean.getMaterialEditState().getCurrentVersiondate());
    }

    public boolean isBackwardButtonEnabled() {
        return bean.getMaterialEditState().getCurrentVersiondate() != null;
    }

    public boolean isFormulaAndMassesInputsDisabled() {
        return bean.isAutoCalcFormularAndMasses()
                || bean.getMode() == MaterialBean.Mode.HISTORY;
    }

    /**
     * To keep it simple in the first run, only the acl of the material is used
     * for checking rights on sub components
     *
     * @param type
     * @param permission
     * @return
     */
    private boolean isOwnerOrPermitted(MaterialDetailType type, ACPermission permission) {
        ACList aclist = bean.getMaterialEditState().getMaterialToEdit().getACList();
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

            return materialGotDetail
                    && (isOwner()
                    || userHasAccessRight());

        } catch (Exception e) {
            logger.error("Error in isVisible(): " + typeName);
            logger.error("Current MaterialType: " + bean.getCurrentMaterialType());
            logger.error(ExceptionUtils.getStackTrace(e));
        }
        return false;
    }

    private boolean userHasAccessRight() {
        if (bean.getMode() != MaterialBean.Mode.CREATE) {
            return bean.getAcListService().isPermitted(
                    ACPermission.permREAD,
                    bean.getMaterialEditState().getMaterialToEdit().getACList(),
                    bean.getUserBean().getCurrentAccount());
        }
        return true;

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
            logger.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
    }
}
