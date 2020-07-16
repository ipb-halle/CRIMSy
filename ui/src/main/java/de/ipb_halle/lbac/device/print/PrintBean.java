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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.device.job.Job;
import de.ipb_halle.lbac.device.job.JobService;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.faces.model.SelectItem;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 * Bean for label printing. This bean provides a list of 
 * available printers and labels and manages the printing 
 * of labels 
 * 
 *
 * <h2>JSON label configuration</h2>
 *
 * <h3>Attributes</h3>
 *
 * <code>
 *     type         LABEL, PICTURE, INTERLEAVE25, CODE39, CODE128, QR, DATAMATRIX  
 *     fonts        MONOSPACED, SANS_SERIF (default),  SERIF
 *     style        PLAIN (default), BOLD, ITALIC, BOLD_ITALIC
 *     size         default 10
 *     x, y         position on label
 *     w, h         size (where applicable), e.g. for picture or barcode elements
 *     data         static data (String for labels and barcodes, hex string for pictures)
 *     field        name of the annotated field, takes precedence over data
 * </code>
 *
 * Implementation for picture is currently missing. Some attributes and default 
 * to printer defaults when omitted (<code>w</code> and <code>h</code> for 
 * the <code>form</code> element; <code>font, style</code> and <code>size</code>
 * for <code>label</code> elements.
 * 
 * <h3>Example</h3>
 *
 *  <code>
 *  { "form" : { 
 *      "w" : 200,
 *      "h" : 80,
 *      "elements" : [
 *          {   
 *              "type" : "LABEL",
 *              "x" : 10,
 *              "y" : 20,
 *              "data" : "Hello World",
 *              "font" : "SANS_SERIF",
 *              "size" : 10,
 *              "style" : "BOLD" 
 *          }, {   
 *              "type" : "LABEL",
 *              "x" : 10,
 *              "y" : 40,
 *              "field" : "itemMaterialName" 
 *          }, {
 *              "type" : "INTERLEAVE25",
 *              "x" : 100,
 *              "y" : 40,
 *              "w" : 80,
 *              "h" : 30,
 *              "field" : "itemLabel"
 *          }
 *
 *      ] } }
 *  </code>
 *
 * @author fbroda
 */
@RequestScoped
@Named
public class PrintBean implements Serializable {

    @Inject
    private JobService jobService;

    @Inject
    private PrinterService printerService;

    @Inject
    private LabelService labelService;

    @Inject
    private UserBean userBean;

    private Object labelDataObject;

    private Printer printer;

    private Label label;

    private Logger logger = LogManager.getLogger(this.getClass().getName()); 


    public void actionPrintLabel() {
        if ((this.labelDataObject != null) && 
                (this.printer != null) &&
                (this.label != null)) {
            Map<String, String> labelData = readLabelData();
            PrintDriver driver = PrintDriverFactory.buildPrintDriver(this.printer);
            parseLabelConfig(labelData, driver);
            submitJob(driver);
        }
    }

    public Label getLabel() {
        return this.label;
    }

    public List<SelectItem> getLabels() {
        Map<String, Object> cmap = new HashMap<String, Object> ();
        cmap.put(LabelService.PRINTER_MODEL, getPrinterModel()); 
        cmap.put(LabelService.LABEL_TYPE, getLabelType());

        List<SelectItem> items = new ArrayList<SelectItem> ();
        for (Label l : this.labelService.load(cmap)) {
            items.add(new SelectItem(l, l.getName(), l.getDescription()));
        }
        return items;
    }

    private String getLabelType() {
        if (this.labelDataObject != null) {
            Class c = this.labelDataObject.getClass();
            if( c.isAnnotationPresent( LabelType.class ) ) {
                return ((LabelType) c.getAnnotation( LabelType.class )).name();
            }
        }
        return null;
    }

    public Printer getPrinter() { 
        return this.printer; 
    }

    /**
     * return a list of available printers
     */
    public List<SelectItem> getPrinters() {
        /*
         * ToDo: xxxxx limit accessible printers
         */
        List<SelectItem> printers = new ArrayList<SelectItem> ();
        for (Printer p : printerService.load()) {
            printers.add(new SelectItem(p, p.getName(), p.getPlace()));
        }
        return printers;
    }

    private String getPrinterModel() {
        if (this.printer != null) {
            return printer.getModel();
        }
        return null;
    }

    private void parseBarcode(JsonObject obj, Map<String, String> labelData, PrintDriver driver, BarcodeType type) {
        try {
            double x = parseDimension(obj, "x");
            double y = parseDimension(obj, "y");
            double w = parseDimension(obj, "w");
            double h = parseDimension(obj, "h");
        
            String data = "";
            String field = parseString(obj, "field");
            if (field != null) {
                data = labelData.get(field);
            } else {
                data = parseString(obj, "data");
            }

            if ((data != null) && (data.length() > 0)) {
                driver.printBarcode(x, y, w, h, type, data);
            }
        } catch (NoSuchFieldException e) {
            this.logger.warn("parseBarcode() label config did not contain dimension");
        }
    }

    private double parseDimension(JsonObject obj, String dimension) throws NoSuchFieldException {
        JsonElement e = obj.get(dimension);
        if ((e != null) && (e.isJsonPrimitive())) {
            return e.getAsDouble();
        }
        throw new NoSuchFieldException("No such field: " + dimension);
    }

    private void parseElements(JsonArray elements,  Map<String, String> labelData, PrintDriver driver) {
        Iterator<JsonElement> iter = elements.iterator();
        while (iter.hasNext()) {
            JsonElement e = iter.next();
            if ((e != null) && e.isJsonObject()) {
                JsonObject obj = e.getAsJsonObject();
                String type = parseString(obj, "type");
                if (type != null) {
                    switch (type) { 
                        case "LABEL" :
                            parseLabel(obj, labelData, driver);
                            break;
                        case "INTERLEAVE25" :
                            parseBarcode(obj, labelData, driver, BarcodeType.INTERLEAVE25);
                            break;
                        case "CODE39" :
                            parseBarcode(obj, labelData, driver, BarcodeType.CODE39);
                            break;
                        case "CODE128" :
                            parseBarcode(obj, labelData, driver, BarcodeType.CODE128);
                            break;
                        case "QR" :
                            parseBarcode(obj, labelData, driver, BarcodeType.QR);
                            break;
                        case "DATAMATRIX" :
                            parseBarcode(obj, labelData, driver, BarcodeType.DATAMATRIX);
                            break;
                        case "PICTURE" :
                            parsePicture(obj,  labelData, driver);
                            break;
                    }
                }
            }
        }
    }

    private String parseFontName(JsonObject obj, PrintDriver driver) {
        String name = parseString(obj, "font");
        if (name != null) {
            return name;
        }
        return driver.getDefaultFontName();
    }

    private int parseFontSize(JsonObject obj, PrintDriver driver) {
        JsonElement elem = obj.get("size");
        if ((elem != null) && elem.isJsonPrimitive()) {
            return elem.getAsJsonPrimitive().getAsInt();
        }
        return driver.getDefaultFontSize();
    }

    private int parseFontStyle(JsonObject obj, PrintDriver driver) {
        String style = parseString(obj, "style");
        if (style != null) {
            switch (style) {
                case "PLAIN" :
                    return AbstractPrintDriver.PLAIN;
                case "BOLD" :
                    return AbstractPrintDriver.BOLD;
                case "ITALIC" :
                    return AbstractPrintDriver.ITALIC;
                case "BOLD_ITALIC" :
                    return AbstractPrintDriver.BOLD
                        + AbstractPrintDriver.ITALIC;
            }
        }
        return driver.getDefaultFontStyle();
    }

    private void parseLabel(JsonObject obj, Map<String, String> labelData, PrintDriver driver) {
        try {
            double x = parseDimension(obj, "x");
            double y = parseDimension(obj, "y");

            String fontName = parseFontName(obj, driver);
            int fontSize = parseFontSize(obj, driver);
            int fontStyle = parseFontStyle(obj, driver);

            String data = "";
            String field = parseString(obj, "field");
            if (field != null) {
                data = labelData.get(field);
            } else { 
                data = parseString(obj, "data");
            }

            if ((data != null) && (data.length() > 0)) {
                driver.printLine(x, y, fontName, fontStyle, fontSize, data);
            }
        } catch (NoSuchFieldException e) {
            this.logger.warn("parseLabel() label config did not contain dimension");
        }
    }

    /**
     * parse the label configuration and consecutively print the label
     * using the given labelData and driver
     */
    private void parseLabelConfig(Map<String, String> labelData, PrintDriver driver) {
        JsonElement jsonTree = JsonParser.parseString(this.label.getConfig());

        if(jsonTree.isJsonObject()) {
            JsonElement formElement = jsonTree.getAsJsonObject().get("form");
            if ((formElement != null) && formElement.isJsonObject()) {
                JsonObject form = formElement.getAsJsonObject();
                try {
                    double w = parseDimension(form, "w");
                    driver.setDefault("width", Double.toString(w));
                } catch(NoSuchFieldException ex) {
                    // ignore, use driver default
                }

                try {
                    double h = parseDimension(form, "h");
                    driver.setDefault("height", Double.toString(h));
                } catch(NoSuchFieldException ex) {
                    // ignore, use driver default
                }
                JsonElement e = form.get("elements");
                if ((e != null) && e.isJsonArray()) {
                    parseElements(e.getAsJsonArray(), labelData, driver);
                }
            }
        }
    }

    private void parsePicture(JsonObject obj, Map<String, String> labelData, PrintDriver driver) {
        // do nothing, not implemented yet
    }

    private String parseString(JsonObject obj, String field) {
        JsonElement elem = obj.get(field);
        if ((elem != null) && elem.isJsonPrimitive()) {
            return elem.getAsJsonPrimitive().getAsString();
        }
        return null;
    }

    /**
     * read the label data from the <code>labelDataObject</code>
     */
    private Map<String, String> readLabelData() {
        Map<String, String> labelData = new HashMap<String, String> ();

        if (this.labelDataObject != null) {

            Method[] methods = this.labelDataObject.getClass().getMethods();
            for( Method m : methods ) {
                if( m.isAnnotationPresent( LabelData.class ) ) {
                    try {
                        LabelData annotation = m.getAnnotation( LabelData.class );
                        labelData.put(
                            annotation.name(), 
                            (String) m.invoke(this.labelDataObject));
                    } catch(IllegalAccessException noAccess) {
                        this.logger.warn("readLabelData() method {} not accessible on class {}", 
                                m.getName(), 
                                this.labelDataObject.getClass().getName());
                    } catch(IllegalArgumentException illegalArgument) {
                        this.logger.warn("readLabelData() argument error for method {} in class {}",
                                m.getName(), 
                                this.labelDataObject.getClass().getName());
                    } catch(InvocationTargetException invokationError) {
                        this.logger.warn("readLabelData() caught exception from target method", 
                                (Throwable) invokationError);
                    } catch(NullPointerException npError) {
                        this.logger.warn("readLabelData() null pointer", (Throwable) npError); 
                    } catch(ExceptionInInitializerError iniError) {
                        this.logger.warn("readLabelData() exception in initialization", (Throwable) iniError);
                    }
                }
            }
        }
        return labelData;
    }

    /**
     * set the id of the label configuration
     */
    public void setLabelDataObject(Object obj) {
        this.labelDataObject = obj;
    }

    /**
     * set the currently selected label 
     */
    public void setLabel(Label label) {
        this.label = label;
    }

    /**
     * set the currently selected printer
     */
    public void setPrinter(Printer printer) {
        this.printer = printer;
    }

    /**
     * submit a job for printing
     */
    private void submitJob(PrintDriver driver) {
        Job job = driver.createJob();
        job.setOwner(userBean.getCurrentAccount());
        this.jobService.save(job); 
    }
}
