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


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * interface for experiment record controllers
 *
 * @author fbroda
 */
public abstract class ExpRecordController {

    private ExperimentBean bean;
    private Logger logger = LogManager.getLogger(this.getClass().getName());

    protected ExpRecordController(ExperimentBean bean) {
        this.bean = bean;
    }

    public void actionCancel() {
        ExpRecord rec = getExpRecord();
        if (rec.getExpRecordId() == null) {
            this.bean.getExpRecords().remove(rec.getIndex());
        } else {
            int index = rec.getIndex();
            rec = this.bean.loadExpRecordById(rec.getExpRecordId());
            rec.setIndex(index);
            this.bean.getExpRecords().set(index, rec);
        }
        this.bean.cleanup();
        this.bean.reIndex();
        this.logger.info("actionCancel() completed");
    }

    public void actionSaveRecord() {
        try {
            ExpRecord rec = this.bean.saveExpRecord(getExpRecord());
            this.bean.adjustOrder(rec);
            setExpRecord(rec); 
            this.bean.cleanup();
            this.bean.reIndex();
            this.logger.info("actionSaveRecord() completed");
        } catch (Exception e) {
            this.logger.warn("actionSaveRecord() caught an exception: ", (Throwable) e);
        }
    }

    public abstract ExpRecord getExpRecord();
    public abstract ExpRecord getNewRecord();
    public abstract ExpRecordController setExpRecord(ExpRecord expRecord);
}
