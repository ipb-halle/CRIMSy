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

import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.exp.ExpRecord;
import de.ipb_halle.lbac.exp.ExpRecordType;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author fbroda
 */
public class NullRecord extends ExpRecord implements DTO {

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    /**
     * default constructor
     */
    public NullRecord() {
        super();
        setType(ExpRecordType.NULL);
    }

    @Override
    public NullRecord createEntity() {
        throw new UnsupportedOperationException("Impossible to create an entity from NullRecord.");
    }

    @Override
    public String getExpRecordDetails() {
        // Messages.getString(MESSAGE_BUNDLE, "expBean_xxxxx", null);
        return "";
    }

    @Override
    public String getExpRecordInfo() {
        return " # -- # ";
    }

    @Override
    public boolean validate() {
        return true;
    }

    @Override
    public Set<ValidationError> getErrors() {
        return new HashSet<>();
    }

}
