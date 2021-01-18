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

import de.ipb_halle.lbac.exp.LinkedData;
import de.ipb_halle.lbac.exp.ExperimentBean;
import de.ipb_halle.lbac.exp.ExpRecord;
import de.ipb_halle.lbac.exp.ExpRecordController;
import de.ipb_halle.lbac.exp.LinkedDataType;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.util.UnitsValidator;
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
public class AssayController extends ExpRecordController {
    
    private Logger logger = LogManager.getLogger(this.getClass().getName());

    /**
     * constructor
     */
    public AssayController(ExperimentBean bean) {
        super(bean);
        bean.getMaterialAgent().setMaterialHolder(this);
        bean.getItemAgent().setItemHolder(this);
    }

    /**
     * append an AssayRecord to the current Assay
     */
    public void actionAppendAssayRecord() {
        this.logger.info("actionAppendAssayRecord()");
        try {
            Assay rec = (Assay) getExpRecord();

            List<LinkedData> records = rec.getLinkedData();

            int rank = rec.getLinkedDataNextRank();

            LinkedData assayRecord = new LinkedData(rec,  
                LinkedDataType.ASSAY_SINGLE_POINT_OUTCOME, rank);
            assayRecord.setPayload(
                new SinglePointOutcome(
                UnitsValidator.getUnitSet(rec.getUnits())
                .iterator()
                .next()
                .toString()));

            records.add(assayRecord);
            rec.reIndexLinkedData();
            setLinkedDataIndex(assayRecord.getIndex());

        } catch (Exception e) {
            this.logger.info("actionAppendAssayRecord() caught an exception", (Throwable) e);
        }
    }
    
    @Override
    public List<MaterialType> getMaterialTypes() {
        switch (getExpRecord()
                .getLinkedData()
                .get(getLinkedDataIndex())
                .getLinkedDataType()) {
            case ASSAY_TARGET :
                return Arrays.asList(MaterialType.BIOMATERIAL);
            case ASSAY_SINGLE_POINT_OUTCOME :
            case ASSAY_MULTI_POINT_OUTCOME :
                return Arrays.asList(MaterialType.STRUCTURE);
            default :
                return super.getMaterialTypes();
        }
    }
    
    public boolean isDiagrammButtonVisible(Assay assay) {
        return !assay.getLinkedData().isEmpty();
    }
    
    public ExpRecord getNewRecord() {
        ExpRecord rec = new Assay();
        rec.setEdit(true);
        return rec;
    }
    
    /**
     * select a LinkedData record for editing and adjust the showMolEditor property
     * of the material agent
     */
    @Override
    public void setLinkedDataIndex(int index) {
        super.setLinkedDataIndex(index);
        if (index < 0) {
            return;
        }

        switch (getExpRecord()
                .getLinkedData()
                .get(index)
                .getLinkedDataType()) {

            case ASSAY_TARGET :
                getExperimentBean().getMaterialAgent().setShowMolEditor(false);
                break;

            case ASSAY_SINGLE_POINT_OUTCOME :
            case ASSAY_MULTI_POINT_OUTCOME :
                getExperimentBean().getMaterialAgent().setShowMolEditor(true);
                break;
        }
    }
}
