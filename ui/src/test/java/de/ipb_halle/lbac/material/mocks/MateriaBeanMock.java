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
package de.ipb_halle.lbac.material.mocks;

import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.material.common.bean.MaterialIndexBean;
import de.ipb_halle.lbac.material.common.bean.MaterialNameBean;
import de.ipb_halle.lbac.material.common.bean.MaterialBean;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.project.ProjectBean;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.material.biomaterial.TaxonomySelectionController;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.material.common.MaterialDetailType;
import de.ipb_halle.lbac.material.common.bean.MaterialEditState;
import de.ipb_halle.lbac.material.common.bean.MaterialHazardBuilder;
import de.ipb_halle.lbac.material.common.bean.MaterialOverviewBean;
import de.ipb_halle.lbac.material.common.bean.StorageInformationBuilder;
import de.ipb_halle.lbac.material.common.history.HistoryOperation;
import de.ipb_halle.lbac.material.common.service.HazardService;
import de.ipb_halle.lbac.material.composition.MaterialCompositionBean;
import de.ipb_halle.lbac.material.sequence.SequenceInformation;
import jakarta.enterprise.context.SessionScoped;

/**
 *
 * @author fmauz
 */
@SessionScoped
public class MateriaBeanMock extends MaterialBean {

    private static final long serialVersionUID = 1L;

    public MateriaBeanMock() {
        this.materialEditState = new MaterialEditState(new MessagePresenterMock());
    }

    public void createStorageInformationBuilder(MessagePresenter messagePresenter,
            MaterialService materialService,
            Material material) {
        this.storageInformationBuilder = new StorageInformationBuilder(messagePresenter, materialService, material);
    }

    boolean rightToEdit = true;

    public void setRightToEdit(boolean right) {
        this.rightToEdit = right;
    }

    public void setMaterialService(MaterialService materialService) {
        this.materialService = materialService;
    }

    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    public void setProjectBean(ProjectBean projectBean) {
        this.projectBean = projectBean;
    }

    public void setAcListService(ACListService acListService) {
        this.acListService = acListService;
    }

    public void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    public void setSequenceInfos(SequenceInformation sequenceInfos) {
        this.sequenceInfos = sequenceInfos;
    }

    @Override
    public void setMaterialNameBean(MaterialNameBean materialNameBean) {
        this.materialNameBean = materialNameBean;
    }

    @Override
    public void setMaterialIndexBean(MaterialIndexBean materialIndexBean) {
        this.materialIndexBean = materialIndexBean;
    }

    @Override
    public boolean hasDetailRight(ACPermission what, MaterialDetailType onWhat) {
        return rightToEdit;
    }

    public void setHazardService(HazardService service) {
        this.hazardService = service;
    }

    public MaterialCompositionBean getCompositionBean() {
        return compositionBean;
    }

    public void setMaterialEditState(MaterialEditState materialEditState) {
        this.materialEditState = materialEditState;
    }

    public void setHazardController(MaterialHazardBuilder hazardController) {
        this.hazardController = hazardController;
    }

    public void setTaxonomyService(TaxonomyService taxonomyService) {
        this.taxonomyService = taxonomyService;
    }

    public void setTissueService(TissueService tissueService) {
        this.tissueService = tissueService;
    }

    public void setTaxonomyController(TaxonomySelectionController taxonomyController) {
        this.taxonomyController = taxonomyController;
    }

    public void setCompositionBean(MaterialCompositionBean compositionBean) {
        this.compositionBean = compositionBean;
    }

    public void setHistoryOperation(HistoryOperation historyOperation) {
        this.historyOperation = historyOperation;
    }

    public MaterialOverviewBean getMaterialOverviewBean() {
        return overviewBean;
    }

}
