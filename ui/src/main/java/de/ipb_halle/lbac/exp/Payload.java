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

import de.ipb_halle.lbac.datalink.LinkedDataType;
import de.ipb_halle.lbac.datalink.LinkText;
import com.google.gson.Gson;
import de.ipb_halle.lbac.exp.assay.SinglePointOutcome;

/**
 * @author fbroda
 */
public abstract class Payload {

    private transient ExpRecord expRecord;

    public static Payload fromString(LinkedDataType type, String outcome) {
        Gson gson = new Gson();
        switch(type) {
            case LINK_DOCUMENT :
            case LINK_MATERIAL : 
            case LINK_ITEM : 
            case LINK_EXPERIMENT :
            case LINK_USER:
                return gson.fromJson(outcome, LinkText.class);
            case ASSAY_SINGLE_POINT_OUTCOME : 
                return gson.fromJson(outcome, SinglePointOutcome.class);
        }
        throw new IllegalArgumentException("fromString() illegal arguments");
    }

    public ExpRecord getExpRecord() {
        return this.expRecord;
    }

    public Payload setExpRecord(ExpRecord expRecord) {
        this.expRecord = expRecord;
        return this;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
