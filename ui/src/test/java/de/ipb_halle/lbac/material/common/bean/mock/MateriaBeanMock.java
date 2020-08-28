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
package de.ipb_halle.lbac.material.common.bean.mock;

import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.material.common.bean.MaterialIndexBean;
import de.ipb_halle.lbac.material.common.bean.MaterialNameBean;
import de.ipb_halle.lbac.material.common.bean.MaterialBean;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.structure.MoleculeService;
import de.ipb_halle.lbac.project.ProjectBean;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.admission.ACListService;

/**
 *
 * @author fmauz
 */
public class MateriaBeanMock extends MaterialBean {

    public void setMaterialService(MaterialService materialService) {
        this.materialService = materialService;
    }

    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    public void setMoleculeService(MoleculeService moleculeService) {
        this.moleculeService = moleculeService;
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

    @Override
    public void setMaterialNameBean(MaterialNameBean materialNameBean) {
        this.materialNameBean = materialNameBean;
    }

    @Override
    public void setMaterialIndexBean(MaterialIndexBean materialIndexBean) {
        
        this.materialIndexBean = materialIndexBean;
    }

    

}
