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
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.device.print.PrintBean;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.Solvent;
import de.ipb_halle.lbac.items.bean.history.HistoryOperation;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.Material;

import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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
    private Material material;
    private HistoryOperation historyOperation;

    @Inject
    private PrintBean printBean;

    @Inject
    private ItemOverviewBean itemOverviewBean;
    @Inject
    private ProjectService projectService;

    @Inject
    private ContainerService containerService;

    @Inject
    private Navigator navigator;

    @Inject
    private ItemService itemService;

    private ContainerController containerController;

    private List<Project> projects = new ArrayList<>();
    private List<Container> containers = new ArrayList<>();
    private List<Solvent> solvents = new ArrayList<>();
    private List<String> purities = new ArrayList<>();
    private List<String> units = new ArrayList<>();
    private List<ContainerType> containerTypes = new ArrayList<>();

    private boolean commercialMaterial;
    @Inject
    private UserBean userBean;

    private Project project;
    private Container container;

    private String description;

    //Direct container of items
    private Double containerSize;
    private String containerUnit;
    private ContainerType basicContainerType;

    //Solvent infos
    private boolean solved;
    private Double concentration;
    private String purityUnit;
    private String solvent;

    //amount infos
    private Double amount;
    private String amountUnit;
    private String purity;

    private boolean directContainer;

    private String containerName;
    private Mode mode;

    public enum Mode {
        CREATE, EDIT, HISTORY
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

    public ContainerType getBasicContainerType() {
        return basicContainerType;
    }

    public void setBasicContainerType(ContainerType basicContainerType) {
        this.basicContainerType = basicContainerType;
    }

    /**
     * Printing an item label makes sense only for persisted items.
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
        state.getEditedItem().setContainer(container);

        if (mode == Mode.CREATE) {
            state.getEditedItem().setACList(material.getACList());
            state.getEditedItem().setOwner(userBean.getCurrentAccount());
            state.getEditedItem().setMaterial(material);
            state.getEditedItem().setcTime(new Date());
            state.setEditedItem(itemService.saveItem(state.getEditedItem()));
            this.printBean.setLabelDataObject(state.getEditedItem());

        } else {
            itemService.saveEditedItem(state.getEditedItem(), state.getOriginalItem(), userBean.getCurrentAccount());
            this.printBean.setLabelDataObject(state.getEditedItem());
        }
        containerService.deleteItemInContainer(state.getEditedItem().getId());
        for (int[] pos : containerController.resolveItemPositions()) {
            containerService.saveItemInContainer(
                    state.getEditedItem().getId(),
                    container.getId(),
                    pos[0],
                    pos[1]);
        }
        itemOverviewBean.reloadItems();
        navigator.navigate("/item/items");
    }

    public boolean isSolventRowVisisble() {
        return solved;
    }

    public void actionStartItemEdit(Item i) {
        projects = projectService.loadReadableProjectsOfUser(userBean.getCurrentAccount());
        containers = containerService.loadContainers(userBean.getCurrentAccount());
        containerTypes = containerService.loadContainerTypes();
        filterAndLocalizeContainerTypes();
        directContainer = i.getContainerType() != null;
        units = loadUnits();
        solvents = loadSolvents();
        purities = loadPurities();
        state = new ItemState(i);
        this.printBean.setLabelDataObject(state.getEditedItem());
        this.material = i.getMaterial();
        container = i.getContainer();
        historyOperation = new HistoryOperation(state);
        containerController = new ContainerController(this, container);
    }

    public void actionStartItemCreation(Material m) {
        mode = Mode.CREATE;
        state = new ItemState();
        this.printBean.setLabelDataObject(state.getEditedItem());
        directContainer = true;
        projects = projectService.loadReadableProjectsOfUser(userBean.getCurrentAccount());
        containers = containerService.loadContainers(userBean.getCurrentAccount());
        containerTypes = containerService.loadContainerTypes();
        filterAndLocalizeContainerTypes();

        units = loadUnits();
        solvents = loadSolvents();
        purities = loadPurities();
        clearFormular();
        containerController = new ContainerController(this, null);

        this.material = m;
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

    public String getPurityUnit() {
        return purityUnit;
    }

    public void setPurityUnit(String purityUnit) {
        this.purityUnit = purityUnit;
    }

    public void actionChangeContainer(Container c) {
        containerController = new ContainerController(this, c);
        this.container = c;
        this.containerName = c.getLabel();
    }

    public void onItemSelect(SelectEvent event) {
        containerName = (String) event.getObject();
        for (Container c : containers) {
            if (c.getLabel().equals(containerName)) {
                actionChangeContainer(c);
                return;
            }
        }

        actionChangeContainer(null);
    }

    public List<String> nameSuggestions(String enteredValue) {
        List<String> matches = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (Container c : containers) {
            names.add(c.getLabel());
        }
        for (String s : names) {
            if (enteredValue.trim().isEmpty() || s.toLowerCase().contains(enteredValue.toLowerCase())) {
                matches.add(s);
            }
        }
        return matches;
    }

    public String getMaterialName() {
        if (material == null) {
            return "no material choosen";
        } else {
            return material.getNames().get(0).getValue();
        }
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public List<Project> getProjects() {
        return projects;
    }

    public Container getContainer() {
        return container;
    }

    public void setContainer(Container container) {
        this.container = container;
    }

    public List<Container> getContainers() {
        return containers;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getContainerName() {
        if (container == null) {
            return "";
        } else {
            return container.getLabel();
        }
    }

    public String getContainerType() {
        if (container == null) {
            return "";
        } else {
            return container.getType().getName();
        }
    }

    public String getContainerProject() {
        if (container == null) {
            return "";
        } else {
            if (container.getProject() == null) {
                return "none";
            } else {
                return container.getProject().getName();
            }
        }
    }

    public String getContainerLocation() {
        if (container == null) {
            return "";
        } else {
            if (container.getLocation(true,true) == null) {
                return "unknown";
            } else {
                return container.getLocation(true,true);
            }
        }
    }

    public List<String> getUnits() {
        return units;
    }

    public String getContainerUnit() {
        return containerUnit;
    }

    public void setContainerUnit(String containerUnit) {
        this.containerUnit = containerUnit;
    }

    public String getSolvent() {
        return solvent;
    }

    public void setSolvent(String solvent) {
        this.solvent = solvent;
    }

    public List<Solvent> getSolvents() {
        return solvents;
    }

    public Double getConcentration() {
        return concentration;
    }

    public void setConcentration(Double concentration) {
        this.concentration = concentration;
    }

    public String getPurity() {
        return purity;
    }

    public void setPurity(String purity) {
        this.purity = purity;
    }

    public List<String> getPurities() {
        return purities;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getContainerSize() {
        return containerSize;
    }

    public void setContainerSize(Double containerSize) {
        this.containerSize = containerSize;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getAmountUnit() {
        return amountUnit;
    }

    public void setAmountUnit(String amountUnit) {
        this.amountUnit = amountUnit;
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

    private void clearFormular() {
        containerUnit = units.get(0);
        containerSize = null;
        container = null;
        basicContainerType = containerTypes.get(0);
        solved = false;
        concentration = null;
        purityUnit = units.get(0);
        solvent = "";
        amount = null;

        amountUnit = units.get(0);
        purity = purities.get(0);
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
                containerTypes.get(i).setLocalizedName(Messages.getString(MESSAGE_BUNDLE, "container_type_" + containerTypes.get(i).getName(), null));
            }
        }
    }

    public boolean isIdVisible() {
        return mode != Mode.CREATE;
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
}
