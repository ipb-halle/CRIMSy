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

import de.ipb_halle.lbac.exp.Experiment;

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
    private ExperimentService experimentService;

    @Inject
    private ExpRecordService expRecordService;

    private Experiment experiment; 

    private Logger logger = LogManager.getLogger(this.getClass().getName());


    @PostConstruct
    public void experimentBeanInit() {
        this.experiment = new Experiment(
            null,               // experiment id
            "code",             // code
            "description",      // description
            null,               // aclist
            null,               // owner
            new Date()          // creation time
            );
    }

    /**
     *
     */
    public void actionCancel() {

    }

    public void actionSelectExperiment(Experiment exp) {
        this.experiment = experiment;
    }

    public List<Experiment> getExperiments() {
        // xxxxx restrict search
        return experimentService.load();
    }

    public List<ExpRecord> getExpRecords() {
        // xxxxx restrict search
        try {
            this.logger.info("geExpRecords() got called");
            return expRecordService.load();
        } catch(Exception e) {
            this.logger.warn("getExpRecords() caught an exception: ", (Throwable) e);
        }
        return new ArrayList<ExpRecord> ();
    }
}
