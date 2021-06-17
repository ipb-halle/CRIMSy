/*
 * Cloud Resource & Information Management System (CRIMSy)
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
package de.ipb_halle.lbac.search.wordcloud;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author fmauz
 */
public class WordTermCategoriserComplex extends WordTermCategoriser {

    public WordTermCategoriserComplex(int totalDocs) {
        super(totalDocs);
    }

    @Override
    public void categorise(List<WordTerm> terms) {
        List<Float> factors = new ArrayList<>();
        float highestFactor = 0;
        for (WordTerm wt : terms) {
            float factor = (float) wt.getDocsWithTerm() / (float) totalDocs;
            factor = (0.5f - Math.abs(factor - 0.5f)) * wt.getAboluteFrequency();
            factors.add(factor);
            highestFactor = Math.max(factor, highestFactor);
        }

        for (int i = 0; i < terms.size(); i++) {
            float normalizedFactor = factors.get(i) / highestFactor;
            if (normalizedFactor >= borderHighest) {
                terms.get(i).setCategory(FreqCategory.HIGHEST);
            } else if (normalizedFactor >= borderHigh) {
                terms.get(i).setCategory(FreqCategory.HIGH);
            } else if (normalizedFactor >= borderMedium) {
                terms.get(i).setCategory(FreqCategory.MEDIUM);
            } else if (normalizedFactor >= borderLow) {
                terms.get(i).setCategory(FreqCategory.LOW);
            } else {
                terms.get(i).setCategory(FreqCategory.LOWEST);
            }

        }
    }

}
