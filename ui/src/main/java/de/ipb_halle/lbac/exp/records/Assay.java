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
package de.ipb_halle.lbac.exp.records;

import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.exp.ExpRecord;
import de.ipb_halle.lbac.exp.ExpRecordType;
import de.ipb_halle.lbac.exp.SOP;
import de.ipb_halle.lbac.exp.entity.AssayEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Assays provide information about effects which can be induced in a 
 * certain target by e.g. compounds. The target and the whole test 
 * system is specified by a standard operation procedure (SOP). An  
 * assay stores a collection of tripels (<code>Material, SOP, outcome</code>).
 * 
 * The outcome will be diverse (boolean, single numbers, numbers with error, 
 * (multi-dimensional) arrays).
 *
 * @author fbroda
 */
public class Assay extends ExpRecord implements DTO {

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    private SOP                 sop;

    private List<AssayRecord>   records;

    /**
     * default constructor
     */
    public Assay() {
        setType(ExpRecordType.ASSAY);
        this.records = new ArrayList<AssayRecord> ();
    }

    public AssayEntity createEntity() {
        return new AssayEntity()
            .setExpRecordId(getExpRecordId())
            .setSopId(this.sop.getSopId());
    }

    public List<AssayRecord> getRecords() {
        return this.records;
    }

    public SOP getSOP() {
        return this.sop;
    }

    public Assay setRecords(List<AssayRecord> records) {
        this.records = records;
        return this;
    }

    public Assay setSOP(SOP sop) {
        this.sop = sop;
        return this;
    }
}
