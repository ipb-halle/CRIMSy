/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.ipb_halle.lbac.search.document;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author fmauz
 */
public class TermOccurrence {

    private Map<Integer, Map<String, Integer>> termOccurences = new HashMap<>();

    public Set<Integer> getFileIds() {
        return termOccurences.keySet();
    }

    public void addOccurence(Integer fileId, String word, Integer amount) {
        if (termOccurences.get(fileId) == null) {
            termOccurences.put(fileId, new HashMap<>());
        }
        Map<String, Integer> termsOfFile = termOccurences.get(fileId);
        if (termsOfFile.get(word) == null) {
            termsOfFile.put(word, 0);
        }
        termsOfFile.put(word, termsOfFile.get(word) + amount);
    }

    public int getTotalWordsOfFile(int id) {
        int count = 0;
        if (termOccurences.get(id) == null) {
            return 0;
        }
        Map<String, Integer> wordsOfDocument = termOccurences.get(id);
        for (Integer occurences : wordsOfDocument.values()) {
            count += occurences;
        }
        return count;
    }

    public Map<String, Integer> getTermsOfDocument(int id) {
        return termOccurences.getOrDefault(id, new HashMap<>());
    }
}
