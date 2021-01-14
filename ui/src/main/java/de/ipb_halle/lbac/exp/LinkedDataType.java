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
 * Record types for LinkedData
 *
 * @author fbroda
 */
public enum LinkedDataType {
    LINK_DOCUMENT(0),
    LINK_MATERIAL(1),
    LINK_ITEM(2),
    LINK_EXPERIMENT(3),
    LINK_USER(4),

    ASSAY_TARGET(5),
    ASSAY_SINGLE_POINT_OUTCOME(6),
    ASSAY_MULTI_POINT_OUTCOME(7);

    private int typeId;

    private LinkedDataType(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public static LinkedDataType getAssayOutcomeTypeById(int id) {
        for (LinkedDataType t : LinkedDataType.values()) {
            if (t.getTypeId() == id) {
                return t;
            }
        }
        return null;
    }
}

