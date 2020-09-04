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

import de.ipb_halle.tx.dict.StopWordDict;
import de.ipb_halle.tx.text.properties.Language;
import de.ipb_halle.tx.text.properties.Sentence;
import de.ipb_halle.tx.text.properties.TextProperty;
import de.ipb_halle.tx.text.properties.Word;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * This filter attempts per sentence language detection based on 
 * the number of stop words. Per sentence detection is necessary 
 * (eventually per paragraph detection would be an alternative), 
 * because some documents are mixed language documents.
 *
 * @see https://github.com/optimaize/language-detector for an alternative 
 * approach to language detection (n-gram statistics)
 */
public class LanguageDetectorFilter extends AbstractFilter {

    public final static String FILTER_TYPE = "LanguageDetectorFilter";
    public final static String LANGUAGES = "languages";

    private Logger                      logger;
    private String                      config;
    private BlockingQueue<TextRecord>   inputQueue;
    private BlockingQueue<TextRecord>   outputQueue;
    private FilterState                 filterState;
    private TextRecord                  textRecord;
    private String[] languages;
    private Set<?>[] dictionaries;


    /**
     * constructor
     */
    public LanguageDetectorFilter(JsonObject config) {
        super(config);
        this.filterState = FilterState.NOT_INITIALIZED;
        this.languages = new String[] { "de", "en", "es", "fr", "it", "pt" };
        this.outputQueue = new LinkedBlockingQueue<TextRecord> (Filter.QUEUE_SIZE);
        this.logger = LoggerFactory.getLogger(this.getClass());
    }


    /**
     * detect language on a per sentence basis.
     * This method iterates over all sentences and computes  
     * the number of stop words for a set of languages. The 
     * language, for which the number of stopwords maximizes 
     * is assigned to the sentence.
     */
    private void detect(TextRecord rec) {
        String text = rec.getText();
        String currentLang = null;
        int currentStart = -1;
        int i = 0;

        int s = 1;
        for (TextProperty sentence : rec.getProperties(Sentence.TYPE)) {
            String detectedLang = null;
            boolean ambiguous = false;
            int[] counts = new int[languages.length];
            int maxCount = 0;
            int start = sentence.getStart();
            int end = sentence.getEnd();

            Set<TextProperty> wordSet = rec.getProperties(Word.TYPE)
                    .tailSet(new Word(start, start)); 
            Iterator<TextProperty> wordIterator = wordSet.iterator();
            Word word = new Word(0,0);

            while (wordIterator.hasNext() && (word.getStart() <= end)) {
                word = (Word) wordIterator.next();
                i = 0;

                for (String lang : languages) {
                    if (this.dictionaries[i].contains(text.substring(word.getStart(), word.getEnd()))) {
                        counts[i]++;
                        int j = counts[i];
                        if (j > maxCount) {
                            maxCount = j;
                            detectedLang = lang;
                            ambiguous = false;
                        }
                        if (j == maxCount) {
                            ambiguous = true;
                        }
                    }
                    i++;
                }
            }
            // this.logger.info("detect(): sentence {} start {} current {} detected {}", s, currentStart, currentLang, detectedLang);
            if ((detectedLang != null) && (! detectedLang.equals(currentLang))) {
                if (currentStart < 0) {
                    currentLang = detectedLang;
                    currentStart = 0;
                } else {
                    rec.addProperty(new Language(currentStart, sentence.getStart() , currentLang));
                    currentStart = sentence.getStart();
                    currentLang = detectedLang;
                }
            }
            s++;
        }
        if (currentLang != null) {
            rec.addProperty(new Language(currentStart, rec.length(), currentLang));
        }
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

            detect(this.textRecord);
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
     */
    public FilterState init() {

        /* 
         * determine set of languages from configuration, defaults to
         * default set
         */
        JsonArray langCfg = getConfigArray(LANGUAGES);
        int i = 0;
        if (langCfg != null) {
            this.languages = new String[langCfg.size()];
            Iterator<JsonElement> iter = langCfg.iterator();
            while (iter.hasNext()) {
                JsonElement elem = iter.next();
                if ((elem != null) && elem.isJsonPrimitive()) {
                    this.languages[i] = elem.getAsString();
                    i++;
                } else {
                    return this.filterState;
                }
            }
        }

        // redefine the source of dictionaries
        String dictKey = getConfigString(StopWordDict.DICT_KEY, null);
        String dictFileNamePattern = getConfigString(StopWordDict.DICT_FILE_NAME_PATTERN, null);
        if (dictKey != null) {
            StopWordDict.addDictionaries(dictKey, dictFileNamePattern);
        }

        i = 0;
        this.dictionaries = new Set<?> [this.languages.length];
        for(String lang : this.languages) {
            this.dictionaries[i] = StopWordDict.getStopWords(dictKey, lang);
            i++;
        }

        this.outputQueue.clear();
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
