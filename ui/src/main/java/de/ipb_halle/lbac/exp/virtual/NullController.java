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
package de.ipb_halle.lbac.exp.virtual;

import de.ipb_halle.lbac.exp.ExperimentBean;
import de.ipb_halle.lbac.exp.ExpRecordController;
import de.ipb_halle.lbac.exp.ExpRecord;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * a void experiment record controller
 *
 * @author fbroda
 */
public class NullController extends ExpRecordController {

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    public NullController(ExperimentBean bean) {
        super(bean);
    }

    @Override
    public boolean getEdit() {
        return true;
    }

    @Override
    public ExpRecord getExpRecord() {
        return null;
    }

    public ExpRecord getNewRecord() {
        return null;
    }

    public ExpRecordController setExpRecord(ExpRecord expRecord) {
        return this;
    }
}
