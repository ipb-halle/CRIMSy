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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * PrintDriver for Star LC10 printer
 * This driver is experimental and incomplete!
 *
 * Parameters:
 * - prologue
 * - height (in millimeters, double)
 * - width (in millimeters, double)
 * - hdpi (horizontal dots per inch, integer)
 * - vdpi (vertical dots per inch, integer)
 *
 * @author fbroda
 */
public class StarLC10Driver extends AbstractPrintDriver { 

    public final static String DRIVER_NAME = "Star LC10";
    private final static double JAVA_DEFAULT_DPI = 72.0;

    private BufferedImage image;
    private int pixelWidth;
    private int pixelHeight;
    private int hdpi;
    private int vdpi;

    private Logger logger;

/*
    Weight  1       2       4       8       16      
    0       n       n       W       W       n   
    1       W       n       n       n       W  
    2       n       W       n       n       W 
    3       W       W       n       n       n
    4       n       n       W       n       W 
    5       W       n       W       n       n 
    6       n       W       W       n       n 
    7       n       n       n       W       W 
    8       W       n       n       W       n 
    9       n       W       n       W       n 
*/
    private final static byte[] interleave2of5 = { 0x0c, 0x11, 0x12, 0x03, 0x14, 0x05, 0x06, 0x18,  0x09, 0x0a };
    private final static byte[] lineFeed8 =   { 0x1b, 0x41, 0x08, 0x1b, 0x32 };     //  8/72"
    private final static byte[] lineFeed12 =  { 0x1b, 0x41, 0x0c, 0x1b, 0x32 };     // 12/72"
    private final static byte[] graphicMode = { 0x1b, 0x2a, 0x01, 0, 0 };         // must be copied to set n1, n2

    private int interleaveClear = 10;   // clear area before and after
    private int interleaveNarrow = 2;   // with of narrow symbols
    private int interleaveRatio = 3;    // with of wide symbols (in units of narrow symbols)
    private int interleaveRepeat = 2;   // barcode height (in lines, not including digits)

    private int offsetX;
    private int offsetY;

    protected StarLC10Driver() {
        super();
        this.logger = LogManager.getLogger(this.getClass().getName());
    }



    @Override
    public PrintDriver clear() {
        super.clear();
        append(getConfig("prologue"));
        double width = getConfigDouble("width", 80.0);
        double height = getConfigDouble("height", 40.0);
        this.hdpi = getConfigInt("hdpi", 120);
        this.vdpi = getConfigInt("vdpi", 72);
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


/* xxxxx NEW driver xxxxx */
    public Font adjustFontDPI(Font font) {
        double dy = this.vdpi / JAVA_DEFAULT_DPI;
        double dx = dy * (double) this.hdpi / (double) this.vdpi;
        AffineTransform transform = new AffineTransform(
            dx, 0.0,
            0.0, dy,
            0.0, 0.0);
        return font.deriveFont(transform);
    }

    public void printLine(double x, double y, String text) {
        Graphics2D graphics = this.image.createGraphics();
        graphics.setColor(Color.BLACK);
        graphics.setXORMode(Color.WHITE);
        Font font = new Font(Font.SANS_SERIF, Font.PLAIN, 24);
        font = adjustFontDPI(font);
        graphics.setFont(font); 
        graphics.drawString(
                text,
                getPixels(x, this.hdpi), 
                getPixels(y, this.vdpi));
    }

    protected void transform() {
        append(lineFeed8);
        int[] pixels = this.image.getData().getPixels(0,0, 
                this.pixelWidth, this.pixelHeight, 
                new int[this.pixelWidth * this.pixelHeight]);

        byte[] cmd = Arrays.copyOf(graphicMode, graphicMode.length);
        cmd[3] = (byte) (this.pixelWidth % 256);
        cmd[4] = (byte) (this.pixelWidth / 256);

        int y = 0;
        int line = 0;
        while (y < this.pixelHeight) {
            byte[] buf = new byte[this.pixelWidth];
            int h = 0;
            while ((h < 8) && (y < this.pixelHeight)) {
                int v = (1 << (7 - h));
                for (int i=0; i<this.pixelWidth; i++) {
                    buf[i] += (pixels[line + i] != 0) ? v : 0;
                }
                h++;
                y++;
                line += this.pixelWidth;
            }
            append(cmd);
            append(buf);
            append("\n".getBytes());
        }
        append(lineFeed12);
        append(getConfig("epilogue", ""));
    }

/* xxxxx NEW driver end xxxxx */



    /**
     * add an Interleave 2 of 5 symbol (black or white bar; either narrow 
     * or broad)
     * @param buffer the buffer for the symbols
     * @param offset the writing position
     * @param wide true if a wide symbol should be added
     * @param black true if a symbol is black (otherwise white)
     * @return new writing position
     */
    private int addItfSymbol(byte[] buffer, int offset, boolean wide, boolean black) {
        int width = wide ? interleaveNarrow * interleaveRatio : interleaveNarrow;
        for(int i=0; i<width; i++) {
            buffer[offset++] = (byte) (black ? 0xff : 0);
        }
        return offset;
    }

    /**
     * add Interleave 2 of 5 clean area and start symbol to buffer
     * @return new writing position
     */
    private int addItfStart(byte[] buffer) {
        int offset = 0;
        for(int i=0; i < interleaveClear; i++) {
            offset = addItfSymbol(buffer, offset, false, false);
        }
        offset = addItfSymbol(buffer, offset, false, true);
        offset = addItfSymbol(buffer, offset, false, false);
        offset = addItfSymbol(buffer, offset, false, true);
        offset = addItfSymbol(buffer, offset, false, false);
        return offset;
    }

    /**
     * add Interleave 2 of 5 stop symbol and clean area
     * @return new writing position
     */
    private int addItfStop(byte[] buffer, int offset) {
        int o = offset;
        o = addItfSymbol(buffer, o, true, true);
        o = addItfSymbol(buffer, o, false, false);
        o = addItfSymbol(buffer, o, false, true);
        for(int i=0; i < interleaveClear; i++) {
            o = addItfSymbol(buffer, o, false, false);
        }
        return o;
    }

    /**
     * print an Interleave 2 of 5 bar code
     */
    private byte[] getItfData(String data) {
        int len = data.length();
        if ((len % 2) != 0) {
            throw new IllegalArgumentException("Illegal barcode length");
        }
        if (! data.matches("^[0-9]+$")) {
            throw new IllegalArgumentException("Illegal barcorde characters");
        }

        int l = (interleaveClear + 4) * interleaveNarrow;                       // clear + start symbol
        l += (interleaveClear + 2 + interleaveRatio) * interleaveNarrow;        // stop symbol + clear
        l += (3 + (2 * interleaveRatio)) * interleaveNarrow * len;              // barcode data

        byte[] buffer = new byte[l];
        int offset = addItfStart(buffer);
        int i = 0;

        while (i < len) {
            int a = interleave2of5[Integer.parseInt(data.substring(i, i+1))];
            i++;
            int b = interleave2of5[Integer.parseInt(data.substring(i, i+1))];
            i++;

            for(int j=0; j<5; j++) {
                offset = addItfSymbol(buffer, offset, (a & (1 << j)) != 0, true);
                offset = addItfSymbol(buffer, offset, (b & (1 << j)) != 0, false);
            }
        }
        offset = addItfStop(buffer, offset);
        return buffer;
    }


    /**
     * print a Interleave 2 of 5 barcode
     */
    private void printItf(String data) {
        append(lineFeed8);

        byte[] buf = getItfData(data);
        int len = buf.length;
        byte[] cmd = Arrays.copyOf(graphicMode, graphicMode.length);
        cmd[3] = (byte) (len % 256);
        cmd[4] = (byte) (len / 256);
        for (int i=0; i<interleaveRepeat; i++) {
            append(cmd);
            append(buf);
            append("\n".getBytes());
        }

        append(lineFeed12);
        append(String.format("%s\n", data).getBytes());
    }

    /**
     * print a barcode 
     */
    public PrintDriver printBarcode(BarcodeType type, String data) {
/*
        switch(type) {
            case INTERLEAVE25 :
                printItf(data);
                return this;
        }
        throw new IllegalArgumentException("Unsupported barcode type");
*/
        return this;
    }

    /**
     * print a line of text
     */
    public PrintDriver printLine(String line) {
        printLine(5.0, 20.0, "Hallo Welt");
/*
        append(line.getBytes());
        append("\n".getBytes());
*/
        return this;
    }
}
