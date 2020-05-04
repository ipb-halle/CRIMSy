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
package de.ipb_halle.lbac.device.print;

/**
 * Enumeration of barcode types
 *
 * @author fbroda
 */
public enum BarcodeType {

    INTERLEAVE25(0),
    CODE39(1),
    CODE128(2),
    QR(3),
    DATAMATRIX(4);

    private int typeId;

    private BarcodeType(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public static BarcodeType getBarcodeTypeById(int id) {
        for (BarcodeType t : BarcodeType.values()) {
            if (t.getTypeId() == id) {
                return t;
            }
        }
        return null;
    }
}
