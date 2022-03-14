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
import java.util.Date;
import java.util.List;

import org.primefaces.event.FlowEvent;

import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.container.ContainerUtils;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.Solvent;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.project.ProjectService;

/**
 * 
 * @author flange
 */
public class ConsumePartOfItemStrategyController implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Item parentItem;
    private final ItemService itemService;
    private final ContainerService containerService;
    private final MessagePresenter messagePresenter;

    private final ConsumePartOfItemStep1Controller step1Controller; // r
    private final ConsumePartOfItemStep2Controller step2Controller; // r
    private final ConsumePartOfItemStep3Controller step3Controller; // r
    private final ConsumePartOfItemStep4Controller step4Controller; // r
    private final ConsumePartOfItemStep5Controller step5Controller; // r
    private final ConsumePartOfItemStep6Controller step6Controller; // r

    private final List<Solvent> solvents; // r
    private final List<ContainerType> availableContainerTypes; // r

    public ConsumePartOfItemStrategyController(Item parentItem, ItemService itemService,
            ContainerService containerService, ProjectService projectService, UserBean userBean,
            MessagePresenter messagePresenter) {
        this.parentItem = parentItem;
        this.itemService = itemService;
        this.containerService = containerService;
        this.messagePresenter = messagePresenter;

        step1Controller = new ConsumePartOfItemStep1Controller(parentItem, messagePresenter);
        step2Controller = new ConsumePartOfItemStep2Controller(step1Controller, parentItem, messagePresenter);
        step3Controller = new ConsumePartOfItemStep3Controller(step1Controller, step2Controller, parentItem);
        step4Controller = new ConsumePartOfItemStep4Controller(step1Controller, step3Controller, messagePresenter);
        step5Controller = new ConsumePartOfItemStep5Controller(parentItem, projectService, userBean);
        step6Controller = new ConsumePartOfItemStep6Controller(containerService, userBean, messagePresenter);

        solvents = loadSolvents();
        availableContainerTypes = loadAvailableContainerTypes();
    }

    private List<Solvent> loadSolvents() {
        return itemService.loadSolvents();
    }

    private List<ContainerType> loadAvailableContainerTypes() {
        List<ContainerType> types = containerService.loadContainerTypes();
        ContainerUtils.filterLocalizeAndSortContainerTypes(types, messagePresenter);
        return types;
    }

    /*
     * PrimeFaces wizard
     */
    private static final String STEP1 = "step1_targetConcAndVol";
    private static final String STEP2 = "step2_weigh";
    private static final String STEP3 = "step3_volumeAndSolvent";
    private static final String STEP4 = "step4_directContainerAndLabel";
    private static final String STEP5 = "step5_project";
    private static final String STEP6 = "step6_location";

    public String onFlowProcess(FlowEvent event) {
        // STEP1 -> STEP2: target mass <= item mass
        if (STEP2.equals(event.getNewStep())) {
            if (step1Controller.isTargetMassGreaterThanItemMass()) {
                messagePresenter.error("itemCreateSolution_error_targetMassTooHigh");
                return STEP1;
            } else {
                step2Controller.init();
                return STEP2;
            }
        }

        // STEP2 -> STEP3: weigh <= item mass
        if (STEP3.equals(event.getNewStep())) {
            if (step2Controller.isWeighGreaterThanItemMass()) {
                messagePresenter.error("itemCreateSolution_error_weighTooHigh");
                return STEP2;
            } else {
                step3Controller.init();
                return STEP3;
            }
        }

        // STEP4 -> STEP5: dispensed volume <= container volume
        if (STEP5.equals(event.getNewStep())) {
            if (step4Controller.isDirectContainer() && step4Controller.isDispensedVolumeGreaterThanContainerSize()) {
                step4Controller.presentContainerTooSmallError();
                return STEP4;
            } else {
                return STEP5;
            }
        }

        return event.getNewStep();
    }

    /*
     * Actions
     */
    public void actionSave() {
        Item newItem = prepareNewItem();
    }

    private Item prepareNewItem() {
        Item item = new Item();

        item.setAmount(step3Controller.getDispensedVolume());
        item.setUnit(step1Controller.getTargetVolumeUnit());
        item.setConcentration(step3Controller.getFinalConcentration());
        item.setConcentrationUnit(step1Controller.getTargetConcentrationUnit());
        item.setContainer(step6Controller.getContainerController().getContainer());

        if (step4Controller.isDirectContainer()) {
            item.setContainerSize(step4Controller.getDirectContainerSize());
            item.setContainerType(step4Controller.getDirectContainerType());
        }

        item.setMaterial(parentItem.getMaterial());
        item.setProject(step5Controller.getSelectedProject());
        item.setPurity(parentItem.getPurity());
        item.setSolvent(step3Controller.getSolvent());
        item.setcTime(new Date());

        if (step4Controller.isCustomLabel()) {
            item.setLabel(step4Controller.getCustomLabelValue());
        }

        return item;
    }

    /*
     * Getters/setters
     */
    public ConsumePartOfItemStep1Controller getStep1Controller() {
        return step1Controller;
    }

    public ConsumePartOfItemStep2Controller getStep2Controller() {
        return step2Controller;
    }

    public ConsumePartOfItemStep3Controller getStep3Controller() {
        return step3Controller;
    }

    public ConsumePartOfItemStep4Controller getStep4Controller() {
        return step4Controller;
    }

    public ConsumePartOfItemStep5Controller getStep5Controller() {
        return step5Controller;
    }

    public ConsumePartOfItemStep6Controller getStep6Controller() {
        return step6Controller;
    }

    public List<Solvent> getSolvents() {
        return solvents;
    }

    public List<ContainerType> getAvailableContainerTypes() {
        return availableContainerTypes;
    }
}