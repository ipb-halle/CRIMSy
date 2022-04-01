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

import de.ipb_halle.pageobjects.pages.materials.tabs.BioSafetyData;

/**
 * Model class for input data in {@link HazardsTab}.
 * 
 * @author flange
 */
public class HazardsModel {
    private GHSModel ghsModel;
    private String hStatements;
    private String pStatements;
    private Boolean radioactive;
    private BioSafetyData.Level bioSafetyLevel;
    private Boolean gmo;
    private String customRemarks;

    /*
     * Fluent setters
     */
    public HazardsModel ghsModel(GHSModel ghsModel) {
        this.ghsModel = ghsModel;
        return this;
    }

    public HazardsModel hStatements(String hStatements) {
        this.hStatements = hStatements;
        return this;
    }

    public HazardsModel pStatements(String pStatements) {
        this.pStatements = pStatements;
        return this;
    }

    public HazardsModel radioactive(Boolean radioactive) {
        this.radioactive = radioactive;
        return this;
    }

    public HazardsModel bioSafetyLevel(BioSafetyData.Level bioSafetyLevel) {
        this.bioSafetyLevel = bioSafetyLevel;
        return this;
    }

    public HazardsModel gmo(Boolean gmo) {
        this.gmo = gmo;
        return this;
    }

    public HazardsModel customRemarks(String customRemarks) {
        this.customRemarks = customRemarks;
        return this;
    }

    /*
     * Getters
     */
    public GHSModel getGhsModel() {
        return ghsModel;
    }

    public String gethStatements() {
        return hStatements;
    }

    public String getpStatements() {
        return pStatements;
    }

    public Boolean getRadioactive() {
        return radioactive;
    }

    public BioSafetyData.Level getBioSafetyLevel() {
        return bioSafetyLevel;
    }

    public Boolean getGmo() {
        return gmo;
    }

    public String getCustomRemarks() {
        return customRemarks;
    }
}