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
import de.ipb_halle.lbac.project.ProjectSearchRequestBuilder;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.SearchResult;
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
    private List<String> units = new ArrayList<>();
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

    public void applyNextPositiveDiff() {
        historyOperation.applyNextPositiveDifference();
    }

    public ContainerController getContainerController() {
        return containerController;
    }

    public void setContainerController(ContainerController containerController) {
        this.containerController = containerController;
    }

    public void applyNextNegativeDiff() {
        historyOperation.applyNextNegativeDifference();
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

    public List<String> getPurityUnits() {
        return Arrays.asList("%", "M", "mM", "µM", "ppm");
    }

    public void actionSave() {
        state.getEditedItem().setContainer(containerController.getContainer());
        if (validator.itemValideToSave(state.getEditedItem(), containerController, customLabel, customLabelValue)) {
            if (mode == Mode.CREATE) {
                saveNewItem();
            } else {
                itemService.saveEditedItem(state.getEditedItem(), state.getOriginalItem(), userBean.getCurrentAccount(), containerController.resolveItemPositions());
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
    }

    public boolean isCustomLabelDisabled() {
        return mode == Mode.EDIT;
    }

    public boolean isLabelVisible() {
        return customLabel || mode == Mode.EDIT;
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
        solvents = loadSolvents();
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
        builder.addDeactivated(false);
        SearchResult response = projectService.loadProjects(builder.buildSearchRequest());
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

    public List<String> getUnits() {
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
        purities.add("sehr rein");
        purities.add("unrein");
        return purities;
    }

    private List<Solvent> loadSolvents() {
        List<Solvent> solvents = new ArrayList<>();
        solvents.add(new Solvent(1, "solvent 1", "Wasser"));
        solvents.add(new Solvent(2, "solvent 2", "Alkohol"));
        solvents.add(new Solvent(3, "solvent 3", "Benzin"));
        return solvents;
    }

    private List<String> loadUnits() {
        List<String> units = new ArrayList<>();
        units.add("l");
        units.add("ml");
        units.add("g");
        units.add("kg");
        units.add("einheit");
        return units;
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
        return mode == Mode.CREATE;
    }

    public boolean isDirectContainer() {
        return directContainer;
    }

    public void setDirectContainer(boolean directContainer) {
        this.directContainer = directContainer;
    }

    public String getSaveButtonText() {
        if (mode == Mode.CREATE) {
            return Messages.getString(MESSAGE_BUNDLE, "itemEdit_materialPanel_save", null);
        } else {
            return Messages.getString(MESSAGE_BUNDLE, "itemEdit_materialPanel_saveEdited", null);
        }
    }

    public boolean isPreviousVersionButtonDisabled() {
        if (state.getEditedItem().getHistory().isEmpty()) {
            return true;
        }
        return state.getEditedItem().getHistory().firstKey().equals(state.getCurrentHistoryDate());
    }

    public boolean isNextVersionButtonDisabled() {
        return state.getEditedItem().getHistory().isEmpty() || state.getCurrentHistoryDate() == null;

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
