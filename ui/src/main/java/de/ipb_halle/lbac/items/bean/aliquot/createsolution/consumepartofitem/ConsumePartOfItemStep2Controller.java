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

import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.util.units.Quantity;
import de.ipb_halle.lbac.util.units.Unit;

/**
 * Controls the second step of the create solution wizard: The target mass is
 * shown with a user-defined margin and the user inputs the weight.
 * 
 * @author flange
 */
public class ConsumePartOfItemStep2Controller implements Serializable {
    private static final long serialVersionUID = 1L;

    private final ConsumePartOfItemStep1Controller step1Controller;
    private final Item parentItem;
    private final MessagePresenter messagePresenter;

    private int errorMargin = 5; // rw
    private Double weight; // rw
    private Unit weightUnit; // rw

    public ConsumePartOfItemStep2Controller(ConsumePartOfItemStep1Controller step1Controller, Item parentItem,
            MessagePresenter messagePresenter) {
        this.step1Controller = step1Controller;
        this.parentItem = parentItem;
        this.messagePresenter = messagePresenter;
    }

    /*
     * Actions
     */
    public void init() {
        if (weightUnit == null) {
            weightUnit = step1Controller.getTargetMassUnit();
        }
    }

    public void actionWeightChange() {
        if (isWeightGreaterThanItemMass()) {
            messagePresenter.error("itemCreateSolution_error_weightTooHigh");
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

    public boolean isWeightGreaterThanItemMass() {
        Quantity massFromItem = parentItem.getAmountAsQuantity();
        Quantity weightAsQuantity = getWeightAsQuantity();

        return weightAsQuantity.isGreaterThan(massFromItem);
    }

    public Quantity getWeightAsQuantity() {
        return Quantity.create(weight, weightUnit);
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

    public Double getWeight() {
        return weight;
    }

    public void setWeight(Double weight) {
        this.weight = weight;
    }

    public Unit getWeightUnit() {
        return weightUnit;
    }

    public void setWeightUnit(Unit weightUnit) {
        this.weightUnit = weightUnit;
    }
}