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
package de.ipb_halle.lbac.items.bean;

import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.material.MessagePresenter;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author fmauz
 */
@RequestScoped
@Named
public class ContainerModalBean {

    private final String TYPE_PREFIX = "container_type_";

    @Inject
    private ContainerService service;

    @Inject
    private MessagePresenter messagePresenter;

    @Inject
    private UserBean userBean;

    /**
     * Loads the containers available to the user without the included items
     *
     * @return
     */
    public List<Container> getContainers() {
        List<Container> containers = service.loadContainersWithoutItems(
                userBean.getCurrentAccount());
        localizeContainerTypes(containers);
        return containers;
    }

    /**
     * Generates a string of container size of the form 'h x b' . Container
     * without size return '-'
     *
     * @param c
     * @return
     */
    public String getDimensionString(Container c) {
        if (c.getItems() != null && c.getItems().length > 0) {
            return String.format("%d x %d", c.getItems().length, c.getItems()[0].length);
        }
        return "-";
    }

    private void localizeContainerTypes(List<Container> containersToLocalize) {
        for (Container container : containersToLocalize) {
            container.getType().setLocalizedName(
                    messagePresenter.presentMessage(
                            TYPE_PREFIX + container.getType().getName()));
            localizeContainerTypes(container.getContainerHierarchy());
        }
    }

}
