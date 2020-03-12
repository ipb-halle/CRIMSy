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

import de.ipb_halle.lbac.entity.Document;
import de.ipb_halle.lbac.entity.TermFrequency;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * Merges a list of wordterms with a new chunk of found documents from a remote
 * request
 *
 * @author fmauz
 */
public class WordTermListMerger {

    public List<WordTerm> mergeTerms(
            List<WordTerm> originalList,
            List<Document> remoteDocs) {

        for (Document d : remoteDocs) {
            for (TermFrequency wt : d.getTermFreqList().getTermFreq()) {
                WordTerm foundTerm = getTermWithWord(wt.getTerm(), originalList);
                if (foundTerm == null) {
                    originalList.add(
                            new WordTerm(
                                    wt.getFrequency(),
                                    wt.getTerm(),
                                    d.getTermFreqList().getWordRepresantationOf(wt.getTerm())
                            )
                    );
                } else {
                    foundTerm.setAboluteFrequency(foundTerm.getAboluteFrequency() + wt.getFrequency());
                }
            }
        }
        return originalList;
    }

    private WordTerm getTermWithWord(String word, List<WordTerm> originalList) {
        for (WordTerm wt : originalList) {
            if (wt.getTerm().equals(word)) {
                return wt;
            }
        }
        return null;
    }
}
