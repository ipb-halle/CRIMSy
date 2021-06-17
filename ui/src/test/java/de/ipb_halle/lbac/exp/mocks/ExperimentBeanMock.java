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
package de.ipb_halle.lbac.exp.mocks;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.exp.ExpRecordController;
import de.ipb_halle.lbac.exp.ExpRecordService;
import de.ipb_halle.lbac.exp.ExperimentBean;
import de.ipb_halle.lbac.exp.ExperimentService;
import de.ipb_halle.lbac.exp.ItemAgent;
import de.ipb_halle.lbac.exp.MaterialAgent;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.project.ProjectService;

/**
 *
 * @author fmauz
 */
public class ExperimentBeanMock extends ExperimentBean {

    public ExperimentBeanMock setGlobalAdmissionContext(GlobalAdmissionContext context) {
        this.globalAdmissionContext = context;
        return this;
    }

    public ExperimentBeanMock setExperimentService(ExperimentService service) {
        this.experimentService = service;
        return this;
    }

    public ExperimentBeanMock setExpRecordService(ExpRecordService service) {
        this.expRecordService = service;
        return this;
    }

    public ExperimentBeanMock setItemAgent(ItemAgent agent) {
        this.itemAgent = agent;
        return this;
    }

    public ExperimentBeanMock setMaterialAgent(MaterialAgent agent) {
        this.materialAgent = agent;
        return this;
    }

    public ExperimentBeanMock setMemberService(MemberService memberService) {
        this.memberService = memberService;
        return this;
    }

    public ExperimentBeanMock setProjectService(ProjectService projectService) {
        this.projectService = projectService;
        return this;
    }
    
    public ExperimentBeanMock setMessagePresenter(MessagePresenter messagePresenter) {
        this.messagePresenter=messagePresenter;
        return this;
    }

    public void setExpRecordController(ExpRecordController expRecordController) {
        this.expRecordController = expRecordController;
    }
}
