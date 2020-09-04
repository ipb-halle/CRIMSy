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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOError;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class AggregatingFilter extends AbstractFilter {

    public final static String FILTER_TYPE = "AggregatingFilter";
    public final static String AGGREGATION_LENGTH = "aggregationLength";

    private Logger                      logger;
    private FilterState                 filterState;
    private BlockingQueue<TextRecord>   inputQueue;
    private BlockingQueue<TextRecord>   outputQueue;
    private int                         aggregationLength;
    private TextRecord                  textRecord;


    /**
     * constructor
     */
    public AggregatingFilter(JsonObject config) {
        super(config);
        this.outputQueue = new LinkedBlockingQueue<TextRecord> (Filter.QUEUE_SIZE);
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.textRecord = new TextRecord();
    }

    /**
     * apply filter operation
     * @param op if no input is available and op == FLUSH, the internal buffers are flushed (content forwarded to the output queue).
     * @return INPUT_STALL, OUTPUT_STALL or READY
     */
    public FilterState filter(FilterOperation op) {
        if (this.filterState == FilterState.NOT_INITIALIZED) {
            return this.filterState;
        }
        while (true) {

            if (this.textRecord.length() >= this.aggregationLength) {
                if (this.outputQueue.offer(this.textRecord)) { 
                    this.textRecord = new TextRecord();
                } else {
                    this.filterState = FilterState.OUTPUT_STALL;
                    return this.filterState;
                }
            }

            TextRecord rec = this.inputQueue.poll();
            if (rec == null) {
                if (op == FilterOperation.FLUSH) {
                    if (this.outputQueue.offer(this.textRecord)) { 
                        this.textRecord = new TextRecord();
                        this.filterState = FilterState.FLUSHED;
                    } else {
                        this.filterState = FilterState.OUTPUT_STALL;
                    }
                } else {
                    this.filterState = FilterState.INPUT_STALL;
                }
                return this.filterState;
            } 

            this.textRecord.append(rec);
        }
    }

    /**
     * @return the output queue for this filter 
     */
    public BlockingQueue<TextRecord> getOutputQueue() {
        return this.outputQueue;
    }

    /**
     * Initialize the filter
     */
    public FilterState init() {
        this.filterState = FilterState.NOT_INITIALIZED;
        this.outputQueue.clear();
        this.aggregationLength = getConfigInt(AGGREGATION_LENGTH, 100000);
        this.filterState = FilterState.READY;
        return this.filterState;
    }

    /**
     * @param queue the input queue
     */
    public void setInputQueue(BlockingQueue<TextRecord> queue) {
        this.inputQueue = queue;
    }
}
