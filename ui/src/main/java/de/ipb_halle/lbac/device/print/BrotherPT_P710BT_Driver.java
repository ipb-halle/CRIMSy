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

import java.util.Arrays;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * PrintDriver for Brother P-Touch PT710BT Cube Plus
 * and compatible models.
 * This driver is experimental and incomplete!
 *
 * Configurable Parameters:
 * - offsetX: empty margin (default 0)
 * - offsetY: upper and lower border (default 31 pins for 12 mm tape)
 * - epilogue: defaults to 0x1a
 * - prologue: default 1b6961011b6921001b697a84001800aa02000000004d00
 * - width: 22.0 mm
 * - height: 12.0 mm
 * - hdpi: 180
 * - vdpi: 180
 *
 * @author fbroda
 */
public class BrotherPT_P710BT_Driver extends AbstractPrintDriver { 

    public final static String DRIVER_NAME = "Brother PT-P710BT";

    private final static byte[] cmdInit = { 0x1b, 0x40 };
    private final static String cmdSetup = "1b6961011b6921001b697a84001800aa02000000004d00";    // diverse settings
    private final static String cmdPrintLast = "1a";    // print last page - i.e. print and cut
    private final static int maxHeight = 128;

    private Logger logger;

    protected BrotherPT_P710BT_Driver() {
        super();
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    public void applyDefaults() {
        setDefault("width", "22.0");
        setDefault("height", "12.0");
        setDefault("hdpi", "180");
        setDefault("vdpi", "180");
        setDefault("offsetX", "0");
        setDefault("offsetY", "31");
    }

    @Override
    public PrintDriver clear() {
        super.clear();
        return this;
    }

    /**
     * initialize printer for a new print job
     */
    private void initialize() {
        append(new byte[100]);  // invalidate
        append(cmdInit);
    }

    /**
     * send the job specific setup commands
     */
    private void setupPrinting(){
        byte[] cfg = getConfig("prologue", cmdSetup);
        append(cfg);
    }

    /**
     * convert the pixel bitmap into a raster 
     * print command
     */
    protected void transform() {

        int pixelWidth = getPixelWidth();
        int pixelHeight = getPixelHeight();
        int[] pixels = getPixelArray();
        int offsetX = getConfigInt("offsetX");
        int offsetY = getConfigInt("offsetY");

        initialize();
        setupPrinting();

        if (offsetX > 0) {
            byte[] buf = new byte[offsetX];
            for (int x=0; x<offsetX; x++) {
                buf[x] = 0x5a;  // zero raster graphics
            }
            append(buf);
        }

        for (int x=0; x<pixelWidth; x++) {
            byte[] buf = new byte[19];
            buf[0] = 0x47;
            buf[1] = 16;
            buf[2] = 0;
            for (int y=0; y<maxHeight; y++) {
                int v = (1 << (7 - (y & 7)));   // MSB first
                if ((y >= offsetY)  && (y < (maxHeight - offsetY))) {
                    buf[3 + (y >> 3)] += (pixels[((y - offsetY) * pixelWidth) + x] != 0) ? v : 0;
                }
            }
            append(buf);
        }
        append(getConfig("epilogue", cmdPrintLast));
    }
}
