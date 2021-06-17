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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This filter attemts to fix hyphenation artifacts.
 */
public class HyphenationFilter extends AbstractFilter {

    public final static String FILTER_TYPE = "HyphenationFilter";

    private Logger                      logger;
    private BlockingQueue<TextRecord>   inputQueue;
    private BlockingQueue<TextRecord>   outputQueue;
    private FilterState                 filterState;
    private TextRecord                  textRecord;


    /**
     * constructor
     */
    public HyphenationFilter(JsonObject config) {
        super(config);
        this.filterState = FilterState.READY;
        this.outputQueue = new LinkedBlockingQueue<TextRecord> (Filter.QUEUE_SIZE);
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * apply filter operation
     * @param op 
     * @return INPUT_STALL, OUTPUT_STALL or FLUSHED
     */
    public FilterState filter(FilterOperation op) {
        while(true) {

            if ((this.textRecord != null) && (! this.outputQueue.offer(this.textRecord))) {
                return FilterState.OUTPUT_STALL;
            }

            this.textRecord = this.inputQueue.poll();
            if (this.textRecord == null) {
                if (op == FilterOperation.FLUSH) {
                    this.filterState = FilterState.FLUSHED;
                    return this.filterState;
                }
                this.filterState = FilterState.INPUT_STALL;
                return this.filterState;
            }

            fixHyphenation();
        }
    }

    /**
     * fix the hyphenation
     */
    private void fixHyphenation() {
        // TODO xxxxx does nothing!
    }

    /**
     * @return the state of this filter
     */
    public FilterState getFilterState() {
        return this.filterState;
    }

    /**
     * @return the output queue 
     */
    public BlockingQueue<TextRecord> getOutputQueue() {
        return this.outputQueue; 
    }

    /**
     * Initialize the filter
     */
    public FilterState init() {
        this.filterState = FilterState.READY; 
        this.outputQueue.clear();
        return this.filterState;
    }

    /**
     * @param queue the input queue
     */
    public void setInputQueue(BlockingQueue<TextRecord> queue) {
        this.inputQueue = queue;
    }
}
