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
package de.ipb_halle.lbac.items.bean.createsolution.consumepartofitem;

/**
 * 
 * @author flange
 */
public class InputWeightStepController {
    private final InputConcentrationAndVolumeStepController step1Controller;

    private int errorMargin = 5; // rw

    public InputWeightStepController(InputConcentrationAndVolumeStepController step1Controller) {
        this.step1Controller = step1Controller;
    }

    /*
     * Getters with logic
     */
    public Double getTargetMassPlusMargin() {
        Double targetMass = step1Controller.getTargetMass();
        if (targetMass == null) {
            return null;
        }
        return targetMass * (1.0 + (errorMargin / 100.0));
    }

    public Double getTargetMassMinusMargin() {
        Double targetMass = step1Controller.getTargetMass();
        if (targetMass == null) {
            return null;
        }
        return targetMass * (1.0 - (errorMargin / 100.0));
    }

    /*
     * Getters/setters
     */
    public int getErrorMargin() {
        return errorMargin;
    }

    public void setErrorMargin(int errorMargin) {
        this.errorMargin = errorMargin;
    }
}