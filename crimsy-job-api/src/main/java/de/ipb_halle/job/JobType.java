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

import jakarta.xml.bind.annotation.XmlEnum;

/**
 * This enum describes the type of a job:
 * <ul>
 * <li>PRINT</li>
 * <li>COMPUTE</li>
 * <li>REPORT</li>
 * </ul>
 *
 * @author fbroda
 */
@XmlEnum
public enum JobType {

    PRINT(0),
    COMPUTE(1),
    REPORT(2);

    private int typeId;

    private JobType(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public static JobType getJobTypeById(int id) {
        for (JobType t : JobType.values()) {
            if (t.getTypeId() == id) {
                return t;
            }
        }
        return null;
    }
}
