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
 * PrintDriver for Star LC10 printer
 * This driver is experimental and incomplete!
 *
 * Configurable Parameters:
 * - prologue
 * - epilogue
 * - height (label height in millimeters, double)
 * - width (label width in millimeters, double)
 * - hdpi (horizontal dots per inch, integer)
 * - vdpi (vertical dots per inch, integer)
 *
 * @author fbroda
 */
public class StarLC10Driver extends AbstractPrintDriver { 

    public final static String DRIVER_NAME = "Star LC10";

    private Logger logger;

    private final static byte[] lineFeed8 =   { 0x1b, 0x41, 0x08, 0x1b, 0x32 };     //  8/72"
    private final static byte[] lineFeed12 =  { 0x1b, 0x41, 0x0c, 0x1b, 0x32 };     // 12/72"
    private final static byte[] graphicMode = { 0x1b, 0x2a, 0x01, 0, 0 };         // must be copied to set n1, n2

    private int offsetX;
    private int offsetY;

    protected StarLC10Driver() {
        super();
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    public void applyDefaults() {
        setDefault("width", "80.0");
        setDefault("height", "40.0");
        setDefault("hdpi", "120");
        setDefault("vdpi", "72");
    }

    @Override
    public PrintDriver clear() {
        super.clear();
        append(getConfig("prologue"));
        return this;
    }

    protected void transform() {
        append(lineFeed8);

        int pixelWidth = getPixelWidth();
        int pixelHeight = getPixelHeight();
        int[] pixels = getPixelArray();

        byte[] cmd = Arrays.copyOf(graphicMode, graphicMode.length);
        cmd[3] = (byte) (pixelWidth % 256);
        cmd[4] = (byte) (pixelWidth / 256);

        int y = 0;
        int line = 0;
        while (y < pixelHeight) {
            byte[] buf = new byte[pixelWidth];
            int h = 0;
            while ((h < 8) && (y < pixelHeight)) {
                int v = (1 << (7 - h));
                for (int i=0; i<pixelWidth; i++) {
                    buf[i] += (pixels[line + i] != 0) ? v : 0;
                }
                h++;
                y++;
                line += pixelWidth;
            }
            append(cmd);
            append(buf);
            append("\n".getBytes());
        }
        append(lineFeed12);
        append(getConfig("epilogue", ""));
    }

}
