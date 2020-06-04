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
package de.ipb_halle.lbac.exp.assay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author fbroda
 */
public class Unit {

    /*  physical quantity */
    private Quality quality;

    /* unit string - e.g. mM */
    private String unit;
    
    /* factor for conversion to base unit */
    private double factor;

    private static Map<Quality, List<Unit>> unitsByQuality;
    private static Map<String, Unit> unitsByUnit;

    /**
     * static constructor
     * base units are added first!
     */
    static {
        unitsByQuality = new HashMap<Quality, List<Unit>> ();
        unitsByUnit = new HashMap<String, Unit> ();

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
        addUnit(new Unit(Quality.VOLUME, "dm^3", 1.0e-3));
        addUnit(new Unit(Quality.VOLUME, "cm^3", 1.0e-6));
        addUnit(new Unit(Quality.VOLUME, "mm^3", 1.0e-9));

        addUnit(new Unit(Quality.MASS, "kg", 1.0));
        addUnit(new Unit(Quality.MASS, "g", 1.0e-3));
        addUnit(new Unit(Quality.MASS, "mg", 1.0e-6));
        addUnit(new Unit(Quality.MASS, "µg", 1.0e-9));

        addUnit(new Unit(Quality.DENSITY, "kg/m^3", 1.0));
        addUnit(new Unit(Quality.DENSITY, "g/cm^3", 1000.0));

        addUnit(new Unit(Quality.AMOUNT_OF_SUBSTANCE, "mol", 1.0));
        addUnit(new Unit(Quality.AMOUNT_OF_SUBSTANCE, "mmol", 1.0e-3));
        addUnit(new Unit(Quality.AMOUNT_OF_SUBSTANCE, "µmol", 1.0e-6));

        addUnit(new Unit(Quality.MOLAR_MASS, "g/mol", 1.0));

        addUnit(new Unit(Quality.MOLAR_CONCENTRATION, "M", 1.0));
        addUnit(new Unit(Quality.MOLAR_CONCENTRATION, "mM", 1.0e-3));
        addUnit(new Unit(Quality.MOLAR_CONCENTRATION, "µM", 1.0e-6));
        addUnit(new Unit(Quality.MOLAR_CONCENTRATION, "nM", 1.0e-9));
        addUnit(new Unit(Quality.MOLAR_CONCENTRATION, "pM", 1.0e-12));

        addUnit(new Unit(Quality.PERCENT_CONCENTRATION, "1", 1.0));
        addUnit(new Unit(Quality.PERCENT_CONCENTRATION, "%", 1.0e-2));
        addUnit(new Unit(Quality.PERCENT_CONCENTRATION, "ppm", 1.0e-6));
        addUnit(new Unit(Quality.PERCENT_CONCENTRATION, "ppb", 1.0e-9));

    }

    /*
     * private constructor
     */
    private Unit(Quality quality, String unit, double factor) {
        this.quality = quality;
        this.unit = unit;
        this.factor = factor;
    }

    /**
     * add the units to the static maps
     */
    private static void addUnit(Unit unit) {
        List<Unit> list = unitsByQuality.get(unit.getQuality());
        if (list == null) {
            list = new ArrayList<Unit> ();
            unitsByQuality.put(unit.getQuality(), list);
        }
        list.add(unit);
        unitsByUnit.put(unit.getUnit(), unit);
    }

    public double getFactor() {
        return this.factor;
    }

    public Quality getQuality() {
        return this.quality;
    }

    public String getUnit() {
        return this.unit;
    }

    public static Unit getUnit(String unit) {
        Unit u = unitsByUnit.get(unit);
        if (u == null) {
            throw new IllegalArgumentException("getUnit() attempt to fetch unknown unit");
        }
        return u;
    }

    /**
     * return the proportionality factor to convert this unit 
     * into the target unit (both units must be of the same quality).
     */
    public double transform(Unit target) {
        if (target.getQuality() != this.quality) {
            throw new IllegalArgumentException("transform(Unit) cannot convert among different physical qualities");
        }
        return this.factor / target.factor;
    }

    /**
     * return the proportionality factor to convert this unit 
     * into the target unit using a second physical quantity 
     * (e.g. transform masses into volumes using density as 
     * factor of proportionality)
     * @param target target unit (e.g. cubic centimeters)
     * @param propFactor the quantity (e.g. 7.87 for iron density at room temperature)
     * @param propUnit the unit (e.g. g / cm^3 for density)
     * @return the factor of proportionality for conversion (e.g. approx. 0.127 for conversion from grams in this example)
     */
    public double transform(Unit target, double propFactor, Unit propUnit) {
        throw new UnsupportedOperationException("transform() between different qualities is not yet implemented");
    }

    public String toString() {
        return this.unit;
    }
}
