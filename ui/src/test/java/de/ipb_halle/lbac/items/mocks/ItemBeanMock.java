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
package de.ipb_halle.lbac.items.mocks;

import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.container.service.ContainerPositionService;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.device.print.PrintBean;
import de.ipb_halle.lbac.items.bean.ItemBean;
import de.ipb_halle.lbac.items.bean.ItemOverviewBean;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.service.NodeService;

/**
 *
 * @author fmauz
 */
public class ItemBeanMock extends ItemBean {

    public void setPrintBean(PrintBean bean) {
        this.printBean = bean;
    }

    public void setItemOverviewBean(ItemOverviewBean itemOverviewBean) {
        this.itemOverviewBean = itemOverviewBean;
    }

    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    public void setContainerService(ContainerService containerService) {
        this.containerService = containerService;
    }

    public void setContainerPositionService(ContainerPositionService containerPositionService) {
        this.containerPositionService = containerPositionService;
    }

    public void setNavigator(Navigator n) {
        this.navigator = n;
    }

    public void setItemService(ItemService itemService) {
        this.itemService = itemService;
    }

    public void setUserBean(UserBean u) {
        this.userBean = u;
    }

}
