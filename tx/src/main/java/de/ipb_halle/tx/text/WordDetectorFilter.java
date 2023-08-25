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

import de.ipb_halle.tx.text.properties.Word;

import java.text.BreakIterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class WordDetectorFilter extends AbstractFilter {

    public final static String FILTER_TYPE = "WordDetectorFilter";
    public final static String WORD_COUNT = "WordDetectorFilter.wordCount";


    private Logger                      logger;
    private BlockingQueue<TextRecord>   inputQueue;
    private BlockingQueue<TextRecord>   outputQueue;
    private FilterState                 filterState;
    private TextRecord                  textRecord;
    private BreakIterator               breakIterator;


    /**
     * constructor
     */
    public WordDetectorFilter(JsonObject config) {
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

                // use language specific BreakIterator instead?
                this.breakIterator.setText(rec.getText());
                int start = this.breakIterator.current();
                int end = this.breakIterator.current();
                int wordCount = getWordCount();
                while (end != BreakIterator.DONE) {
                    if (start != end) {
                        if((end-start != 1) || (rec.getText().charAt(start) != ' ')) {
                            rec.addProperty(new Word(start, end));
                            wordCount++;
                        }
                    }
                    start = end;
                    end = this.breakIterator.next();
                }
                getFilterData().setValue(WORD_COUNT, wordCount);
                this.textRecord = rec; 
            }
        }
    }

    private int getWordCount() {
        Integer w = (Integer) getFilterData().getValue(WORD_COUNT);
        if (w != null) {
            return w;
        }
        return 0;
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
        this.breakIterator = BreakIterator.getWordInstance();
        return this.filterState;
    }

    /**
     * @param queue the input queue
     */
    public void setInputQueue(BlockingQueue<TextRecord> queue) {
        this.inputQueue = queue;
    }
}
