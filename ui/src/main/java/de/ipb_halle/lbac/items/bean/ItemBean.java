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
package de.ipb_halle.lbac.items.bean;

import com.corejsf.util.Messages;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.container.service.ContainerPositionService;
import de.ipb_halle.lbac.device.print.PrintBean;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.Solvent;
import de.ipb_halle.lbac.items.bean.history.HistoryOperation;
import de.ipb_halle.lbac.container.service.ContainerService;

import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.label.LabelService;
import de.ipb_halle.lbac.material.Material;

import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectSearchConditionBuilder;
import de.ipb_halle.lbac.project.ProjectSearchRequestBuilder;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchRequestBuilder;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.util.Quality;
import de.ipb_halle.lbac.util.Unit;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.SelectEvent;

/**
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class ItemBean implements Serializable {

    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";
    private ItemState state;
    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private HistoryOperation historyOperation;
    private boolean customLabel;

    @Inject
    protected PrintBean printBean;

    @Inject
    protected ItemOverviewBean itemOverviewBean;

    @Inject
    protected ProjectService projectService;

    @Inject
    protected ContainerService containerService;
    @Inject
    protected ContainerPositionService containerPositionService;

    @Inject
    protected Navigator navigator;

    @Inject
    protected ItemService itemService;

    @Inject
    protected LabelService labelService;

    private ContainerController containerController;
    private ContainerInfoPresenter containerInfoPresenter;

    private List<Project> projects = new ArrayList<>();
    private List<Container> containers = new ArrayList<>();
    private List<Solvent> solvents = new ArrayList<>();
    private List<String> purities = new ArrayList<>();
    private List<Unit> units = new ArrayList<>();
    private List<ContainerType> containerTypes = new ArrayList<>();

    private boolean commercialMaterial;
    private Validator validator;

    @Inject
    protected UserBean userBean;

    //Solvent infos
    private boolean solved;
    private boolean directContainer;

    private String containerName;
    protected Mode mode;
    private String customLabelValue;
    private ContainerPresenter containerPresenter;

    public enum Mode {
        CREATE, EDIT, HISTORY
    }

    @PostConstruct
    public void init() {
        validator = new Validator(containerPositionService, labelService);

    }

    public void actionApplyNextPositiveDifference() {
        /*
         * This is double-safety: The commandButton is disabled in the UI when
         * creating an item.
         */
        if (mode == Mode.CREATE) {
            return;
        }

        historyOperation.applyNextPositiveDifference();

        /*
         * We jump from a historic view to the edit view of the item, thus the
         * input components should be enabled. This check is done in the view
         * via the 'mode' field.
         */
        if ((mode == Mode.HISTORY) && state.isLastHistoryItem()) {
            mode = Mode.EDIT;
        }
    }

    public ContainerController getContainerController() {
        return containerController;
    }

    public void setContainerController(ContainerController containerController) {
        this.containerController = containerController;
    }

    public void actionApplyNextNegativeDifference() {
        /*
         * This is double-safety: The commandButton is disabled in the UI when
         * creating an item.
         */
        if (mode == Mode.CREATE) {
            return;
        }

        historyOperation.applyNextNegativeDifference();

        /*
         * We jump from the edit view to a historic view of the item, thus all
         * input components should be disabled. This check is done in the view
         * via the 'mode' field.
         */
        if (mode == Mode.EDIT) {
            mode = Mode.HISTORY;
        }
    }

    /**
     * Printing an item label makes sense only for persisted items.
     *
     * @return true if item id is not null
     */
    public boolean getLabelPrintingEnabled() {
        return (state.getEditedItem().getId() != null);
    }

    public PrintBean getPrintBean() {
        return this.printBean;
    }

    public List<String> getConcentrationUnits() {
        return Arrays.asList("%", "M", "mM", "µM", "ppm");
    }

    public void actionSave() {
        state.getEditedItem().setContainer(containerController.getContainer());
        if (validator.itemValideToSave(state.getEditedItem(), containerController, customLabel, customLabelValue)) {
            // TODO: what should apply if we are in history mode?
            if (isCreateMode()) {
                saveNewItem();
            } else {
                itemService.saveEditedItem(
                        state.getEditedItem(),
                        state.getOriginalItem(),
                        userBean.getCurrentAccount(),
                        containerController.resolveItemPositions());
            }
            this.printBean.setLabelDataObject(state.getEditedItem());
            itemOverviewBean.reloadItems();
            navigator.navigate("/item/items");
        }

    }

    private void saveNewItem() {
        state.getEditedItem().setACList(state.getEditedItem().getMaterial().getACList());
        state.getEditedItem().setOwner(userBean.getCurrentAccount());
        state.getEditedItem().setcTime(new Date());
        if (customLabel) {
            state.getEditedItem().setLabel(customLabelValue);
        }
        state.setEditedItem(itemService.saveItem(state.getEditedItem()));
        if (containerController.getContainer() != null && containerController.getItemPositions() != null) {
            int[] positions = containerController.resolveItemPositions().iterator().next();
            containerPositionService.saveItemInContainer(state.getEditedItem().getId(), containerController.getContainer().getId(), positions[0], positions[1]);
        }
    }

    public boolean isCreateMode() {
        return mode == Mode.CREATE;
    }

    public boolean isEditMode() {
        return mode == Mode.EDIT;
    }

    public boolean isHistoryMode() {
        return mode == Mode.HISTORY;
    }

    public boolean isCustomLabelDisabled() {
        return !isCreateMode();
    }

    public boolean isLabelVisible() {
        return customLabel || isEditMode();
    }

    public boolean isSolventRowVisisble() {
        return solved;
    }

    public void actionStartItemEdit(Item i) {
        mode = Mode.EDIT;
        directContainer = i.getContainerType() != null;
        state = new ItemState(i);
        solved = i.getSolvent() != null;
        containerController = new ContainerController(this, i.getContainer());
        historyOperation = new HistoryOperation(state, containerController);
        customLabelValue = i.getLabel();
        initData();
    }

    private void initData() {
        projects = loadReadableProjects(userBean.getCurrentAccount());
        containers = containerService.loadContainers(userBean.getCurrentAccount());
        containerTypes = containerService.loadContainerTypes();
        filterAndLocalizeContainerTypes();
        units = loadUnits();
        solvents = loadAndI18nSolvents();
        purities = loadPurities();
        this.printBean.setLabelDataObject(state.getEditedItem());
        this.containerPresenter = new ContainerPresenter(this, containerName, containerService, containers);
        this.containerInfoPresenter = new ContainerInfoPresenter(containerController.getContainer());

    }

    public void setContainerInfoPresenter(ContainerInfoPresenter containerInfoPresenter) {
        this.containerInfoPresenter = containerInfoPresenter;
    }

    public ContainerInfoPresenter getContainerInfoPresenter() {
        return containerInfoPresenter;
    }

    public void actionStartItemCreation(Material m) {
        mode = Mode.CREATE;
        state = new ItemState();
        state.getEditedItem().setMaterial(m);
        directContainer = true;
        solved = false;
        containerController = new ContainerController(this, null);
        customLabelValue = "";
        initData();
    }

    private List<Project> loadReadableProjects(User user) {
        ProjectSearchRequestBuilder builder = new ProjectSearchRequestBuilder(user, 0, Integer.MAX_VALUE);
        builder.setDeactivated(false);
        SearchResult response = projectService.loadProjects(builder.build());
        return response.getAllFoundObjects(Project.class, response.getNode());
    }

    public ContainerPresenter getContainerPresenter() {
        return containerPresenter;
    }

    public List<ContainerType> getContainerTypes() {
        return containerTypes;
    }

    public boolean isCommercialMaterial() {
        return commercialMaterial;
    }

    public void setCommercialMaterial(boolean commercialMaterial) {
        this.commercialMaterial = commercialMaterial;
    }

    public void actionChangeContainer(Container c) {
        containerController = new ContainerController(this, c);
        containerInfoPresenter = new ContainerInfoPresenter(c);
        this.containerName = c.getLabel();

    }

    public void onItemSelect(SelectEvent event) {
        containerName = (String) event.getObject();
        int containerId = Integer.parseInt(containerName.split("-")[0]);
        containerService.loadContainerById(containerId);
        Container c = containerService.loadContainerById(containerId);
        actionChangeContainer(c);

    }

    public List<String> nameSuggestions(String enteredValue) {
        List<String> matches = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (Container c : containers) {
            names.add(c.getAutoCompleteString());
        }
        for (String s : names) {
            if (enteredValue != null && (enteredValue.trim().isEmpty() || s.toLowerCase().contains(enteredValue.toLowerCase()))) {
                matches.add(s);
            }
        }
        return matches;
    }

    public String getMaterialName() {
        return state.getEditedItem().getMaterial().getNames().get(0).getValue();
    }

    public List<Project> getProjects() {
        return projects;
    }

    public Container getContainer() {
        return containerController.getContainer();
    }

    public void setContainer(Container container) {
        containerController = new ContainerController(this, container);
    }

    public List<Container> getContainers() {
        return containers;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public List<Unit> getUnits() {
        return units;
    }

    public List<Solvent> getSolvents() {
        return solvents;
    }

    public List<String> getPurities() {
        return purities;
    }

    public ItemState getState() {
        return state;
    }

    public Boolean getSolved() {
        return solved;
    }

    public void setSolved(Boolean solved) {
        this.solved = solved;
    }

    private List<String> loadPurities() {
        List<String> purities = new ArrayList<>();
        purities.add("unbekannt");
        purities.add("> 80 %");
        purities.add("> 90 %");
        purities.add("> 95 %");
        purities.add("> 98 %");
        purities.add("> 99 %");
        purities.add("> 99.9 %");
        return purities;
    }

    private List<Solvent> loadAndI18nSolvents() {
        List<Solvent> solvents = itemService.loadSolvents();
        // TO TO: i18n solvents
        return solvents;
    }

    private List<Unit> loadUnits() {
        return Unit.getUnitsOfQuality(
                Quality.MASS,
                Quality.VOLUME,
                Quality.PIECES);
    }

    /**
     * Removes all container with a rank greater than zero and set its localized
     * name
     */
    private void filterAndLocalizeContainerTypes() {
        for (int i = containerTypes.size() - 1; i >= 0; i--) {
            if (containerTypes.get(i).getRank() > 0) {
                containerTypes.remove(i);
            } else {
                try {
                    containerTypes.get(i).setLocalizedName(Messages.getString(MESSAGE_BUNDLE, "container_type_" + containerTypes.get(i).getName(), null));
                } catch (Exception e) {
                    logger.error("Could not set localized containerTypeName");
                }
            }
        }
    }

    public boolean isUnitEditable() {
        return isCreateMode();
    }

    public boolean isDirectContainer() {
        return directContainer;
    }

    public void setDirectContainer(boolean directContainer) {
        this.directContainer = directContainer;
    }

    public boolean isCustomLabel() {
        return customLabel;
    }

    public void setCustomLabel(boolean customLabel) {
        this.customLabel = customLabel;
    }

    public String getCustomLabelValue() {
        return customLabelValue;
    }

    public void setCustomLabelValue(String customLabelValue) {
        this.customLabelValue = customLabelValue;
    }

    public void setState(ItemState state) {
        this.state = state;
    }

}
