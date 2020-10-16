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
import de.ipb_halle.lbac.util.UnitsValidator;

import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author fbroda
 */
public class SinglePointOutcome extends AssayOutcome {

    private double stddev;
    private double value;
    private String unit;
    private String remarks;

    private transient Logger logger = LogManager.getLogger(this.getClass().getName());

    public SinglePointOutcome(String unit) {
        this.unit = unit;
    }

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

    public Set<Unit> getUnits() {
        try {
            return UnitsValidator.getUnitSet(
                    getAssay()
                            .getUnits());
        } catch (Exception e) {
            this.logger.warn("getUnits() caught an exception", (Throwable) e);
        }
        return null;

        /*
        List<Unit> units = new ArrayList<Unit> ();
        for(String u : new String[] {"mM", "µM", "nM", "pM"}) {
            units.add(Unit.getUnit(u));
        }
        return units;
         */
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
        if (unit != null && !unit.trim().isEmpty()) {
            this.unit = unit;
        }
    }

    public void setValue(double value) {
        this.value = value;
    }
}
