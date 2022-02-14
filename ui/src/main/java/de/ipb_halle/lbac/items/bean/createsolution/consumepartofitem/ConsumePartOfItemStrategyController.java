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

import java.io.Serializable;

import org.primefaces.event.FlowEvent;

import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.MessagePresenter;

/**
 * 
 * @author flange
 */
public class ConsumePartOfItemStrategyController implements Serializable {
    private static final long serialVersionUID = 1L;

    private final MessagePresenter messagePresenter;
    private final InputConcentrationAndVolumeStepController step1Controller;

    public ConsumePartOfItemStrategyController(Item parentItem, MessagePresenter messagePresenter) {
        this.messagePresenter = messagePresenter;
        step1Controller = new InputConcentrationAndVolumeStepController(parentItem, messagePresenter);
    }

    /*
     * PrimeFaces wizard
     */
    private static final String STEP1 = "step1_inputConcAndVol";
    private static final String STEP2 = "step2_weigh";

    public String onFlowProcess(FlowEvent event) {
        if (STEP2.equals(event.getNewStep())) {
            if (step1Controller.isTargetMassGreaterThanItemMass()) {
                messagePresenter.error("itemCreateSolution_error_targetMassTooHigh");
                return STEP1;
            } else {
                return STEP2;
            }
        }

        return event.getNewStep();
    }

    /*
     * Getters/setters
     */
    public InputConcentrationAndVolumeStepController getStep1Controller() {
        return step1Controller;
    }
}