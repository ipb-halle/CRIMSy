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

import java.util.Collections;
import java.util.List;

/**
 * Splits a list of words into categories. 5 groups of categories are created,
 * each group having the same size. The group with the most frequent occurrences
 * will be placed in the category HIGHEST, the group with the fewest in the
 * category LOWEST.
 *
 * @author fmauz
 */
public class WordTermCategoriser {

    protected int totalDocs;
    protected int totalWords;
    protected float borderLow = 0.2f;
    protected float borderMedium = 0.4f;
    protected float borderHigh = 0.6f;
    protected float borderHighest = 0.8f;

    public WordTermCategoriser(int totalDocs) {
        this.totalDocs = totalDocs;
    }

    /**
     * Sets the category for each wordterm
     *
     * @param terms List of terms for which the category should be set
     */
    public void categorise(List<WordTerm> terms) {
        Collections.sort(terms);
        for (WordTerm t : terms) {
            totalWords += t.getAboluteFrequency();
        }
        for (WordTerm t : terms) {
            t.setRelativeFrequency(t.getAboluteFrequency() / totalWords);
        }

        for (int i = 0; i < terms.size(); i++) {
            if (i >= terms.size() * borderHighest) {
                terms.get(i).setCategory(FreqCategory.HIGHEST);
            } else if (i >= terms.size() * borderHigh) {
                terms.get(i).setCategory(FreqCategory.HIGH);
            } else if (i >= terms.size() * borderMedium) {
                terms.get(i).setCategory(FreqCategory.MEDIUM);
            } else if (i >= terms.size() * borderLow) {
                terms.get(i).setCategory(FreqCategory.LOW);
            } else {
                terms.get(i).setCategory(FreqCategory.LOWEST);
            }

        }

    }
}
