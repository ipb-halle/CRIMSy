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
import de.ipb_halle.lbac.container.service.ContainerPositionService;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.label.LabelService;
import de.ipb_halle.lbac.material.JsfMessagePresenter;
import de.ipb_halle.lbac.material.MessagePresenter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class Validator {

    private MessagePresenter messagePresenter;
    boolean valid = true;
    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private final ContainerPositionService containerPositionService;
    private final LabelService labelService;

    public Validator(ContainerPositionService containerPositionService, LabelService labelService) {
        this.containerPositionService = containerPositionService;
        this.labelService = labelService;
        messagePresenter = JsfMessagePresenter.getInstance();
    }

    public boolean itemValidToSave(
            Item itemToCheck,
            ContainerController containerController,
            boolean customLabel,
            String customLabelValue) {
        valid = true;
        boolean areSlotsEmpty = containerPositionService.areContainerSlotsFree(
                itemToCheck,
                containerController.getContainer(),
                containerController.resolveItemPositions());
        if (!areSlotsEmpty) {
            messagePresenter.error("itemEdit_container_blocked");
            valid = false;
        }
        if (customLabel && !labelService.isLabelAvailable(customLabelValue)) {
            messagePresenter.error("itemEdit_label_unavailable");
            valid = false;
        }
        if (containerWithPlaces(containerController.getContainer())) {
            if (containerController.resolveItemPositions().isEmpty()) {
                messagePresenter.error("itemEdit_item_not_placed");
                valid = false;
            }
        }
        return valid;
    }

    private boolean containerWithPlaces(Container c) {
        return c != null
                && c.getRows() != null
                && c.getRows() > 0;
    }

    public void setMessagePresenter(MessagePresenter messagePresenter) {
        this.messagePresenter = messagePresenter;
    }

}
