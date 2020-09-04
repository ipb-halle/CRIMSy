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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class PatternFilter extends AbstractFilter {

    public final static String FILTER_TYPE = "PatternFilter";
    public final static String PATTERN_CONFIG = "patternConfig";
    public final static String PATTERN = "pattern";
    public final static String REPLACEMENT = "replacement";

    private List<Pattern>               filterPatterns;
    private List<String>                replacements;
    private Logger                      logger;
    private FilterState                 filterState;
    private BlockingQueue<TextRecord>   inputQueue;
    private BlockingQueue<TextRecord>   outputQueue;
    private TextRecord                  textRecord;


    /**
     * constructor
     */
    public PatternFilter(JsonObject config) {
        super(config);
        this.filterState = FilterState.NOT_INITIALIZED;
        this.outputQueue = new LinkedBlockingQueue<TextRecord> (Filter.QUEUE_SIZE);
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * apply filter operation
     * @param op is ignored by this filter
     * @param 
     */
    public FilterState filter(FilterOperation op) {
        if (this.filterState == FilterState.NOT_INITIALIZED) {
            return this.filterState;
        }

        while (true) {
            if (this.textRecord != null) {
                if (! this.outputQueue.offer(this.textRecord)) {
                    this.filterState = FilterState.OUTPUT_STALL;
                    return this.filterState;
                } 
                this.textRecord = null;
            } else {
                TextRecord rec = this.inputQueue.poll();
                if (rec == null) {
                    if (op == FilterOperation.FLUSH) {
                        this.filterState = FilterState.FLUSHED;
                        return this.filterState;
                    }
                    this.filterState = FilterState.INPUT_STALL;
                    return this.filterState;
                }
                filterText(rec);
                this.textRecord = rec; 
            }
        }
    }

    /**
     * filter (match and replace) text record according to the 
     * filterPatterns and replacements. Adjusts the start and end 
     * positions for associated TextProperties.
     */
    private void filterText(TextRecord rec) {

        String st = rec.getText();
        int i = 0;
        for(Pattern pattern : this.filterPatterns) {
            int accumulatedDelta = 0;
            List<Adjustment> adjustments = new ArrayList<Adjustment> ();
            String replacement = this.replacements.get(i);
            int origLen = st.length();
            Matcher matcher = pattern.matcher(st);
            while (matcher.find()) {
                int start = matcher.start();
                int end = matcher.end();
                st = matcher.replaceFirst(replacement);
                int len = st.length();
                int delta = len - origLen;
                if (delta != 0) {
                    /*
                     * this.logger.info(String.format("rule %d: start %4d, end %4d, delta %d, len %4d, origLen %4d\n", i, start, end, delta, len, origLen));
                     */
                    adjustments.add(new Adjustment(start, end, delta, accumulatedDelta));
                    accumulatedDelta += delta;
                }
                origLen = len;
                matcher = pattern.matcher(st);
            }
            rec.adjustAll(adjustments);
            i++;
        }
        rec.setText(st);
    }

    /**
     * @return the state of this filter
     */
    public FilterState getFilterState() { 
        return this.filterState;
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
        this.filterPatterns = new ArrayList<Pattern> ();
        this.replacements = new ArrayList<String> ();

        JsonArray js = getConfigArray(PATTERN_CONFIG);
        if ((js == null) || (js.size() == 0)) {
            return this.filterState;
        }

        Iterator<JsonElement> iter = js.iterator();
        while (iter.hasNext()) {
            JsonElement elem = iter.next();
            if (elem.isJsonObject()) {
                JsonElement jsPattern = elem.getAsJsonObject().get(PATTERN);
                JsonElement jsReplacement = elem.getAsJsonObject().get(REPLACEMENT);

                if ((jsPattern != null)
                        && (jsReplacement != null)
                        && jsPattern.isJsonPrimitive()
                        && jsReplacement.isJsonPrimitive()) {

                    String patternString = jsPattern.getAsString();
                    String replacement = jsReplacement.getAsString();
                    this.filterPatterns.add(Pattern.compile(patternString));
                    this.replacements.add(replacement);
                    this.logger.info("Pattern: {} Replacement: {}", patternString, replacement);
                }
            }
        }
        if (this.inputQueue != null) {
            this.filterState = FilterState.READY;
        }
        return this.filterState;
    }

    /**
     * @param queue the input queue
     */
    public void setInputQueue(BlockingQueue<TextRecord> queue) {
        this.inputQueue = queue;
    }
}
