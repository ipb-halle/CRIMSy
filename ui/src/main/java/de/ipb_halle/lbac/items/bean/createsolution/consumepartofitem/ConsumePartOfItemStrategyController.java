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
import java.util.List;

import org.primefaces.event.FlowEvent;

import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.Solvent;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.MessagePresenter;

/**
 * 
 * @author flange
 */
public class ConsumePartOfItemStrategyController implements Serializable {
    private static final long serialVersionUID = 1L;

    private final ItemService itemService;
    private final MessagePresenter messagePresenter;

    private final InputConcentrationAndVolumeStepController step1Controller; // r
    private final InputWeightStepController step2Controller; // r
    private final InputVolumeAndSolventStepController step3Controller; // r

    private final List<Solvent> solvents; // r

    public ConsumePartOfItemStrategyController(Item parentItem, ItemService itemService,
            MessagePresenter messagePresenter) {
        this.itemService = itemService;
        this.messagePresenter = messagePresenter;

        step1Controller = new InputConcentrationAndVolumeStepController(parentItem, messagePresenter);
        step2Controller = new InputWeightStepController(step1Controller, parentItem, messagePresenter);
        step3Controller = new InputVolumeAndSolventStepController(step1Controller, step2Controller, parentItem);

        solvents = loadSolvents();
    }

    private List<Solvent> loadSolvents() {
        return itemService.loadSolvents();
    }

    /*
     * PrimeFaces wizard
     */
    private static final String STEP1 = "step1_targetConcAndVol";
    private static final String STEP2 = "step2_weigh";
    private static final String STEP3 = "step3_volumeAndSolvent";

    public String onFlowProcess(FlowEvent event) {
        if (STEP2.equals(event.getNewStep())) {
            if (step1Controller.isTargetMassGreaterThanItemMass()) {
                messagePresenter.error("itemCreateSolution_error_targetMassTooHigh");
                return STEP1;
            } else {
                step2Controller.init();
                return STEP2;
            }
        }

        if (STEP3.equals(event.getNewStep())) {
            if (step2Controller.isWeighGreaterThanItemMass()) {
                messagePresenter.error("itemCreateSolution_error_weighTooHigh");
                return STEP2;
            } else {
                step3Controller.init();
                return STEP3;
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

    public InputWeightStepController getStep2Controller() {
        return step2Controller;
    }

    public InputVolumeAndSolventStepController getStep3Controller() {
        return step3Controller;
    }

    public List<Solvent> getSolvents() {
        return solvents;
    }
}