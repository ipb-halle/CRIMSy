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

import static de.ipb_halle.lbac.util.units.Quality.MASS;
import static de.ipb_halle.lbac.util.units.Quality.MASS_CONCENTRATION;
import static de.ipb_halle.lbac.util.units.Quality.VOLUME;

import de.ipb_halle.lbac.util.units.Quantity;
import de.ipb_halle.lbac.util.units.Unit;

/**
 * Calculations related to the formula c = m / V.
 * 
 * @author flange
 */
public class MassConcentrationCalculations {
    private MassConcentrationCalculations() {
    }

    /**
     * c = m / V
     * 
     * @param mass   m
     * @param volume V
     * @return mass concentration c in the base unit (kg/m^3)
     */
    public static Quantity calculateMassConcentration(Quantity mass, Quantity volume) {
        mass.require(MASS);
        volume.require(VOLUME);
        return mass.divide(volume, MASS_CONCENTRATION);
    }

    /**
     * c = m / V
     * 
     * @param mass                  m
     * @param volume                V
     * @param massConcentrationUnit
     * @return mass concentration c in the given unit
     */
    public static Quantity calculateMassConcentration(Quantity mass, Quantity volume, Unit massConcentrationUnit) {
        massConcentrationUnit.require(MASS_CONCENTRATION);
        return calculateMassConcentration(mass, volume).to(massConcentrationUnit);
    }

    /**
     * m = c * V
     * 
     * @param massConcentration c
     * @param volume            V
     * @return mass m in the base unit (kg)
     */
    public static Quantity calculateMass(Quantity massConcentration, Quantity volume) {
        massConcentration.require(MASS_CONCENTRATION);
        volume.require(VOLUME);
        return massConcentration.multiply(volume, MASS);
    }

    /**
     * m = c * V
     * 
     * @param massConcentration c
     * @param volume            V
     * @param massUnit
     * @return mass m in the given unit
     */
    public static Quantity calculateMass(Quantity massConcentration, Quantity volume, Unit massUnit) {
        massUnit.require(MASS);
        return calculateMass(massConcentration, volume).to(massUnit);
    }

    /**
     * V = m / c
     * 
     * @param mass              m
     * @param massConcentration c
     * @return volume V in the base unit (m^3)
     */
    public static Quantity calculateVolume(Quantity mass, Quantity massConcentration) {
        mass.require(MASS);
        massConcentration.require(MASS_CONCENTRATION);
        return mass.divide(massConcentration, VOLUME);
    }

    /**
     * V = m / c
     * 
     * @param mass              m
     * @param massConcentration c
     * @param volumeUnit
     * @return volume V in the given unit
     */
    public static Quantity calculateVolume(Quantity mass, Quantity massConcentration, Unit volumeUnit) {
        volumeUnit.require(VOLUME);
        return calculateVolume(mass, massConcentration).to(volumeUnit);
    }
}