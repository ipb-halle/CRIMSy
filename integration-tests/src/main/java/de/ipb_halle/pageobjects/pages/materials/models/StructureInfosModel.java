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
package de.ipb_halle.pageobjects.pages.materials.models;

/**
 * Model class for input data in {@link StructureInfosTab}.
 * 
 * @author flange
 */
public class StructureInfosModel {
    private String molfile;
    private Boolean autocalc;
    private String sumFormula;
    private String molarMass;
    private String exactMolarMass;

    /*
     * Fluent setters
     */
    public StructureInfosModel molfile(String molfile) {
        this.molfile = molfile;
        return this;
    }

    public StructureInfosModel autocalc(Boolean autocalc) {
        this.autocalc = autocalc;
        return this;
    }

    public StructureInfosModel sumFormula(String sumFormula) {
        this.sumFormula = sumFormula;
        return this;
    }

    public StructureInfosModel molarMass(String molarMass) {
        this.molarMass = molarMass;
        return this;
    }

    public StructureInfosModel exactMolarMass(String exactMolarMass) {
        this.exactMolarMass = exactMolarMass;
        return this;
    }

    /*
     * Getters
     */
    public String getMolfile() {
        return molfile;
    }

    public Boolean getAutocalc() {
        return autocalc;
    }

    public String getSumFormula() {
        return sumFormula;
    }

    public String getMolarMass() {
        return molarMass;
    }

    public String getExactMolarMass() {
        return exactMolarMass;
    }
}