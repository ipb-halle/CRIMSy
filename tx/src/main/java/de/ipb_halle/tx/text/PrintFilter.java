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

import de.ipb_halle.tx.text.properties.Line;
import de.ipb_halle.tx.text.properties.Sentence;
import de.ipb_halle.tx.text.properties.TextProperty;
import de.ipb_halle.tx.text.properties.Word;

import java.util.SortedSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Uses the <code>dump()</code> method of <code>TextProperty</code> 
 * to print information of <code>TextRecord</code> objects to 
 * <code>System.out</code>. The filter can be configured which 
 * properties it should print.
 */
public class PrintFilter extends AbstractFilter {

    public final static String FILTER_TYPE = "PrintFilter";
    public final static String PROPERTY = "property";

    private Logger                      logger;
    private BlockingQueue<TextRecord>   inputQueue;
    private FilterState                 filterState;
    private String                      printProperty;


    /**
     * constructor
     */
    public PrintFilter(JsonObject config) {
        super(config);
        this.filterState = FilterState.READY;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    /**
     * apply filter operation
     * @param op is ignored for this class 
     * @return INPUT_STALL or FLUSHED
     */
    public FilterState filter(FilterOperation op) {
        while(true) {

            TextRecord rec = this.inputQueue.poll();
            if (rec == null) {
                if (op == FilterOperation.FLUSH) {
                    this.filterState = FilterState.FLUSHED;
                    return this.filterState;
                }
                this.filterState = FilterState.INPUT_STALL;
                return this.filterState;
            }

            print(rec);
        }
    }

    /**
     * print a record according to the filter
     */
    private void print(TextRecord rec) {
        SortedSet<TextProperty> properties = rec.getProperties(this.printProperty); 
        String text = rec.getText();
        if (properties != null) {
            for(TextProperty prop : properties) {
                System.out.println(prop.dump(text.substring(prop.getStart(), prop.getEnd())));
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
        this.printProperty = getConfigString(PROPERTY, Line.TYPE);
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
