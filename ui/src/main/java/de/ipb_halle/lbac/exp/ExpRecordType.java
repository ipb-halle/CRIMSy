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



/**
 * Record types for ExpRecord.
 *
 * The <code>type</code> attribute in <code>ExpRecord</code> is 
 * currently mapped to an integer. <b>NOTE: Please bear in mind, that 
 * this is work in progress.  Additions, reorderings and deletions 
 * may occur at any time. This will likely invalidate any 
 * persisted data.</b>
 *
 * @author fbroda
 */
public enum ExpRecordType {
    NULL(0),
    ASSAY(1),
    ATTACHMENT(2),
    COMPOUND_DATA(3),
    REACTION(4),
    TEXT(5),
    IMAGE(6);

    private int typeId;

    private ExpRecordType(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public static ExpRecordType getExpRecordTypeById(int id) {
        for (ExpRecordType t : ExpRecordType.values()) {
            if (t.getTypeId() == id) {
                return t;
            }
        }
        return null;
    }
}

