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
 * This entity describes a printer status:
 * <ul>
 * <li>READY</li>
 * <li>FAILED</li>
 * <li>DISABLED</li>
 * </ul>
 *
 * @author fbroda
 */
public enum PrinterStatus {

    READY(0),
    FAILED(1),
    DISABLED(2);

    private int typeId;

    private PrinterStatus(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public static PrinterStatus getPrinterStatusById(int id) {
        for (PrinterStatus p : PrinterStatus.values()) {
            if (p.getTypeId() == id) {
                return p;
            }
        }
        return null;
    }
}
