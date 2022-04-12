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

import static de.ipb_halle.lbac.util.calculation.MolarConcentrationCalculations.calculateMass;
import static de.ipb_halle.lbac.util.calculation.MolarConcentrationCalculations.calculateMolarConcentration;
import static de.ipb_halle.lbac.util.calculation.MolarConcentrationCalculations.calculateVolume;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import de.ipb_halle.lbac.util.units.Quantity;
import de.ipb_halle.lbac.util.units.Unit;

/**
 * @author flange
 */
class MolarConcentrationCalculationsTest {
    Quantity oneMeter = Quantity.create(1.0, "m");

    @Test
    public void test_calculateMolarConcentration() {
        Quantity mass = Quantity.create(200.0, "mg");
        Quantity molarMass = Quantity.create(58.44, "g/mol");
        Quantity volume = Quantity.create(150.0, "µl");
        Quantity molarConcentration;

        molarConcentration = calculateMolarConcentration(mass, molarMass, volume);
        assertEquals(22815.423, molarConcentration.getValue(), 1e-3);
        assertEquals("mol/m^3", molarConcentration.getUnit().toString());

        molarConcentration = calculateMolarConcentration(mass, molarMass, volume, Unit.getUnit("M"));
        assertEquals(22.815423, molarConcentration.getValue(), 1e-6);
        assertEquals("M", molarConcentration.getUnit().toString());

        // validations
        assertThrows(IllegalArgumentException.class, () -> calculateMolarConcentration(oneMeter, molarMass, volume));
        assertThrows(IllegalArgumentException.class, () -> calculateMolarConcentration(mass, oneMeter, volume));
        assertThrows(IllegalArgumentException.class, () -> calculateMolarConcentration(mass, molarMass, oneMeter));

        assertThrows(IllegalArgumentException.class,
                () -> calculateMolarConcentration(mass, molarMass, volume, oneMeter.getUnit()));

        // divide by 0
        molarConcentration = calculateMolarConcentration(mass, molarMass, Quantity.create(0.0, "l"));
        assertEquals(Double.POSITIVE_INFINITY, molarConcentration.getValue(), 1e-3);
    }

    @Test
    public void test_calculateMass() {
        Quantity molarConcentration = Quantity.create(22.815423, "M");
        Quantity molarMass = Quantity.create(58.44, "g/mol");
        Quantity volume = Quantity.create(150.0, "µl");
        Quantity mass;

        mass = calculateMass(molarConcentration, molarMass, volume);
        assertEquals(200 * 1e-6, mass.getValue(), 1e-9);
        assertEquals("kg", mass.getUnit().toString());

        mass = calculateMass(molarConcentration, molarMass, volume, Unit.getUnit("mg"));
        assertEquals(200, mass.getValue(), 1e-3);
        assertEquals("mg", mass.getUnit().toString());

        // validations
        assertThrows(IllegalArgumentException.class, () -> calculateMass(oneMeter, molarMass, volume));
        assertThrows(IllegalArgumentException.class, () -> calculateMass(molarConcentration, oneMeter, volume));
        assertThrows(IllegalArgumentException.class, () -> calculateMass(molarConcentration, molarMass, oneMeter));

        assertThrows(IllegalArgumentException.class,
                () -> calculateMass(molarConcentration, molarMass, volume, oneMeter.getUnit()));
    }

    @Test
    public void test_calculateVolume() {
        Quantity mass = Quantity.create(200.0, "mg");
        Quantity molarConcentration = Quantity.create(22.815423, "M");
        Quantity molarMass = Quantity.create(58.44, "g/mol");
        Quantity volume;

        volume = calculateVolume(mass, molarConcentration, molarMass);
        assertEquals(150 * 1e-9, volume.getValue(), 1e-12);
        assertEquals("m^3", volume.getUnit().toString());

        volume = calculateVolume(mass, molarConcentration, molarMass, Unit.getUnit("µl"));
        assertEquals(150, volume.getValue(), 1e-3);
        assertEquals("µl", volume.getUnit().toString());

        // validations
        assertThrows(IllegalArgumentException.class, () -> calculateVolume(oneMeter, molarConcentration, molarMass));
        assertThrows(IllegalArgumentException.class, () -> calculateVolume(mass, oneMeter, molarMass));
        assertThrows(IllegalArgumentException.class, () -> calculateVolume(mass, molarConcentration, oneMeter));

        assertThrows(IllegalArgumentException.class,
                () -> calculateVolume(mass, molarConcentration, molarMass, oneMeter.getUnit()));
    }
}