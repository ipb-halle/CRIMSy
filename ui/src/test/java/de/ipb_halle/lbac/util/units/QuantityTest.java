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
package de.ipb_halle.lbac.util.units;

import static de.ipb_halle.lbac.util.units.Quality.LENGTH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class QuantityTest {
    private static final double DELTA = 1.0e-9;
    private Quantity q1 = Quantity.create(12.34, "mm");
    private Quantity q2 = Quantity.create(5.67, "m");
    private Quantity q3 = Quantity.create(8.9, "mol");

    @Test
    public void test_create() {
        Quantity quantity;

        assertNull(Quantity.create(null, (Unit) null));
        assertNull(Quantity.create(13.0, (Unit) null));
        assertNull(Quantity.create(null, Unit.getUnit("m")));
        quantity = Quantity.create(13.0, Unit.getUnit("m"));
        assertEquals(13.0, quantity.getValue(), DELTA);
        assertEquals("m", quantity.getUnit().toString());

        assertNull(Quantity.create(null, (String) null));
        assertNull(Quantity.create(13.0, (String) null));
        assertNull(Quantity.create(null, "m"));
        quantity = Quantity.create(13.0, "m");
        assertEquals(13.0, quantity.getValue(), DELTA);
        assertEquals("m", quantity.getUnit().toString());
    }

    @Test
    public void test_getters() {
        Quantity quantity = Quantity.create(42.123, "cm");
        assertEquals(42.123, quantity.getValue(), DELTA);
        assertEquals("cm", quantity.getUnit().toString());
    }

    @Test
    public void test_to() {
        Quantity quantity = Quantity.create(42.123, "cm");
        Quantity newQuantity = quantity.to(Unit.getUnit("mm"));
        assertEquals(421.23, newQuantity.getValue(), DELTA);
        assertEquals("mm", newQuantity.getUnit().toString());

        assertThrows(NullPointerException.class, () -> quantity.to(null));
        assertThrows(IllegalArgumentException.class, () -> quantity.to(Unit.getUnit("mol")));
    }

    @Test
    public void test_toBaseUnit() {
        Quantity quantity = Quantity.create(42.123, "cm");
        Quantity newQuantity = quantity.toBaseUnit();
        assertEquals(0.42123, newQuantity.getValue(), DELTA);
        assertEquals("m", newQuantity.getUnit().toString());
    }

    @Test
    public void test_toHumanReadableUnit() {
        assertHumanReadability(0.01, "mm", 10.0, "µm");
        assertHumanReadability(0.1, "mm", 0.1, "mm");
        assertHumanReadability(1.0, "mm", 1.0, "mm");
        assertHumanReadability(10.0, "mm", 1.0, "cm");
        assertHumanReadability(100.0, "mm", 10, "cm");
        assertHumanReadability(1000.0, "mm", 1.0, "m");
        assertHumanReadability(10000.0, "mm", 10.0, "m");

        // This works with the fudge factor -0.5, but it breaks the previous tests.
        //assertHumanReadability(0.058, "mm", 58.0, "µm");

        Quantity quantity;
        Quantity humanReadableQuantity;
        List<Unit> availableUnits;

        quantity = Quantity.create(1000.0, "mm");
        availableUnits = Arrays.asList(Unit.getUnit("mol"), Unit.getUnit("m^2"));
        humanReadableQuantity = quantity.toHumanReadableUnit(availableUnits);
        // All available units are incompatible, so we get the original quantity.
        assertEquals(1000.0, humanReadableQuantity.getValue(), DELTA);
        assertEquals("mm", humanReadableQuantity.getUnit().toString());

        quantity = Quantity.create(1000.0, "mm");
        availableUnits = Arrays.asList(Unit.getUnit("mol"), Unit.getUnit("µm"));
        humanReadableQuantity = quantity.toHumanReadableUnit(availableUnits);
        // Only one compatible unit, which has a worse score compared to the given unit.
        assertEquals(1000.0, humanReadableQuantity.getValue(), DELTA);
        assertEquals("mm", humanReadableQuantity.getUnit().toString());
    }

    private void assertHumanReadability(double value, String unit, double expectedValue, String expectedUnit) {
        Quantity humanReadableQuantity = Quantity.create(value, unit).toHumanReadableUnit(Unit.getUnitsOfQuality(LENGTH));
        assertEquals(expectedValue, humanReadableQuantity.getValue(), DELTA);
        assertEquals(expectedUnit, humanReadableQuantity.getUnit().toString());
    }

    @Test
    public void test_add() {
        Quantity sum;

        sum = q1.add(q1);
        assertEquals(24.68, sum.getValue(), DELTA);
        assertEquals("mm", sum.getUnit().toString());

        sum = q1.add(q2);
        assertEquals(5682.34, sum.getValue(), DELTA);
        assertEquals("mm", sum.getUnit().toString());

        sum = q2.add(q1);
        assertEquals(5.68234, sum.getValue(), DELTA);
        assertEquals("m", sum.getUnit().toString());

        assertThrows(NullPointerException.class, () -> q1.add(null));
        assertThrows(IllegalArgumentException.class, () -> q1.add(q3));
    }

    @Test
    public void test_subtract() {
        Quantity difference;

        difference = q1.subtract(q1);
        assertEquals(0.0, difference.getValue(), DELTA);
        assertEquals("mm", difference.getUnit().toString());

        difference = q1.subtract(q2);
        assertEquals(-5657.66, difference.getValue(), DELTA);
        assertEquals("mm", difference.getUnit().toString());

        difference = q2.subtract(q1);
        assertEquals(5.65766, difference.getValue(), DELTA);
        assertEquals("m", difference.getUnit().toString());

        assertThrows(NullPointerException.class, () -> q1.subtract(null));
        assertThrows(IllegalArgumentException.class, () -> q1.subtract(q3));
    }

    @Test
    public void test_multiply() {
        Quantity product;

        product = q1.multiply(q1, Quality.AREA);
        assertEquals(152.2756 * 1e-6, product.getValue(), DELTA);
        assertEquals("m^2", product.getUnit().toString());

        product = q1.multiply(q2, Quality.AREA);
        assertEquals(0.0699678, product.getValue(), DELTA);
        assertEquals("m^2", product.getUnit().toString());

        product = q2.multiply(q1, Quality.AREA);
        assertEquals(0.0699678, product.getValue(), DELTA);
        assertEquals("m^2", product.getUnit().toString());

        product = q1.multiply(q2, Quality.MOLAR_CONCENTRATION);
        assertEquals(0.0699678, product.getValue(), DELTA);
        assertEquals("mol/m^3", product.getUnit().toString());

        assertThrows(NullPointerException.class, () -> q1.multiply(null, Quality.AREA));
        assertThrows(NullPointerException.class, () -> q1.multiply(q2, null));
        assertThrows(NullPointerException.class, () -> q1.multiply(null, null));
    }

    @Test
    public void test_divide() {
        Quantity quotient;

        quotient = q1.divide(q1, Quality.MASS);
        assertEquals(1.0, quotient.getValue(), DELTA);
        assertEquals("kg", quotient.getUnit().toString());

        quotient = q1.divide(q2, Quality.MOLAR_MASS);
        assertEquals(0.002176366843, quotient.getValue(), DELTA);
        assertEquals("kg/mol", quotient.getUnit().toString());

        quotient = q2.divide(q1, Quality.PIECES);
        assertEquals(459.481361426, quotient.getValue(), DELTA);
        assertEquals("ea", quotient.getUnit().toString());

        quotient = q1.divide(q2, Quality.MOLAR_CONCENTRATION);
        assertEquals(0.002176366843, quotient.getValue(), DELTA);
        assertEquals("mol/m^3", quotient.getUnit().toString());

        assertThrows(NullPointerException.class, () -> q1.divide(null, Quality.AREA));
        assertThrows(NullPointerException.class, () -> q1.divide(q2, null));
        assertThrows(NullPointerException.class, () -> q1.divide(null, null));
    }

    @Test
    public void test_isGreaterThan() {
        assertFalse(q1.isGreaterThan(q1));
        assertFalse(q1.isGreaterThan(q2));
        assertTrue(q2.isGreaterThan(q1));

        assertThrows(NullPointerException.class, () -> q1.isGreaterThan(null));
        assertThrows(IllegalArgumentException.class, () -> q1.isGreaterThan(q3));
    }

    @Test
    public void test_isGreaterThanOrEqualTo() {
        assertTrue(q1.isGreaterThanOrEqualTo(q1));
        assertFalse(q1.isGreaterThanOrEqualTo(q2));
        assertTrue(q2.isGreaterThanOrEqualTo(q1));

        assertThrows(NullPointerException.class, () -> q1.isGreaterThanOrEqualTo(null));
        assertThrows(IllegalArgumentException.class, () -> q1.isGreaterThanOrEqualTo(q3));
    }

    @Test
    public void test_isLessThan() {
        assertFalse(q1.isLessThan(q1));
        assertTrue(q1.isLessThan(q2));
        assertFalse(q2.isLessThan(q1));

        assertThrows(NullPointerException.class, () -> q1.isLessThan(null));
        assertThrows(IllegalArgumentException.class, () -> q1.isLessThan(q3));
    }

    @Test
    public void test_isLessThanOrEqualTo() {
        assertTrue(q1.isLessThanOrEqualTo(q1));
        assertTrue(q1.isLessThanOrEqualTo(q2));
        assertFalse(q2.isLessThanOrEqualTo(q1));

        assertThrows(NullPointerException.class, () -> q1.isLessThanOrEqualTo(null));
        assertThrows(IllegalArgumentException.class, () -> q1.isLessThanOrEqualTo(q3));
    }

    @Test
    public void test_require() {
        assertDoesNotThrow(() -> q1.require(Quality.LENGTH));
        assertThrows(IllegalArgumentException.class, () -> q1.require(Quality.MASS));
    }
}
