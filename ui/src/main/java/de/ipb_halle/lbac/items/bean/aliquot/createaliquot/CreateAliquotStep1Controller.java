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
package de.ipb_halle.lbac.items.bean.aliquot.createaliquot;

import java.io.Serializable;
import java.util.List;

import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.util.units.Quantity;
import de.ipb_halle.lbac.util.units.Unit;

/**
 * Controls the first step of the create aliquot wizard: The user inputs the
 * amount of the aliquot.
 * 
 * @author flange
 */
public class CreateAliquotStep1Controller implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Unit> availableAmountUnits; // r

    private Double availableAmountFromItem; // r
    private Unit availableAmountFromItemUnit; // r
    private Double amount; // rw
    private Unit amountUnit; // rw

    public CreateAliquotStep1Controller(Item parentItem) {
        availableAmountFromItem = parentItem.getAmount();
        availableAmountFromItemUnit = parentItem.getUnit();

        availableAmountUnits = Unit.getVisibleUnitsOfQuality(availableAmountFromItemUnit.getQuality());
    }

    /*
     * Getters with logic
     */
    public boolean isAmountGreaterThanItemAmount() {
        Quantity amountFromItemAsQuantity = Quantity.create(availableAmountFromItem, availableAmountFromItemUnit);
        Quantity amountAsQuantity = getAmountAsQuantity();

        if ((amountFromItemAsQuantity == null) || (amountAsQuantity == null)) {
            return true;
        }

        return amountAsQuantity.isGreaterThan(amountFromItemAsQuantity);
    }

    public Quantity getAmountAsQuantity() {
        return Quantity.create(amount, amountUnit);
    }

    /*
     * Getters/setters
     */
    public List<Unit> getAvailableAmountUnits() {
        return availableAmountUnits;
    }

    public Double getAvailableAmountFromItem() {
        return availableAmountFromItem;
    }

    public Unit getAvailableAmountFromItemUnit() {
        return availableAmountFromItemUnit;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Unit getAmountUnit() {
        return amountUnit;
    }

    public void setAmountUnit(Unit amountUnit) {
        this.amountUnit = amountUnit;
    }
}