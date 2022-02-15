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
package de.ipb_halle.lbac.items.bean.createsolution.consumepartofitem;

import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.util.units.Quantity;
import de.ipb_halle.lbac.util.units.Unit;

/**
 * 
 * @author flange
 */
public class InputWeightStepController {
    private final InputConcentrationAndVolumeStepController step1Controller;
    private final Item parentItem;
    private final MessagePresenter messagePresenter;

    private int errorMargin = 5; // rw
    private Double weigh; // rw
    private Unit weighUnit; // rw

    public InputWeightStepController(InputConcentrationAndVolumeStepController step1Controller, Item parentItem,
            MessagePresenter messagePresenter) {
        this.step1Controller = step1Controller;
        this.parentItem = parentItem;
        this.messagePresenter = messagePresenter;
    }

    /*
     * Actions
     */
    public void actionWeighChange() {
        if (isWeighGreaterThanItemMass()) {
            messagePresenter.error("itemCreateSolution_error_weighTooHigh");
        }
    }

    public void init() {
        if (weighUnit == null) {
            weighUnit = step1Controller.getTargetMassUnit();
        }
    }

    /*
     * Getters with logic
     */
    public Double getTargetMassPlusMargin() {
        Double targetMass = step1Controller.getTargetMass();
        if (targetMass == null) {
            return null;
        }
        return targetMass * (1.0 + (errorMargin / 100.0));
    }

    public Double getTargetMassMinusMargin() {
        Double targetMass = step1Controller.getTargetMass();
        if (targetMass == null) {
            return null;
        }
        return targetMass * (1.0 - (errorMargin / 100.0));
    }

    public boolean isWeighGreaterThanItemMass() {
        Quantity massFromItem = parentItem.getAmountAsQuantity();
        Quantity weighAsQuantity = new Quantity(weigh, weighUnit);

        if (weighAsQuantity.isGreaterThan(massFromItem)) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * Getters/setters
     */
    public int getErrorMargin() {
        return errorMargin;
    }

    public void setErrorMargin(int errorMargin) {
        this.errorMargin = errorMargin;
    }

    public Double getWeigh() {
        return weigh;
    }

    public void setWeigh(Double weigh) {
        this.weigh = weigh;
    }

    public Unit getWeighUnit() {
        return weighUnit;
    }

    public void setWeighUnit(Unit weighUnit) {
        this.weighUnit = weighUnit;
    }
}