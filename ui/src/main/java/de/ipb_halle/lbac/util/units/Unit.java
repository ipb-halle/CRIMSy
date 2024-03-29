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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit of measurement.
 * 
 * @author fbroda
 */
public class Unit implements Serializable {
    private static final long serialVersionUID = 1L;

    /* physical quantity */
    private Quality quality;

    /* unit string - e.g. mM */
    private String unit;

    /* factor for conversion to base unit */
    private double factor;

    /* visibility to the user */
    private boolean visible;

    private static Map<Quality, List<Unit>> unitsByQuality;
    private static Map<String, Unit> unitsByUnit;

    /**
     * static constructor base units are added first!
     */
    static {
        unitsByQuality = new HashMap<Quality, List<Unit>>();
        unitsByUnit = new HashMap<String, Unit>();

        addUnit(new Unit(Quality.PIECES, "ea", 1.0));

        addUnit(new Unit(Quality.LENGTH, "m", 1.0));
        addUnit(new Unit(Quality.LENGTH, "cm", 1.0e-2));
        addUnit(new Unit(Quality.LENGTH, "mm", 1.0e-3));
        addUnit(new Unit(Quality.LENGTH, "µm", 1.0e-6));
        addUnit(new Unit(Quality.LENGTH, "nm", 1.0e-9));

        addUnit(new Unit(Quality.AREA, "m^2", 1.0));
        addUnit(new Unit(Quality.AREA, "cm^2", 1.0e-4));
        addUnit(new Unit(Quality.AREA, "mm^2", 1.0e-6));

        addUnit(new Unit(Quality.VOLUME, "m^3", 1.0));
        addUnit(new Unit(Quality.VOLUME, "l", 1.0e-3));
        addUnit(new Unit(Quality.VOLUME, "ml", 1.0e-6));
        addUnit(new Unit(Quality.VOLUME, "µl", 1.0e-9));
//        addUnit(new Unit(Quality.VOLUME, "dm^3", 1.0e-3));
//        addUnit(new Unit(Quality.VOLUME, "cm^3", 1.0e-6));
//        addUnit(new Unit(Quality.VOLUME, "mm^3", 1.0e-9));

        addUnit(new Unit(Quality.MASS, "kg", 1.0));
        addUnit(new Unit(Quality.MASS, "g", 1.0e-3));
        addUnit(new Unit(Quality.MASS, "mg", 1.0e-6));
        addUnit(new Unit(Quality.MASS, "µg", 1.0e-9));

        addUnit(new Unit(Quality.DENSITY, "kg/m^3", 1.0));
        addUnit(new Unit(Quality.DENSITY, "g/cm^3", 1000.0));

        addUnit(new Unit(Quality.AMOUNT_OF_SUBSTANCE, "mol", 1.0));
        addUnit(new Unit(Quality.AMOUNT_OF_SUBSTANCE, "mmol", 1.0e-3));
        addUnit(new Unit(Quality.AMOUNT_OF_SUBSTANCE, "µmol", 1.0e-6));

        addUnit(new Unit(Quality.MOLAR_MASS, "kg/mol", 1.0, false));
        addUnit(new Unit(Quality.MOLAR_MASS, "g/mol", 1.0e-3));

        addUnit(new Unit(Quality.MOLAR_CONCENTRATION, "mol/m^3", 1.0, false));
        addUnit(new Unit(Quality.MOLAR_CONCENTRATION, "M", 1000.0));
        addUnit(new Unit(Quality.MOLAR_CONCENTRATION, "mM", 1.0));
        addUnit(new Unit(Quality.MOLAR_CONCENTRATION, "µM", 1.0e-3));
        addUnit(new Unit(Quality.MOLAR_CONCENTRATION, "nM", 1.0e-6));
        addUnit(new Unit(Quality.MOLAR_CONCENTRATION, "pM", 1.0e-9));

        addUnit(new Unit(Quality.PERCENT_CONCENTRATION, "[1]", 1.0));
        addUnit(new Unit(Quality.PERCENT_CONCENTRATION, "%", 1.0e-2));
        addUnit(new Unit(Quality.PERCENT_CONCENTRATION, "ppm", 1.0e-6));
        addUnit(new Unit(Quality.PERCENT_CONCENTRATION, "ppb", 1.0e-9));

        addUnit(new Unit(Quality.MASS_CONCENTRATION, "kg/m^3", 1.0, false));
        addUnit(new Unit(Quality.MASS_CONCENTRATION, "g/l", 1.0));
        addUnit(new Unit(Quality.MASS_CONCENTRATION, "mg/l", 1.0e-3));
    }

    /*
     * private constructors
     */
    private Unit(Quality quality, String unit, double factor) {
        this(quality, unit, factor, true);
    }

    private Unit(Quality quality, String unit, double factor, boolean visible) {
        this.quality = quality;
        this.unit = unit;
        this.factor = factor;
        this.visible = visible;
    }

    /**
     * This default constructor is needed because JSF fails in state restoration
     * after a validation error in ItemState.xhtml.
     */
    public Unit() {
        unit = "";
    }

    /**
     * adds the units to the static maps
     */
    private static void addUnit(Unit unit) {
        List<Unit> list = unitsByQuality.get(unit.getQuality());
        if (list == null) {
            list = new ArrayList<Unit>();
            unitsByQuality.put(unit.getQuality(), list);
        }
        list.add(unit);
        unitsByUnit.put(unit.getUnit(), unit);
    }

    @Override
    public boolean equals(Object o) {
        if ((o != null) && (o instanceof Unit)) {
            Unit u = (Unit) o;
            return this.unit.equals(u.getUnit());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return this.unit.hashCode();
    }

    /**
     * @return conversion factor to the base unit of the quantity
     */
    public double getFactor() {
        return this.factor;
    }

    /**
     * @return the quantity of the unit
     */
    public Quality getQuality() {
        return this.quality;
    }

    /**
     * @return the unit string
     */
    public String getUnit() {
        return this.unit;
    }

    public String toString() {
        return getUnit();
    }

    /**
     * @param unit
     * @return unit for the given string
     * @throws IllegalArgumentException if unit is not known
     */
    public static Unit getUnit(String unit) {
        Unit u = unitsByUnit.get(unit);
        if (u == null) {
            throw new IllegalArgumentException("getUnit() attempt to fetch unknown unit");
        }
        return u;
    }

    /**
     * @param qualities
     * @return all units for the given qualities
     */
    public static List<Unit> getUnitsOfQuality(Quality... qualities) {
        List<Unit> units = new ArrayList<>();
        for (Quality q : qualities) {
            units.addAll(unitsByQuality.get(q));
        }
        return units;
    }

    /**
     * @param qualities
     * @return all visible units for the given qualities
     */
    public static List<Unit> getVisibleUnitsOfQuality(Quality... qualities) {
        List<Unit> units = getUnitsOfQuality(qualities);
        units.removeIf(u -> !u.visible);
        return units;
    }

    /**
     * @param targetUnit
     * @return the proportionality factor to convert this unit into the target unit
     * @throws IllegalArgumentException if this unit and the target unit do not have
     *                                  the same quality
     * @throws NullPointerException     if targetUnit is null
     */
    public double transform(Unit targetUnit) {
        if (targetUnit == null) {
            throw new NullPointerException();
        }
        if (targetUnit.getQuality() != this.quality) {
            throw new IllegalArgumentException("Cannot convert among different physical qualities");
        }
        return this.factor / targetUnit.factor;
    }

    /**
     * return the proportionality factor to convert this unit into the target unit
     * using a second physical quantity (e.g. transform masses into volumes using
     * density as factor of proportionality)
     *
     * @param target     target unit (e.g. cubic centimeters)
     * @param propFactor the quantity (e.g. 7.87 for iron density at room
     *                   temperature)
     * @param propUnit   the unit (e.g. g / cm^3 for density)
     * @return the factor of proportionality for conversion (e.g. approx. 0.127 for
     *         conversion from grams in this example)
     */
    @Deprecated
    public double transform(Unit target, double propFactor, Unit propUnit) {
        throw new UnsupportedOperationException("transform() between different qualities is not yet implemented");
    }

    /**
     * @param quality
     * @throws IllegalArgumentException if the quality of this unit does not match
     *                                  the given quality
     */
    public void require(Quality quality) {
        if (this.quality != quality) {
            throw new IllegalArgumentException(
                    "Wrong physical quality: Expected " + quality + " but got " + this.quality);
        }
    }
}
