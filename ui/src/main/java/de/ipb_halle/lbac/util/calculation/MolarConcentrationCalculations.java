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
package de.ipb_halle.lbac.util.calculation;

import static de.ipb_halle.lbac.util.units.Quality.AMOUNT_OF_SUBSTANCE;
import static de.ipb_halle.lbac.util.units.Quality.MASS;
import static de.ipb_halle.lbac.util.units.Quality.MASS_CONCENTRATION;
import static de.ipb_halle.lbac.util.units.Quality.MOLAR_CONCENTRATION;
import static de.ipb_halle.lbac.util.units.Quality.MOLAR_MASS;
import static de.ipb_halle.lbac.util.units.Quality.VOLUME;

import de.ipb_halle.lbac.util.units.Quantity;
import de.ipb_halle.lbac.util.units.Unit;

/**
 * Calculations related to the formula c = m / (M * V).
 * 
 * @author flange
 */
public class MolarConcentrationCalculations {
    private MolarConcentrationCalculations() {
    }

    /**
     * c = m / (M * V)
     * 
     * @param mass      m
     * @param molarMass M
     * @param volume    V
     * @return molar concentration c in the base unit (mol/m^3)
     */
    public static Quantity calculateMolarConcentration(Quantity mass, Quantity molarMass, Quantity volume) {
        mass.require(MASS);
        molarMass.require(MOLAR_MASS);
        volume.require(VOLUME);
        return mass.divide(molarMass, AMOUNT_OF_SUBSTANCE).divide(volume, MOLAR_CONCENTRATION);
    }

    /**
     * c = m / (M * V)
     * 
     * @param mass                   m
     * @param molarMass              M
     * @param volume                 V
     * @param molarConcentrationUnit
     * @return molar concentration c in the given unit
     */
    public static Quantity calculateMolarConcentration(Quantity mass, Quantity molarMass, Quantity volume,
            Unit molarConcentrationUnit) {
        molarConcentrationUnit.require(MOLAR_CONCENTRATION);
        return calculateMolarConcentration(mass, molarMass, volume).to(molarConcentrationUnit);
    }

    /**
     * m = c * M * V
     * 
     * @param molarConcentration c
     * @param molarMass          M
     * @param volume             V
     * @return mass m in the base unit (kg)
     */
    public static Quantity calculateMass(Quantity molarConcentration, Quantity molarMass, Quantity volume) {
        molarConcentration.require(MOLAR_CONCENTRATION);
        molarMass.require(MOLAR_MASS);
        volume.require(VOLUME);
        return molarConcentration.multiply(molarMass, MASS_CONCENTRATION).multiply(volume, MASS);
    }

    /**
     * m = c * M * V
     * 
     * @param molarConcentration c
     * @param molarMass          M
     * @param volume             V
     * @param massUnit
     * @return mass m in the given unit
     */
    public static Quantity calculateMass(Quantity molarConcentration, Quantity molarMass, Quantity volume,
            Unit massUnit) {
        massUnit.require(MASS);
        return calculateMass(molarConcentration, molarMass, volume).to(massUnit);
    }

    /**
     * V = m / (c * M)
     * 
     * @param mass               m
     * @param molarConcentration c
     * @param molarMass          M
     * @return volume V in the base unit (m^3)
     */
    public static Quantity calculateVolume(Quantity mass, Quantity molarConcentration, Quantity molarMass) {
        mass.require(MASS);
        molarConcentration.require(MOLAR_CONCENTRATION);
        molarMass.require(MOLAR_MASS);
        return mass.divide(molarMass, AMOUNT_OF_SUBSTANCE).divide(molarConcentration, VOLUME);
    }

    /**
     * V = m / (c * M)
     * 
     * @param mass               m
     * @param molarConcentration c
     * @param molarMass          M
     * @param volumeUnit
     * @return volume in the given unit
     */
    public static Quantity calculateVolume(Quantity mass, Quantity molarConcentration, Quantity molarMass,
            Unit volumeUnit) {
        volumeUnit.require(VOLUME);
        return calculateVolume(mass, molarConcentration, molarMass).to(volumeUnit);
    }
}