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
import de.ipb_halle.lbac.material.JsfMessagePresenter;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectSearchRequestBuilder;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.util.Quality;
import de.ipb_halle.lbac.util.Unit;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.ValidatorException;
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
    private List<Solvent> availableSolvents = new ArrayList<>();
    private List<String> availablePurities = new ArrayList<>();
    private List<Unit> availableAmountUnits = new ArrayList<>();
    private List<Unit> availableConcentrationUnits = new ArrayList<>();
    private List<ContainerType> availableContainerTypes = new ArrayList<>();

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

    protected MessagePresenter messagePresenter = JsfMessagePresenter.getInstance();

    public enum Mode {
        CREATE, EDIT, HISTORY
    }

    @PostConstruct
    public void init() {
        validator = new Validator(containerPositionService, labelService);
        availableAmountUnits = loadAmountUnits();
        availableConcentrationUnits = loadConcentrationUnits();
        availableSolvents = loadAndI18nSolvents();
        availablePurities = loadPurities();
        availableContainerTypes = containerService.loadContainerTypes();
        filterLocalizeAndSortAvailableContainerTypes();
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
     * This action is called when clicking the 'cancel' button in
     * itemEdit.xhtml. It navigates the user depending on the {@code mode}
     * state.
     */
    public void actionCancel() {
        /*
         * We tried to create a new item via the materials table, so we go back
         * there.
         */
        if (mode == Mode.CREATE) {
            navigator.navigate("material/materials");
        } else {
            navigator.navigate("item/items");
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

    public void actionSave() {
        try {
            /*
             * This is double-safety: The commandButton is disabled in the UI when
             * viewing the history.
             */
            if (mode == Mode.HISTORY) {
                return;
            }
            if (!solved) {
                state.getEditedItem().setConcentration(null);
                state.getEditedItem().setConcentrationUnit(null);
                state.getEditedItem().setSolvent(null);
            }
            state.getEditedItem().setContainer(containerController.getContainer());
            if (validator.itemValideToSave(state.getEditedItem(), containerController, customLabel, customLabelValue)) {
                if (isCreateMode()) {
                    saveNewItem();
                    messagePresenter.info("itemEdit_save_new_success");
                } else {
                    itemService.saveEditedItem(
                            state.getEditedItem(),
                            state.getOriginalItem(),
                            userBean.getCurrentAccount(),
                            containerController.resolveItemPositions());
                    messagePresenter.info("itemEdit_save_edit_success");
                }
                this.printBean.setLabelDataObject(state.getEditedItem());
                itemOverviewBean.reloadItems();
                navigator.navigate("/item/items");
            }
        } catch (Exception e) {
            messagePresenter.error("itemEdit_save_failed");
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
        if (i.getContainer() != null) {
            i.setContainer(containerService.loadContainerById(i.getContainer().getId()));
        }
        mode = Mode.EDIT;
        directContainer = i.getContainerType() != null;
        state = new ItemState(i);
        solved = i.getSolvent() != null;
        containerController = new ContainerController(this, i.getContainer());
        historyOperation = new HistoryOperation(state, containerController);
        customLabelValue = i.getLabel();
        customLabel = false;
        initData();
    }

    private void initData() {
        projects = loadReadableProjects(userBean.getCurrentAccount());
        containers = containerService.loadContainersWithoutItems(userBean.getCurrentAccount());
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
        state.getEditedItem().setUnit(availableAmountUnits.get(0));
        state.getEditedItem().setMaterial(m);
        directContainer = true;
        solved = false;
        containerController = new ContainerController(this, null);
        customLabel = false;
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

    public List<ContainerType> getAvailableContainerTypes() {
        return availableContainerTypes;
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

    public List<Unit> getAvailableAmountUnits() {
        return availableAmountUnits;
    }

    public List<Unit> getAvailableConcentrationUnits() {
        return availableConcentrationUnits;
    }

    public List<Solvent> getAvailableSolvents() {
        return availableSolvents;
    }

    public List<String> getAvailablePurities() {
        return availablePurities;
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

    private List<Unit> loadAmountUnits() {
        return Unit.getUnitsOfQuality(
                Quality.MASS,
                Quality.VOLUME,
                Quality.PIECES);
    }

    private List<Unit> loadConcentrationUnits() {
        return Unit.getUnitsOfQuality(
                Quality.MOLAR_CONCENTRATION,
                Quality.MASS_CONCENTRATION,
                Quality.PERCENT_CONCENTRATION);
    }

    /**
     * Removes all containers with a rank greater than zero in the list of
     * available container types, sets the localized name for the remaining
     * containers and sorts the list.
     */
    private void filterLocalizeAndSortAvailableContainerTypes() {
        for (int i = availableContainerTypes.size() - 1; i >= 0; i--) {
            ContainerType type = availableContainerTypes.get(i);
            if (type.getRank() > 0) {
                availableContainerTypes.remove(i);
            } else {
                type.setLocalizedName(messagePresenter.presentMessage("container_type_" + type.getName()));

                if ((type.getLocalizedName() == null) || type.getLocalizedName().isEmpty()) {
                    logger.error("Could not set localized containerTypeName for " + type.getName());

                    // fallback to name of the entity or sorting will fail with a NPE
                    type.setLocalizedName(type.getName());
                }
            }
        }

        availableContainerTypes.sort(Comparator.comparing(ContainerType::getLocalizedName));
    }

    /**
     * This method should be triggered when the amount unit changes. It
     * transforms the amount and (if necessary) the container size of the
     * currently edited item to the new unit if the new and old unit qualities
     * match.
     * 
     * @param event
     */
    public void amountUnitChanged(ValueChangeEvent event) {
        /*
         * Postpone this event to a later phase because we want to update some
         * item values, which will be overwritten in the upcoming
         * UPDATE_MODEL_VALUES phase. See https://stackoverflow.com/a/11883021
         */
        if (event.getPhaseId() != PhaseId.INVOKE_APPLICATION) {
            event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            event.queue();
            return;
        }

        Unit oldUnit = (Unit) event.getOldValue();
        Unit newUnit = (Unit) event.getNewValue();

        if (oldUnit == null) {
            return;
        }

        if (oldUnit.getQuality() == newUnit.getQuality()) {
            Item item = getState().getEditedItem();

            if (item.getAmount() != null) {
                item.setAmount(item.getAmount() * oldUnit.transform(newUnit));
            }

            if (isDirectContainer() && (item.getContainerSize() != null)) {
                item.setContainerSize(item.getContainerSize() * oldUnit.transform(newUnit));
            }
        }
    }

    /**
     * This method should be triggered when the concentration unit changes. It
     * transforms the concentration of the currently edited item to the new unit
     * if the new and old unit qualities match.
     * 
     * @param event
     */
    public void concentrationUnitChanged(ValueChangeEvent event) {
        /*
         * Postpone this event to a later phase because we want to update some
         * item values, which will be overwritten in the upcoming
         * UPDATE_MODEL_VALUES phase. See https://stackoverflow.com/a/11883021
         */
        if (event.getPhaseId() != PhaseId.INVOKE_APPLICATION) {
            event.setPhaseId(PhaseId.INVOKE_APPLICATION);
            event.queue();
            return;
        }

        Item item = getState().getEditedItem();
        if (item.getConcentration() == null) {
            return;
        }

        Unit oldUnit = (Unit) event.getOldValue();
        Unit newUnit = (Unit) event.getNewValue();

        if (oldUnit == null) {
            return;
        }

        if ((oldUnit.getQuality() == newUnit.getQuality()) && getSolved()) {
            item.setConcentration(
                    item.getConcentration() * oldUnit.transform(newUnit));
        }
    }

    /**
     * Called when the 'substance solved' status of the currently edited item
     * changes. This method initializes/resets solvent-related properties of the
     * item.
     */
    public void onChangeSolved() {
        Item item = getState().getEditedItem();
        if (getSolved()) {
            item.setConcentrationUnit(availableConcentrationUnits.get(0));

            if (!availableSolvents.isEmpty()) {
                item.setSolvent(availableSolvents.get(0));
            }
        } else {
            item.setConcentration(null);
            item.setConcentrationUnit(null);
            item.setSolvent(null);
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

    /**
     * Cross-field validator that checks if the amount is less than or equal the
     * container size. See https://github.com/omnifaces/omnifaces/issues/411 for
     * details on how to use o:validateMultiple with disabled target components.
     * 
     * @param context
     * @param components components to be validated
     * @param values the components' submitted values
     * @return true if validation succeeds
     */
    public boolean validateAmountVsContainerSize(FacesContext context, List<UIInput> components, List<Object> values) {
        /*
         * This is a workaround.
         * The validation process of o:validateMultiple could be disabled via
         * disabled="#{itemBean.historyMode}". For some reason, the validator
         * is not invoked when returning back to the edit state from navigation
         * through the history.
         */
        if (!isDirectContainer() || isHistoryMode()) {
            return true;
        }
        if (values.size() != 2) {
            return false;
        }

        Object val1 = values.get(0);
        Object val2 = values.get(1);

        // amount field is null, that's not expected.
        if (val1 == null) {
            return false;
        }

        // containerSize field can be null in case the component is disabled (e.g. in edit mode).
        if (val2 == null) {
            val2 = components.get(1).getValue();
        }

        /*
         * JSF's convertNumber is not very exact on the specific type. Thus, we
         * need to convert to double.
         */
        if (!(val1 instanceof Number) || !(val2 instanceof Number)) {
            return false;
        }
        double amount = ((Number) val1).doubleValue();
        double containerSize = ((Number) val2).doubleValue();

        return amount <= containerSize;
    }
}
