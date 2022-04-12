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

/**
 * Physical quality of units of measurement
 *
 * @author fbroda
 */
public enum Quality {
    PIECES("ea"),
    LENGTH("m"),
    AREA("m^2"),
    VOLUME("m^3"),
    MASS("kg"),
    DENSITY("kg/m^3"),
    AMOUNT_OF_SUBSTANCE("mol"),
    MOLAR_MASS("kg/mol"),
    MOLAR_CONCENTRATION("mol/m^3"),
    PERCENT_CONCENTRATION("[1]"),
    MASS_CONCENTRATION("kg/m^3");

    /*
     * Do not change this to Unit, because this will clash with the static block in
     * Unit.
     */
    private String baseUnit;

    private Quality(String baseUnit) {
        this.baseUnit = baseUnit;
    }

    public Unit getBaseUnit() {
        return Unit.getUnit(baseUnit);
    }
}
