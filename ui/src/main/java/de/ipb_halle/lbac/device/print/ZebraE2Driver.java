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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * PrintDriver for Zebra E2 barcode printers (e.g. Zebra TLP 2844)
 *
 * The capabilities of this print driver are currently very
 * limited and many settings (e.g. font selection, ...) are 
 * hardcoded into the driver.
 *
 * Sample Configuration:
 *      #
 *      # Thermotransfer Labels 50.8 mm x 25.4 mm
 *      # with gaps on TLP 2844
 *      #
 *      prologue=49382c412c3030310a513230332c3032\
 *               340a713530360a724e0a53310a443135\
 *               0a5a540a4a460a4f440a4f43310a5232\
 *               32302c300a6639300a4e0a 
 *      epilogue=50310a
 *      offsetX=30
 *      offsetY=30
 *
 * Prologue:
 *          I8,A,001
 *          Q203,024
 *          q506
 *          rN
 *          S1
 *          D15
 *          ZT
 *          JF
 *          OD
 *          OC1
 *          R220,0
 *          f90
 *          N
 *
 * Epilogue:
 *          P1
 *
 * @author fbroda
 */
public class ZebraE2Driver extends AbstractPrintDriver { 

    public final static String DRIVER_NAME = "Zebra E2";

    private int offsetX;
    private int offsetY;

    private Logger logger;

    public ZebraE2Driver() {
        super();
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    public void applyDefaults() {
        setDefault("width", "50.0");
        setDefault("height", "25.0");
        setDefault("hdpi", "203");
        setDefault("vdpi", "203");
    }

    @Override
    public PrintDriver clear() {
        super.clear();
        this.offsetX = Integer.parseInt(new String(getConfig("offsetX"), StandardCharsets.UTF_8)); 
        this.offsetY = Integer.parseInt(new String(getConfig("offsetY"), StandardCharsets.UTF_8));
        append(getConfig("prologue"));
        return this;
    }

    /** 
     * for future use
     */
    protected void transform() {
        int pixelWidth = getPixelWidth();
        int pixelHeight = getPixelHeight();
        int[] pixels = getPixelArray();

        // make linewidth a multiple of 8 pixels
        int lineWidth = ((pixelWidth & 7) == 0) ? pixelWidth / 8 :  1 + (pixelWidth / 8); 

        this.logger.info("transform(): width={}, lineWidth={}, height={}", pixelWidth, lineWidth, pixelHeight);
        this.logger.info("transform(): offsetX={}, offsetY={}", offsetX, offsetY);

        append(String.format("GW%d,%d,%d,%d,",
            this.offsetX,
            this.offsetY,
            lineWidth,
            pixelHeight).getBytes());

        int line = 0;
        for (int y=0; y<pixelHeight; y++) {
            byte[] buf = new byte[lineWidth];
            for (int x=0; x<pixelWidth; x++) {
                int v = (1 << (7 - (x & 7)));   // set bits, MSB=pixel_0, LSB=pixel_7
                buf[x >> 3] += (pixels[line + x] != 0) ? 0 : v;
            }
            line += pixelWidth;
            append(buf);
        }
        append("\n".getBytes());
        append(getConfig("epilogue", ""));
    }

}
