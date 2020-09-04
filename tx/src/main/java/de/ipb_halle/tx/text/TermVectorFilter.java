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

import de.ipb_halle.tx.text.properties.TextProperty;
import de.ipb_halle.tx.text.properties.Word;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOError;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The <code>TermVectorFilter</code> expects a <code>TextRecord</code>
 * with annotated <code>Word</code>s as input and collects how often 
 * each word occurs. If word stems have been assigned to the words, 
 * (e.g. by the <code>StemmingFilter</code> it will collect the number 
 * of occurence of each stem and build a dictionary mapping the stem
 * to each original word. 
 */
public class TermVectorFilter extends AbstractFilter {

    public final static String FILTER_TYPE = "TermVectorFilter";
    public final static String TERM_VECTOR = "TermVectorFilter.termVector";
    public final static String STEM_DICT = "TermVectorFilter.stemDict";


    private Logger                      logger;
    private BlockingQueue<TextRecord>   inputQueue;
    private Map<String, Integer>        termVector;
    private Map<String, String>         stemDict;
    private FilterState                 filterState;


    /**
     * constructor
     */
    public TermVectorFilter(JsonObject config) {
        super(config);
        this.filterState = FilterState.NOT_INITIALIZED;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * apply filter operation
     * @param op is ignored for this class 
     * @return INPUT_STALL or FLUSHED
     */
    public FilterState filter(FilterOperation op) {
        if (this.filterState == FilterState.NOT_INITIALIZED) {
            return this.filterState;
        }
        while(true) {

            TextRecord rec = this.inputQueue.poll();
            if (rec == null) {
                if (op == FilterOperation.FLUSH) {
                    return FilterState.FLUSHED;
                }
                return FilterState.INPUT_STALL;
            }
       
            String text = rec.getText();
            for(TextProperty prop : rec.getProperties(Word.TYPE)) { 
                Word word = (Word) prop;
                String wordString = text.substring(word.getStart(), word.getEnd());
                if (! word.getStopWord()) {
                    String stem = word.getStem();
                    if (stem == null) {
                        stem = wordString;
                    } else {
                        stemDict.put(wordString, stem);
                    }
                    Integer count = this.termVector.get(stem);
                    if (count == null) {
                        count = Integer.valueOf(0);
                    }
                    this.termVector.put(stem, Integer.valueOf(count.intValue() + 1));
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
        this.filterState = FilterState.READY;
        this.termVector = new HashMap<String, Integer> ();
        this.stemDict = new HashMap<String, String> ();
        getFilterData().setValue(TERM_VECTOR,  this.termVector);
        getFilterData().setValue(STEM_DICT, this.stemDict);
        return this.filterState;
    }

    /**
     * @param queue the input queue
     */
    public void setInputQueue(BlockingQueue<TextRecord> queue) {
        this.inputQueue = queue;
    }
}
