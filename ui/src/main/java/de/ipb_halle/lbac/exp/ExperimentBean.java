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

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.exp.Experiment;
import de.ipb_halle.lbac.exp.assay.AssayController;
import de.ipb_halle.lbac.exp.text.TextController;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Bean for interacting with the ui to present and manipulate a experiments 
 *
 * @author fbroda
 */
@SessionScoped
@Named
public class ExperimentBean implements Serializable {

    @Inject
    private GlobalAdmissionContext globalAdmissionContext;

    @Inject 
    private ExperimentService experimentService;

    @Inject
    private ExpRecordService expRecordService;

    private Experiment experiment; 

    private List<ExpRecord> expRecords;

    private ExpRecordController expRecordController;

    private String newRecordType;

    private Logger logger = LogManager.getLogger(this.getClass().getName());


    @PostConstruct
    private void experimentBeanInit() {
        /*
         * ToDo: create an experiment with real user and ACL
         */
        this.expRecords = new ArrayList<ExpRecord> ();

        this.experiment = new Experiment(
            null,                                               // experiment id
            "code",                                             // code
            "description",                                      // description
            this.globalAdmissionContext.getPublicReadACL(),     // aclist
            this.globalAdmissionContext.getPublicAccount(),     // owner
            new Date()                                          // creation time
            );
    }


    /**
     * ToDo: xxxxx support additional record types
     */
    public void actionAppendRecord(long id) {

        if (this.experiment == null) {
            this.logger.info("actionAppendRecord(): experiment not set");
            return;
        }

        createExpRecordController(this.newRecordType);
        if (this.expRecordController != null) {
            ExpRecord record = this.expRecordController.getNewRecord();
            record.setExperiment(this.experiment);

            // where to insert?
            int index = this.expRecords.size();
            if (id != 0) {
                index = getIndexOfExpRecord(Long.valueOf(id));
                if (index < 0) {
                    this.logger.info("actionAppendRecord() invalid insert position");
                    return;
                }
                index++;
            }  

            if (index < (this.expRecords.size())) {
                // link to the following record
                record.setNext(this.expRecords.get(index).getExpRecordId());
            }

            // remember index (initially id is null)
            record.setIndex(index);
            this.expRecords.add(index, record);
        }
    }

    /**
     *
     */
    public void actionCancel() {

    }

    /**
     * select a record for editing
     */
    public void actionEditRecord(Long id) {
        int index = getIndexOfExpRecord(Long.valueOf(id));
        if (index > -1) {
            this.logger.info("actionEditRecord(): ExpRecordId = {}", id);
            ExpRecord record = this.expRecords.get(index);
            record.setIndex(index);
            record.setEdit(true);
            createExpRecordController(record.getType().toString());
            this.expRecordController.setExpRecord(record);
        }
    }

    public void actionNewExperiment() {
        experimentBeanInit();
    }

    public void actionSaveExperiment() {
        this.experimentService.save(this.experiment);
    }

    /**
     * toggle the currently active experiment
     * ToDo: xxxxx restrict search
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
            } catch(Exception e) {
                this.logger.warn("actionToggleExperiment() caught an exception: ", (Throwable) e);
                this.expRecords = new ArrayList<ExpRecord> ();
            }
        }
    }

    /** 
     * maintain the proper chaining of ExpRecords
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
        this.expRecordController = null;
        this.newRecordType = "";
    }

    public void createExpRecordController(String recordType) {
        switch(recordType) {
            case "ASSAY" :
                this.expRecordController = new AssayController(this); 
                break;
            case "TEXT" :
                this.expRecordController = new TextController(this);
                break;
            default :
                this.expRecordController = null;
        }
    }

    public Experiment getExperiment() {
        return this.experiment;
    }

    public List<Experiment> getExperiments() {
        // xxxxx restrict search
        return experimentService.load();
    }

    public ExpRecordController getExpRecordController() {
        return this.expRecordController;
    }

    public List<ExpRecord> getExpRecords() {
        return this.expRecords;
    }

    /**
     * obtain the list index of a record with a specific
     * expRecordId.
     */
    public int getIndexOfExpRecord(Long expRecordId) {
        int index = -1;
        if (this.expRecords == null) {
            return index;
        }
        ListIterator<ExpRecord> iter = this.expRecords.listIterator();
        while (iter.hasNext()) {
            index++;
            if (iter.next().getExpRecordId().equals(expRecordId)) {
                return index;
            }
        }
        return -1;
    }

    public String getNewRecordType() {
        return "";
    }

    /**
     * load a specific record by id
     * e.g. for canceling edits
     */
    public ExpRecord loadExpRecordById(Long id) {
        return this.expRecordService.loadById(id);
    }

    /**
     * load experiment records 
     */
    public void loadExpRecords() {
        Map<String, Object> cmap = new HashMap<String, Object> ();
        if ((this.experiment != null) && (this.experiment.getExperimentId() != null)) {
            cmap.put(ExpRecordService.EXPERIMENT_ID, this.experiment.getExperimentId());
        }
        this.expRecords = this.expRecordService.orderList(
                this.expRecordService.load(cmap));
    }

    /**
     * save experiment record; to be called by ExpRecordController
     */
    public ExpRecord saveExpRecord(ExpRecord record) {
        return this.expRecordService.save(record);
    }

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }

    public void setNewRecordType(String newRecordType) {
        this.newRecordType = newRecordType;
    }
}
