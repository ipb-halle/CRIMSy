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

import de.ipb_halle.tx.dict.StopWordDict;
import de.ipb_halle.tx.text.properties.Language;
import de.ipb_halle.tx.text.properties.TextProperty;
import de.ipb_halle.tx.text.properties.Word;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class StopWordFilter extends AbstractFilter {

    public final static String FILTER_TYPE = "StopWordFilter";

    private Logger                      logger;
    private String                      dictKey;
    private BlockingQueue<TextRecord>   inputQueue;
    private BlockingQueue<TextRecord>   outputQueue;
    private FilterState                 filterState;
    private TextRecord                  textRecord;
    private Set<String>                 currentStopWords;



    /**
     * constructor
     */
    public StopWordFilter(JsonObject config) {
        super(config);
        this.filterState = FilterState.NOT_INITIALIZED;
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

            SortedSet<TextProperty> languages = this.textRecord.getProperties(Language.TYPE);
            if (languages.size() > 0) {
                for(TextProperty lang : languages) {

                    this.currentStopWords = StopWordDict.getStopWords(
                            this.dictKey,
                            ((Language) lang).getLanguage());

                    // this.logger.info("currentStopWords({}) size={}", ((Language) lang).getLanguage(), currentStopWords.size());
                    markStopWords(lang.getStart(), lang.getEnd());
                }
            } else {
                markStopWords(0, this.textRecord.length());
            }
        }
    }

    /**
     * mark words as stopwords if they belong to the language 
     * specific set of stopwords or if they are shorter than 
     * 2 characters
     * @param start start of the text region
     * @param end end of the text region 
     */
    public void markStopWords(int start, int end) {
        SortedSet<TextProperty> wordSet = this.textRecord
                .getProperties(Word.TYPE)
                .tailSet(new Word(start, start))
                .headSet(new Word(end + 1, end + 1));

        // this.logger.info("markStopWords({}, {}) size={}", start, end, wordSet.size());
        int len = this.textRecord.getText().length();
        for(TextProperty word : wordSet) {
            int s = word.getStart();
            int e = word.getEnd();

            if ((e < len) && (s < len)) {
                if ( ((e - s) < 2) || this.currentStopWords.contains(this.textRecord.getText().substring(s,e))) {
                    ((Word) word).setStopWord(true);
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
     * @return the output queue 
     */
    public BlockingQueue<TextRecord> getOutputQueue() {
        return this.outputQueue; 
    }

    /**
     * Initialize the filter
     * NOTE: language currently defaults to "de"
     */
    public FilterState init() {
        this.outputQueue.clear();
        this.dictKey = getConfigString("dictKey", null);
        String dictFileNamePattern = getConfigString("dictFileNamePattern", null);
        if (dictKey != null) {
            StopWordDict.addDictionaries(this.dictKey, dictFileNamePattern);
        }
        // load English stop words as default
        this.currentStopWords = StopWordDict.getStopWords(null, "en"); 
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
