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

import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.exp.entity.ExpRecordEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Experiment records (<code>ExpRecord</code>) are the abstract base class 
 * for the single elements, which form an experiment. Given enough time, we
 * plan to deliver implementations for the following use cases:
 *
 * - (Bio-) assays
 * - simple or formatted text records
 * - chemical reactions
 * - attached pictures and documents
 * - breeding / cultivation of organisms
 * - manipulation of DNA / RNA sequences
 * - etc.
 *
 * @author fbroda
 */
public abstract class ExpRecord implements DTO {

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    private Long            exprecordid;
    private Experiment      experiment;
    private ExpRecordType   type;
    private Date            creationtime;
    private Date            changetime;

    public ExpRecordEntity createExpRecordEntity() {
        return new ExpRecordEntity()
            .setChangeTime(this.changetime)
            .setCreationTime(this.creationtime)
            .setExpRecordId(this.exprecordid)
            .setExperimentId(this.experiment.getExperimentId())
            .setType(this.type);
    }

    public Date getChangeTime() {
        return this.changetime;
    }

    public Date getCreationTime() {
        return this.creationtime;
    }

    public Experiment getExperiment() { 
        return this.experiment; 
    }

    /**
     * update the current object from the entity
     */
    public ExpRecord setExpRecordEntity(ExpRecordEntity entity) {
        this.changetime = entity.getChangeTime();
        this.creationtime = entity.getCreationTime();
        this.exprecordid = entity.getExpRecordId();
        this.type = entity.getType();
        return this;
    }

    public Long getExpRecordId() {
        return this.exprecordid;
    }

    public ExpRecordType getType() {
        return this.type;
    }

    public ExpRecord setChangeTime(Date changetime) {
        this.changetime = changetime;
        return this;
    }

    public ExpRecord setCreationTime(Date creationtime) {
        this.creationtime = creationtime;
        return this;
    }

    public ExpRecord setExperiment(Experiment experiment) { 
        this.experiment = experiment;
        return this;
    }

    public ExpRecord setExpRecordId(Long exprecordid) { 
        this.exprecordid = exprecordid; 
        return this;
    }

    public ExpRecord setType(ExpRecordType type) {
        this.type = type;
        return this;
    }
}
