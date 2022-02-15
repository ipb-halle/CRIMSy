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

import static de.ipb_halle.lbac.util.units.Quality.MASS;
import static de.ipb_halle.lbac.util.units.Quality.MASS_CONCENTRATION;
import static de.ipb_halle.lbac.util.units.Quality.MOLAR_CONCENTRATION;
import static de.ipb_halle.lbac.util.units.Quality.VOLUME;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.util.calculation.MassConcentrationCalculations;
import de.ipb_halle.lbac.util.calculation.MolarConcentrationCalculations;
import de.ipb_halle.lbac.util.units.Quantity;
import de.ipb_halle.lbac.util.units.Unit;

/**
 * 
 * @author flange
 */
public class InputConcentrationAndVolumeStepController implements Serializable {
    private static final long serialVersionUID = 1L;

    private final MessagePresenter messagePresenter;
    private final Item parentItem;

    private List<Unit> availableVolumeUnits; // r
    private List<Unit> availableMassUnits; // r
    private List<Unit> availableConcentrationUnits; // r

    private Double targetConcentration; // rw
    private Unit targetConcentrationUnit; // rw
    private Double targetVolume; // rw
    private Unit targetVolumeUnit; // rw
    private Double targetMass; // r
    private Unit targetMassUnit; // rw
    private Double availableMassFromItem; // r
    private Unit availableMassFromItemUnit; // r

    private boolean userChangedMassUnit = false;

    public InputConcentrationAndVolumeStepController(Item parentItem, MessagePresenter messagePresenter) {
        this.parentItem = parentItem;
        this.messagePresenter = messagePresenter;

        init();
    }

    private void init() {
        availableVolumeUnits = Unit.getVisibleUnitsOfQuality(VOLUME);
        availableMassUnits = Unit.getVisibleUnitsOfQuality(MASS);

        List<Unit> massConcentrations = Unit.getVisibleUnitsOfQuality(MASS_CONCENTRATION);
        List<Unit> molarConcentrations = Unit.getVisibleUnitsOfQuality(MOLAR_CONCENTRATION);
        availableConcentrationUnits = new ArrayList<>();
        if (molarMassFromParentItem() != null) {
            availableConcentrationUnits.addAll(molarConcentrations);
            availableConcentrationUnits.addAll(massConcentrations);
            targetConcentrationUnit = Unit.getUnit("mM");
        } else {
            availableConcentrationUnits.addAll(massConcentrations);
            targetConcentrationUnit = Unit.getUnit("g/l");
        }

        targetVolumeUnit = Unit.getUnit("ml");

        Quantity massFromItem = massFromParentItem();
        if (massFromItem != null) {
            targetMassUnit = massFromItem.getUnit();
            availableMassFromItem = massFromItem.getValue();
            availableMassFromItemUnit = massFromItem.getUnit();
        }
    }

    private Quantity massFromParentItem() {
        return parentItem.getAmountAsQuantity();
    }

    /**
     * @return quantity with molar mass or null if the item does not have a molar
     *         mass
     */
    private Quantity molarMassFromParentItem() {
        Material materialOfItem = parentItem.getMaterial();
        if (materialOfItem == null) {
            return null;
        }
        if (!MaterialType.STRUCTURE.equals(materialOfItem.getType())) {
            return null;
        }

        return ((Structure) materialOfItem).getAverageMolarMassAsQuantity();
    }

    /*
     * Actions
     */
    public void actionUpdateTargetMass() {
        targetMass = null;

        Quantity concentration = Quantity.create(targetConcentration, targetConcentrationUnit);
        Quantity volume = Quantity.create(targetVolume, targetVolumeUnit);

        if ((concentration == null) || (volume == null)) {
            return;
        }

        Quantity targetMassAsQuantity = calculateTargetMass(concentration, volume, targetMassUnit);
        if (!userChangedMassUnit) {
            targetMassAsQuantity = targetMassAsQuantity.toHumanReadableUnit(Unit.getVisibleUnitsOfQuality(MASS));
        }
        targetMass = targetMassAsQuantity.getValue();
        targetMassUnit = targetMassAsQuantity.getUnit();

        if (isTargetMassGreaterThanItemMass()) {
            messagePresenter.error("itemCreateSolution_error_targetMassTooHigh");
        }
    }

    public void actionOnChangeTargetMassUnit() {
        userChangedMassUnit = true;
        actionUpdateTargetMass();
    }

    private Quantity calculateTargetMass(Quantity concentration, Quantity volume, Unit targetMassUnit) {
        Quantity molarMass = molarMassFromParentItem();
        if ((molarMass == null) || (MASS_CONCENTRATION == concentration.getUnit().getQuality())) {
            return MassConcentrationCalculations.calculateMass(concentration, volume, targetMassUnit);
        } else {
            return MolarConcentrationCalculations.calculateMass(concentration, molarMass, volume, targetMassUnit);
        }
    }

    /*
     * Getters with logic
     */
    public boolean isTargetMassGreaterThanItemMass() {
        Quantity massFromItem = massFromParentItem();
        Quantity targetMassAsQuantity = new Quantity(targetMass, targetMassUnit);

        if (targetMassAsQuantity.isGreaterThan(massFromItem)) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * Getters/setters
     */
    public List<Unit> getAvailableVolumeUnits() {
        return availableVolumeUnits;
    }

    public List<Unit> getAvailableMassUnits() {
        return availableMassUnits;
    }

    public List<Unit> getAvailableConcentrationUnits() {
        return availableConcentrationUnits;
    }

    public Double getTargetConcentration() {
        return targetConcentration;
    }

    public void setTargetConcentration(Double targetConcentration) {
        this.targetConcentration = targetConcentration;
    }

    public Unit getTargetConcentrationUnit() {
        return targetConcentrationUnit;
    }

    public void setTargetConcentrationUnit(Unit targetConcentrationUnit) {
        this.targetConcentrationUnit = targetConcentrationUnit;
    }

    public Double getTargetVolume() {
        return targetVolume;
    }

    public void setTargetVolume(Double targetVolume) {
        this.targetVolume = targetVolume;
    }

    public Unit getTargetVolumeUnit() {
        return targetVolumeUnit;
    }

    public void setTargetVolumeUnit(Unit targetVolumeUnit) {
        this.targetVolumeUnit = targetVolumeUnit;
    }

    public Double getTargetMass() {
        return targetMass;
    }

    public Unit getTargetMassUnit() {
        return targetMassUnit;
    }

    public void setTargetMassUnit(Unit targetMassUnit) {
        this.targetMassUnit = targetMassUnit;
    }

    public Double getAvailableMassFromItem() {
        return availableMassFromItem;
    }

    public Unit getAvailableMassFromItemUnit() {
        return availableMassFromItemUnit;
    }
}