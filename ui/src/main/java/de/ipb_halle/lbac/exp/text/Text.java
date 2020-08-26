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
package de.ipb_halle.lbac.exp.text;

import de.ipb_halle.lbac.exp.assay.AssayRecord;
import java.util.ArrayList;
import java.util.List;

import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.DTO;
import de.ipb_halle.lbac.exp.ExpRecord;
import de.ipb_halle.lbac.exp.ExpRecordType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * @author fbroda
 */
public class Text extends ExpRecord implements DTO {

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    private String text;

    /**
     * default constructor
     */
    public Text() {
        super();
        setType(ExpRecordType.TEXT);
    }

    @Override
    public TextEntity createEntity() {
        return new TextEntity()
            .setExpRecordId(getExpRecordId())
            .setText(this.text);
    }

    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
