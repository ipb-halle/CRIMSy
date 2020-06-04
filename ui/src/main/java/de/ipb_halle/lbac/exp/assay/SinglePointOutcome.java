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

import de.ipb_halle.lbac.util.Unit;

import java.util.ArrayList;
import java.util.List;
/**
 * @author fbroda
 */
public class SinglePointOutcome extends AssayOutcome {

    private double              stddev;
    private double              value;
    private String              unit;
    private String              remarks;


    public String getRemarks() {
        return this.remarks;
    }

    public double getStdDev() {
        return this.stddev;
    }

    public AssayOutcomeType getType() {
        return AssayOutcomeType.SINGLE_POINT;
    } 

    public String getUnit() {
        return this.unit;
    }

    public List<Unit> getUnits() {
        List<Unit> units = new ArrayList<Unit> ();
        for(String u : new String[] {"mM", "µM", "nM", "pM"}) {
            units.add(Unit.getUnit(u));
        }
        return units;
    }

    public double getValue() {
        return this.value;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void setStdDev(double stddev) {
        this.stddev = stddev;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
