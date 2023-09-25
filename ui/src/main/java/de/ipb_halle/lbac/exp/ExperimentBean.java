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
package de.ipb_halle.lbac.exp;

import de.ipb_halle.lbac.datalink.LinkedDataAgent;
import com.corejsf.util.Messages;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.ACObject;
import de.ipb_halle.lbac.admission.ACObjectBean;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.datalink.LinkCreationProcess;
import static de.ipb_halle.lbac.exp.ExperimentBean.CreationState.*;
import de.ipb_halle.lbac.exp.assay.AssayController;
import de.ipb_halle.lbac.exp.image.ImageController;
import de.ipb_halle.lbac.exp.search.ExperimentSearchRequestBuilder;
import de.ipb_halle.lbac.exp.text.TextController;
import de.ipb_halle.lbac.exp.virtual.NullController;
import de.ipb_halle.lbac.exp.virtual.NullRecord;
import de.ipb_halle.lbac.globals.ACObjectController;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.SearchResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.primefaces.model.chart.BarChartModel;

/**
 * Bean for interacting with the ui to present and manipulate a experiments
 *
 * @author fbroda
 */
@SessionScoped
@Named
public class ExperimentBean implements Serializable, ACObjectBean {

    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";
    protected ACObjectController acoController;
    /* access set to 'protected' to facilitate mocking during unit tests */
    @Inject
    protected GlobalAdmissionContext globalAdmissionContext;

    @Inject
    protected ExperimentService experimentService;

    @Inject
    protected ExpRecordService expRecordService;

    @Inject
    protected ItemAgent itemAgent;

    @Inject
    protected LinkedDataAgent linkedDataAgent;

    @Inject
    protected LinkCreationProcess linkCreationProcess;

    @Inject
    protected MaterialAgent materialAgent;

    @Inject
    protected MemberService memberService;

    @Inject
    protected ACListService aclistService;

    @Inject
    protected ProjectService projectService;

    private Experiment experiment;

    private List<ExpRecord> expRecords;

    protected ExpRecordController expRecordController;

    private String newRecordType;

    private boolean templateMode = false;

    private BarChartModel barChart;

    private int expRecordIndex;
    private User currentUser;

    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private ExpProjectController projectController;

    @Inject
    protected MessagePresenter messagePresenter;
    private List<Experiment> experiments = new ArrayList<>();
    private List<Experiment> templates = new ArrayList<>();
    private CreationState creationState = CreationState.CREATE;
    private String searchTerm;
    private final Integer MAX_EXPERIMENTS_TO_LOAD = 50;
    private ExperimentCode experimentCode;

    private ExpRecord lastSavedExpRecord = null;

    protected static final String expRecordEditCssClass = "expRecordEdit";
    protected static final String expRecordEvenCssClass = "expRecordEven";
    protected static final String expRecordOddCssClass = "expRecordOdd";
    protected static final String expRecordLastSavedCssClass = "expRecordLastSaved";

    public enum CreationState {
        CREATE,
        EDIT,
    }

    public ExperimentBean() {
    }

    public ExperimentBean(
            ItemAgent itemAgent,
            MaterialAgent materialAgent,
            GlobalAdmissionContext globalAdmissionContext,
            ProjectService projectService,
            ExperimentService experimentService,
            MessagePresenter messagePresenter,
            ExpRecordService expRecordService) {
        this.itemAgent = itemAgent;
        this.materialAgent = materialAgent;
        this.globalAdmissionContext = globalAdmissionContext;
        this.projectService = projectService;
        this.experimentService = experimentService;
        this.messagePresenter = messagePresenter;
        this.expRecordService = expRecordService;
    }

    public void setCurrentAccount(@Observes LoginEvent evt) {
        currentUser = evt.getCurrentAccount();
        templates = loadExperiments(true);
        experiments = loadExperiments(false);
        cleanup();
        initEmptyExperiment();
    }

    @PostConstruct
    public void init() {
        experimentBeanInit();
    }

    protected void experimentBeanInit() {
        projectController = new ExpProjectController(projectService, currentUser);
        /*
         * ToDo: create an experiment with real user and ACL
         */
        cleanup();
        initEmptyExperiment();
        this.expRecords = new ArrayList<>();
    }

    private void initEmptyExperiment() {
        this.experiment = new Experiment(
                null, // experiment id
                "", // code
                "", // description
                templateMode, // template or experiment
                this.globalAdmissionContext.getPublicReadACL(), // aclist
                this.globalAdmissionContext.getPublicAccount(), // owner
                new Date() // creation time
        );
        if (currentUser == null) {
            experimentCode = ExperimentCode.createNewInstance("");
        } else {
            experimentCode = ExperimentCode.createNewInstance(currentUser.getShortcut());
        }
        this.expRecords = new ArrayList<>();
        creationState = CREATE;
    }

    /**
     * insert a record
     *
     * @param index insert position - 1 (i.e. index of the preceding record)
     */
    public void actionAppendRecord(int index) {

        if ((this.experiment == null) || (this.experiment.getExperimentId() == null)) {
            this.logger.info("actionAppendRecord(): experiment not set");
            return;
        }

        createExpRecordController(this.newRecordType);
        if (this.expRecordController != null) {
            ExpRecord record = this.expRecordController.getNewRecord();
            record.setExperiment(this.experiment);

            if ((index < 0) || (index > this.expRecords.size())) {
                this.logger.info("actionAppendRecord() out of range");
                return;
            }

            switch (this.expRecords.size() - index) {
                case 0:
                    // NullRecord at end of ExpRecord list
                    break;
                case 1:
                    // last real record in ExpRecord list
                    index++;
                    break;
                default:
                    // start or middle of the list; link to the following record
                    index++;
                    record.setNext(this.expRecords.get(index).getExpRecordId());
            }

            // remember index (initially id is null)
            record.setIndex(index);
            this.expRecords.add(index, record);
            this.expRecordIndex = index;
            reIndex();
            record.setEdit(true);
        }
    }

    /**
     * cancel everything and reset this bean to clean state. This is especially
     * important when changing from Template to Experiment mode.
     */
    public void actionCancel() {
        try {
            this.expRecordController.actionCancel();
            experimentBeanInit();
        } catch (Exception e) {
            this.logger.warn("actionCancel() caught an exception: ", (Throwable) e);
        }
    }

    /**
     * make an experiment from the current template
     */
    public void actionCopyTemplate() {

        // load the template records
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(ExpRecordService.EXPERIMENT_ID, this.experiment.getExperimentId());
        List<ExpRecord> records = this.expRecordService.load(cmap, currentUser);

        // copy the experiment
        this.experiment = experiment.createExpFromTemplate(
                experimentCode.generateNewExperimentCode(
                        experimentService.getNextExperimentNumber(currentUser)
                ));

        /* ToDo: xxxx set Owner 
        this.experiment.setOwner(...);
         */
        this.experiment = this.experimentService.save(this.experiment);

        // copy all experiment records
        for (ExpRecord rec : records) {
            rec.setCreationTime(experiment.getCreationTime());
            rec.setExperiment(this.experiment);
            rec.setExpRecordId(null);
            rec.copy();
            this.expRecordService.save(rec, currentUser);
        }

        // activate the copied experiment 
        this.templateMode = false;
        loadExpRecords();
        this.experiments.add(0, experiment);
    }

    /**
     * select a record for editing
     */
    public void actionEditRecord(ExpRecord recordIn) {
        setExpRecordIndex(recordIn.getIndex());
        if ((this.expRecordIndex > -1) && (this.expRecordIndex < this.expRecords.size())) {
            ExpRecord record = recordIn;//this.expRecords.get(this.expRecordIndex);
            record.setEdit(true);
            createExpRecordController(record.getType().toString());
        }
    }

    public void actionStartEditExperiment(Experiment exp) {
        creationState = CreationState.EDIT;
        this.experiment = exp;
        projectController = new ExpProjectController(projectService, currentUser);
        experimentCode = ExperimentCode.createInstanceOfExistingExp(exp.getCode());
        loadExpRecords();
    }

    /**
     * creates a new Experiment or a new template
     */
    public void actionNewExperiment() {
        //New exp from template
        if (!templateMode || experiment.getId() == null) {
            this.expRecords = new ArrayList<>();
            projectController = new ExpProjectController(projectService, currentUser);
            initEmptyExperiment();
        }
    }

    public void actionNewExperimentRecord(String type, int index) {
        newRecordType = type;
        actionAppendRecord(index);
    }

    /**
     * rearranges a record record in the list. Needs to update links and to
     * reIndex all records
     *
     * @param delta number of positions to move the record up or down
     */
    public void actionRearrangeRecord(int delta) {
        ExpRecord other;
        int size = this.expRecords.size();
        int newPosition = this.expRecordIndex + delta;
        if ((this.expRecordIndex > -1)
                && (this.expRecordIndex < size)
                && (newPosition > -1)
                && (newPosition < size)) {

            ExpRecord record = this.expRecords.remove(this.expRecordIndex);
            List<ExpRecord> saveList = new ArrayList<>();

            if ((this.expRecordIndex == (size - 1))
                    && (size > 1)) {
                // set next field to null for new last record 
                other = this.expRecords.get(this.expRecordIndex - 1);
                other.setNext(null);
                saveList.add(other);
            } else {
                if (this.expRecordIndex > 0) {
                    // no action if first record has been removed
                    other = this.expRecords.get(this.expRecordIndex - 1);
                    other.setNext(this.expRecords.get(this.expRecordIndex).getExpRecordId());
                    saveList.add(other);
                }
            }
            if (newPosition == (size - 1)) {
                record.setNext(null);
            } else {
                if (newPosition > 0) {
                    other = this.expRecords.get(newPosition - 1);
                    other.setNext(record.getExpRecordId());
                    saveList.add(other);
                }
                other = this.expRecords.get(newPosition);
                record.setNext(other.getExpRecordId());
            }

            // save affected records
            for (ExpRecord rec : saveList) {
                this.expRecordService.saveOnly(rec);
            }
            record = this.expRecordService.save(record, currentUser);
            this.expRecords.add(newPosition, record);

            reIndex();
            cleanup();
        }
    }

    /**
     * Saves a new or edited experiment and puts it in the corresponding list to
     * show it
     */
    public void actionSaveExperiment() {
        this.experiment.setProject(projectController.getChoosenProject());
        Experiment savedExp;
        if (this.experiment.getExperimentId() == null) {
            savedExp = saveNewExperiment();
        } else {
            savedExp = saveEditedExperiment();
        }
        putExpInList(savedExp, getShownExperimentList());
        this.experiment = savedExp;
    }

    public void actionStartLinkCreation(ExpRecord record) {
        linkCreationProcess.startLinkCreation();
        actionEditRecord(record);
    }

    /**
     * Saves an experiment or a template depending on the template flag and
     * sends a message to the ui. The created experiment is add to the shown
     * experiments list
     *
     * @return
     */
    private Experiment saveNewExperiment() {
        this.experiment.setCode(
                experimentCode.generateNewExperimentCode(
                        experimentService.getNextExperimentNumber(currentUser))
        );
        Experiment savedExp = this.experimentService.save(this.experiment);
        addCreationMessageToUI();
        return savedExp;
    }

    /**
     * Sends a message to the ui which appears as a growl for the user
     */
    private void addCreationMessageToUI() {
        if (templateMode) {
            messagePresenter.info("exp_save_new_template");
        } else {
            messagePresenter.info("exp_save_new_experiment");
        }
    }

    private Experiment saveEditedExperiment() {
        this.experiment.setCode(experimentCode.generateExistingExperimentCode());
        Experiment savedExp = this.experimentService.save(this.experiment);
        messagePresenter.info("exp_save_edit");
        return savedExp;
    }

    private List<Experiment> getShownExperimentList() {
        if (templateMode) {
            return templates;
        } else {
            return experiments;
        }
    }

    private void putExpInList(Experiment exp, List<Experiment> list) {
        boolean alreadyIn = false;
        for (Experiment e : list) {
            if (Objects.equals(e.getId(), exp.getExperimentId())) {
                alreadyIn = true;
            }
        }
        if (!alreadyIn) {
            list.add(exp);
        }
    }

    /**
     * toggle the currently active experiment ToDo: xxxxx restrict search
     *
     * @param exp
     */
    public void actionToggleExperiment(Experiment exp) {
        if (isSavedInDb(exp)) {
            if (isExpSelected(exp)) {
                experimentBeanInit();
            } else {
                selectExperiment(exp);
            }
        }
    }

    private boolean isSavedInDb(Experiment exp) {
        return (exp != null) && (exp.getExperimentId() != null);
    }

    private boolean isExpSelected(Experiment exp) {
        return exp.getExperimentId().equals(this.experiment.getExperimentId());
    }

    public boolean isNewExpButtonDisabled() {
        return !currentUser.hasShortCut();
    }

    public String getNewExpToolTip() {
        if (!isNewExpButtonDisabled()) {
            return messagePresenter.presentMessage("exp_save_button_tooltip_valide");
        } else {
            if (currentUser.hasShortCut()) {
                return messagePresenter.presentMessage("exp_save_button_tooltip_noAccessRight");
            } else {
                return messagePresenter.presentMessage("exp_save_button_tooltip_noShortcut");
            }
        }
    }

    private void selectExperiment(Experiment exp) {
        this.experiment = exp;
        try {
            loadExpRecords();
        } catch (Exception e) {
            this.logger.warn("actionToggleExperiment() caught an exception: ", (Throwable) e);
            this.expRecords = new ArrayList<>();
        }
    }

    /**
     * maintain the proper chaining of ExpRecords
     *
     * @param record
     */
    public void adjustOrder(ExpRecord record) {
        int index = record.getIndex();
        if (index > 0) {
            ExpRecord rec = this.expRecords.get(index - 1);
            rec.setNext(record.getExpRecordId());
            this.expRecordService.saveOnly(rec);
        }
    }

    /**
     */
    public void cleanup() {
        this.expRecordController = new NullController(this);
        this.newRecordType = "";
        this.barChart = null;
        this.expRecordIndex = -1;
    }

    public void createExpRecordController(String recordType) {
        switch (recordType) {
            case "ASSAY":
                this.expRecordController = new AssayController(this);
                break;
            case "TEXT":
                this.expRecordController = new TextController(this);
                break;
            case "IMAGE":
                this.expRecordController = new ImageController(this);
                break;
            default:
                this.expRecordController = new NullController(this);
        }
    }

    public BarChartModel getBarChart() {
        return this.barChart;
    }

    /**
     * @return true if in template mode and an experiment has been selected
     */
    public boolean getCopyEnabled() {
        return this.templateMode
                && (this.experiment.getExperimentId() != null)
                && creationState == CREATE;
    }

    public void closeDialog() {
        creationState = CREATE;
    }

    public Experiment getExperiment() {
        return this.experiment;
    }

    /**
     * @return a localized label for the experiment edit button
     */
    public String getCreateNewExperimentButtonLabel() {
        if (this.templateMode) {
            if (experiment.getId() == null) {
                return Messages.getString(MESSAGE_BUNDLE, "expNewTemplate", null);
            } else {
                return Messages.getString(MESSAGE_BUNDLE, "expNewExperimentFromTemplate", null);
            }
        } else {
            return Messages.getString(MESSAGE_BUNDLE, "expNewExperiment", null);
        }
    }

    public List<Experiment> getExperiments() {
        if (templateMode) {
            return templates;
        } else {
            return experiments;
        }
    }

    public String getStyleClassOfLink(Experiment expToStyle) {
        return Objects.equals(expToStyle.getExperimentId(), experiment.getExperimentId()) ? "expSelectedEntry" : "expNormalEntry";
    }

    public ExpRecordController getExpRecordController() {
        return this.expRecordController;
    }

    /*
     * index of the currently selected ExpRecord
     */
    protected int getExpRecordIndex() {
        return this.expRecordIndex;
    }

    /**
     * @return the current list of ExpRecords
     */
    public List<ExpRecord> getExpRecords() {
        return this.expRecords;
    }

    /**
     * @return if <code>expRecords</code> is not empty, returns a copy of the
     * list of <code>expRecords</code> with an added NullRecord. The NullRecord
     * allows to append records to an empty list and is also a graphical
     * termination of a non-empty list of ExpRecords.
     */
    public List<ExpRecord> getExpRecordsWithNullRecord() {
        if (this.experiment.getExperimentId() != null) {
            List<ExpRecord> list = new ArrayList<>(this.expRecords);
            list.add(new NullRecord().setIndex(list.size()));
            return list;
        }
        // should be an empty list
        return this.expRecords;
    }

    public String getExpRecordStyle(ExpRecord record, boolean even) {
        StringJoiner sj = new StringJoiner(" ");

        if (record.getEdit()) {
            sj.add(expRecordEditCssClass);
        }

        if (even) {
            sj.add(expRecordEvenCssClass);
        } else {
            sj.add(expRecordOddCssClass);
        }

        if ((lastSavedExpRecord != null) && (record.getExpRecordId() != null)
                && (record.getExpRecordId()
                        .equals(lastSavedExpRecord.getExpRecordId()))) {
            sj.add(expRecordLastSavedCssClass);
        }

        return sj.toString();
    }

    public ItemAgent getItemAgent() {
        return this.itemAgent;
    }

    public LinkedDataAgent getLinkedDataAgent() {
        return this.linkedDataAgent;
    }

    public MaterialAgent getMaterialAgent() {
        return this.materialAgent;
    }

    public String getNewRecordType() {
        return "";
    }

    public String getRowStyle(boolean isLast) {
        if (!isLast) {
            return "experimentRecordBottomRow";
        } else {
            return "";
        }
    }

    public boolean getTemplateMode() {
        return this.templateMode;
    }

    public boolean isRecordEditable(ExpRecord record) {
        return record.getEdit();
    }

    public boolean isExperimentEditable(Experiment e) {
        return aclistService.isPermitted(
                ACPermission.permEDIT,
                e,
                currentUser
        );

    }

    public List<Experiment> loadExperiments(boolean template) {
        if (isSearchTermEmpty()) {
            return new ArrayList<>();
        }
        ExperimentSearchRequestBuilder builder = new ExperimentSearchRequestBuilder(currentUser, 0, MAX_EXPERIMENTS_TO_LOAD);
        builder.setTemplate(String.valueOf(template));
        builder.setCode(searchTerm);
        SearchResult result = experimentService.load(builder.build());
        if (!result.getAllFoundObjects().isEmpty()) {

            return result.getAllFoundObjects(Experiment.class, result.getNode());
        } else {
            return new ArrayList<>();
        }
    }

    private boolean isSearchTermEmpty() {
        return searchTerm == null || searchTerm.trim().isEmpty();
    }

    public void actionActualizeExperimentsList() {
        if (!isSearchTermEmpty()) {
            if (templateMode) {
                templates = loadExperiments(templateMode);
            } else {
                experiments = loadExperiments(templateMode);
            }
        } else {
            templates = new ArrayList<>();
            experiments = new ArrayList<>();
        }
    }

    /**
     * load a specific record by id e.g.for canceling edits
     *
     * @param id
     * @return
     */
    public ExpRecord loadExpRecordById(Long id) {
        return this.expRecordService.loadById(id, currentUser);
    }

    /**
     * load experiment records
     */
    public void loadExpRecords() {
        Map<String, Object> cmap = new HashMap<>();
        if ((this.experiment != null) && (this.experiment.getExperimentId() != null)) {
            cmap.put(ExpRecordService.EXPERIMENT_ID, this.experiment.getExperimentId());
        }
        cmap.put(ExperimentService.TEMPLATE_FLAG, this.templateMode);
        this.expRecords = this.expRecordService.orderList(
                this.expRecordService.load(cmap, currentUser));
        reIndex();

    }

    /**
     * re-index all records, clear edit flag and set last and first properties
     */
    public void reIndex() {
        int i = 0;
        for (ExpRecord rec : this.expRecords) {
            rec.setEdit(false);
            rec.setIndex(i);
            i++;
        }
    }

    /**
     * Saves an experiment record and caches it as last saved record. To be
     * called by ExpRecordController.
     *
     * @param record
     * @return
     */
    public ExpRecord saveExpRecord(ExpRecord record) {
        lastSavedExpRecord = null;
        lastSavedExpRecord = this.expRecordService.save(record, currentUser);
        return lastSavedExpRecord;
    }

    /**
     * Obtain a BarChart from an ExpRecord at the given index
     *
     * @param index the transient index property of the experiment record
     */
    public void setBarChartModel(int index) {
        this.barChart = this.expRecords.get(index).getBarChart();
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public void setExpRecordIndex(int index) {
        this.expRecordIndex = index;
    }

    public void setNewRecordType(String newRecordType) {
        this.newRecordType = newRecordType;
    }

    public void setTemplateMode(boolean templateMode) {
        this.templateMode = templateMode;
    }

    @Override
    public void applyAclChanges() {
        experimentService.updateExperimentAcl(
                experiment.getId(),
                experiment.getACList());
    }

    @Override
    public void cancelAclChanges() {

    }

    @Override
    public void actionStartAclChange(ACObject aco) {
        experiment = (Experiment) aco;
        acoController = new ACObjectController(
                aco,
                memberService.loadGroups(new HashMap<>()),
                this,
                ((Experiment) aco).getCode());

    }

    @Override
    public ACObjectController getAcObjectController() {
        return acoController;
    }

    public ExpProjectController getProjectController() {
        return projectController;
    }

    public boolean areLinksAddable(ExpRecord record) {
        return record.getType() != ExpRecordType.NULL;
    }

    public MessagePresenter getMessagePresenter() {
        return messagePresenter;
    }

    public ExpRecordService getExpRecordService() {
        return expRecordService;
    }

    public String getExperimentDialogHeader() {
        if (!templateMode && creationState == CREATE) {
            return messagePresenter.presentMessage("expAddNew_dialogHeader_new");

        } else if (templateMode && experiment.getId() != null && creationState == CREATE) {
            return messagePresenter.presentMessage("expAddNew_dialogHeader_clone");

        } else if (templateMode && experiment.getId() != null && creationState == EDIT) {
            return "Template anpassen";

        } else if (experiment.getId() != null && creationState == EDIT) {
            return messagePresenter.presentMessage("expAddNew_dialogHeader_edit");
        } else if (experiment.getId() == null && templateMode && creationState == CREATE) {
            return messagePresenter.presentMessage("expAddNew_dialogHeader_new");
        }
        return "no state set";
    }

    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Returns JavaScript code to be executed in the onclick event of the
     * experiment record "save" commandButton before executing its AJAX call.
     * This includes the onclick code specified via the specific experiment
     * record controller's {@link ExpRecordController#getSaveButtonOnClick()}
     * method. The call to actionDoNothing() in former releases is now skipped!
     * The actionListener and action of the commandButton are called afterwards.
     *
     * @return JavaScript code to be executed
     */
    public String getSaveButtonOnClick() {
        String onClickFromController = expRecordController.getSaveButtonOnClick();
        StringBuilder sb = new StringBuilder(onClickFromController.length() + 64);

        if (!onClickFromController.isEmpty()) {
            sb.append(onClickFromController);
            if (!onClickFromController.endsWith(";")) {
                sb.append(";");
            }
        }
        sb.append("PrimeFaces.oncomplete=function(xhr, status, args) { experimentBean.actionDoNothing(); return false; };");

        return sb.toString();

    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    /**
     * This method is used as fake action for the experiment record "save"
     * commandButton's AJAX call. It does nothing.
     */
    public void actionDoNothing() {
    }

    /**
     * Whether to disable the manipulation buttons shown for each experiment
     * record in the record list. This is the case as soon as a record is
     * edited.
     */
    public boolean isExpRecordButtonsDisabled() {
        return (expRecordIndex != -1);
    }

    public String getExperimentCodePrefix() {
        return experimentCode.getPrefix();
    }

    public String getExperimentCodeSuffix() {
        return experimentCode.getSuffix();
    }

    public void setExperimentCodeSuffix(String suffix) {
        experimentCode.setSuffix(suffix);
    }
}
