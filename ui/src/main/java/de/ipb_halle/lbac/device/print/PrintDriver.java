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

import java.util.ArrayList;
import java.util.List;

/**
 * Interface for print drivers.
 *
 * @author fbroda
 */
public interface PrintDriver { 

    public PrintDriver clear();
    public PrintJob createJob();

    public PrintDriver printBarcode(BarcodeType type, String data);
    public PrintDriver printLine(String line);

    public PrintDriver setPrinter(Printer printer);

}
