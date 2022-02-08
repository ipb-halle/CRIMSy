/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2020 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.util.units;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * @author fbroda
 */
public class UnitTest {
    private static final double DELTA = 1.0e-30;

    @Test
    @SuppressWarnings("unlikely-arg-type")
    public void test_equalsAndHashCode() {
        Unit kg1 = Unit.getUnit("kg");
        Unit kg2 = Unit.getUnit("kg");
        Unit mol = Unit.getUnit("mol");

        assertFalse(kg1.equals(null));
        assertFalse(kg1.equals("I'm not of class Unit"));

        assertTrue(kg1.equals(kg1));

        assertTrue(kg1.equals(kg2));
        assertTrue(kg2.equals(kg1));
        assertEquals(kg1.hashCode(), kg2.hashCode());

        assertFalse(kg1.equals(mol));
        assertFalse(mol.equals(kg1));
        assertNotEquals(kg1.hashCode(), mol.hashCode());
    }

    @Test
    public void test_getFactor() {
        Unit kg = Unit.getUnit("kg");
        Unit g = Unit.getUnit("g");

        assertEquals(1.0, kg.getFactor(), DELTA);
        assertEquals(0.001, g.getFactor(), DELTA);
    }

    @Test
    public void test_getQuality() {
        Unit kg = Unit.getUnit("kg");
        Unit pM = Unit.getUnit("pM");

        assertEquals(Quality.MASS, kg.getQuality());
        assertEquals(Quality.MOLAR_CONCENTRATION, pM.getQuality());
    }

    @Test
    public void test_getUnitAndToString() {
        Unit kg = Unit.getUnit("kg");
        Unit pM = Unit.getUnit("pM");

        assertEquals("kg", kg.getUnit());
        assertEquals("pM", pM.getUnit());
        assertEquals("kg", kg.toString());
        assertEquals("pM", pM.toString());
    }

    @Test
    public void test_static_getUnit() {
        assertNotNull(Unit.getUnit("kg"));
        assertSame(Unit.getUnit("kg"), Unit.getUnit("kg"));

        assertThrows(IllegalArgumentException.class, () -> Unit.getUnit(null));
        assertThrows(IllegalArgumentException.class, () -> Unit.getUnit("does not exist"));
    }

    @Test
    public void test_static_getUnitsOfQuality() {
        List<Unit> massUnits = Unit.getUnitsOfQuality(Quality.MASS);
        Unit kg = Unit.getUnit("kg");
        Unit g = Unit.getUnit("g");
        Unit mg = Unit.getUnit("mg");
        Unit ug = Unit.getUnit("µg");
        assertThat(massUnits, hasSize(4));
        assertThat(massUnits, contains(kg, g, mg, ug));

        // visibility of units doesn't matter
        List<Unit> molarConcentratuionUnits = Unit.getUnitsOfQuality(Quality.MOLAR_CONCENTRATION);
        assertThat(molarConcentratuionUnits, hasSize(6));

        // request more than one quality
        List<Unit> manyUnits = Unit.getUnitsOfQuality(Quality.MASS, Quality.VOLUME, Quality.PIECES);
        assertThat(manyUnits, hasSize(9));

        // check that unitsByQuality works correctly
        for (Quality q : Quality.values()) {
            for (Unit u : Unit.getUnitsOfQuality(q)) {
                assertEquals(q, u.getQuality());
            }
        }
    }

    @Test
    public void test_static_getVisibleUnitsOfQuality() {
        Unit molar = Unit.getUnit("M");
        Unit mM = Unit.getUnit("mM");
        Unit uM = Unit.getUnit("µM");
        Unit nM = Unit.getUnit("nM");
        Unit pM = Unit.getUnit("pM");

        // visibility of units matters, mol/m^3 is missing
        List<Unit> molarConcentratuionUnits = Unit.getVisibleUnitsOfQuality(Quality.MOLAR_CONCENTRATION);
        assertThat(molarConcentratuionUnits, hasSize(5));
        assertThat(molarConcentratuionUnits, contains(molar, mM, uM, nM, pM));

        // request more than one quality
        List<Unit> manyUnits = Unit.getVisibleUnitsOfQuality(Quality.MOLAR_CONCENTRATION, Quality.MASS_CONCENTRATION);
        assertThat(manyUnits, hasSize(7));

        // check that unitsByQuality works correctly
        for (Quality q : Quality.values()) {
            for (Unit u : Unit.getUnitsOfQuality(q)) {
                assertEquals(q, u.getQuality());
            }
        }
    }

    @Test
    public void test_transform() {
        Unit kg = Unit.getUnit("kg");
        Unit mol = Unit.getUnit("mol");
        assertThrows(NullPointerException.class, () -> kg.transform(null));
        assertThrows(IllegalArgumentException.class, () -> kg.transform(mol));

        Unit cm = Unit.getUnit("cm");
        Unit mm = Unit.getUnit("mm");
        assertEquals(1.0, cm.transform(cm), DELTA);
        assertEquals(10.0, cm.transform(mm), DELTA);
        assertEquals(0.1, mm.transform(cm), DELTA);

        Unit molar = Unit.getUnit("M");
        Unit molesPerCubicMeter = Unit.getUnit("mol/m^3");
        assertEquals(1000, molar.transform(molesPerCubicMeter), DELTA);
        assertEquals(0.001, molesPerCubicMeter.transform(molar), DELTA);

        Unit gramsPerMol = Unit.getUnit("g/mol");
        Unit kilogramsPerMol = Unit.getUnit("kg/mol");
        assertEquals(0.001, gramsPerMol.transform(kilogramsPerMol), DELTA);
        assertEquals(1000, kilogramsPerMol.transform(gramsPerMol), DELTA);

        // all possible transformations
        for (Quality q : Quality.values()) {
            for (Unit u1 : Unit.getUnitsOfQuality(q)) {
                for (Unit u2 : Unit.getUnitsOfQuality(q)) {
                    double factor = u1.transform(u2);
                    assertEquals(u1.getFactor() / u2.getFactor(), factor, DELTA);
                }
            }
        }
    }

    @Test
    public void test_require() {
        Unit mm = Unit.getUnit("mm");
        assertDoesNotThrow(() -> mm.require(Quality.LENGTH));
        assertThrows(IllegalArgumentException.class, () -> mm.require(Quality.MASS));
    }
}
