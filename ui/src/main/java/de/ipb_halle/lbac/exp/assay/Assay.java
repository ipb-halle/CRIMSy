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

import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.exp.ExpRecord;
import de.ipb_halle.lbac.exp.ExpRecordType;
import de.ipb_halle.lbac.material.Material;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Assays provide information about effects which can be induced in a 
 * certain target by e.g. compounds. The target and the whole test 
 * system is specified by a standard operation procedure (which is 
 * stored in the experiment template). Assay stores a the target,
 * additional conditions and remarks, acceptable units etc. and a 
 * collection of tupels <code>material, outcome</code>).
 * 
 * The outcome will be diverse (boolean, single numbers, numbers with error, 
 * (multi-dimensional) arrays). In a first stage, only single point
 * results (numbers) will be implemented.
 *
 * @author fbroda
 */
public class Assay extends ExpRecord implements DTO {

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    /**
     * remarks and conditions
     */
    private String remarks;

    /*
     * the target material (an enzyme, organ, organism etc.)
     */
    private Material target;

    /**
     * comma separated list of acceptable units
     */
    private String units;

    /**
     * outcometype limits the type of outcome this assay object accepts.
     * This is done becaus rendering multiple outcome types in the same 
     * table might prove difficult.
     */
    private AssayOutcomeType    outcomeType;

    /**
     * the tupels (material, outcome)
     */
    private List<AssayRecord>   records;

    /**
     * default constructor
     */
    public Assay() {
        super();
        setType(ExpRecordType.ASSAY);
        this.remarks = "";
        this.units = "mM, , nM";
        this.records = new ArrayList<AssayRecord> ();
        this.outcomeType = AssayOutcomeType.SINGLE_POINT;
    }

    public Assay(AssayEntity entity, Material target) {
        super();
        setType(ExpRecordType.ASSAY);
        this.remarks = entity.getRemarks();
        this.target = target;
        this.units = entity.getUnits();
        this.records = new ArrayList<AssayRecord> ();
        if (entity != null) {
            this.outcomeType = entity.getOutcomeType();
        } 
    }

    public AssayEntity createEntity() {
        return new AssayEntity()
            .setExpRecordId(getExpRecordId())
            .setOutcomeType(this.outcomeType)
            .setRemarks(this.remarks)
            .setTargetId(this.target.getId())
            .setUnits(this.units);
    }

    public AssayOutcomeType getOutcomeType() {
        return this.outcomeType;
    }

    public List<AssayRecord> getRecords() {
        return this.records;
    }

    public String getRemarks() {
        return this.remarks;
    }

    public Material getTarget() {
        return this.target;
    }

    public String getUnits() {
        return this.units;
    }

    public void setOutcomeType(AssayOutcomeType outcomeType) {
        this.outcomeType = outcomeType;
    }

    public Assay setRecords(List<AssayRecord> records) {
        this.records = records;
        return this;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public void setTarget(Material target) {
        this.target = target;
    }

    public void setUnits(String units) {
        this.units = units;
    }
}
