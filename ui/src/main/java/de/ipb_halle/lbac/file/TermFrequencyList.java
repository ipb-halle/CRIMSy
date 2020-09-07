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
package de.ipb_halle.lbac.file;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

/**
 * Represents a list of termvectors and its frequency with the representations
 * (unstemmed words) of the stemmed word from the text
 *
 * @author fmauz
 */
public class TermFrequencyList {

    private List<TermFrequency> termFreq = new ArrayList<>();
    private Logger logger = LogManager.getLogger(TermFrequencyList.class);
    private List<StemmedWordOrigin> unstemmedWords = new ArrayList<>();

    /**
     *
     * @param wordsAndFreq key - wordroot, value - amount of word in text
     */
    public TermFrequencyList(Map<String, Integer> wordsAndFreq) {
        for (String s : wordsAndFreq.keySet()) {
            termFreq.add(new TermFrequency(s, wordsAndFreq.get(s)));
        }
    }

    public TermFrequencyList() {

    }

    /**
     * Is the given tag in the termfrequency list.
     *
     * @param tag word to look for
     * @return word present or not
     */
    public boolean containsWord(String tag) {
        for (TermFrequency tf : termFreq) {
            if (tf.getTerm().equals(tag)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the frequency of the word in the list. Returns zero if the word
     * is not present. The search is strict, only total matching words are
     * counted
     *
     * @param term word to look for
     * @return frequency of the word. Zero if not present.
     */
    public Integer getFreqOf(String term) {
        for (TermFrequency tf : termFreq) {
            if (tf.getTerm().equals(term)) {
                return tf.getFrequency();
            }
        }
        return 0;
    }

    /**
     * Returns the frequency of the first word containing the term in the list.
     * Returns zero if the word is not present. The search is strict, only total
     * matching words are counted
     *
     * @param term word to look for
     * @return frequency of the word. Zero if not present.
     */
    public Integer getFreqOfVagueWord(String term) {
        for (TermFrequency tf : termFreq) {
            if (term.contains(tf.getTerm()) || tf.getTerm().contains(term)) {
                return tf.getFrequency();
            }
        }
        return 0;
    }

    /**
     * Returns the apperance of a woordroot in the text.
     *
     * @param wordRoot
     * @return the first appearance of the woordroot in the text
     */
    public String getWordRepresantationOf(String wordRoot) {
        for (StemmedWordOrigin swo : unstemmedWords) {
            
            if (swo.getStemmedWord().equals(wordRoot)) {
                return swo.getOriginalWord().get(0);
            }
        }
        return wordRoot;
    }

    /**
     * Returns the stemmed word for an appearance of a word. returns null if the
     * unstemmed word is not found.
     *
     * @param d the unstemmed word
     * @return the stemmed word, null if not found
     */
    public String getStemmedWordFor(String d) {
        for (StemmedWordOrigin swo : unstemmedWords) {
            for (String s : swo.getOriginalWord()) {
                if (s.equals(d)) {
                    return swo.getStemmedWord();
                }
            }
        }
        return null;
    }

    /**
     * Returns all Terms from the termvectorlist
     *
     * @return
     */
    public List<String> getWordRoots() {
        List<String> roots = new ArrayList<>();
        for (TermFrequency tf : termFreq) {
            roots.add(tf.getTerm());
        }
        return roots;

    }

    public List<TermFrequency> getTermFreq() {
        return termFreq;
    }

    public void setTermFreq(List<TermFrequency> termFreq) {
        this.termFreq = termFreq;
    }

    public List<StemmedWordOrigin> getUnstemmedWords() {
        return unstemmedWords;
    }

    public void setUnstemmedWords(List<StemmedWordOrigin> unstemmedWords) {
        this.unstemmedWords = unstemmedWords;
    }

}
