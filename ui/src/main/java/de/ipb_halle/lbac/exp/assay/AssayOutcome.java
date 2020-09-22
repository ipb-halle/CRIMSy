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

import com.google.gson.Gson;

/**
 * @author fbroda
 */
public abstract class AssayOutcome {

    private transient Assay       assay;

    public static AssayOutcome fromString(AssayOutcomeType type, String outcome) {
        Gson gson = new Gson();
        switch(type) {
            case SINGLE_POINT : return gson.fromJson(outcome, SinglePointOutcome.class);
        }
        throw new IllegalArgumentException("fromString() illegal arguments");
    }

    public Assay getAssay() {
        return this.assay;
    }

    public abstract AssayOutcomeType getType(); 

    public AssayOutcome setAssay(Assay assay) {
        this.assay = assay;
        return this;
    }

    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
