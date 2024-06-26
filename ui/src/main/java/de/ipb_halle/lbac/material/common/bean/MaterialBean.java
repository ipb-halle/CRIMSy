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
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.sequence.Sequence;
import de.ipb_halle.lbac.material.sequence.SequenceInformation;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.material.biomaterial.BioMaterial;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.material.structure.StructureInformation;
import de.ipb_halle.lbac.material.biomaterial.Taxonomy;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectBean;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.material.common.HazardType;
import de.ipb_halle.lbac.material.common.Invalidity;
import de.ipb_halle.lbac.material.common.MaterialDetailType;
import de.ipb_halle.lbac.material.common.MaterialValidator;

import static de.ipb_halle.lbac.material.common.bean.MaterialBean.Mode.HISTORY;

import de.ipb_halle.lbac.material.common.service.HazardService;
import de.ipb_halle.lbac.material.composition.Concentration;
import de.ipb_halle.lbac.material.composition.MaterialComposition;
import de.ipb_halle.lbac.material.composition.MaterialCompositionBean;
import de.ipb_halle.lbac.material.structure.Molecule;
import de.ipb_halle.lbac.project.ProjectType;
import de.ipb_halle.lbac.util.chemistry.Calculator;
import de.ipb_halle.lbac.util.performance.LoggingProfiler;

import java.io.Serializable;
import java.util.*;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
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

    private static final long serialVersionUID = 1L;

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
    protected MaterialCompositionBean compositionBean;

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

    @Inject
    protected LoggingProfiler loggingProfiler;

    protected Logger logger = LogManager.getLogger(this.getClass().getName());

    protected MaterialType currentMaterialType = null;

    protected Mode mode;
    protected HazardInformation hazards;

    protected StructureInformation structureInfos = new StructureInformation();

    protected SequenceInformation sequenceInfos;

    protected List<String> errorMessages = new ArrayList<>();

    private boolean autoCalcFormularAndMasses = true;

    protected HistoryOperation historyOperation;

    private MaterialEditPermission permission;

    private MaterialCreationSaver creationSaver;
    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";

    protected TaxonomySelectionController taxonomyController;
    private TissueController tissueController;

    @Inject
    protected transient MessagePresenter messagePresenter;

    protected MaterialEditState materialEditState;

    protected MaterialHazardBuilder hazardController;
    protected StorageInformationBuilder storageInformationBuilder;

    public enum Mode {
        CREATE, EDIT, HISTORY
    }

    ;

    @PostConstruct
    public void init() {
        loggingProfiler.profilerStart("MaterialBean");

        permission = new MaterialEditPermission(this);
        tissueController = new TissueController(this);
        loggingProfiler.profilerStop("MaterialBean");

    }

    public void startMaterialCreation() {

        try {
            initState();
            compositionBean.clearBean();
            materialEditState = new MaterialEditState(messagePresenter);
            materialEditState.addPossibleProjects(projectBean.getReadableProjects());
            mode = Mode.CREATE;
            taxonomyController = new TaxonomySelectionController(loggingProfiler, taxonomyService, tissueService, taxonomyService.loadTaxonomyById(1));
            hazardController = new MaterialHazardBuilder(
                    hazardService,
                    currentMaterialType,
                    true,
                    new HashMap<>(),
                    messagePresenter);
            storageInformationBuilder = new StorageInformationBuilder(
                    messagePresenter,
                    materialService
            );
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

            hazardController = new MaterialHazardBuilder(
                    hazardService,
                    m.getType(),
                    acListService.isPermitted(ACPermission.permEDIT, m, userBean.getCurrentAccount()),
                    m.getHazards().getHazards(),
                    messagePresenter);
            materialEditState = new MaterialEditState(p, currentVersionDate, m.copyMaterial(), m.copyMaterial(), hazardController, messagePresenter);
            loadReadableProjects(p);
            materialEditState.addPossibleProjects(projectBean.getReadableProjects());
            currentMaterialType = m.getType();
            materialNameBean.getNames().addAll(m.getCopiedNames());
            materialIndexBean.getIndices().addAll(m.getIndices());
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
                taxonomyController = new TaxonomySelectionController(loggingProfiler, taxonomyService, tissueService, bm.getTaxonomy());
            }
            if (m.getType() == MaterialType.COMPOSITION) {
                compositionBean.startCompositionEdit((MaterialComposition) m);
            }
            if (m.getType() == MaterialType.SEQUENCE) {
                Sequence sequence = (Sequence) m;
                sequenceInfos = new SequenceInformation();
                sequenceInfos.setSequenceData(sequence.getSequenceData());
            }

            storageInformationBuilder = new StorageInformationBuilder(
                    messagePresenter,
                    materialService,
                    m
            );
            storageInformationBuilder.setAccessRightToEdit(acListService.isPermitted(ACPermission.permEDIT, m, userBean.getCurrentAccount()));

            historyOperation = new HistoryOperation(this);
        } catch (Exception e) {
            logger.error("startMaterialEdit() caught an exception:", (Throwable) e);
        }
    }

    private void loadReadableProjects(final Project p) {
        List<Project> readableProjects = projectBean.getReadableProjects();

        boolean isIn = false;
        for (Project project : readableProjects) {
            if (p.getId() == project.getId()) {
                isIn = true;
            }
        }
        if (!isIn) {
            readableProjects.add(p);
        }
    }

    public List<HazardType> getAllPossibleHazards() {
        return hazardService.getAllHazardTypes();
    }

    private void initState() {
        errorMessages = new ArrayList<>();
        hazards = new HazardInformation();
        structureInfos = new StructureInformation();
        sequenceInfos = new SequenceInformation();
        materialNameBean.init();
        materialIndexBean.init();
        currentMaterialType = MaterialType.STRUCTURE;
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
            logger.error("getMaterialTypes(): error in project {}.", materialEditState.getCurrentProject().getName());
            logger.error("stack trace:", (Throwable) e);
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
        hazardController = new MaterialHazardBuilder(
                hazardService,
                currentMaterialType,
                true,
                new HashMap<>(),
                messagePresenter);
    }

    public Project getCurrentProject() {
        return materialEditState.getCurrentProject();
    }

    public void setCurrentProject(Project currentProject) {
        try {
            this.materialEditState.setCurrentProject(currentProject);
            currentMaterialType = currentProject.getProjectType().getMaterialTypes().get(0);
        } catch (Exception e) {
            logger.error("setCurrentProject(): error setting project '{}'", currentProject.getName());
            logger.error("stack trace:", (Throwable) e);
        }

    }

    public HazardInformation getHazards() {
        return hazards;
    }

    public void setHazards(HazardInformation hazards) {
        this.hazards = hazards;
    }

    public void saveNewMaterial() throws Exception {
        if (checkInputValidity()) {
            hazards.setHazards(hazardController.buildHazardsMap());
            if (currentMaterialType == MaterialType.STRUCTURE) {
                creationSaver.saveNewStructure(autoCalcFormularAndMasses,
                        structureInfos,
                        materialEditState.getCurrentProject(),
                        hazards,
                        storageInformationBuilder.build(),
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
                        storageInformationBuilder.build(),
                        userBean.getCurrentAccount());
            } else if (currentMaterialType == MaterialType.CONSUMABLE) {
                creationSaver.saveConsumable(
                        materialEditState.getCurrentProject(),
                        hazards,
                        storageInformationBuilder.build(),
                        materialIndexBean.getIndices(),
                        userBean.getCurrentAccount());
            } else if (currentMaterialType == MaterialType.COMPOSITION) {
                MaterialComposition composition = new MaterialComposition(
                        null,
                        materialNameBean.getNames(),
                        materialEditState.getCurrentProject().getId(),
                        hazards,
                        storageInformationBuilder.build(),
                        compositionBean.getChoosenType());
                composition.getIndices().addAll(materialIndexBean.getIndices());
                for (Concentration c : compositionBean.getConcentrationsInComposition()) {
                    composition.addComponent(c.getMaterial(), c.getConcentration(), c.getUnit());
                }

                materialService.saveMaterialToDB(composition, materialEditState.getCurrentProject().getDetailTemplates().get(MaterialDetailType.COMMON_INFORMATION).getId(), new HashMap<>(), userBean.getCurrentAccount());
            } else if (currentMaterialType == MaterialType.SEQUENCE) {
                Sequence sequence = new Sequence(
                        null,
                        materialNameBean.getNames(),
                        materialEditState.getCurrentProject().getId(),
                        hazards,
                        storageInformationBuilder.build(),
                        sequenceInfos.getSequenceData());
                materialService.saveMaterialToDB(sequence, materialEditState.getCurrentProject().getDetailTemplates().get(MaterialDetailType.COMMON_INFORMATION).getId(), new HashMap<>(), userBean.getCurrentAccount());
            }
        } else {
            throw new Exception("Material not valide");
        }
    }

    public void saveEditedMaterial() throws Exception {
        setBasicInfos();
        if (checkInputValidity()) {
            if (materialEditState.getMaterialToEdit().getType() == MaterialType.STRUCTURE) {
                saveEditedStructure();
            }
            if (materialEditState.getMaterialToEdit().getType() == MaterialType.BIOMATERIAL) {
                setTaxonomyToBioMaterial();
            }
            if (materialEditState.getMaterialToEdit().getType() == MaterialType.COMPOSITION) {
                MaterialComposition composition = (MaterialComposition) materialEditState.getMaterialToEdit();
                composition.getComponents().clear();
                for (Concentration c : compositionBean.getConcentrationsInComposition()) {
                    composition.addComponent(c.getMaterial(), c.getConcentration(), c.getUnit());
                }
            }
            if (materialEditState.getMaterialToEdit().getType() == MaterialType.SEQUENCE) {
                Sequence sequence = (Sequence) materialEditState.getMaterialToEdit();
                sequence.setSequenceData(sequenceInfos.getSequenceData());
            }
            materialService.saveEditedMaterial(
                    materialEditState.getMaterialToEdit(),
                    materialEditState.getMaterialBeforeEdit(),
                    materialEditState.getCurrentProject().getUserGroups().getId(),
                    userBean.getCurrentAccount().getId());

        } else {
            throw new Exception("Material not valide");
        }
    }

    private void setBasicInfos() {
        HazardInformation tmpHazards = new HazardInformation();
        tmpHazards.setHazards(hazardController.buildHazardsMap());
        materialEditState.getMaterialToEdit().setProjectId(materialEditState.getCurrentProject().getId());
        materialEditState.getMaterialToEdit().setNames(materialNameBean.getNames());
        materialEditState.getMaterialToEdit().setIndices(materialIndexBean.getIndices());
        materialEditState.getMaterialToEdit().setHazards(tmpHazards);
        materialEditState.getMaterialToEdit().setStorageInformation(storageInformationBuilder.build());

    }

    private void saveEditedStructure() {
        Structure s = (Structure) materialEditState.getMaterialToEdit();
        Structure originalStruc = (Structure) materialEditState.getMaterialBeforeEdit();
        int newMoleculeId = 0;
        if (originalStruc.getMolecule() != null) {
            newMoleculeId = originalStruc.getMolecule().getId();
        }
        Molecule m = new Molecule(structureInfos.getStructureModel(), newMoleculeId);
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

    private void setTaxonomyToBioMaterial() {
        BioMaterial biomaterial = (BioMaterial) materialEditState.getMaterialToEdit();
        try {
            biomaterial.setTaxonomy((Taxonomy) taxonomyController.getSelectedTaxonomy().getData());
        } catch (Exception e) {
            logger.error("setTaxonomyToBioMaterial() caught an exception:", (Throwable) e);
        }
    }

    public void actionSaveMaterial() {
        try {
            if (mode == Mode.CREATE) {
                saveNewMaterial();
                messagePresenter.info("materialCreation_creation_new_completed");
            } else {
                saveEditedMaterial();
                messagePresenter.info("materialCreation_creation_edit_completed");
            }
            overviewBean.getSearchController().actionStartMaterialSearch();
            navigator.navigate("/material/materials");
        } catch (Exception e) {
            logger.error("actionSaveMaterial() caught an exception:", (Throwable) e);
            messagePresenter.error("materialCreation_creation_error", getErrorMessages());
        }
    }

    public StructureInformation getStructureInfos() {
        return structureInfos;
    }

    public void setStructureInfos(StructureInformation structureInfos) {
        this.structureInfos = structureInfos;
    }

    public SequenceInformation getSequenceInfos() {
        return sequenceInfos;
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
        errorMessages.clear();
        MaterialValidator validator = currentMaterialType
                .getFactory()
                .createValidator();

        boolean validMaterial = validator.checkValidity(this);
        if (!validMaterial) {
            for (Invalidity error : validator.getInvalidities()) {
                errorMessages.add(messagePresenter.presentMessage("materialCreation_error_" + error.toString()));
            }

        }
        return validMaterial;
    }

    public List<String> getErrorMessages() {
        return errorMessages;
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
        storageInformationBuilder.setInHistoryMode(true);
        hazardController.setEditable(false);
        mode = Mode.HISTORY;

    }

    public void switchOneVersionForward() {
        historyOperation.applyNextPositiveDifference();
        mode = Mode.HISTORY;
        if (materialEditState.getMaterialBeforeEdit().getHistory().isMostRecentVersion(materialEditState.getCurrentVersiondate())) {
            hazardController.setEditable(
                    acListService.isPermitted(
                            ACPermission.permEDIT,
                            materialEditState.getMaterialBeforeEdit(),
                            userBean.getCurrentAccount()));
            storageInformationBuilder.setInHistoryMode(false);
            mode = Mode.EDIT;
        } else {
            hazardController.setEditable(false);
        }
    }

    public MaterialEditState getMaterialEditState() {
        return materialEditState;
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
        ACList aclist = getMaterialEditState().getMaterialToEdit().getACList();
        boolean userHasEditRight = aclist != null && getAcListService().isPermitted(what, aclist, getUserBean().getCurrentAccount());
        boolean userIsOwner = getMaterialEditState().getMaterialToEdit().getOwner().getId().equals(getUserBean().getCurrentAccount().getId());
        return !(userIsOwner || userHasEditRight);
    }

    public boolean isTissueSelectionVisible() {
        return tissueController.isTissueRendered();
    }

    public MaterialHazardBuilder getHazardController() {
        return hazardController;
    }

    public void setMessagePresenter(MessagePresenter messagePresenter) {
        this.messagePresenter = messagePresenter;
    }

    public void setTaxonomyService(TaxonomyService taxonomyService) {
        this.taxonomyService = taxonomyService;
    }

    public StorageInformationBuilder getStorageInformationBuilder() {
        return storageInformationBuilder;
    }

    public MaterialService getMaterialService() {
        return materialService;
    }

    public MaterialCompositionBean getCompositionBean() {
        return compositionBean;
    }

    public ProjectBean getProjectBean() {
        return projectBean;
    }

    public boolean isInHistoryMode() {
        return mode == Mode.HISTORY;
    }

}
