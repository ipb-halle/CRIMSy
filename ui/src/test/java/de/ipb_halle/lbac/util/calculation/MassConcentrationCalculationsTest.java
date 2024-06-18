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

import static de.ipb_halle.lbac.util.calculation.MassConcentrationCalculations.calculateMass;
import static de.ipb_halle.lbac.util.calculation.MassConcentrationCalculations.calculateMassConcentration;
import static de.ipb_halle.lbac.util.calculation.MassConcentrationCalculations.calculateVolume;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import de.ipb_halle.lbac.util.units.Quantity;
import de.ipb_halle.lbac.util.units.Unit;

/**
 * @author flange
 */
class MassConcentrationCalculationsTest {
    Quantity oneMeter = Quantity.create(1.0, "m");

    @Test
    public void test_calculateMassConcentration() {
        Quantity mass = Quantity.create(200.0, "mg");
        Quantity volume = Quantity.create(150.0, "µl");
        Quantity massConcentration;

        massConcentration = calculateMassConcentration(mass, volume);
        assertEquals(1333.333333, massConcentration.getValue(), 1e-6);
        assertEquals("kg/m^3", massConcentration.getUnit().toString());

        massConcentration = calculateMassConcentration(mass, volume, Unit.getUnit("mg/l"));
        assertEquals(1333333.333, massConcentration.getValue(), 1e-3);
        assertEquals("mg/l", massConcentration.getUnit().toString());

        // validations
        assertThrows(IllegalArgumentException.class, () -> calculateMassConcentration(oneMeter, volume));
        assertThrows(IllegalArgumentException.class, () -> calculateMassConcentration(mass, oneMeter));

        assertThrows(IllegalArgumentException.class,
                () -> calculateMassConcentration(mass, volume, oneMeter.getUnit()));
    }

    @Test
    public void test_calculateMass() {
        Quantity massConcentration = Quantity.create(1333.333333, "g/l");
        Quantity volume = Quantity.create(150.0, "µl");
        Quantity mass;

        mass = calculateMass(massConcentration, volume);
        assertEquals(200 * 1e-6, mass.getValue(), 1e-9);
        assertEquals("kg", mass.getUnit().toString());

        mass = calculateMass(massConcentration, volume, Unit.getUnit("mg"));
        assertEquals(200.0, mass.getValue(), 1e-3);
        assertEquals("mg", mass.getUnit().toString());

        // validations
        assertThrows(IllegalArgumentException.class, () -> calculateMass(oneMeter, volume));
        assertThrows(IllegalArgumentException.class, () -> calculateMass(massConcentration, oneMeter));

        assertThrows(IllegalArgumentException.class,
                () -> calculateMass(massConcentration, volume, oneMeter.getUnit()));
    }

    @Test
    public void test_calculateVolume() {
        Quantity mass = Quantity.create(200.0, "mg");
        Quantity massConcentration = Quantity.create(1333.333333, "g/l");
        Quantity volume;

        volume = calculateVolume(mass, massConcentration);
        assertEquals(150 * 1e-9, volume.getValue(), 1e-12);
        assertEquals("m^3", volume.getUnit().toString());

        volume = calculateVolume(mass, massConcentration, Unit.getUnit("µl"));
        assertEquals(150.0, volume.getValue(), 1e-3);
        assertEquals("µl", volume.getUnit().toString());

        // validations
        assertThrows(IllegalArgumentException.class, () -> calculateVolume(oneMeter, massConcentration));
        assertThrows(IllegalArgumentException.class, () -> calculateVolume(mass, oneMeter));

        assertThrows(IllegalArgumentException.class,
                () -> calculateVolume(mass, massConcentration, oneMeter.getUnit()));
    }
}
