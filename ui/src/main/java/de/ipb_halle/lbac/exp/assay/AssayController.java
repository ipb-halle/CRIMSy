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
import de.ipb_halle.lbac.exp.ItemHolder;
import de.ipb_halle.lbac.exp.MaterialHolder;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.MaterialType;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * controller for experiment records of subtype Assay
 *
 * @author fbroda
 */
public class AssayController extends ExpRecordController implements ItemHolder, MaterialHolder {

    private AssayRecord assayRecord;
    private String materialTarget;
    private Logger logger = LogManager.getLogger(this.getClass().getName());

    /**
     * constructor
     */
    public AssayController(ExperimentBean bean) {
        super(bean);
        bean.getMaterialAgent().setMaterialHolder(this);
        bean.getItemAgent().setItemHolder(this);
        this.materialTarget = "";
    }

    /**
     * append an AssayRecord to the current Assay
     */
    public void actionAppendAssayRecord() {
        this.logger.info("actionAppendAssayRecord()");
        try {
            Assay rec = (Assay) getExpRecord();
            List<AssayRecord> records = rec.getRecords();
            int rank = records.size();
            this.assayRecord = new AssayRecord(rec, rank);
            records.add(this.assayRecord);
            setRecordEdit(rank);    // select this record for edit
        } catch (Exception e) {
            this.logger.info("actionAppendAssayRecord() caught an exception", (Throwable) e);
        }
    }

    public AssayRecord getAssayRecord() {
        return this.assayRecord;
    }

    public Item getItem() {
        if (this.assayRecord != null) {
            return this.assayRecord.getItem();
        }
        return null;
    }

    public Material getMaterial() {
        if (this.assayRecord != null) {
            return this.assayRecord.getMaterial();
        }
        return null;
    }

    @Override
    public List<MaterialType> getMaterialTypes() {
        switch (this.materialTarget) {
            case "TARGET":
                return Arrays.asList(MaterialType.BIOMATERIAL);
            case "RECORD":
                return Arrays.asList(MaterialType.STRUCTURE);
        }
        return new ArrayList<>();
    }

    public ExpRecord getNewRecord() {
        ExpRecord rec = new Assay();
        rec.setEdit(true);
        return rec;
    }

    public void setItem(Item item) {
        if (this.assayRecord != null) {
            this.assayRecord.setItem(item);
        }
    }

    public void setMaterial(Material material) {
        switch (this.materialTarget) {
            case "TARGET":
                ((Assay) getExpRecord()).setTarget(material);
                break;
            case "RECORD":
                if (this.assayRecord != null) {
                    this.assayRecord.setMaterial(material);
                }
                break;
        }
    }

    public void setMaterialTarget(String target) {
        this.materialTarget = target;
        switch (target) {
            case "TARGET":
                getExperimentBean().getMaterialAgent().setShowMolEditor(false);
                break;
            case "RECORD":
                getExperimentBean().getMaterialAgent().setShowMolEditor(true);
                break;
        }
        this.logger.info("setMaterialTarget() {}", target);
    }

    /**
     * set record
     */
    public void setRecordEdit(int rank) {
        List<AssayRecord> records = ((Assay) getExpRecord()).getRecords();
        for (AssayRecord rec : records) {
            if (rec.getRank() == rank) {
                rec.setEdit(true);
                this.assayRecord = rec;
                this.logger.info("setRecordEdit({})", rank);
            } else {
                rec.setEdit(false);
            }
        }
    }

    public void editRecord(AssayRecord record) {
        this.assayRecord = record;
        this.assayRecord.setEdit(true);
        setMaterialTarget("RECORD");
        
    }
}
