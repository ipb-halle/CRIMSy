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
 *
 * @author fbroda
 */
public class BrotherPT_P710BT_Driver extends AbstractPrintDriver { 

    public final static String DRIVER_NAME = "Brother PT-P710BT";

    private Logger logger;

    private int offsetX;
    private int offsetY;

    protected BrotherPT_P710BT_Driver() {
        super();
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    public void applyDefaults() {
        setDefault("width", "22.0");
        setDefault("height", "12.0");
        setDefault("hdpi", "180");
        setDefault("vdpi", "180");
    }

    @Override
    public PrintDriver clear() {
        super.clear();
        append(getConfig("prologue"));
        return this;
    }

    protected void transform() {

        int pixelWidth = getPixelWidth();
        int pixelHeight = getPixelHeight();
        int[] pixels = getPixelArray();

        /* missing */

        append(getConfig("epilogue", ""));
    }
}
