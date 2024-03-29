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

import static de.ipb_halle.lbac.util.units.Quality.MASS_CONCENTRATION;

import java.io.Serializable;

import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.ItemUtils;
import de.ipb_halle.lbac.items.Solvent;
import de.ipb_halle.lbac.util.calculation.MassConcentrationCalculations;
import de.ipb_halle.lbac.util.calculation.MolarConcentrationCalculations;
import de.ipb_halle.lbac.util.units.Quantity;
import de.ipb_halle.lbac.util.units.Unit;

/**
 * Controls the third step of the create solution wizard: The volume to dispense
 * is shown and the user inputs the dispensed volume. The controller calculates
 * the final concentration from the previously defined weight and the dispensed
 * volume. The solvent is also stored here.
 * 
 * @author flange
 */
public class ConsumePartOfItemStep3Controller implements Serializable {
    private static final long serialVersionUID = 1L;

    private final ConsumePartOfItemStep1Controller step1Controller;
    private final ConsumePartOfItemStep2Controller step2Controller;
    private final Item parentItem;

    private Double dispensedVolume; // rw
    private Solvent solvent; // rw

    public ConsumePartOfItemStep3Controller(ConsumePartOfItemStep1Controller step1Controller,
            ConsumePartOfItemStep2Controller step2Controller, Item parentItem) {
        this.step1Controller = step1Controller;
        this.step2Controller = step2Controller;
        this.parentItem = parentItem;
    }

    /*
     * Actions
     */
    public void init() {
        dispensedVolume = getVolumeToDispense();
    }

    /*
     * Getters with logic
     */
    public Double getVolumeToDispense() {
        return calculateVolumeToDispense().getValue();
    }

    private Quantity calculateVolumeToDispense() {
        Quantity concentration = step1Controller.getTargetConcentrationAsQuantity();
        Quantity mass = step2Controller.getWeightAsQuantity();
        Unit volumeUnit = step1Controller.getTargetVolumeUnit();

        if (MASS_CONCENTRATION == concentration.getUnit().getQuality()) {
            return MassConcentrationCalculations.calculateVolume(mass, concentration, volumeUnit);
        } else {
            Quantity molarMass = ItemUtils.molarMassFromItem(parentItem);
            return MolarConcentrationCalculations.calculateVolume(mass, concentration, molarMass, volumeUnit);
        }
    }

    public Double getFinalConcentration() {
        return calculateFinalConcentration().getValue();
    }

    private Quantity calculateFinalConcentration() {
        Quantity mass = step2Controller.getWeightAsQuantity();
        Quantity volume = Quantity.create(dispensedVolume, step1Controller.getTargetVolumeUnit());
        Unit concentrationUnit = step1Controller.getTargetConcentrationUnit();

        if (MASS_CONCENTRATION == concentrationUnit.getQuality()) {
            return MassConcentrationCalculations.calculateMassConcentration(mass, volume, concentrationUnit);
        } else {
            Quantity molarMass = ItemUtils.molarMassFromItem(parentItem);
            return MolarConcentrationCalculations.calculateMolarConcentration(mass, molarMass, volume,
                    concentrationUnit);
        }
    }

    public Quantity getDispensedVolumeAsQuantity() {
        return Quantity.create(dispensedVolume, step1Controller.getTargetVolumeUnit());
    }

    /*
     * Getters/setters
     */
    public Double getDispensedVolume() {
        return dispensedVolume;
    }

    public void setDispensedVolume(Double dispensedVolume) {
        this.dispensedVolume = dispensedVolume;
    }

    public Solvent getSolvent() {
        return solvent;
    }

    public void setSolvent(Solvent solvent) {
        this.solvent = solvent;
    }
}
