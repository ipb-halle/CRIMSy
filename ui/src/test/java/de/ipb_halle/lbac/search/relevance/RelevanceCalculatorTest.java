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
package de.ipb_halle.lbac.search.relevance;

import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.search.document.Document;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.search.document.StemmedWordGroup;
import de.ipb_halle.tx.file.TermFrequency;
import de.ipb_halle.tx.file.TermFrequencyList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import org.junit.jupiter.api.Test;

/**
 *
 * @author fmauz
 */
public class RelevanceCalculatorTest {

    private RelevanceCalculator instance;
    private float EPSILON = 0.01f;

    /**
     * Tests consists of two steps: (1) Calculate the relevance scores for local
     * documents (2) After the retrieving of documents from a remote node,
     */
    @Test
    public void calculateRelevanceFactorsTest() {
        instance = new RelevanceCalculator(Arrays.asList("java"));
        StemmedWordGroup normalizedTerms = new StemmedWordGroup();
        normalizedTerms.addStemmedWord("java", new HashSet<>(Arrays.asList("java","jav")));
        instance.setSearchTerms(normalizedTerms);
        int totalDocsInFirstIteration = 10;
        double averageWordsInCollection = 55;

        List<Document> docsFromFirstIteration = createFakeDocs(3, "TestInstitut-local");
        docsFromFirstIteration.get(0).getTermFreqList().getTermFreq().add(new TermFrequency("java", 3));
        docsFromFirstIteration.get(0).setWordCount(40);
        docsFromFirstIteration.get(0).getTermFreqList().getTermFreq().add(new TermFrequency("futures", 1));
        docsFromFirstIteration.get(1).getTermFreqList().getTermFreq().add(new TermFrequency("java", 2));
        docsFromFirstIteration.get(1).setWordCount(100);
        docsFromFirstIteration.get(2).setWordCount(25);

        List<Document> calculatedDocs = instance.calculateRelevanceFactors(
                totalDocsInFirstIteration,
                averageWordsInCollection,
                docsFromFirstIteration);

        assertEquals(1.299d, calculatedDocs.get(0).getRelevance(), EPSILON);
        assertEquals(0.870d, calculatedDocs.get(1).getRelevance(), EPSILON);
        assertEquals(0.000d, calculatedDocs.get(2).getRelevance(), EPSILON);

        int totalDocsInSecondIteration = 17;
        averageWordsInCollection = 57;
        List<Document> docsFromSecondIteration = createFakeDocs(3, "TestInstitut-Remote");
        docsFromSecondIteration.get(0).getTermFreqList().getTermFreq().add(new TermFrequency("java", 1));
        docsFromSecondIteration.get(0).setWordCount(80);
        docsFromSecondIteration.get(0).getTermFreqList().getTermFreq().add(new TermFrequency("futures", 4));
        docsFromSecondIteration.get(1).getTermFreqList().getTermFreq().add(new TermFrequency("java", 2));
        docsFromSecondIteration.get(1).setWordCount(23);
        docsFromSecondIteration.get(2).setWordCount(65);
        docsFromSecondIteration.get(2).getTermFreqList().getTermFreq().add(new TermFrequency("futures", 2));

        calculatedDocs.addAll(docsFromSecondIteration);
        calculatedDocs = instance.calculateRelevanceFactors(
                totalDocsInSecondIteration,
                averageWordsInCollection,
                calculatedDocs);

        assertEquals(1.209d, calculatedDocs.get(0).getRelevance(), EPSILON);
        assertEquals(0.817d, calculatedDocs.get(1).getRelevance(), EPSILON);
        assertEquals(0.000d, calculatedDocs.get(2).getRelevance(), EPSILON);
        assertEquals(0.618d, calculatedDocs.get(3).getRelevance(), EPSILON);
        assertEquals(1.190d, calculatedDocs.get(4).getRelevance(), EPSILON);
        assertEquals(0.000d, calculatedDocs.get(5).getRelevance(), EPSILON);

    }

    @Test
    public void getFreqOfVagueWordTest() {
        TermFrequencyList tfl = new TermFrequencyList();
        TermFrequency tf = new TermFrequency("eimer", 2);
        tfl.getTermFreq().add(tf);
        assertEquals(2, (long) tfl.getFreqOfVagueWord("eimerweise"));
        assertEquals(2, (long) tfl.getFreqOfVagueWord("eim"));
    }

    private List<Document> createFakeDocs(long amount, String institut) {
        List<Document> back = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            Document d = new Document();
            Node n = new Node();
            n.setId(UUID.randomUUID());
            n.setInstitution(institut);
            Collection c = new Collection();
            c.setId(100000);
            c.setName("TestColl" + i);
            d.setCollection(c);
            d.setCollectionId(c.getId());
            d.setNode(n);
            d.setNodeId(n.getId());
            d.setLanguage("en");
            d.setOriginalName("TestDocument");
            d.setContentType("pdf");
            d.setPath("xx/xx");
            back.add(d);
        }
        return back;
    }

}
