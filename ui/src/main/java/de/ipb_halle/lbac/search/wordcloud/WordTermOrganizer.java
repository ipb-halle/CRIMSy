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
package de.ipb_halle.lbac.search.wordcloud;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reorganises the order of list of WordTerms to get a better look in the tag
 * cloud panel. The aim ist that important words are in the middle and the more
 * the words are at the top and down the less the importance
 *
 * @author fmauz
 */
public class WordTermOrganizer implements Serializable{

    boolean insertAsFirst = true;
    private int wordsSet = 0;

    /**
     * Reorganises the order of the list that words with category HIGHEST are in
     * the middle and less important words are in the outer sides
     *
     * @param terms List with terms to be ordered
     * @param maxTerms maximum words to be displayed
     * @return ordered list
     */
    public List<WordTerm> orderTerms(
            List<WordTerm> terms,
            int maxTerms) {

        //Put them in Buckets
        Map<FreqCategory, List<WordTerm>> categories = new HashMap<>();
        for (FreqCategory cat : FreqCategory.values()) {
            categories.put(cat, new ArrayList<>());
        }

        for (WordTerm t : terms) {
            categories.get(t.getCategory()).add(t);
        }
        wordsSet = categories.get(FreqCategory.HIGHEST).size();
        putWordsOfCatIntoFinalList(
                categories.get(FreqCategory.HIGHEST),
                categories.get(FreqCategory.HIGH), maxTerms);

        putWordsOfCatIntoFinalList(
                categories.get(FreqCategory.HIGHEST),
                categories.get(FreqCategory.MEDIUM), maxTerms);

        putWordsOfCatIntoFinalList(
                categories.get(FreqCategory.HIGHEST),
                categories.get(FreqCategory.LOW), maxTerms);

        putWordsOfCatIntoFinalList(
                categories.get(FreqCategory.HIGHEST),
                categories.get(FreqCategory.LOWEST), maxTerms);

        return categories.get(FreqCategory.HIGHEST);
    }

    /**
     * adds a list of terms with one category to the final list by alterating
     * the insert position by top and botton switching with each element.
     *
     * @param finalList
     * @param listToAdd
     * @return
     */
    private List<WordTerm> putWordsOfCatIntoFinalList(
            List<WordTerm> finalList,
            List<WordTerm> listToAdd,
            int maxTerms) {

        for (WordTerm t : listToAdd) {
            if (wordsSet < maxTerms) {
                wordsSet++;
                if (insertAsFirst) {
                    finalList.add(0, t);
                } else {
                    finalList.add(t);
                }
                insertAsFirst = !insertAsFirst;

            }

        }

        return finalList;
    }
}
