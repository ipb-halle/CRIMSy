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
package de.ipb_halle.tx.dict;


import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class StopWordDict {

    public final static String DICT_KEY = "dictKey";
    public final static String DICT_FILE_NAME_PATTERN = "dictFileNamePattern";

    private Logger                                  logger;
    private Map<String, Map<String, Set<String>>>   dictionaries;
    private Map<String, String>                     dictFileNamePatterns = new HashMap<String, String> ();
    private static StopWordDict                     stopWordDict = new StopWordDict();


    /**
     * constructor
     */
    private StopWordDict() {
        this.dictionaries = new HashMap<String, Map<String, Set<String>>> ();
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    /*
     * add additional dictionary sources
     */
    public static void addDictionaries(String dictKey, String dictFileNamePattern) {
        stopWordDict.dictFileNamePatterns.put(dictKey, dictFileNamePattern);
    }

    /**
     * return (and eventually load) the stopWords
     */
    private Set<String> getDictionary(String dictKey, String langCode) {
        Map<String, Set<String>> dict = dictionaries.get(dictKey);
        if (dict == null) {
            synchronized(dictionaries) {
                dict = dictionaries.get(dictKey);
                if(dict == null) {
                    dict = new HashMap<String, Set<String>> ();
                    dictionaries.put(dictKey, dict);
                }
            }
        }
        Set<String> words = dict.get(langCode);
        if (words == null) {
            synchronized(dictionaries) {
                words = dict.get(langCode);
                if (words == null) {
                    words = loadStopWords(dictKey, langCode);
                    dict.put(langCode, words);
                }
            }
        }
        return words;
    }

    public static Set<String> getStopWords(String dictKey, String langCode) {
        return stopWordDict.getDictionary(dictKey, langCode);
    }

    /**
     * load the stopwords
     */
    private Set<String> loadStopWords(String dictKey, String lang) {
        Set<String> sw = new HashSet<String> ();

        Reader rd; 
        String pattern = null;
        if (dictKey != null) {
            pattern = dictFileNamePatterns.get(dictKey); 
        }

        if ((pattern != null) && (pattern.length() > 0)) {
            try {
                String name = String.format(pattern, lang);
                rd = new FileReader(Paths.get(name).toFile());
            } catch (FileNotFoundException e) {
                this.logger.warn("loadStopWords() caught an exception", (Throwable) e);
                return sw;
            }
        } else {
            String name = String.format("stopwords-%s.txt", lang);
            try {
                rd = new InputStreamReader(this.getClass()
                        .getResourceAsStream("stopwords/" + name));
            } catch(Exception e) {
                this.logger.warn("loadStopWords() caught an exception", (Throwable) e);
                return sw;
            }
        }

        try (BufferedReader reader = new BufferedReader(rd)) {

            String line = reader.readLine();
            while(line != null) {
                if (line.length() > 0) {
                    sw.add(line);
                }
                line = reader.readLine();
            }
        } catch(IOException e) {
            this.logger.warn("loadStopWords() caught an exception", (Throwable) e);
        }
        return sw;
    }
}
