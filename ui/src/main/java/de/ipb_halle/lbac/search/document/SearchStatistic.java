/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.search.document;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Class for storing the amount of total docs and average words length of nodes
 *
 * @author fmauz
 */
public class SearchStatistic implements Serializable {

    private final Logger LOGGER = LogManager.getLogger(SearchStatistic.class);
    private final Map<UUID, Integer[]> wordsInNode = new HashMap<>();

    /**
     * Calculates the average length of all documents from all nodes
     *
     * @return
     */
    public double getAverageWordLength() {
        int sumOfWords = 0;
        int sumOfDocs = 0;
        for (Integer[] entry : wordsInNode.values()) {
            sumOfWords += entry[0];
            sumOfDocs += entry[1];
        }

        for (UUID id : wordsInNode.keySet()) {
            Integer[] entry = wordsInNode.get(id);
        }
        return (double) sumOfWords / (double) sumOfDocs;
    }

    /**
     * Will add the found statistics to the object. The docs are added for a
     * node while the words will be overwritten.
     *
     * @param nodeId
     * @param docs
     * @param words
     */
    public void addSearchResult(UUID nodeId, int docs, int words) {
        if (wordsInNode.get(nodeId) == null) {
            wordsInNode.put(nodeId, new Integer[]{words, docs});
        } else {
            Integer[] temp = wordsInNode.get(nodeId);
            temp[0] = words;
            temp[1] += docs;
            wordsInNode.put(nodeId, temp);
        }
    }

    /**
     * Will add the found statistics to the object. The docs are added for a
     * node while the words will be overwritten.
     *
     * @param nodeId
     * @param docs
     * @param words
     */
    public void addSearchResult(UUID nodeId, int docs, long words) {
        int wordsAsInt = (int) words;
        addSearchResult(nodeId, docs, wordsAsInt);
    }

}
