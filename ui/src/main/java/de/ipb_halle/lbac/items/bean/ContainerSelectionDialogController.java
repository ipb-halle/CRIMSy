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
import de.ipb_halle.lbac.material.MessagePresenter;
import java.util.List;
import java.util.function.Consumer;

import javax.faces.context.FacesContext;

/**
 * Controller for the composite component containerModal.xhtml.
 * 
 * @author fmauz
 */
public class ContainerSelectionDialogController {
    private static final String TYPE_PREFIX = "container_type_";

    private final List<Container> availableContainers;
    private final Consumer<Container> onSelectCallback;
    private final MessagePresenter messagePresenter;

    /**
     * 
     * @param availableContainers
     * @param onSelectCallback    function to call when the datatable's onselect
     *                            AJAX event is fired
     * @param messagePresenter
     */
    public ContainerSelectionDialogController(List<Container> availableContainers, Consumer<Container> onSelectCallback,
            MessagePresenter messagePresenter) {
        this.availableContainers = availableContainers;
        this.onSelectCallback = onSelectCallback;
        this.messagePresenter = messagePresenter;

        localizeContainerTypes(availableContainers);
    }

    private void localizeContainerTypes(List<Container> containersToLocalize) {
        for (Container container : containersToLocalize) {
            container.getType()
                    .setLocalizedName(messagePresenter.presentMessage(TYPE_PREFIX + container.getType().getName()));
            localizeContainerTypes(container.getContainerHierarchy());
        }
    }

    /*
     * Actions
     */
    public void actionOnSelect() {
        String indexAsString = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap()
                .get("selectedRow");
        int index = -1;

        try {
            index = Integer.parseInt(indexAsString);
        } catch (NumberFormatException e) {
            return;
        }
        if ((index < 0) || index >= availableContainers.size()) {
            return;
        }

        Container selectedContainer = availableContainers.get(index);
        onSelectCallback.accept(selectedContainer);
    }

    /*
     * Getters with logic
     */
    /**
     * Generates a string of container size of the form 'h x b' . Container without
     * size return '-'
     *
     * @param c
     * @return
     */
    public String getDimensionString(Container c) {
        if (c.getRows() != null && c.getRows() > 0 && c.getColumns() != null && c.getColumns() > 0) {
            return String.format("%d x %d", c.getItems().length, c.getItems()[0].length);
        }
        return "-";
    }

    /*
     * Getters
     */
    public List<Container> getAvailableContainers() {
        return availableContainers;
    }
}
