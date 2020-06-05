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
import de.ipb_halle.lbac.exp.text.Text;
import de.ipb_halle.lbac.exp.text.TextController;

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
    public void actionAddRecord() {
        this.expRecordController = new TextController();
        ExpRecord record = this.expRecordController.getNewRecord();
        
        record.setExperiment(this.experiment);
        this.expRecords.add(record);
    }

    /**
     * ToDo: xxxxx validation and restrict search
     */
    public void actionSaveRecord() {
        this.expRecordService.save(this.expRecordController.getRecord());
        this.expRecordService.load();
    }

    /**
     *
     */
    public void actionCancel() {

    }

    public void actionNewExperiment() {
        experimentBeanInit();
    }

    public void actionSaveExperiment() {
        this.experimentService.save(this.experiment);
    }

    /**
     * ToDo: xxxxx restrict search
     */
    public void actionSelectExperiment(Experiment experiment) {
        this.experiment = experiment;
        try {
            this.expRecords = expRecordService.load();
        } catch(Exception e) {
            this.logger.warn("actionSelectExperiment() caught an exception: ", (Throwable) e);
            this.expRecords = new ArrayList<ExpRecord> ();
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

    public void setExperiment(Experiment experiment) {
        this.experiment = experiment;
    }
}
