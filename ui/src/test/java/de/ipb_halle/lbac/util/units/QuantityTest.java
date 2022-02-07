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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.jupiter.api.Test;

class QuantityTest {
    private static final double DELTA = 1.0e-9;

    @Test
    void test_getters() {
        Quantity quantity = new Quantity(42.123, Unit.getUnit("cm"));
        assertEquals(42.123, quantity.getValue(), DELTA);
        assertEquals("cm", quantity.getUnit().toString());
    }

    @Test
    void test_to() {
        Quantity quantity = new Quantity(42.123, Unit.getUnit("cm"));
        Quantity newQuantity = quantity.to(Unit.getUnit("mm"));
        assertEquals(421.23, newQuantity.getValue(), DELTA);
        assertEquals("mm", newQuantity.getUnit().toString());

        assertThrows(NullPointerException.class, () -> quantity.to(null));
        assertThrows(IllegalArgumentException.class, () -> quantity.to(Unit.getUnit("mol")));
    }

    @Test
    void test_toBaseUnit() {
        Quantity quantity = new Quantity(42.123, Unit.getUnit("cm"));
        Quantity newQuantity = quantity.toBaseUnit();
        assertEquals(0.42123, newQuantity.getValue(), DELTA);
        assertEquals("m", newQuantity.getUnit().toString());
    }

    @Test
    void test_add() {
        Quantity q1 = new Quantity(12.34, Unit.getUnit("mm"));
        Quantity q2 = new Quantity(5.67, Unit.getUnit("m"));
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
        Quantity wrongQuality = new Quantity(1.0, Unit.getUnit("mol"));
        assertThrows(IllegalArgumentException.class, () -> q1.add(wrongQuality));
    }

    @Test
    void test_subtract() {
        Quantity q1 = new Quantity(12.34, Unit.getUnit("mm"));
        Quantity q2 = new Quantity(5.67, Unit.getUnit("m"));
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
        Quantity wrongQuality = new Quantity(1.0, Unit.getUnit("mol"));
        assertThrows(IllegalArgumentException.class, () -> q1.subtract(wrongQuality));
    }

    @Test
    void test_multiply() {
        Quantity q1 = new Quantity(12.34, Unit.getUnit("mm"));
        Quantity q2 = new Quantity(5.67, Unit.getUnit("m"));
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
    void test_divide() {
        Quantity q1 = new Quantity(12.34, Unit.getUnit("mm"));
        Quantity q2 = new Quantity(5.67, Unit.getUnit("m"));
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
}