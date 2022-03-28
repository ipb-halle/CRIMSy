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
package de.ipb_halle.lbac.items.bean.aliquot.createaliquot;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

import javax.ejb.EJBException;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.cxf.jaxrs.utils.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.FlowEvent;

import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.container.ContainerUtils;
import de.ipb_halle.lbac.container.service.ContainerPositionService;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.bean.ItemOverviewBean;
import de.ipb_halle.lbac.items.bean.Validator;
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
@SessionScoped
@Named
public class CreateAliquotBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private Logger logger = LogManager.getLogger(this.getClass().getName());

    @Inject
    private Navigator navigator;

    @Inject
    private transient MessagePresenter messagePresenter;

    @Inject
    private ItemService itemService;

    @Inject
    private ProjectService projectService;

    @Inject
    private ContainerService containerService;

    @Inject
    private ContainerPositionService containerPositionService;

    @Inject
    private ItemLabelService labelService;

    @Inject
    private UserBean userBean;

    @Inject
    private ItemOverviewBean itemOverviewBean;

    private Item parentItem; // r
    private Validator validator;

    private CreateAliquotStep1Controller step1Controller; // r
    private CreateAliquotStep2Controller step2Controller; // r
    private CreateAliquotStep3Controller step3Controller; // r
    private CreateAliquotStep4Controller step4Controller; // r

    private List<ContainerType> availableContainerTypes; // r

    private void init(Item parentItem) {
        this.parentItem = parentItem;
        validator = new Validator(containerPositionService, labelService);

        step1Controller = new CreateAliquotStep1Controller(parentItem);
        step2Controller = new CreateAliquotStep2Controller(step1Controller, messagePresenter);
        step3Controller = new CreateAliquotStep3Controller(parentItem, projectService, userBean);
        step4Controller = new CreateAliquotStep4Controller(containerService, userBean, messagePresenter);

        availableContainerTypes = loadAvailableContainerTypes();
    }

    private List<ContainerType> loadAvailableContainerTypes() {
        List<ContainerType> types = containerService.loadContainerTypes();
        ContainerUtils.filterLocalizeAndSortContainerTypes(types, messagePresenter);
        return types;
    }

    /*
     * PrimeFaces wizard
     */
    static final String STEP1 = "step1_amount";
    static final String STEP2 = "step2_directContainerAndLabel";
    static final String STEP3 = "step3_project";
    static final String STEP4 = "step4_location";

    public String onFlowProcess(FlowEvent event) {
        // STEP1 -> STEP2: amount <= item amount
        if (STEP2.equals(event.getNewStep())) {
            if (step1Controller.isAmountGreaterThanItemAmount()) {
                messagePresenter.error("itemCreateAliquot_error_amountTooHigh");
                return STEP1;
            } else {
                return STEP2;
            }
        }

        // STEP2 -> STEP3: amount <= container size
        if (STEP3.equals(event.getNewStep())) {
            if (step2Controller.isDirectContainer() && step2Controller.isAmountGreaterThanContainerSize()) {
                step2Controller.presentContainerTooSmallError();
                return STEP2;
            } else {
                return STEP3;
            }
        }

        return event.getNewStep();
    }

    /*
     * Actions
     */
    public void actionStartCreateAliquot(Item parentItem) {
        init(parentItem);
        navigator.navigate("/item/aliquot/createAliquot/createAliquot");
    }

    public void actionSave() {
        if (step1Controller.isAmountGreaterThanItemAmount()) {
            messagePresenter.error("itemCreateAliquot_error_amountTooHigh");
            return;
        }
        if (step2Controller.isDirectContainer() && step2Controller.isAmountGreaterThanContainerSize()) {
            step2Controller.presentContainerTooSmallError();
            return;
        }

        Item newItem = prepareNewItem();

        if (!validator.itemValidToSave(newItem, step4Controller.getContainerController(),
                step2Controller.isCustomLabel(), newItem.getLabel())) {
            return;
        }

        Quantity amountToSubtractFromParentItem = step1Controller.getAmountAsQuantity();

        try {
            saveAliquot(newItem, amountToSubtractFromParentItem);
            messagePresenter.info("itemEdit_save_new_success");

            itemOverviewBean.reloadItems();
            navigator.navigate("/item/items");
        } catch (EJBException e) {
            messagePresenter.error("itemEdit_save_failed");
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    private Item prepareNewItem() {
        Item item = new Item();

        item.setAmount(step1Controller.getAmount());
        item.setUnit(step1Controller.getAmountUnit());
        item.setContainer(step4Controller.getContainerController().getContainer());

        if (step2Controller.isDirectContainer()) {
            item.setContainerSize(step2Controller.getDirectContainerSize());
            item.setContainerType(step2Controller.getDirectContainerType());
        }

        item.setMaterial(parentItem.getMaterial());
        item.setProject(step3Controller.getSelectedProject());
        item.setPurity(parentItem.getPurity());
        item.setcTime(new Date());

        if (step2Controller.isCustomLabel()) {
            item.setLabel(step2Controller.getCustomLabelValue());
        }

        item.setParentId(parentItem.getId());
        item.setACList(parentItem.getACList());
        item.setOwner(userBean.getCurrentAccount());

        return item;
    }

    private void saveAliquot(Item newItem, Quantity massToSubtractFromParentItem) {
        int[] position = null;
        Set<int[]> setWithPositions = step4Controller.getContainerController().resolveItemPositions();
        if (newItem.getContainer() != null && !setWithPositions.isEmpty()) {
            position = setWithPositions.iterator().next();
        }

        itemService.saveAliquot(newItem, position, massToSubtractFromParentItem, userBean.getCurrentAccount());
    }

    public void actionCancel() {
        navigator.navigate("item/items");
    }

    /*
     * Getters/setters
     */
    public Item getParentItem() {
        return parentItem;
    }

    public CreateAliquotStep1Controller getStep1Controller() {
        return step1Controller;
    }

    public CreateAliquotStep2Controller getStep2Controller() {
        return step2Controller;
    }

    public CreateAliquotStep3Controller getStep3Controller() {
        return step3Controller;
    }

    public CreateAliquotStep4Controller getStep4Controller() {
        return step4Controller;
    }

    public List<ContainerType> getAvailableContainerTypes() {
        return availableContainerTypes;
    }
}