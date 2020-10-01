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
package de.ipb_halle.lbac.container.mock;

import de.ipb_halle.lbac.container.bean.ContainerEditBean;
import de.ipb_halle.lbac.container.bean.ContainerOverviewBean;
import de.ipb_halle.lbac.container.bean.ContainerSearchMaskBean;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.project.ProjectService;

/**
 *
 * @author fmauz
 */
public class ContainerOverviewBeanMock extends ContainerOverviewBean {

    public ContainerOverviewBeanMock() {
        this.localizer = new ContainerLocalizerMock();
    }

    public ContainerOverviewBeanMock setContainerService(ContainerService service) {
        this.containerService = service;
        return this;
    }

    public ContainerOverviewBeanMock setContainerEditBean(ContainerEditBean bean) {
        this.editBean = bean;
        return this;
    }

    public ContainerOverviewBeanMock setProjectService(ProjectService service) {
        projectService = service;
        return this;
    }

    public ContainerOverviewBeanMock setContainerSearchMaskBean(ContainerSearchMaskBean bean) {
        this.searchMaskBean = bean;
        return this;
    }

}
