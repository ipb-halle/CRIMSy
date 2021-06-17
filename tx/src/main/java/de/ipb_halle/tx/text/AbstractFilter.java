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
import com.google.gson.JsonPrimitive;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractFilter implements Filter {

    private BlockingQueue<TextRecord> inputQueue;
    private FilterData filterData;
    private JsonObject jsonConfig;
    private Logger logger;

    /**
     * default constructor
     */
    public AbstractFilter() {
        this(null);
    }

    /**
     * constructor
     */
    public AbstractFilter(JsonObject jsonConfig) {
        this.jsonConfig = jsonConfig;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * apply filter operation
     */
    public abstract FilterState filter(FilterOperation op); 

    /**
     * @return the FilterData object
     */
    public FilterData getFilterData() { 
        return this.filterData;
    }

    /**
     * @return the output queue of a filter or null if the filter
     * does not produce output to a queue
     */
    public abstract BlockingQueue<TextRecord> getOutputQueue();

    protected JsonArray getConfigArray(String name) {
        if (this.jsonConfig == null) {
            return null;
        }
        JsonElement elem = this.jsonConfig.get(name);
        if ((elem != null) && elem.isJsonArray()) {
            return elem.getAsJsonArray();
        }
        return null;
    }

    protected Double getConfigDouble(String name, Double dflt) {
        JsonElement elem = this.jsonConfig.get(name);
        if (this.jsonConfig == null) {
            return dflt;
        } 
        if ((elem != null) && elem.isJsonPrimitive()) {
            try {
                return Double.valueOf(elem.getAsJsonPrimitive().getAsDouble());
            } catch (Exception e) {
                this.logger.info("getConfigDouble() failed on " + name);
            }
        }
        return dflt;
    }

    protected Integer getConfigInt(String name, Integer dflt) {
        if (this.jsonConfig == null) {
            return dflt;
        } 
        JsonElement elem = this.jsonConfig.get(name);
        if ((elem != null) && elem.isJsonPrimitive()) {
            try {
                return Integer.valueOf(elem.getAsJsonPrimitive().getAsInt());
            } catch (Exception e) {
                this.logger.info("getConfigInt() failed on " + name);
            }
        }
        return dflt;
    }

    protected String getConfigString(String name, String dflt) {
        if (this.jsonConfig == null) {
            return dflt;
        } 
        JsonElement elem = this.jsonConfig.get(name);
        if ((elem != null) && elem.isJsonPrimitive()) {
            return elem.getAsJsonPrimitive().getAsString();
        }
        return dflt;
    }

    /**
     * Initialize the reader
     * @return status of the filter (either READY or NOT_INITIALIZED)
     */
    public abstract FilterState init();


    /**
     * @param data filter data
     */
    public void setFilterData(FilterData data) {
        this.filterData = data;
    }

    /**
     * @param queue input queue
     */
    public void setInputQueue(BlockingQueue<TextRecord> queue) {
        this.inputQueue = queue;
    }

}

