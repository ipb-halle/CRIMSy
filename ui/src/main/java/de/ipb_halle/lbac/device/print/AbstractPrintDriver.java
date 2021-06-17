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

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import de.ipb_halle.lbac.device.job.Job;
import de.ipb_halle.lbac.device.job.JobType;
import de.ipb_halle.lbac.util.HexUtil;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Abstract base class for print drivers 
 *
 * This class defines several configurables:
 * - hdpi (horizontal dots per inch)
 * - vdpi (vertical dots per inch)
 * - width (label width in millimeters)
 * - height (label height in millimeters)
 *
 * @author fbroda
 */
public abstract class AbstractPrintDriver implements PrintDriver { 

    private final static int INITIAL_BUFFER_SIZE = 256;
    public final static int JAVA_DEFAULT_DPI = 72;

    /* font styles */
    public final static int BOLD = Font.BOLD;
    public final static int ITALIC = Font.ITALIC;
    public final static int PLAIN = Font.PLAIN;

    /* font types */
    public final static String MONOSPACED = Font.MONOSPACED;
    public final static String SANS_SERIF = Font.SANS_SERIF;
    public final static String SERIF = Font.SERIF;

    private final static int NORMAL_FONT_SIZE = 10;

    private ByteBuffer          buffer;
    private Printer             printer;
    private Map<String, byte[]> configMap;

    private BufferedImage image;
    private Font defaultFont;
    private int pixelWidth;
    private int pixelHeight;
    private int hdpi;
    private int vdpi;


    private Logger logger;

    /**
     * default constructor
     */
    public AbstractPrintDriver() {
        this.logger = LogManager.getLogger(this.getClass().getName());
        this.defaultFont = new Font(Font.SANS_SERIF, Font.PLAIN, NORMAL_FONT_SIZE);
    }

    /**
     * adjust font metrics, especially for printers with 
     * unequal horizontal and vertical resolution 
     */
    public Font adjustFontDPI(Font font) {
        double dy = (double) this.vdpi / JAVA_DEFAULT_DPI;
        double dx = dy * (double) this.hdpi / (double) this.vdpi;
        AffineTransform transform = new AffineTransform(
            dx, 0.0,
            0.0, dy,
            0.0, 0.0);
        return font.deriveFont(transform);
    }


    /**
     * append a byte array to the output buffer
     */
    protected void append(byte[] data) {
        this.buffer = append(this.buffer, data);
    }

    /**
     * append a byte array to a ByteBuffer.
     * This method checks if data fits in remaining 
     * capacity and eventually re-allocates a new buffer.
     * Capacity is doubled in each round until the data 
     * can be appended to the buffer.
     * This method is also for internal use (e.g. also during
     * parsing of the configuration).
     * @param buf the buffer
     * @param data the data
     * @param buf a buffer with the original data and the 
     * new data appended to it
     */
    private ByteBuffer append(ByteBuffer buf, byte[] data) {
        while (buf.remaining() < data.length) {
            ByteBuffer tmp = ByteBuffer.allocate(2 * buf.capacity());
            /*
             * tmp.put((ByteBuffer) buf.flip());
             *
             * Workaround for incompatibility when compiling under 
             * JDK 11 and running under JDK 8. Didn't succeed to 
             * fix this in the POM :-(.
             */
            tmp.put((ByteBuffer) ((Buffer) buf).flip());
            buf = tmp;
        }
        buf.put(data);
        return buf;
    }

    /**
     * apply the default values for a print driver. Must 
     * supply defaults for hdpi, vdpi, height, width 
     * (and maybe other values in the future).
     */
    protected abstract void applyDefaults(); 

    /**
     * Allocate a new buffer. Implementations might want to 
     * override this method to add the prologue section to 
     * the buffer.
     * @return the driver object
     */
    public PrintDriver clear() {
        this.configMap = new HashMap<String, byte[]> ();
        applyDefaults();
        parseConfig((printer == null) ? "" : printer.getConfig());
        this.buffer = ByteBuffer.allocate(INITIAL_BUFFER_SIZE);

        this.hdpi = getConfigInt("hdpi");
        this.vdpi = getConfigInt("vdpi");
        double width = getConfigDouble("width");
        double height = getConfigDouble("height");
        this.pixelWidth = getPixels(width, this.hdpi);
        this.pixelHeight = getPixels(height, this.vdpi);
/*
        this.logger.info(String.format("clear(): Millimeter: w=%f, h=%f, hdpi=%d, vdpi=%d", width, height, hdpi, vdpi)); 
        this.logger.info(String.format("clear(): Pixel:      w=%d, h=%d", pixelWidth, pixelHeight));
*/
        this.image = new BufferedImage(this.pixelWidth,
                this.pixelHeight,
                BufferedImage.TYPE_BYTE_BINARY);
        return this;
    }

    /**
     * calls the transform() method and creates a print job.
     * @return the Job
     */
    public Job createJob() {
        transform();
        return new Job()
            .setJobType(JobType.PRINT)
            .setQueue(this.printer.getQueue())
            .setInput(Arrays.copyOf(this.buffer.array(), this.buffer.position()));
    }

    /**
     * @param key the key 
     * @return the corresponding configuration value
     */
    public byte[] getConfig(String key) {
        return this.configMap.get(key);
    }

    /**
     * @param key the key
     * @param defaultValue the hex encoded default value
     * @return the corresponding configuration value or the 
     * provided default value (converted ty bytes)
     */
    public byte[] getConfig(String key, String defaultValue) {
        byte[] result = this.configMap.get(key);
        return (result != null) ? result : HexUtil.fromHex(defaultValue);
    }

    /**
     * @param key the key
     * @return the corresponding configuration value 
     */
    public int getConfigInt(String key) {
        byte[] result = this.configMap.get(key);
        return Integer.parseInt(new String(result, StandardCharsets.UTF_8));
    }

    /**
     * @param key the key
     * @return the corresponding configuration value 
     */
    public double getConfigDouble(String key) {
        byte[] result = this.configMap.get(key);
        return Double.parseDouble(new String(result, StandardCharsets.UTF_8));
    }

    public String getDefaultFontName() {
        return Font.SANS_SERIF;
    }

    public int getDefaultFontSize() {
        return NORMAL_FONT_SIZE;
    }

    public int getDefaultFontStyle() {
        return PLAIN;
    }

    /**
     * compute the pixel coordinate from a length in 
     * millimeters and a resolution in pixels per inch (dpi)
     * @param coord the coordinate in mm
     * @param dpi the resolution (dots per inch)
     * @return the pixel value
     */
    public int getPixels(double coord, int dpi) {
        return Double.valueOf(Math.floor(coord * dpi / 25.4)).intValue();
    }

    public int[] getPixelArray() {
        return this.image.getData().getPixels(0,0,
                pixelWidth, pixelHeight,
                new int[pixelWidth * pixelHeight]);
    }

    public int getPixelHeight() {
        return this.pixelHeight;
    }

    public int getPixelWidth() {
        return this.pixelWidth;
    }

    /**
     * parse printer configuration
     * Printer configuration is expected in KEY=VALUE format
     * with one key=value pair per line. VALUE is expected as 
     * a hex digit string. Long values may be split over 
     * multiple lines by providing a backslash as the last
     * character on the line. Lines starting with hash sign 
     * are ignored. Interpretation of the values is up to the 
     * printer:
     *
     * Example:
     *          # this is a comment
     *          prologue=2758
     *          epilogue=4352494d53790a286329203230323020\
     *                4c6569626e697a20496e737469747574\
     *                20662e2050666c616e7a656e62696f63\
     *                68656d69650a
     *          offsetX=313430
     *          offsetY=3230
     * 
     * @param config the config string
     */
    private void parseConfig(String config) {
        Pattern pattern = Pattern.compile("[^0-9a-fA-F]");
        ByteBuffer buf = ByteBuffer.allocate(INITIAL_BUFFER_SIZE);
        String key = null; 
        boolean cont = false;

        try (BufferedReader reader = new BufferedReader(new StringReader(config))) {
            String line = reader.readLine();
            while(line != null) {
                if (! line.startsWith("#")) {
                    if (! cont) {
                        if (key != null) {
                            this.configMap.put(key, Arrays.copyOf(buf.array(), buf.position()));
                            buf = ByteBuffer.allocate(INITIAL_BUFFER_SIZE); 
                        }
                        int idx = line.indexOf("=");
                        key = line.substring(0,idx); 
                        line = line.substring(idx + 1); 
                    }
                    cont = line.endsWith("\\"); 
                    buf = append(buf, 
                            HexUtil.fromHex(pattern.matcher(line).replaceAll(""))); 
                }
                line = reader.readLine();
            }
            if(key != null) {
                this.configMap.put(key, Arrays.copyOf(buf.array(), buf.position()));
            }
        } catch(IOException e) {
            // ignore
        }
    }

    /**
     * print a barcode 
     * at position x,y in size w,h. All values in millimeters
     */
    public PrintDriver printBarcode(double x, double y, double w, double h, BarcodeType type, String data) {

        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix barcode;
        int width = getPixels(w, this.hdpi);
        int height = getPixels(h, this.vdpi);

        try {
            switch(type) {
                case INTERLEAVE25 :
                    barcode = writer.encode(data, BarcodeFormat.ITF,  width, height);
                    break;
                case CODE39 :
                    barcode = writer.encode(data, BarcodeFormat.CODE_39, width, height);
                    break;
                case CODE128 :
                    barcode = writer.encode(data, BarcodeFormat.CODE_128, width, height);
                    break;
                case QR :
                    barcode = writer.encode(data, BarcodeFormat.QR_CODE, width, height);
                    break;
                case DATAMATRIX :
                    barcode = writer.encode(data, BarcodeFormat.DATA_MATRIX, width, height);
                    break;
                default :
                    throw new IllegalArgumentException("Unsupported barcode type");
            }
        } catch(WriterException e) {
            this.logger.warn("printBarcode() caught an exception", (Throwable) e);
            return this;
        }

        WritableRaster raster = this.image.getSubimage(
                getPixels(x, this.hdpi),
                getPixels(y, this.vdpi),
                width, height).getRaster();
        for (int i=0; i<width; i++) {
            for (int j=0; j<height; j++) {
                raster.setSample(i, j, 0, barcode.get(i,j) ? 255 : 0);
            }
        }

        return this;
    }

    /**
     * Print a line of text
     * @param x horizontal offset from upper left corner in millimeters
     * @param y vertical offset from upper left corner in millimeters
     * @param text the text to be printed
     */
    public PrintDriver printLine(double x, double y, String text) {
        return printLine(x, y, this.defaultFont, text);
    }

    public PrintDriver printLine(double x, double y, int size, String text) {
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, size);
        return printLine(x, y, font, text);
    }

    public PrintDriver printLine(double x, double y, String fontName, int fontStyle, int fontSize, String text) {
        Font font = new Font(fontName, fontStyle, fontSize);
        return printLine(x, y, font, text);
    }

    public PrintDriver printLine(double x, double y, Font font, String text) {
        Graphics2D graphics = this.image.createGraphics();
        graphics.setColor(Color.BLACK);
        graphics.setXORMode(Color.WHITE);
        graphics.setFont(adjustFontDPI(font)); 
        graphics.drawString(
                text,
                getPixels(x, this.hdpi),
                getPixels(y, this.vdpi));
        return this;
    }

    /**
     * print a raster image
     * @param x horizontal position
     * @param y vertical position
     * @param picture the picture to print
     */
    public PrintDriver printPicture(double x, double y, Raster picture) {
        WritableRaster raster = this.image.getSubimage(
                getPixels(x, this.hdpi),
                getPixels(y, this.vdpi),
                picture.getWidth(), 
                picture.getHeight()).getRaster();

        raster.setRect(picture);
        return this;
    }

    /**
     * set the default values for a printer
     */
    public void setDefault(String key, String data) {
        this.configMap.put(key, data.getBytes());
    }

    /**
     * set the printer for this driver. The printer comes 
     * with configuration data and is also needed for
     * Job creation.
     * @param printer the Printer instance
     * @return this
     */
    public PrintDriver setPrinter(Printer printer) {
        this.printer = printer;
        clear();
        return this;
    }

    protected abstract void transform();

}
