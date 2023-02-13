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
package de.ipb_halle.lbac.items.bean.aliquot.createsolution.consumepartofitem;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

import jakarta.ejb.EJBException;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.FlowEvent;

import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.container.ContainerUtils;
import de.ipb_halle.lbac.container.service.ContainerPositionService;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.Solvent;
import de.ipb_halle.lbac.items.bean.ItemOverviewBean;
import de.ipb_halle.lbac.items.bean.Validator;
import de.ipb_halle.lbac.items.bean.aliquot.common.ContainerSelectionController;
import de.ipb_halle.lbac.items.bean.aliquot.common.ProjectSelectionController;
import de.ipb_halle.lbac.items.service.ItemLabelService;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.util.units.Quantity;

/**
 * 
 * @author flange
 */
@Dependent
public class ConsumePartOfItemStrategyController implements Serializable {
    private static final long serialVersionUID = 1L;
    private Logger logger = LogManager.getLogger(this.getClass().getName());

    private Item parentItem;
    private Validator validator;

    @Inject
    private ItemService itemService;

    @Inject
    private ContainerService containerService;

    @Inject
    private ContainerPositionService containerPositionService;

    @Inject
    private ItemLabelService labelService;

    @Inject
    private ProjectService projectService;

    @Inject
    private UserBean userBean;

    @Inject
    private ItemOverviewBean itemOverviewBean;

    @Inject
    private Navigator navigator;

    @Inject
    private transient MessagePresenter messagePresenter;

    private ConsumePartOfItemStep1Controller step1Controller; // r
    private ConsumePartOfItemStep2Controller step2Controller; // r
    private ConsumePartOfItemStep3Controller step3Controller; // r
    private ConsumePartOfItemStep4Controller step4Controller; // r
    private ProjectSelectionController step5Controller; // r
    private ContainerSelectionController step6Controller; // r

    private List<Solvent> solvents; // r
    private List<ContainerType> availableContainerTypes; // r

    public void init(Item parentItem) {
        this.parentItem = parentItem;
        validator = new Validator(containerPositionService, labelService);

        step1Controller = new ConsumePartOfItemStep1Controller(parentItem, messagePresenter);
        step2Controller = new ConsumePartOfItemStep2Controller(step1Controller, parentItem, messagePresenter);
        step3Controller = new ConsumePartOfItemStep3Controller(step1Controller, step2Controller, parentItem);
        step4Controller = new ConsumePartOfItemStep4Controller(step1Controller, step3Controller, messagePresenter);
        step5Controller = new ProjectSelectionController(parentItem, projectService, userBean);
        step6Controller = new ContainerSelectionController(containerService, userBean, messagePresenter);

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
    static final String STEP1 = "step1_targetConcAndVol";
    static final String STEP2 = "step2_weigh";
    static final String STEP3 = "step3_volumeAndSolvent";
    static final String STEP4 = "step4_directContainerAndLabel";
    static final String STEP5 = "step5_project";
    static final String STEP6 = "step6_location";

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

        // STEP2 -> STEP3: weight <= item mass
        if (STEP3.equals(event.getNewStep())) {
            if (step2Controller.isWeightGreaterThanItemMass()) {
                messagePresenter.error("itemCreateSolution_error_weightTooHigh");
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
        if (step4Controller.isDirectContainer() && step4Controller.isDispensedVolumeGreaterThanContainerSize()) {
            step4Controller.presentContainerTooSmallError();
            return;
        }

        Item newItem = prepareNewItem();

        if (!validator.itemValidToSave(newItem, step6Controller.getContainerController(),
                step4Controller.isCustomLabel(), newItem.getLabel())) {
            return;
        }

        Quantity massToSubtractFromParentItem = step2Controller.getWeightAsQuantity();

        try {
            saveAliquot(newItem, massToSubtractFromParentItem);
            messagePresenter.info("itemEdit_save_new_success");

            itemOverviewBean.reloadItems();
            navigator.navigate("/item/items");
        } catch (EJBException e) {
            messagePresenter.error("itemEdit_save_failed");
            logger.error("actionSave() caught an exception:", (Throwable) e);
        }
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

        item.setParentId(parentItem.getId());
        item.setACList(parentItem.getACList());
        item.setOwner(userBean.getCurrentAccount());

        return item;
    }

    private void saveAliquot(Item newItem, Quantity massToSubtractFromParentItem) {
        int[] position = null;
        Set<int[]> setWithPositions = step6Controller.getContainerController().resolveItemPositions();
        if (newItem.getContainer() != null && !setWithPositions.isEmpty()) {
            position = setWithPositions.iterator().next();
        }

        itemService.saveAliquot(newItem, position, massToSubtractFromParentItem, userBean.getCurrentAccount());
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

    public ProjectSelectionController getStep5Controller() {
        return step5Controller;
    }

    public ContainerSelectionController getStep6Controller() {
        return step6Controller;
    }

    public List<Solvent> getSolvents() {
        return solvents;
    }

    public List<ContainerType> getAvailableContainerTypes() {
        return availableContainerTypes;
    }
}
