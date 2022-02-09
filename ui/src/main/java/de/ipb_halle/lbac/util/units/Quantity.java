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

/**
 * Physical quantity that unites value and unit.
 * 
 * @author flange
 */
public class Quantity {
    private final double value;
    private final Unit unit;

    public Quantity(double value, Unit unit) {
        this.value = value;
        this.unit = unit;
    }

    public Quantity(double value, String unit) {
        this(value, Unit.getUnit(unit));
    }

    /**
     * @return value of this quantity
     */
    public double getValue() {
        return value;
    }

    /**
     * @return unit of this quantity
     */
    public Unit getUnit() {
        return unit;
    }

    /**
     * Converts this quantity to the given target unit.
     * 
     * @param targetUnit
     * @return new quantity in the given target unit
     * @throws IllegalArgumentException if this quantity's unit and the target unit
     *                                  do not have the same quality
     * @throws NullPointerException     if targetUnit is null
     */
    public Quantity to(Unit targetUnit) {
        double factor = unit.transform(targetUnit);
        return new Quantity(factor * value, targetUnit);
    }

    /**
     * Converts this quantity to the base unit
     * 
     * @return new quantity in the base unit
     */
    public Quantity toBaseUnit() {
        Unit baseUnit = unit.getQuality().getBaseUnit();
        return to(baseUnit);
    }

    /**
     * Adds the given quantity to this quantity.
     * 
     * @param quantity
     * @return new quantity with the sum value and the unit of this quantity
     * @throws IllegalArgumentException if this quantity's unit and the given
     *                                  quantity's unit do not have the same quality
     * @throws NullPointerException     if quantity is null
     */
    public Quantity add(Quantity quantity) {
        Quantity quantityInMyUnit = quantity.to(unit);
        double newValue = value + quantityInMyUnit.getValue();
        return new Quantity(newValue, unit);
    }

    /**
     * Subtracts the given quantity from this quantity.
     * 
     * @param quantity
     * @return new quantity with the difference value and the unit of this quantity
     * @throws IllegalArgumentException if this quantity's unit and the given
     *                                  quantity's unit do not have the same quality
     * @throws NullPointerException     if quantity is null
     */
    public Quantity subtract(Quantity quantity) {
        Quantity quantityInMyUnit = quantity.to(unit);
        double newValue = value - quantityInMyUnit.getValue();
        return new Quantity(newValue, unit);
    }

    /**
     * Multiplies this quantity and the given quantity.
     * <p>
     * Note: There is no check for the correctness of the result's quality.
     * 
     * @param quantity
     * @param quality
     * @return new quantity with the product value and the base unit of the given
     *         quality
     * @throws NullPointerException if quantity or quality are null
     */
    public Quantity multiply(Quantity quantity, Quality quality) {
        Quantity thisInBaseUnit = toBaseUnit();
        Quantity quantityInBaseUnit = quantity.toBaseUnit();
        double newValue = thisInBaseUnit.getValue() * quantityInBaseUnit.getValue();
        return new Quantity(newValue, quality.getBaseUnit());
    }

    /**
     * Divides this quantity and the given quantity.
     * <p>
     * Note: There is no check for the correctness of the result's quality.
     * 
     * @param quantity
     * @param quality
     * @return new quantity with the quotient value and the base unit of the given
     *         quality
     * @throws NullPointerException if quantity or quality are null
     */
    public Quantity divide(Quantity quantity, Quality quality) {
        Quantity thisInBaseUnit = toBaseUnit();
        Quantity quantityInBaseUnit = quantity.toBaseUnit();
        double newValue = thisInBaseUnit.getValue() / quantityInBaseUnit.getValue();
        return new Quantity(newValue, quality.getBaseUnit());
    }

    /**
     * @param quantity
     * @return true if this quantity is greater than the given quantity
     * @throws IllegalArgumentException if this quantity's unit and the given
     *                                  quantity's unit do not have the same quality
     * @throws NullPointerException     if quantity is null
     */
    public boolean isGreaterThan(Quantity quantity) {
        Quantity quantityInMyUnit = quantity.to(unit);
        return this.getValue() > quantityInMyUnit.getValue();
    }

    /**
     * @param quantity
     * @return true if this quantity is greater than or equal to the given quantity
     * @throws IllegalArgumentException if this quantity's unit and the given
     *                                  quantity's unit do not have the same quality
     * @throws NullPointerException     if quantity is null
     */
    public boolean isGreaterThanOrEqualTo(Quantity quantity) {
        Quantity quantityInMyUnit = quantity.to(unit);
        return this.getValue() >= quantityInMyUnit.getValue();
    }

    /**
     * @param quantity
     * @return true if this quantity is less than the given quantity
     * @throws IllegalArgumentException if this quantity's unit and the given
     *                                  quantity's unit do not have the same quality
     * @throws NullPointerException     if quantity is null
     */
    public boolean isLessThan(Quantity quantity) {
        Quantity quantityInMyUnit = quantity.to(unit);
        return this.getValue() < quantityInMyUnit.getValue();
    }

    /**
     * @param quantity
     * @return true if this quantity is less than or equal to the given quantity
     * @throws IllegalArgumentException if this quantity's unit and the given
     *                                  quantity's unit do not have the same quality
     * @throws NullPointerException     if quantity is null
     */
    public boolean isLessThanOrEqualTo(Quantity quantity) {
        Quantity quantityInMyUnit = quantity.to(unit);
        return this.getValue() <= quantityInMyUnit.getValue();
    }

    /**
     * @param quality
     * @throws IllegalArgumentException if the quality of this quantity's unit does
     *                                  not match the given quality
     */
    public void require(Quality quality) {
        Quality qualityFromUnit = unit.getQuality();
        if (qualityFromUnit != quality) {
            throw new IllegalArgumentException(
                    "Wrong physical quality: Expected " + quality + " but got " + qualityFromUnit);
        }
    }
}