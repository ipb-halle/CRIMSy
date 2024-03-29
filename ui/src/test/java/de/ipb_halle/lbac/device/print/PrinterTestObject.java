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
 * This class is a mock object for for the label printing module.
 */
@LabelType(name="printerTestMock")
public class PrinterTestObject {

    @LabelData(name="testText")
    public String getTestText() {
        return "CRIMSy TEST";
    }

    @LabelData(name="testBarcode")
    public String getTestBarcode() {
        return "1234567895";
    }
}
