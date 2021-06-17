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
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;


import java.beans.XMLDecoder;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilterFactory {
    
    private final static String CHAIN = "chain";
    private final static String CHAINS = "chains";
    private final static String TYPE = "type";

    private static Logger logger() {
        return LoggerFactory.getLogger(FilterFactory.class);
    }

    /**
     * build a single filter
     */
    public static Filter buildFilter(String type, JsonObject config) {
        if (type == null) {
            throw new NullPointerException();
        }

        logger().info("Factory: {}", type); 
        switch(type) {
            case AggregatingFilter.FILTER_TYPE:
                return new AggregatingFilter(config); 

            case FilterDriver.FILTER_TYPE:
                FilterDriver filterDriver = new FilterDriver();
                buildFilterChains(filterDriver, config);
                return filterDriver;

            case HyphenationFilter.FILTER_TYPE :
                return new HyphenationFilter(config); 

            case LanguageDetectorFilter.FILTER_TYPE :
                return new LanguageDetectorFilter(config);

            case LowerCaseFilter.FILTER_TYPE :
                return new LowerCaseFilter(config);

            case PatternFilter.FILTER_TYPE :
                return new PatternFilter(config);

            case OutputFilter.FILTER_TYPE :
                return new OutputFilter(config);

            case PrintFilter.FILTER_TYPE :
                return new PrintFilter(config);

            case SentenceDetectorFilter.FILTER_TYPE :
                return new SentenceDetectorFilter(config);

            case StemmingFilter.FILTER_TYPE :
                return new StemmingFilter(config);

            case StopWordFilter.FILTER_TYPE :
                return new StopWordFilter(config);

            case TermVectorFilter.FILTER_TYPE :
                return new TermVectorFilter(config);

            case WordDetectorFilter.FILTER_TYPE :
                return new WordDetectorFilter(config);

            default :
                throw new IllegalArgumentException("Unknown filter type");
        }
    }

    /**
     * build a filter chain
     */
    public static Filter buildFilter(InputStream stream) {
        try {
            JsonElement jsonTree = JsonParser.parseReader(
                new InputStreamReader(stream));
            if(jsonTree.isJsonObject()) {
                JsonObject obj = jsonTree.getAsJsonObject();
                JsonElement type = obj.get(TYPE); 
                if ((type != null)
                     && type.isJsonPrimitive()) {
                    return buildFilter(type.getAsString(), obj); 
                }
            }
                
        } catch(Exception e) {
           throw new RuntimeException("Failed to build filterchain", (Throwable) e);
        }
        return null;
    }

    /**
     * build an individual filter chain
     */
    private static List<Filter> buildFilterChain(JsonArray chain) {
        List<Filter> filterList = new ArrayList<> ();
        Iterator<JsonElement> iter = chain.iterator();
        while (iter.hasNext()) {
            JsonElement elem = iter.next();
            if ((elem != null) 
                    && elem.isJsonObject()) {
                JsonObject obj = elem.getAsJsonObject();
                JsonElement type = obj.get(TYPE);
                if ((type != null) 
                        && type.isJsonPrimitive()) {
                    filterList.add(buildFilter(type.getAsString(), obj));
                }
            }
        }
        return filterList;
    }

    /**
     * build all filter chains for a filterDriver
     */
    private static void buildFilterChains(FilterDriver filterDriver, JsonObject config) { 
        JsonElement chains = config.get(CHAINS);
        if ((chains != null) 
                && chains.isJsonArray()) {
            Iterator<JsonElement> iter = chains.getAsJsonArray().iterator();
            while (iter.hasNext()) {
                JsonElement elem = iter.next();

                // 'Array of JsonObjects' instead of 'Array of Arrays' allows to add comment fields
                if ((elem != null) &&
                        elem.isJsonObject()) {
                    JsonElement chain = elem.getAsJsonObject().get(CHAIN);
                    if ((chain != null) 
                            && chain.isJsonArray()) {
                        filterDriver.append(buildFilterChain(chain.getAsJsonArray()));
                    }
                }
            }
        }
    }
}
