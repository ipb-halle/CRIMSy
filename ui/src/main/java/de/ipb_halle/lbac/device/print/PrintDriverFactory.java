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
 * Factory for print drivers.
 *
 * @author fbroda
 */
public class PrintDriverFactory { 

    private static List<String> drivers;

    static {
        drivers = new ArrayList<String> ();
        drivers.add(ZebraE2Driver.DRIVER_NAME); 
        drivers.add(StarLC10Driver.DRIVER_NAME);
    }

    public static PrintDriver buildPrintDriver(Printer printer) {
        switch(printer.getDriver()) {
            case ZebraE2Driver.DRIVER_NAME :
                return new ZebraE2Driver().setPrinter(printer);
            case StarLC10Driver.DRIVER_NAME : 
                return new StarLC10Driver().setPrinter(printer);
        }
        throw new IllegalArgumentException("Unknown driver");
    }

    public static List<String> getDrivers() {
        return drivers;
    }
}
