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

import com.google.gson.JsonObject;

import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.SortedSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Outputs the characterstream to an <code>OutputStream</code>
 * without any processing.
 */
public class OutputFilter extends AbstractFilter {

    public final static String FILTER_TYPE = "OutputFilter";
    public final static String OUTPUT_KEY = "outputKey";

    private boolean                     failed;
    private Logger                      logger;
    private String                      outputKey;
    private BlockingQueue<TextRecord>   inputQueue;
    private FilterState                 filterState;


    /**
     * constructor
     */
    public OutputFilter(JsonObject config) {
        super(config);
        this.filterState = FilterState.READY;
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.failed = false;
    }

    /**
     * apply filter operation
     * @param op is ignored for this class 
     * @return INPUT_STALL or FLUSHED
     */
    public FilterState filter(FilterOperation op) {
        while(true) {

            TextRecord rec = this.inputQueue.poll();
            if (rec == null) {
                if (op == FilterOperation.FLUSH) {
                    this.filterState = FilterState.FLUSHED;
                    return this.filterState;
                }
                this.filterState = FilterState.INPUT_STALL;
                return this.filterState;
            }

            output(rec);
        }
    }

    /**
     * output the text to the configured output stream
     */
    private void output(TextRecord rec) {
        
        OutputStream os = (OutputStream) getFilterData().getValue(this.outputKey);
        if (os != null) {
            try {
                os.write(rec.getText().getBytes(Charset.forName("UTF-8")));
            } catch(IOException e) {
                if (! this.failed) {
                    this.logger.warn("output caught an exception; further errors will be silenced until init() is called.", (Throwable) e);
                    this.failed = true;
                }
            }
        }
    }

    /**
     * @return the state of this filter
     */
    public FilterState getFilterState() {
        return this.filterState;
    }

    /**
     * @return this filter does not produce output
     */
    public BlockingQueue<TextRecord> getOutputQueue() {
        return null; 
    }

    /**
     * Initialize the filter
     */
    public FilterState init() {
        this.outputKey = getConfigString(OutputFilter.OUTPUT_KEY, OutputFilter.FILTER_TYPE);
        this.filterState = FilterState.READY; 
        this.failed = false;
        return this.filterState;
    }

    /**
     * @param queue the input queue
     */
    public void setInputQueue(BlockingQueue<TextRecord> queue) {
        this.inputQueue = queue;
    }
}
