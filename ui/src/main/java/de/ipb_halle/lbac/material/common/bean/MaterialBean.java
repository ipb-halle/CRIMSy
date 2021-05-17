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
package de.ipb_halle.lbac.material.common.bean;

import de.ipb_halle.lbac.material.common.history.HistoryOperation;
import de.ipb_halle.lbac.material.biomaterial.TaxonomySelectionController;
import com.corejsf.util.Messages;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.i18n.UIMessage;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.structure.Molecule;
import static de.ipb_halle.lbac.material.common.bean.MaterialBean.Mode.HISTORY;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.material.biomaterial.BioMaterial;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.material.structure.StructureInformation;
import de.ipb_halle.lbac.material.biomaterial.Taxonomy;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageClass;
import de.ipb_halle.lbac.material.common.StorageClassInformation;

import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectBean;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.project.ProjectType;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.material.JsfMessagePresenter;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.material.common.MaterialDetailType;
import de.ipb_halle.lbac.material.common.service.HazardService;
import de.ipb_halle.lbac.util.chemistry.Calculator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Bean for interacting with the ui to present and manipulate a single material
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class MaterialBean implements Serializable {

    @Inject
    protected ProjectService projectService;

    @Inject
    protected MaterialService materialService;

    @Inject
    protected ProjectBean projectBean;

    @Inject
    protected ACListService acListService;

    @Inject
    protected Navigator navigator;

    @Inject
    protected UserBean userBean;

    @Inject
    protected MaterialOverviewBean overviewBean;

    @Inject
    protected MaterialNameBean materialNameBean;

    @Inject
    protected MaterialIndexBean materialIndexBean;

    @Inject
    protected TaxonomyService taxonomyService;

    @Inject
    protected HazardService hazardService;

    @Inject
    protected TissueService tissueService;

    protected Logger logger = LogManager.getLogger(this.getClass().getName());

    protected MaterialType currentMaterialType = null;

    protected List<Project> possibleProjects = new ArrayList<>();
    protected Mode mode;
    protected HazardInformation hazards;
    protected StorageClassInformation storageClassInformation;

    protected StructureInformation structureInfos;

    protected List<String> errorMessages = new ArrayList<>();

    private boolean autoCalcFormularAndMasses = true;

    private MaterialEditState materialEditState = new MaterialEditState();
    private HistoryOperation historyOperation;

    private MaterialEditPermission permission;

    private MaterialCreationSaver creationSaver;
    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";

    private TaxonomySelectionController taxonomyController;
    private TissueController tissueController;
    private MessagePresenter messagePresenter = JsfMessagePresenter.getInstance();

    private MaterialHazardController hazardController;

    public enum Mode {
        CREATE, EDIT, HISTORY
    };

    @PostConstruct
    public void init() {
        permission = new MaterialEditPermission(this);
        tissueController = new TissueController(this);
    }

    public void setCurrentAccount(@Observes LoginEvent evt) {

    }

    public void startMaterialCreation() {
        try {
            initState();
            materialEditState = new MaterialEditState();
            mode = Mode.CREATE;
            possibleProjects.clear();
            possibleProjects.add(materialEditState.getDefaultProject());
            possibleProjects.addAll(projectBean.getReadableProjects());
            taxonomyController = new TaxonomySelectionController(taxonomyService, tissueService, taxonomyService.loadTaxonomyById(1));
            hazardController = new MaterialHazardController(
                    hazardService, 
                    materialEditState.getMaterialToEdit(),
                    true);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    public void startMaterialEdit(Material m) {
        try {
            initState();
            mode = Mode.EDIT;
            materialNameBean.setNames(new ArrayList<>());
            Date currentVersionDate = null;
            if (!m.getHistory().getChanges().isEmpty()) {
                currentVersionDate = m.getHistory().getChanges().lastKey();
            }
            hazards = new HazardInformation(m);
            Project p = projectService.loadProjectById(m.getProjectId());
            materialEditState = new MaterialEditState(p, currentVersionDate, m.copyMaterial(), m, hazards);
            hazardController = new MaterialHazardController(
                    hazardService,
                    m,
                    acListService.isPermitted(ACPermission.permEDIT, m, userBean.getCurrentAccount()));
            possibleProjects.clear();
            possibleProjects.addAll(projectBean.getReadableProjects());
            currentMaterialType = m.getType();
            materialNameBean.getNames().addAll(m.getNames());
            materialIndexBean.getIndices().addAll(m.getIndices());
            storageClassInformation = new StorageClassInformation(m, initStorageClassNames());
            storageClassInformation.setRemarks(m.getStorageInformation().getRemarks());

            if (m.getType() == MaterialType.STRUCTURE) {
                Structure struc = (Structure) m;
                structureInfos = new StructureInformation(m);
                if (struc.getMolecule() != null) {
                    structureInfos.setStructureModel(struc.getMolecule().getStructureModel());
                }
                structureInfos.setExactMolarMass(struc.getExactMolarMass());
                structureInfos.setAverageMolarMass(struc.getAverageMolarMass());
                structureInfos.setSumFormula(struc.getSumFormula());
            }
            if (m.getType() == MaterialType.BIOMATERIAL) {
                BioMaterial bm = (BioMaterial) m;
                taxonomyController = new TaxonomySelectionController(taxonomyService, tissueService, bm.getTaxonomy());
            }
            historyOperation = new HistoryOperation(
                    materialEditState,
                    projectBean,
                    materialNameBean,
                    materialIndexBean,
                    structureInfos,
                    storageClassInformation,
                    taxonomyController,
                    hazardService.getAllHazardTypes()
            );

        } catch (Exception e) {
            logger.info("Error in Line " + e.getStackTrace()[0].getLineNumber());
            logger.error(e);
        }
    }

    private void initState() {
        hazards = new HazardInformation();
        storageClassInformation = new StorageClassInformation(initStorageClassNames());
        structureInfos = new StructureInformation();
        materialNameBean.init();
        materialIndexBean.init();
        currentMaterialType = MaterialType.CONSUMABLE;
        creationSaver = new MaterialCreationSaver(
                materialNameBean,
                materialService);
    }

    public List<MaterialType> getMaterialTypes() {
        try {
            if (materialEditState.getCurrentProject() == null) {
                return new ArrayList<>();
            } else {
                return materialEditState.getCurrentProject().getProjectType().getMaterialTypes();
            }
        } catch (Exception e) {
            logger.info("Error in getMaterialTypes(): " + materialEditState.getCurrentProject().getName());
            logger.error(e);
        }
        return new ArrayList<>();
    }

    public String getCreateButtonText() {
        if (mode == Mode.CREATE) {
            return Messages.getString(MESSAGE_BUNDLE, "materialCreation_buttonText_create", null);
        } else {
            return Messages.getString(MESSAGE_BUNDLE, "materialCreation_buttonText_save", null);
        }
    }

    public MaterialType getCurrentMaterialType() {
        return currentMaterialType;
    }

    public void setCurrentMaterialType(MaterialType currentMaterialType) {
        this.currentMaterialType = currentMaterialType;
    }

    public Project getCurrentProject() {
        return materialEditState.getCurrentProject();
    }

    public void setCurrentProject(Project currentProject) {
        try {
            this.materialEditState.setCurrentProject(currentProject);
            currentMaterialType = currentProject.getProjectType().getMaterialTypes().get(0);
        } catch (Exception e) {
            logger.info("Error in setCurrentProject(): " + currentProject.getName());
            logger.warn(e);
        }

    }

    public List<Project> getPossibleProjects() {
        return possibleProjects;
    }

    public HazardInformation getHazards() {
        return hazards;
    }

    public void setHazards(HazardInformation hazards) {
        this.hazards = hazards;
    }

    public StorageClassInformation getStorageClassInformation() {
        return storageClassInformation;
    }

    public void setStorageClassInformation(StorageClassInformation storageClassInformation) {
        this.storageClassInformation = storageClassInformation;
    }

    public void saveNewMaterial() throws Exception {
        if (checkInputValidity()) {
            hazards.setHazards(hazardController.createHazardMap());
            if (currentMaterialType == MaterialType.STRUCTURE) {
                creationSaver.saveNewStructure(autoCalcFormularAndMasses,
                        structureInfos,
                        materialEditState.getCurrentProject(),
                        hazards,
                        storageClassInformation,
                        materialIndexBean.getIndices(),
                        userBean.getCurrentAccount());
            } else if (currentMaterialType == MaterialType.BIOMATERIAL) {
                Taxonomy t = (Taxonomy) taxonomyController.getSelectedTaxonomy().getData();
                creationSaver.saveNewBioMaterial(
                        materialEditState.getCurrentProject(),
                        materialNameBean.getNames(),
                        t,
                        taxonomyController.getSelectedTissue(),
                        hazards,
                        storageClassInformation,
                        userBean.getCurrentAccount());
            } else if (currentMaterialType == MaterialType.CONSUMABLE) {
                creationSaver.saveConsumable(
                        materialEditState.getCurrentProject(),
                        hazards, storageClassInformation,
                        materialIndexBean.getIndices(),
                        userBean.getCurrentAccount());
            }
        } else {
            throw new Exception("Material not valide");
        }
    }

    public void saveEditedMaterial() throws Exception {
        setBasicInfos();
        if (materialEditState.getMaterialToEdit().getType() == MaterialType.STRUCTURE) {
            saveEditedStructure();
        }
        if (materialEditState.getMaterialToEdit().getType() == MaterialType.BIOMATERIAL) {
            saveEditedBioMaterial();
        }

        materialService.saveEditedMaterial(
                materialEditState.getMaterialToEdit(),
                materialEditState.getMaterialBeforeEdit(),
                materialEditState.getCurrentProject().getUserGroups().getId(),
                userBean.getCurrentAccount().getId());

    }

    private void setBasicInfos() {
        hazards.setHazards(hazardController.createHazardMap());
        materialEditState.getMaterialToEdit().setProjectId(materialEditState.getCurrentProject().getId());
        materialEditState.getMaterialToEdit().setNames(materialNameBean.getNames());
        materialEditState.getMaterialToEdit().setIndices(materialIndexBean.getIndices());
        materialEditState.getMaterialToEdit().setHazards(hazards);
        materialEditState.getMaterialToEdit().setStorageInformation(storageClassInformation);
    }

    private void saveEditedStructure() {
        Structure s = (Structure) materialEditState.getMaterialToEdit();
        Molecule m = new Molecule(structureInfos.getStructureModel(), 0);
        if (m.isEmptyMolecule()) {
            s.setMolecule(null);
            if (autoCalcFormularAndMasses) {
                structureInfos.setExactMolarMass(null);
                structureInfos.setAverageMolarMass(null);
                structureInfos.setSumFormula(null);
            }
        } else {
            s.setMolecule(m);
            if (autoCalcFormularAndMasses) {
                Calculator calc = new Calculator();
                structureInfos = calc.calculate(structureInfos);
            }
        }
        s.setExactMolarMass(structureInfos.getExactMolarMass());
        s.setAverageMolarMass(structureInfos.getAverageMolarMass());
        s.setSumFormula(structureInfos.getSumFormula());
    }

    private void saveEditedBioMaterial() {
        BioMaterial biomaterial = (BioMaterial) materialEditState.getMaterialToEdit();
        biomaterial.setTaxonomy((Taxonomy) taxonomyController.getSelectedTaxonomy().getData());
    }

    public void actionSaveMaterial() {
        try {
            if (mode == Mode.CREATE) {
                saveNewMaterial();
                UIMessage.info(MESSAGE_BUNDLE, "materialCreation_creation_new_completed");

            } else {
                saveEditedMaterial();
                UIMessage.info(MESSAGE_BUNDLE, "materialCreation_creation_edit_completed");

            }
            overviewBean.getSearchController().actionStartMaterialSearch();
            navigator.navigate("/material/materials");
        } catch (Exception e) {
            messagePresenter.error("materialCreation_creation_error", getErrorMessages());
        }
    }

    public StructureInformation getStructureInfos() {
        return structureInfos;
    }

    public void setStructureInfos(StructureInformation structureInfos) {
        this.structureInfos = structureInfos;
    }

    public MaterialNameBean getMaterialNameBean() {
        return materialNameBean;
    }

    public MaterialIndexBean getMaterialIndexBean() {
        return materialIndexBean;
    }

    public void setMaterialNameBean(MaterialNameBean materialNameBean) {
        this.materialNameBean = materialNameBean;
    }

    public void setMaterialIndexBean(MaterialIndexBean materialIndexBean) {
        this.materialIndexBean = materialIndexBean;
    }

    public boolean isValideProjectChoosen() {
        if (materialEditState.getCurrentProject() == null) {
            return false;
        }
        return materialEditState.getCurrentProject().getProjectType() != ProjectType.DUMMY_PROJECT;
    }

    public boolean isCreationAllowed() {
        if (mode == HISTORY) {
            return false;
        }
        if (materialEditState.getCurrentProject() == null || materialEditState.getCurrentProject().getProjectType() == ProjectType.DUMMY_PROJECT) {
            return false;
        }
        return true;
    }

    public boolean checkInputValidity() {
        boolean isValide = true;
        errorMessages.clear();
        int count = 1;
        for (MaterialName mn : materialNameBean.getNames()) {
            if (mn.getValue().isEmpty()) {
                isValide = false;
                errorMessages.add("Materialname at position " + count + " must not be empty");
            }
            count++;
        }
        return isValide;
    }

    public String getErrorMessages() {
        return String.join(" ", errorMessages);
    }

    public boolean isAutoCalcFormularAndMasses() {
        return autoCalcFormularAndMasses;
    }

    public void setAutoCalcFormularAndMasses(boolean autoCalc) {
        this.autoCalcFormularAndMasses = autoCalc;
    }

    public boolean isTypeChoiseDisabled() {
        return mode == Mode.EDIT || mode == Mode.HISTORY;
    }

    /**
     * Checks if the current user is the owner or has the right to edit the
     * project. If mode is in CREATE then always true, if in HISTORY then always
     * false.
     *
     * @return true if user is project edit is possible
     */
    public boolean isProjectEditEnabled() {
        logger.info("Mode " + mode);
        switch (mode) {
            case CREATE:
                return true;
            case HISTORY:
                return false;
            default:
                boolean isOnwer = materialEditState.getMaterialToEdit().getOwner().getId().equals(userBean.getCurrentAccount().getId());
                boolean hastRights = acListService.isPermitted(ACPermission.permEDIT, materialEditState.getMaterialToEdit().getACList(), userBean.getCurrentAccount());
                return isOnwer || hastRights;
        }
    }

    public boolean areRevisionElementsVisible() {
        return mode != Mode.CREATE;
    }

    public void switchOneVersionBack() {
        historyOperation.applyNextNegativeDifference();
        mode = Mode.HISTORY;

    }

    public void switchOneVersionForward() {
        historyOperation.applyNextPositiveDifference();
        mode = Mode.HISTORY;
        if (materialEditState.getMaterialBeforeEdit().getHistory().isMostRecentVersion(materialEditState.getCurrentVersiondate())) {
            mode = Mode.EDIT;
        }
    }

    public MaterialEditState getMaterialEditState() {
        return materialEditState;
    }

    private List<StorageClass> initStorageClassNames() {
        List<StorageClass> classes = materialService.loadStorageClasses();
        for (StorageClass sc : classes) {
            try {
                sc.setName(
                        Messages.getString(
                                MESSAGE_BUNDLE,
                                "materialCreation_storageclass_" + sc.getName(),
                                null));
            } catch (Exception e) {
                logger.error("Could not set locatlized storage class name", e);
            }
        }
        return classes;
    }

    public HistoryOperation getHistoryOperation() {
        return historyOperation;
    }

    public MaterialEditPermission getPermission() {
        return permission;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public ACListService getAcListService() {
        return acListService;
    }

    public UserBean getUserBean() {
        return userBean;
    }

    public TaxonomySelectionController getTaxonomyController() {
        return taxonomyController;
    }

    public boolean hasDetailRight(ACPermission what, MaterialDetailType onWhat) {
        ACList aclist = getMaterialEditState().getMaterialToEdit().getDetailRight(onWhat);
        boolean userHasEditRight = aclist != null && getAcListService().isPermitted(what, aclist, getUserBean().getCurrentAccount());
        boolean userIsOwner = getMaterialEditState().getMaterialToEdit().getOwner().getId().equals(getUserBean().getCurrentAccount().getId());
        return !(userIsOwner || userHasEditRight);
    }

    public boolean isTissueSelectionVisible() {
        return tissueController.isTissueRendered();
    }

    public MaterialHazardController getHazardController() {
        return hazardController;
    }

}
