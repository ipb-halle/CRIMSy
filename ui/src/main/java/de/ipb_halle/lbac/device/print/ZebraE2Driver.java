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

import de.ipb_halle.lbac.device.job.Job;

import java.nio.charset.StandardCharsets;

/**
 * PrintDriver for Zebra E2 barcode printers (e.g. Zebra TLP 2844)
 *
 * The capabilities of this print driver are currently very
 * limited and many settings (e.g. font selection, ...) are 
 * hardcoded into the driver.
 *
 * Sample Configuration:
 *      prologue=4f0a4e0a4431350a53310a4f43310a71\
 *               3530300a49382c412c3034390a
 *      epilogue=50310a
 *      offsetX=313430
 *      offsetY=3230
 *
 * @author fbroda
 */
public class ZebraE2Driver extends AbstractPrintDriver { 

    public final static String DRIVER_NAME = "Zebra E2";

    private int offsetX;
    private int offsetY;

    @Override
    public PrintDriver clear() {
        super.clear();
        this.offsetX = Integer.parseInt(new String(getConfig("offsetX"), StandardCharsets.UTF_8)); 
        this.offsetY = Integer.parseInt(new String(getConfig("offsetY"), StandardCharsets.UTF_8));
        append(getConfig("prologue"));
        return this;
    }

    /**
     * print a barcode 
     */
    public PrintDriver printBarcode(BarcodeType type, String data) {
        switch(type) {
            case INTERLEAVE25 :
                append(String.format("B%d,%d,0,2,2,4,50,B,\"%s\"\n",
                    this.offsetX, 
                    this.offsetY, 
                    data).getBytes());
                this.offsetY += 90;
                return this;
            default :
                throw new IllegalArgumentException("Unsupported barcode type");
        }
    }

    /**
     * print a line of text
     */
    public PrintDriver printLine(String line) {
        append(String.format("A%d,%d,0,1,1,1,N,\"%s\"\n",
            this.offsetX, 
            this.offsetY, 
            zebraEscape(line)).getBytes());
        this.offsetY += 20;
        return this;
    }

    /**
     * append the epilogue to the print job
     */
    @Override
    public Job createJob() {
        append(getConfig("epilogue"));
        return super.createJob();
    }

	/**
	 * @return a string properly escaped for Zebra label printers
	 */
    private String zebraEscape(String s) {
        String t = s.replace("\\","\\\\");
        t = t.replace("\"", "\\\"");
        return t;
    }
}
