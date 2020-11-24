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

import de.ipb_halle.lbac.container.bean.ErrorMessagePresenter;
import de.ipb_halle.lbac.container.service.ContainerPositionService;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.label.LabelService;

/**
 *
 * @author fmauz
 */
public class Validator {

    private ErrorMessagePresenter messagePresenter;
    boolean valide = true;

    private final ContainerPositionService containerPositionService;
    private final LabelService labelService;

    public Validator(ContainerPositionService containerPositionService, LabelService labelService) {
        this.containerPositionService = containerPositionService;
        this.labelService = labelService;
        messagePresenter = new ErrorMessagePresenter();
    }

    public boolean itemValideToSave(
            Item itemToCheck,
            ContainerController containerController,
            boolean customLabel,
            String customLabelValue) {
        valide = true;
        boolean areSlotsEmpty = containerPositionService.areContainerSlotsFree(
                itemToCheck,
                containerController.getContainer(),
                containerController.resolveItemPositions());
        if (!areSlotsEmpty) {
            messagePresenter.presentErrorMessage("itemEdit_container_blocked");
            valide = false;
        }
        if (customLabel && !labelService.isLabelAvailable(customLabelValue)) {
            messagePresenter.presentErrorMessage("itemEdit_label_unavailable");
            valide = false;
        }
        return valide;
    }

    public void setMessagePresenter(ErrorMessagePresenter messagePresenter) {
        this.messagePresenter = messagePresenter;
    }

}
