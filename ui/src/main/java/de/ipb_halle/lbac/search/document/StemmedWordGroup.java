/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ipb_halle.lbac.search.document;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author fmauz
 */
public class StemmedWordGroup {

    Map<String, Set<String>> stemmedWords = new HashMap<>();

    public void addStemmedWord(String original, Set<String> stemmedWords) {
        this.stemmedWords.put(original, stemmedWords);
    }

    public Set<String> getAllStemmedWords() {
        Set<String> allWords = new HashSet<>();
        for (Set<String> set : stemmedWords.values()) {
            allWords.addAll(set);
        }
        return allWords;
    }

    public Map<String, Set<String>> getStemmedWords() {
        return stemmedWords;
    }

    public Set<String> getStemmedWordsFor(String word) {
        return stemmedWords.getOrDefault(word, new HashSet<>());
    }

}
