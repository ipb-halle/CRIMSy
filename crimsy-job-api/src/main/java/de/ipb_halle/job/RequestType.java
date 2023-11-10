/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2023 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.job;

import javax.xml.bind.annotation.XmlEnum;

/**
 * This enum describes the type of a request:
 * <ul>
 * <li>QUERY (get a list of jobs)</li>
 * <li>UPDATE (update a single job)</li>
 * </ul>
 * 
 * In query mode, the fields <code>jobid</code>, <code>jobtype</code>, 
 * <code>queue</code> and <code>status</code> shall be evaluated by the 
 * server. If a single job is queried (by <code>jobid</code>), the server 
 * shall return the plain <code>NetJob</code> object. Otherwise, the 
 * server shall return all matching <code>NetJob</code> objects 
 * in the field <code>joblist</code>.  
 *
 * In update mode, the server shall update the fields <code>output</code> and 
 * <code>status</code> for the job identified by <code>jobid</code>.
 *
 * @author fbroda
 */
@XmlEnum
public enum RequestType {

    QUERY(0),
    UPDATE(1);

    private int typeId;

    private RequestType(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public static RequestType getRequestTypeById(int id) {
        for (RequestType r : RequestType.values()) {
            if (r.getTypeId() == id) {
                return r;
            }
        }
        return null;
    }
}
