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

import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.service.ContainerService;
import java.util.ArrayList;
import java.util.List;
import org.primefaces.event.SelectEvent;

/**
 *
 * @author fmauz
 */
public class ContainerPresenter {

    private ItemBean itemBean;
    private String containerName;
    private ContainerService containerService;
    private List<Container> containers;

    public ContainerPresenter(ItemBean itemBean, String containerName, ContainerService containerService, List<Container> containers) {
        this.itemBean = itemBean;
        this.containerName = containerName;
        this.containerService = containerService;
        this.containers = containers;
    }

    public void actionChangeContainer(Container c) {
        itemBean.setContainerController(new ContainerController(itemBean, c));

        this.containerName = c.getLabel();

    }

    public void onItemSelect(SelectEvent event) {
        containerName = (String) event.getObject();
        int containerId = Integer.parseInt(containerName.split("-")[0]);
        containerService.loadContainerById(containerId);
        Container c = containerService.loadContainerById(containerId);
        actionChangeContainer(c);

    }

    public List<String> nameSuggestions(String enteredValue) {
        List<String> matches = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (Container c : containers) {
            names.add(c.getAutoCompleteString());
        }
        for (String s : names) {
            if (enteredValue != null && (enteredValue.trim().isEmpty() || s.toLowerCase().contains(enteredValue.toLowerCase()))) {
                matches.add(s);
            }
        }
        return matches;
    }

    public void removeContainer() {
        ContainerController newController = new ContainerController(itemBean, null);
        itemBean.setContainerController(newController);
        itemBean.setContainerInfoPresenter(new ContainerInfoPresenter(null));
    }
}
