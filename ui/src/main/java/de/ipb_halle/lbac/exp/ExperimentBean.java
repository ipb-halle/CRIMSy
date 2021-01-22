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

import com.corejsf.util.Messages;
import de.ipb_halle.lbac.admission.ACObject;
import de.ipb_halle.lbac.admission.ACObjectBean;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.exp.assay.AssayController;
import de.ipb_halle.lbac.exp.search.ExperimentSearchRequestBuilder;
import de.ipb_halle.lbac.exp.text.TextController;
import de.ipb_halle.lbac.exp.virtual.NullController;
import de.ipb_halle.lbac.exp.virtual.NullRecord;
import de.ipb_halle.lbac.globals.ACObjectController;
import de.ipb_halle.lbac.i18n.UIMessage;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.SearchResult;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
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
    protected MaterialAgent materialAgent;

    @Inject
    protected MemberService memberService;

    @Inject
    protected ProjectService projectService;

    private Experiment experiment;

    private List<ExpRecord> expRecords;

    private ExpRecordController expRecordController;

    private String newRecordType;

    private boolean templateMode = false;

    private BarChartModel barChart;

    private int expRecordIndex;
    private User currentUser;

    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private Experiment experimentInFocus;
    private ExpProjectController projectController;

    public void setCurrentAccount(@Observes LoginEvent evt) {
        currentUser = evt.getCurrentAccount();
        cleanup();
        initEmptyExperiment();
    }

    @PostConstruct
    protected void experimentBeanInit() {
        projectController = new ExpProjectController(projectService, currentUser);
        /*
         * ToDo: create an experiment with real user and ACL
         */
        cleanup();
        initEmptyExperiment();
        this.expRecords = new ArrayList<ExpRecord>();

    }

    private void initEmptyExperiment() {
        this.experiment = new Experiment(
                null, // experiment id
                "code", // code
                "description", // description
                templateMode, // template or experiment
                this.globalAdmissionContext.getPublicReadACL(), // aclist
                this.globalAdmissionContext.getPublicAccount(), // owner
                new Date() // creation time
        );
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
            this.logger.info("actionCancel() completed");
        } catch (Exception e) {
            this.logger.warn("actionCancel() caught an exception: ", (Throwable) e);
        }
    }

    /**
     * make an experiment from the current template
     */
    public void actionCopyTemplate() {
        Date copyDate = new Date();

        // load the template records
        Map<String, Object> cmap = new HashMap<String, Object>();
        cmap.put(ExpRecordService.EXPERIMENT_ID, this.experiment.getExperimentId());
        List<ExpRecord> records = this.expRecordService.load(cmap, currentUser);

        // copy the experiment
        this.experiment.setExperimentId(null);
        this.experiment.setTemplate(false);
        this.experiment.setCreationTime(copyDate);
        /* ToDo: xxxx set Owner 
        this.experiment.setOwner(...);
         */
        this.experiment = this.experimentService.save(this.experiment);

        // copy all experiment records
        for (ExpRecord rec : records) {
            rec.setCreationTime(copyDate);
            rec.setExperiment(this.experiment);
            rec.setExpRecordId(null);
            rec.copy();
            this.expRecordService.save(rec);
        }

        // activate the copied experiment 
        this.templateMode = false;
        loadExpRecords();
    }

    /**
     * select a record for editing
     */
    public void actionEditRecord() {
        if ((this.expRecordIndex > -1) && (this.expRecordIndex < this.expRecords.size())) {
            ExpRecord record = this.expRecords.get(this.expRecordIndex);
            this.logger.info("actionEditRecord(): ExpRecordId = {}", record.getExpRecordId());
            record.setEdit(true);
            createExpRecordController(record.getType().toString());
        }
    }

    public void actionLog() {
        this.logger.info("actionLog()");
    }

    /**
     * creates a new Experiment or a new template
     */
    public void actionNewExperiment() {
        projectController = new ExpProjectController(projectService, currentUser);
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
            record = this.expRecordService.save(record);
            this.expRecords.add(newPosition, record);

            reIndex();
            cleanup();
        }
    }

    public void actionSaveExperiment() {
        this.experiment.setProject(projectController.getChoosenProject());
        Experiment savedExp = this.experimentService.save(this.experiment);
        if (this.experiment.getExperimentId() == null) {
            UIMessage.info("exp_save_new");
        } else {
            UIMessage.info("exp_save_edit");
        }
        this.experiment = savedExp;
    }

    /**
     * toggle the currently active experiment ToDo: xxxxx restrict search
     *
     * @param exp
     */
    public void actionToggleExperiment(Experiment exp) {
        if ((exp != null) && (exp.getExperimentId() != null)) {
            if (exp.getExperimentId().equals(this.experiment.getExperimentId())) {
                experimentBeanInit();
                return;
            }
            this.experiment = exp;
            try {
                loadExpRecords();
            } catch (Exception e) {
                this.logger.warn("actionToggleExperiment() caught an exception: ", (Throwable) e);
                this.expRecords = new ArrayList<ExpRecord>();
            }
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
        return this.templateMode && (this.experiment.getExperimentId() != null);
    }

    public Experiment getExperiment() {
        return this.experiment;
    }

    /**
     * @return a localized label for the experiment edit button
     */
    public String getExperimentEditLabel() {
        if (this.experiment.getExperimentId() == null) {
            return Messages.getString(MESSAGE_BUNDLE, "NewBtnDlg", null);
        }
        if (this.templateMode) {
            return Messages.getString(MESSAGE_BUNDLE, "expEditCloneDlg", null);
        }
        return Messages.getString(MESSAGE_BUNDLE, "editBtnDlg", null);
    }

    public List<Experiment> getExperiments() {
        Map<String, Object> cmap = new HashMap<String, Object>();
        cmap.put(ExperimentService.TEMPLATE_FLAG, Boolean.valueOf(this.templateMode));
        ExperimentSearchRequestBuilder builder = new ExperimentSearchRequestBuilder(currentUser, 0, Integer.MAX_VALUE);
        builder.addTemplate(this.templateMode);
        SearchResult result = experimentService.load(builder.buildSearchRequest());
        if (!result.getAllFoundObjects().isEmpty()) {
            return result.getAllFoundObjects(Experiment.class, result.getNode());
        } else {
            return new ArrayList<>();
        }
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
            List<ExpRecord> list = new ArrayList<ExpRecord>(this.expRecords);
            list.add(new NullRecord().setIndex(list.size()));
            return list;
        }
        // should be an empty list
        return this.expRecords;
    }

    public String getExpRecordStyle(boolean edit, boolean even) {
        if (edit) {
            return "expRecordEdit";
        }
        if (even) {
            return "expRecordEven";
        }
        return "expRecordOdd";
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
        Map<String, Object> cmap = new HashMap<String, Object>();
        if ((this.experiment != null) && (this.experiment.getExperimentId() != null)) {
            cmap.put(ExpRecordService.EXPERIMENT_ID, this.experiment.getExperimentId());
        }
        cmap.put(ExperimentService.TEMPLATE_FLAG, Boolean.valueOf(this.templateMode));
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
            if (i == 0) {
                rec.setFirst(true);
            }
            i++;
        }
        if (i > 0) {
            this.expRecords.get(i - 1).setLast(true);
        }
    }

    /**
     * save experiment record; to be called by ExpRecordController
     *
     * @param record
     * @return
     */
    public ExpRecord saveExpRecord(ExpRecord record) {
        return this.expRecordService.save(record);
    }

    /**
     * Obtain a BarChart from an ExpRecord at the given index 
     * @param index the transient index property of the experiment record
     */
    public void setBarChartModel(int index) {
        this.barChart = this.expRecords.get(index).getBarChart();
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public void setExpRecordIndex(int index) {
        this.logger.info("setExpRecordIndex() index = {}", index);
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
                experimentInFocus.getId(),
                experimentInFocus.getACList());
    }

    @Override
    public void cancelAclChanges() {

    }

    @Override
    public void actionStartAclChange(ACObject aco) {
        experimentInFocus = (Experiment) aco;
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
    
    public boolean areLinksAddable(ExpRecord record){
        return record.getType()!=ExpRecordType.NULL;
    }

}
