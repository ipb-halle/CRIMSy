/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.items.bean.aliquot.createsolution.consumepartofitem;

import java.io.Serializable;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.util.jsf.NumberFunctions;
import de.ipb_halle.lbac.util.units.Quantity;

/**
 * Controls the fourth step of the create solution wizard: The user defines the
 * direct container and the label (both optional).
 * 
 * @author flange
 */
public class ConsumePartOfItemStep4Controller implements Serializable {
    private static final long serialVersionUID = 1L;

    private final ConsumePartOfItemStep1Controller step1Controller;
    private final ConsumePartOfItemStep3Controller step3Controller;
    private final MessagePresenter messagePresenter;

    private boolean directContainer = false; // rw
    private Double directContainerSize; // rw
    private ContainerType directContainerType; // rw

    private boolean customLabel = false; // rw
    private String customLabelValue; // rw

    public ConsumePartOfItemStep4Controller(ConsumePartOfItemStep1Controller step1Controller,
            ConsumePartOfItemStep3Controller step3Controller, MessagePresenter messagePresenter) {
        this.step1Controller = step1Controller;
        this.step3Controller = step3Controller;
        this.messagePresenter = messagePresenter;
    }

    /*
     * Actions
     */
    public void actionOnChangeDirectContainerSize() {
        if (isDispensedVolumeGreaterThanContainerSize()) {
            presentContainerTooSmallError();
        }
    }

    public void presentContainerTooSmallError() {
        Quantity dispensedVolume = step3Controller.getDispensedVolumeAsQuantity();
        messagePresenter.error("itemCreateSolution_error_containerTooSmall",
                NumberFunctions.formatAmount(dispensedVolume.getValue()), dispensedVolume.getUnit().toString());
    }

    /*
     * Getters with logic
     */
    public boolean isDispensedVolumeGreaterThanContainerSize() {
        Quantity dispensedVolume = step3Controller.getDispensedVolumeAsQuantity();
        Quantity containerSize = Quantity.create(directContainerSize, step1Controller.getTargetVolumeUnit());

        return dispensedVolume.isGreaterThan(containerSize);
    }

    /*
     * Getters/setters
     */
    public boolean isDirectContainer() {
        return directContainer;
    }

    public void setDirectContainer(boolean directContainer) {
        this.directContainer = directContainer;
    }

    public Double getDirectContainerSize() {
        return directContainerSize;
    }

    public void setDirectContainerSize(Double directContainerSize) {
        this.directContainerSize = directContainerSize;
    }

    public ContainerType getDirectContainerType() {
        return directContainerType;
    }

    public void setDirectContainerType(ContainerType directContainerType) {
        this.directContainerType = directContainerType;
    }

    public boolean isCustomLabel() {
        return customLabel;
    }

    public void setCustomLabel(boolean customLabel) {
        this.customLabel = customLabel;
    }

    public String getCustomLabelValue() {
        return customLabelValue;
    }

    public void setCustomLabelValue(String customLabelValue) {
        this.customLabelValue = customLabelValue;
    }
}
