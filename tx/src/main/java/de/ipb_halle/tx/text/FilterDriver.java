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
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class FilterDriver extends AbstractFilter {

    public final static String FILTER_TYPE = "FilterDriver";

    private Logger                          logger;
    private FilterState                     filterState;
    private BlockingQueue<TextRecord>       inputQueue;

    // both lists must contain the same number of elements!
    private List<List<Filter>>              filterChains;
    private List<BlockingQueue<TextRecord>> chainQueues;


    /**
     * default constructor
     */
    public FilterDriver() {
        this(null);
    }

    /**
     * constructor
     */
    public FilterDriver(JsonObject config) {
        super(config);
        this.filterState = FilterState.NOT_INITIALIZED;
        this.filterChains = new ArrayList<List<Filter>> ();
        this.chainQueues = new ArrayList<BlockingQueue<TextRecord>> ();
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * add a filter chain to this FilterDriver
     */
    public void append(List<Filter> chain) {
        this.chainQueues.add(new LinkedBlockingQueue<TextRecord> (Filter.QUEUE_SIZE));
        this.filterChains.add(chain);
    }

    /**
     * apply filter operation
     * @param op 
     * @return may return NOT_INITIALIZED, INPUT_STALL or FLUSHED (only when op == FLUSH) 
     * but will never return OUTPUT_STALL
     */
    public FilterState filter(FilterOperation op) {
        if (this.filterState == FilterState.NOT_INITIALIZED) {
            return this.filterState;
        }

        int capacity = Filter.QUEUE_SIZE;
        for(BlockingQueue queue : this.chainQueues) {
            int c = queue.remainingCapacity();
            capacity = (c < capacity) ? c : capacity;
        }
        while(capacity > 0) {
            TextRecord rec = this.inputQueue.poll();
            if (rec == null) {
                if (op == FilterOperation.FLUSH) {
                    this.filterState = filterChains(op);
                    return this.filterState;
                }
                this.filterState = FilterState.INPUT_STALL;
                return this.filterState;
            }

            for(BlockingQueue<TextRecord> queue : this.chainQueues) {
                try {
                    queue.offer((TextRecord) rec.clone()); 
                } catch(CloneNotSupportedException e) {
                    throw new RuntimeException("Cloning of TextRecord object failed in FilterDriver", (Throwable) e);
                }
            }
            capacity--;
        }

        this.filterState = filterChains(op);
        return this.filterState;
    }

    /**
     *
     */
    private FilterState filterChains(FilterOperation op) {
        for(List<Filter> chain : this.filterChains) {
            FilterState state = filterChain(op, chain);
            if (state != FilterState.INPUT_STALL) {
                if ((op != FilterOperation.FLUSH) || (state != FilterState.FLUSHED)) {
                    return state;
                }
            }
        }
        return ((op == FilterOperation.FLUSH) ? FilterState.FLUSHED : FilterState.INPUT_STALL);
    }

    /**
     * run a single filter chain
     * @return may return NOT_INITIALIZED, INPUT_STALL or FLUSHED (only when op == FLUSH), 
     * but will never return OUTPUT_STALL
     */
    private FilterState filterChain(FilterOperation op, List<Filter> chain) {
        int filterIndex = 0;

        while(true) {
            Filter filter = chain.get(filterIndex);
            FilterState state = filter.filter(op);
            switch(state) {
                case OUTPUT_STALL :
                    filterIndex++;
                    break;
                case INPUT_STALL :
                    if (filterIndex < 1) {
                        return FilterState.INPUT_STALL;
                    }
                    filterIndex--;
                    break;
                case FLUSHED :
                    if (filterIndex < (chain.size() - 1)) {
                        filterIndex++;
                    } else {
                        return FilterState.FLUSHED;
                    }
                    break;
                default :
                    return state;
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
     * @return the FilterDriver returns the output queue of 
     * the first filter chain as an output queue
     */
    public BlockingQueue<TextRecord> getOutputQueue() {
        List<Filter> chain = this.filterChains.get(0);
        int lastFilter = chain.size() - 1;
        return chain.get(lastFilter).getOutputQueue();
    }

    /**
     * Initialize the filter chains
     */
    public FilterState init() {
        this.filterState = FilterState.NOT_INITIALIZED;
        int ci = 0;
        int fi = 0;

        ListIterator<BlockingQueue<TextRecord>> queueIterator = this.chainQueues.listIterator();
        for(List<Filter> chain : this.filterChains) {
            ci++;
            BlockingQueue<TextRecord> input = queueIterator.next();
            for(Filter filter : chain) {

                fi++;
                filter.setInputQueue(input);
                input = filter.getOutputQueue();

                FilterState state = filter.init();
                if ( state != FilterState.READY) {
                    this.logger.warn("init() failed for chain {} filter {}", ci, fi);
                    return this.filterState;
                }
            }
        }
        this.filterState = FilterState.READY;
        return this.filterState;
    }

    /**
     */
    @Override
    public void setFilterData(FilterData data) { 
        super.setFilterData(data);
        for(List<Filter> chain : this.filterChains) {
            for(Filter filter : chain) {
                filter.setFilterData(data);
            }
        }
    }

    /**
     * @param queue the input queue
     */
    public void setInputQueue(BlockingQueue<TextRecord> queue) {
        this.inputQueue = queue;
    }
}
