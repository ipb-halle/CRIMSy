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
import de.ipb_halle.lbac.container.Container.DimensionType;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.MessagePresenter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.SelectEvent;

/**
 * Handles the rendering and the actions for manipulating the container of an
 * item and its position in it
 *
 * @author fmauz
 */
public class ContainerController {
    private Logger logger = LogManager.getLogger(this.getClass().getName());

    private final Item originalItem;
    private final ContainerService containerService;
    private final MessagePresenter messagePresenter;

    private final List<Container> availableContainers;
    private final ContainerSelectionDialogController containerSelectionDialogController;
    private Container2dController container2dController;

    private Container container;
    private ContainerInfoPresenter containerInfoPresenter;

    public ContainerController(Item originalItem, ContainerService containerService, UserBean userBean,
            MessagePresenter messagePresenter) {
        this.originalItem = originalItem;
        this.containerService = containerService;
        this.messagePresenter = messagePresenter;

        availableContainers = containerService.loadContainersWithoutItems(userBean.getCurrentAccount());
        containerSelectionDialogController = new ContainerSelectionDialogController(availableContainers,
                (c) -> this.actionChangeContainer(c), messagePresenter);

        actionChangeContainer(this.originalItem.getContainer());
    }

    /*
     * Actions
     */
    public void actionOnItemSelect(SelectEvent event) {
        String selectedContainerName = (String) event.getObject();
        int containerId = Integer.parseInt(selectedContainerName.split("-")[0]);
        Container c = containerService.loadContainerById(containerId);

        actionChangeContainer(c);
    }

    public void actionRemoveContainer() {
        actionChangeContainer(null);
    }

    /**
     * Creates a new container with empty itemPositions (default false) and creates
     * the index lists for rows and cols(for ui:repeat) .
     *
     * @param c
     */
    public void actionChangeContainer(Container c) {
        container = c;

        containerInfoPresenter = new ContainerInfoPresenter(container, messagePresenter);
        container2dController = new Container2dController(container, originalItem, messagePresenter);
    }

    /*
     * Getters with logic
     */
    public List<String> nameSuggestions(String enteredValue) {
        List<String> matches = new ArrayList<>();
        List<String> names = new ArrayList<>();
        // TODO: cache the list of lowercase names
        for (Container c : availableContainers) {
            names.add(c.getAutoCompleteString());
        }
        for (String s : names) {
            if (enteredValue != null
                    && (enteredValue.trim().isEmpty() || s.toLowerCase().contains(enteredValue.toLowerCase()))) {
                matches.add(s);
            }
        }
        return matches;
    }

    public boolean isContainerSubComponentRendered(String typename) {
        if (typename == null) {
            return container == null;
        }
        DimensionType type = DimensionType.valueOf(typename);
        return container != null && container.getDimensionType() == type;
    }

    public boolean[][] getItemPositions() {
        return container2dController.getItemPositions();
    }

    public Set<int[]> resolveItemPositions() {
        return container2dController.resolveItemPositions();
    }

    /*
     * Getters/setters
     */
    public Container getContainer() {
        return container;
    }

    public ContainerInfoPresenter getContainerInfoPresenter() {
        return containerInfoPresenter;
    }

    public ContainerSelectionDialogController getContainerSelectionDialogController() {
        return containerSelectionDialogController;
    }

    public Container2dController getContainer2dController() {
        return container2dController;
    }
}
