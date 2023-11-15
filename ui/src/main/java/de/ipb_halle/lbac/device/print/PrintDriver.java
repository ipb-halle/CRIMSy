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

import de.ipb_halle.lbac.device.job.PrintJob;

import java.awt.image.Raster;
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

    public String getDefaultFontName();
    public int getDefaultFontSize();
    public int getDefaultFontStyle();

    public PrintDriver printBarcode(double x, double y, double w, double h, BarcodeType type, String data);
    public PrintDriver printLine(double x, double y, String line);
    public PrintDriver printLine(double x, double y, String fontName, int fontStyle, int fontSize, String line);
    public PrintDriver printPicture(double x, double y, Raster picture);

    public void setDefault(String key, String value);
    public PrintDriver setPrinter(Printer printer);

}
