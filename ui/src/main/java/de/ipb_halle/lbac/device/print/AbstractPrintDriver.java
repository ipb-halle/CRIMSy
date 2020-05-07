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
import de.ipb_halle.lbac.device.job.JobType;
import de.ipb_halle.lbac.util.HexUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Abstract base class for print drivers 
 *
 * @author fbroda
 */
public abstract class AbstractPrintDriver implements PrintDriver { 

    /* xxxxxx rediculously small initial buffer size; increase to 256 or larger */
    private final static int INITIAL_BUFFER_SIZE = 16;

    private ByteBuffer          buffer;
    private Printer             printer;
    private Map<String, byte[]> configMap;

    private Logger logger;

    /**
     * default constructor
     */
    public AbstractPrintDriver() {
        this.logger = LogManager.getLogger(this.getClass().getName());
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
            tmp.put((ByteBuffer) buf.flip());
            buf = tmp;
        }
        buf.put(data);
        return buf;
    }

    /**
     * Allocate a new buffer. Implementations might want to 
     * override this method to add the prologue section to 
     * the buffer.
     * @return the driver object
     */
    public PrintDriver clear() {
        this.buffer = ByteBuffer.allocate(INITIAL_BUFFER_SIZE);
        return this;
    }

    /**
     * @param key the key 
     * @return the corresponding configuration value
     */
    public byte[] getConfig(String key) {
        return this.configMap.get(key);
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
        this.configMap = new HashMap<String, byte[]> ();
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

    public PrintDriver setPrinter(Printer printer) {
        this.printer = printer;
        parseConfig(printer.getConfig());
        clear();
        return this;
    }

    /**
     * Implementations may want to override this method,
     * e.g. to append an epilogue section
     * @return the Job
     */
    public Job createJob() {
        return new Job()
            .setJobType(JobType.PRINT)
            .setQueue(this.printer.getName())
            .setInput(Arrays.copyOf(this.buffer.array(), this.buffer.position()));
    }
}
