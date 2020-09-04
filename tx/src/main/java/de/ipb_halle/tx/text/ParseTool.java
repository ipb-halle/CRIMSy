/*
 * Text eXtractor
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
package de.ipb_halle.tx.text;

import de.ipb_halle.tx.text.properties.Line;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;


import org.apache.tika.metadata.Metadata;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParseTool {

    private FilterData filterData;
    private Filter filter;
    private Logger logger;

    private BlockingQueue inputQueue;
    private InputStream inputStream;
    private InputStream filterDefinition;
    private OutputStream outputStream;

    /**
     * default constructor 
     */
    public ParseTool() {
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * return the accumulated filter data
     */
    public FilterData getFilterData() {
        return this.filterData;
    }

    /**
     * filter initialization
     */
    public void initFilter() {
        try {
            this.filter = FilterFactory.buildFilter(this.filterDefinition);
            this.filterData = new FilterData();
            this.filterData.setValue(OutputFilter.FILTER_TYPE, this.outputStream);
            this.filter.setFilterData(this.filterData);
            this.inputQueue = new LinkedBlockingQueue<TextRecord> (Filter.QUEUE_SIZE);
            this.filter.setInputQueue(this.inputQueue);

            if (this.filter.init() != FilterState.READY) {
                throw new RuntimeException("Configuration of FilterDriver failed");
            }
        } catch(Exception e) {
            throw new RuntimeException("Filter initialization failed", (Throwable) e);
        }
    }

    /**
     * setup run Tika for parsing and run the parser job
     */
    public void parse() {
        Pattern lineEndPattern = Pattern.compile("[-\\s]$");
        Tika tika = new Tika();
        Metadata metadata = new Metadata();
        try {
            BufferedReader rd = new BufferedReader(tika.parse(this.inputStream, metadata));
            int lineNumber = 1;

            String line = rd.readLine();
            while(line != null) {

                line = lineEndPattern.matcher(line).matches() ? line : line + " ";
                TextRecord rec = new TextRecord(line);
                rec.addProperty( new Line(0, line.length(), lineNumber));

                if(! this.inputQueue.offer(rec)) {
                    this.filter.filter(FilterOperation.NORMAL);
                    this.inputQueue.offer(rec);
                }
                line = rd.readLine();
                lineNumber++;
            }
            this.filter.filter(FilterOperation.FLUSH);
        } catch(Exception e) {
            this.logger.warn("parse() caught an exception", (Throwable) e);
        }
    }


    public void setInputStream(InputStream is) { this.inputStream = is; }
    public void setFilterDefinition(InputStream is) { this.filterDefinition = is; }
    public void setOutputStream(OutputStream os) { this.outputStream = os; }

}
