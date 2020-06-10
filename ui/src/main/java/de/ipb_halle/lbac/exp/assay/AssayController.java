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
package de.ipb_halle.lbac.exp.assay;

import de.ipb_halle.lbac.exp.ExperimentBean;
import de.ipb_halle.lbac.exp.ExpRecord;
import de.ipb_halle.lbac.exp.ExpRecordController;
import de.ipb_halle.lbac.material.Material;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * controller for experiment records of subtype Assay
 *
 * @author fbroda
 */
public class AssayController implements ExpRecordController, MaterialHolder {

    private ExperimentBean bean;
    private Assay expRecord;
    private AssayRecord assayRecord;
    private Logger logger = LogManager.getLogger(this.getClass().getName());

    /**
     * constructor
     */
    public AssayController(ExperimentBean bean) {
        this.bean = bean;
    }

    public void actionAppendAssayRecord() {
        List<AssayRecord> records = this.expRecord.getRecords();
        int rank = records.size();
        this.assayRecord = new AssayRecord(this.expRecord, rank);
        records.add(this.assayRecord);
        actionEditAssayRecord(rank);    // disable edit on all other records
    }

    public void actionCancel() {
        if (this.expRecord.getExpRecordId() == null) {
            this.bean.getExpRecords().remove(this.expRecord.getIndex());
        } else {
            this.bean.getExpRecords().set(
                    this.expRecord.getIndex(), 
                    this.bean.loadExpRecordById(this.expRecord.getExpRecordId()));
        }
        bean.cleanup();
        this.logger.info("actionCancel() completed");
    }

    /**
     * set record 
     */
    public void actionEditAssayRecord(int rank) {
        List<AssayRecord> records = this.expRecord.getRecords();
        for (AssayRecord rec : records) {
            if (rec.getRank() == rank) {
                rec.setEdit(true);
                this.assayRecord = rec;
            } else {
                rec.setEdit(false);
            }
        }
    }

    public void actionSaveRecord() {
        this.expRecord = (Assay) this.bean.saveExpRecord(this.expRecord);
        this.bean.adjustOrder(this.expRecord);
        actionEditAssayRecord(-1);
        this.expRecord.setEdit(false);
        this.bean.cleanup();
        this.logger.info("actionSave() completed");
    }

    public AssayRecord getAssayRecord() {
        return this.assayRecord;
    }

    public Material getMaterial() {
        if (this.assayRecord != null) {
            return this.assayRecord.getMaterial();
        }
        return null;
    }

    public ExpRecord getNewRecord() {
        this.expRecord = new Assay();
        this.expRecord.setEdit(true);
        return this.expRecord;
    }

    public ExpRecord getRecord() {
        return this.expRecord;
    }

    public ExpRecordController setExpRecord(ExpRecord expRecord) {
        this.expRecord = (Assay) expRecord;
        return this;
    }

    public void setMaterial(Material material) {
        if (this.assayRecord != null) {
            this.assayRecord.setMaterial(material);
        }
    }

}
