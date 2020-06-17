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

import org.primefaces.model.chart.BarChartModel;

/**
 * controller for experiment records of subtype Assay
 *
 * @author fbroda
 */
public class AssayController extends ExpRecordController implements MaterialHolder {

    private ExperimentBean bean;
    private Assay expRecord;
    private AssayRecord assayRecord;
    private Logger logger = LogManager.getLogger(this.getClass().getName());

    /**
     * constructor
     */
    public AssayController(ExperimentBean bean) {
        super(bean);
    }

    public void actionAppendAssayRecord() {
        this.logger.info("actionAppendAssayRecord()");
        try {
            List<AssayRecord> records = this.expRecord.getRecords();
            int rank = records.size();
            this.assayRecord = new AssayRecord(this.expRecord, rank);
            records.add(this.assayRecord);
            actionEditAssayRecord(rank);    // select this record for edit
        } catch (Exception e) {
            this.logger.info("actionAppendAssayRecord() caught an exception" , (Throwable) e); 
        }
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
                this.logger.info("actionEditAssayRecord({})", rank);
            } else {
                rec.setEdit(false);
            }
        }
    }

    public AssayRecord getAssayRecord() {
        return this.assayRecord;
    }

    @Override
    public BarChartModel getBarChart() {
        return this.expRecord.getBarChart();
    }

    public Material getMaterial() {
        if (this.assayRecord != null) {
            return this.assayRecord.getMaterial();
        }
        return null;
    }

    public ExpRecord getExpRecord() {
        return this.expRecord;
    }

    public ExpRecord getNewRecord() {
        this.expRecord = new Assay();
        this.expRecord.setEdit(true);
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
