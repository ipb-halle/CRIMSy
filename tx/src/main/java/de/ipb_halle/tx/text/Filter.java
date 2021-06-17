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

import java.util.concurrent.BlockingQueue;

public interface Filter {

    public final static int QUEUE_SIZE = 10;

    /*
     * proposed filters:
     * - elimination of control characters 
     * - stop words
     * - short digit sequences
     * - stemming
     * - language detection (e.g. based on https://github.com/optimaize/language-detector or stopword counts)
     * - ligatures
     * - lower case
     * - umlaut filter
     * - abbreviation / dictionary lookup
     */

    /**
     * apply filter operation
     */
    public FilterState filter(FilterOperation op); 

    /**
     */
    public BlockingQueue<TextRecord> getOutputQueue();

    /**
     * Initialize the reader
     * @return status of the filter (either READY or NOT_INITIALIZED)
     */
    public FilterState init();

    /**
     * @param data filter data
     */
    public void setFilterData(FilterData data);

    /**
     *
     */
    public void setInputQueue(BlockingQueue<TextRecord> queue);

}
