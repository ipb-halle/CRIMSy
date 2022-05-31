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

import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.device.job.Job;
import de.ipb_halle.lbac.device.job.JobService;
import de.ipb_halle.lbac.util.HexUtil;
import de.ipb_halle.lbac.util.pref.Preference;
import de.ipb_halle.lbac.util.pref.PreferenceService;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.Dependent;
import javax.faces.model.SelectItem;
import javax.inject.Inject;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Bean for label printing. This bean provides a list of available printers and
 * labels and manages the printing of labels
 *
 *
 * <h2>JSON label configuration</h2>
 *
 * <h3>Attributes</h3>
 *
 * <pre>
 *   type         LABEL, PICTURE, INTERLEAVE25, CODE39, CODE128, QR, DATAMATRIX
 *   fonts        MONOSPACED, SANS_SERIF (default),  SERIF
 *   style        PLAIN (default), BOLD, ITALIC, BOLD_ITALIC
 *   size         default 10
 *   x, y         position on label
 *   w, h         size (where applicable), e.g. for form, picture or barcode elements
 *   data         static data: String for labels and barcodes, hex String (inside raster element) for pictures
 *   field        name of the annotated field or a JSON object (name, param) specifiying the field name
 *                and a parameter; field takes precedence over data or raster
 *   name         the name of the LabelData annotation of a method
 *   param        a String parameter to the annotated method (e.g. format string; see method documentation)
 *   raster       JSON object (w, h, data)
 * </pre>
 *
 * Implementation for picture is currently missing. Some attributes and default
 * to printer defaults when omitted (<code>w</code> and <code>h</code> for the
 * <code>form</code> element; <code>font, style</code> and <code>size</code> for
 * <code>label</code> elements.
 *
 * <h3>Example</h3>
 *
 * <pre>
 *   { "form" : {
 *       "w" : 200,
 *       "h" : 80,
 *       "elements" : [
 *           {
 *               "type" : "LABEL",
 *               "x" : 10,
 *               "y" : 20,
 *               "data" : "Hello World",
 *               "font" : "SANS_SERIF",
 *               "size" : 10,
 *               "style" : "BOLD"
 *           }, {
 *               "type" : "LABEL",
 *               "x" : 10,
 *               "y" : 40,
 *               "field" : "itemMaterialName"
 *           }, {
 *               "type" : "LABEL",
 *               "x" : 10,
 *               "y" : 60,
 *               "field" : { "name" : "itemCode", "param" : "*%04d*" }
 *           }, {
 *               "type" : "INTERLEAVE25",
 *               "x" : 100,
 *               "y" : 40,
 *               "w" : 80,
 *               "h" : 30,
 *               "field" : "itemLabel"
 *           }, {
 *               "type" : "PICTURE",
 *               "x" : 80,
 *               "y" : 50,
 *               "raster" : { "w":3, "h":3, "data": "ffffffff00ffffffff" }
 *           }
 *       ] }
 *   }
 * </pre>
 *
 * ToDo: remember the last used printer and label type via user preferences
 *
 * @author fbroda
 */
@Dependent
public class PrintBean implements Serializable {

    private final static String PREFERRED_QUEUE = "LABEL_PRINTER";
    private static final long serialVersionUID = 1L;

    @Inject
    private JobService jobService;

    @Inject
    private PrinterService printerService;

    @Inject
    private LabelService labelService;

    @Inject
    private PreferenceService preferenceService;

    @Inject
    protected UserBean userBean;

    private Object labelDataObject;

    private Preference preferredQueue;

    private String printerQueue;

    private Integer labelId;

    private Logger logger;

    /**
     * default constructor
     */
    public PrintBean() {
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    public void actionDoNothing() {
        this.logger.info("actionDoNoting()");
    }

    public void actionPrintLabel() {
        if ((this.labelDataObject != null)
                && (this.printerQueue != null) && (this.printerQueue.length() > 0)
                && (this.labelId != null)) {
            try {
                PrintDriver driver = PrintDriverFactory.buildPrintDriver(
                        this.printerService.loadById(this.printerQueue));
                parseLabelConfig(driver);
                submitJob(driver);
                this.preferredQueue.setValue(this.printerQueue);
                this.preferredQueue = this.preferenceService.save(this.preferredQueue);
            } catch (Exception e) {
                this.logger.warn("actionPrintLabel() caught an exception", (Throwable) e);
            }
        }
    }

    public Integer getLabelId() {
        return this.labelId;
    }

    public List<SelectItem> getLabels() {
        Map<String, Object> cmap = new HashMap<>();
        String printerModel = getPrinterModel();
        if ((printerModel == null) || (printerModel.length() == 0)) {
            return new ArrayList<>();
        }
        cmap.put(LabelService.PRINTER_MODEL, printerModel);

        String labelType = getLabelType();
        if (labelType == null) {
            this.logger.info("getLabels(): empty label type");
            return new ArrayList<>();
        }
        cmap.put(LabelService.LABEL_TYPE, labelType);

        List<SelectItem> items = new ArrayList<>();
        for (Label l : this.labelService.load(cmap)) {
            items.add(new SelectItem(l.getId(), l.getName(), l.getDescription()));
        }

        return items;
    }

    @SuppressWarnings("unchecked")
    private String getLabelType() {
        if (this.labelDataObject != null) {
            Class c = this.labelDataObject.getClass();
            if (c.isAnnotationPresent(LabelType.class)) {
                return ((LabelType) c.getAnnotation(LabelType.class)).name();
            }
        }
        return null;
    }

    private Preference getPreferredQueue() {
        if (this.preferredQueue == null) {
            this.preferredQueue = this.preferenceService.getPreference(
                    this.userBean.getCurrentAccount(),
                    PREFERRED_QUEUE,
                    null);
        }
        return this.preferredQueue;
    }

    public String getPrinterQueue() {
        return this.printerQueue;
    }

    /**
     * return a list of available printers
     *
     * @return a SelectItem list of accessible printers
     */
    public List<SelectItem> getPrinters() {

        /*
         * ToDo: xxxxx limit accessible printers
         */
        List<SelectItem> menu = new ArrayList<SelectItem>();
        List<Printer> printers = printerService.load();
        for (Printer p : printers) {
            if ((this.printerQueue == null)
                    && p.getQueue().equals(getPreferredQueue().getValue())) {
                this.printerQueue = p.getQueue();
            }
            menu.add(new SelectItem(p.getQueue(), p.getName(), p.getPlace()));
        }

        // failsafe, if preferences is not contained in actual printer list
        if ((this.printerQueue == null) && (printers.size() > 0)) {
            this.printerQueue = printers.get(0).getQueue();
        }
        return menu;
    }

    private String getPrinterModel() {
        if ((this.printerQueue != null) && (this.printerQueue.length() > 0)) {
            return this.printerService.loadById(this.printerQueue).getModel();
        }
        return null;
    }

    private void parseBarcode(JsonObject obj, PrintDriver driver, BarcodeType type) {
        try {
            double x = parseDimension(obj, "x");
            double y = parseDimension(obj, "y");
            double w = parseDimension(obj, "w");
            double h = parseDimension(obj, "h");

            String data = "";
            JsonElement field = obj.get("field");
            if (field != null) {
                data = readLabelData(field, String.class);
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

    private void parseElements(JsonArray elements, PrintDriver driver) {
        Iterator<JsonElement> iter = elements.iterator();
        while (iter.hasNext()) {
            JsonElement e = iter.next();
            if ((e != null) && e.isJsonObject()) {
                JsonObject obj = e.getAsJsonObject();
                String type = parseString(obj, "type");
                if (type != null) {
                    switch (type) {
                        case "LABEL":
                            parseLabel(obj, driver);
                            break;
                        case "INTERLEAVE25":
                            parseBarcode(obj, driver, BarcodeType.INTERLEAVE25);
                            break;
                        case "CODE39":
                            parseBarcode(obj, driver, BarcodeType.CODE39);
                            break;
                        case "CODE128":
                            parseBarcode(obj, driver, BarcodeType.CODE128);
                            break;
                        case "QR":
                            parseBarcode(obj, driver, BarcodeType.QR);
                            break;
                        case "DATAMATRIX":
                            parseBarcode(obj, driver, BarcodeType.DATAMATRIX);
                            break;
                        case "PICTURE":
                            parsePicture(obj, driver);
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
                case "PLAIN":
                    return AbstractPrintDriver.PLAIN;
                case "BOLD":
                    return AbstractPrintDriver.BOLD;
                case "ITALIC":
                    return AbstractPrintDriver.ITALIC;
                case "BOLD_ITALIC":
                    return AbstractPrintDriver.BOLD
                            + AbstractPrintDriver.ITALIC;
            }
        }
        return driver.getDefaultFontStyle();
    }

    private void parseLabel(JsonObject obj, PrintDriver driver) {
        try {
            double x = parseDimension(obj, "x");
            double y = parseDimension(obj, "y");

            String fontName = parseFontName(obj, driver);
            int fontSize = parseFontSize(obj, driver);
            int fontStyle = parseFontStyle(obj, driver);

            String data = "";
            JsonElement field = obj.get("field");
            if (field != null) {
                data = readLabelData(field, String.class);
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
     * parse the label configuration and consecutively print the label using the
     * given driver
     */
    private void parseLabelConfig(PrintDriver driver) {
        Label label = this.labelService.loadById(this.labelId);
        JsonElement jsonTree = JsonParser.parseString(label.getConfig());

        if (jsonTree.isJsonObject()) {
            JsonElement formElement = jsonTree.getAsJsonObject().get("form");
            if ((formElement != null) && formElement.isJsonObject()) {
                JsonObject form = formElement.getAsJsonObject();
                try {
                    double w = parseDimension(form, "w");
                    driver.setDefault("width", Double.toString(w));
                } catch (NoSuchFieldException ex) {
                    // ignore, use driver default
                }

                try {
                    double h = parseDimension(form, "h");
                    driver.setDefault("height", Double.toString(h));
                } catch (NoSuchFieldException ex) {
                    // ignore, use driver default
                }
                JsonElement e = form.get("elements");
                if ((e != null) && e.isJsonArray()) {
                    parseElements(e.getAsJsonArray(), driver);
                }
            }
        }
    }

    private void parsePicture(JsonObject obj, PrintDriver driver) {
        // do nothing, not implemented yet
        try {
            double x = parseDimension(obj, "x");
            double y = parseDimension(obj, "y");

            Raster raster = null;
            JsonElement field = obj.get("field");
            if (field != null) {
                raster = readLabelData(field, Raster.class);
            } else {
                raster = parseRaster(obj, "raster");
            }

            if (raster != null) {
                driver.printPicture(x, y, raster);
            }
        } catch (NoSuchFieldException e) {
            this.logger.warn("parsePicture() label config did not contain dimension");
        }

    }

    private Raster parseRaster(JsonObject obj, String field) {
        JsonElement elem = obj.get(field);
        if ((elem != null) && elem.isJsonObject()) {
            int w = 0;
            int h = 0;
            JsonObject pic = elem.getAsJsonObject();

            elem = pic.get("w");
            if ((elem != null) && elem.isJsonPrimitive()) {
                w = elem.getAsJsonPrimitive().getAsInt();
            }
            elem = pic.get("h");
            if ((elem != null) && elem.isJsonPrimitive()) {
                h = elem.getAsJsonPrimitive().getAsInt();
            }
            byte[] data = HexUtil.fromHex(parseString(pic, "data"));
            if ((data != null) && (data.length > 0) && (w > 0) && (h > 0)) {
                BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
                WritableRaster raster = img.getRaster();
                raster.setDataElements(0, 0, w, h, data);
                return raster;
            }
        }
        return null;
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
    @SuppressWarnings("unchecked")
    private <T> T readLabelData(JsonElement elem, Class T) {

        if ((elem == null) || (this.labelDataObject == null)) {
            return null;
        }

        String name = null;
        String param = null;
        if (elem.isJsonObject()) {
            name = parseString(elem.getAsJsonObject(), "name");
            param = parseString(elem.getAsJsonObject(), "param");
        }
        if (elem.isJsonPrimitive()) {
            name = elem.getAsJsonPrimitive().getAsString();
        }

        if (name == null) {
            return null;
        }

        Method[] methods = this.labelDataObject.getClass().getMethods();
        for (Method m : methods) {
            if (m.isAnnotationPresent(LabelData.class)) {
                try {
                    LabelData annotation = m.getAnnotation(LabelData.class);
                    if (name.equals(annotation.name())) {
                        if (param == null) {
                            return (T) m.invoke(this.labelDataObject);
                        }
                        return (T) m.invoke(this.labelDataObject, param);
                    }
                } catch (IllegalAccessException noAccess) {
                    this.logger.warn("readLabelData() method {} not accessible on class {}",
                            m.getName(),
                            this.labelDataObject.getClass().getName());
                } catch (IllegalArgumentException illegalArgument) {
                    this.logger.warn("readLabelData() argument error for method {} in class {}",
                            m.getName(),
                            this.labelDataObject.getClass().getName());
                } catch (InvocationTargetException invokationError) {
                    this.logger.warn("readLabelData() caught exception from target method",
                            (Throwable) invokationError);
                } catch (NullPointerException npError) {
                    this.logger.warn("readLabelData() null pointer", (Throwable) npError);
                } catch (ExceptionInInitializerError iniError) {
                    this.logger.warn("readLabelData() exception in initialization", (Throwable) iniError);
                }
            }
        }
        return null;
    }

    /**
     * set the id of the label configuration
     */
    public void setLabelDataObject(Object obj) {
        this.labelDataObject = obj;
    }

    /**
     * set the currently selected label
     * @param labelId
     */
    public void setLabelId(Integer labelId) {
        this.logger.info("setLabelId() {}", labelId);
        this.labelId = labelId;
    }

    /**
     * set the currently selected printer
     * @param printerQueue
     */
    public void setPrinterQueue(String printerQueue) {
        this.printerQueue = printerQueue;
    }

    /**
     * for mocking the UserBean during testing
     */
    protected void setUserBean(UserBean userBean) {
        this.userBean = userBean;
    }

    /**
     * submit a job for printing
     */
    private void submitJob(PrintDriver driver) {
        Job job = driver.createJob();
        job.setOwner(userBean.getCurrentAccount());
        this.jobService.saveJob(job);
    }
}
