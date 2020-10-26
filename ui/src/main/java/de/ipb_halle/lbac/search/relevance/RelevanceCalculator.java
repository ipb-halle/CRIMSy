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

import de.ipb_halle.lbac.search.SearchQueryStemmer;
import de.ipb_halle.lbac.search.document.Document;
import de.ipb_halle.lbac.search.document.StemmedWordGroup;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Implementation of the Okapi BM25 Ranking
 * {@link https://en.wikipedia.org/wiki/Okapi_BM25)} with an modification of the
 * inverse document frequency term to avoid negative weights function for
 * calculating a relevanzfactor for documents.
 *
 * @author fmauz
 */
public class RelevanceCalculator implements Serializable {

    private StemmedWordGroup searchTerms;
    private final Logger logger;
    private List<String> originalSearchTerms;
    private final float k1 = 1.2f;
    private final float b = 0.75f;
    private SearchQueryStemmer searchQueryStemmer = new SearchQueryStemmer();

    public RelevanceCalculator() {
        this.searchTerms = new StemmedWordGroup();
        this.logger = LogManager.getLogger(this.getClass().getName());
        this.originalSearchTerms = new ArrayList<>();
    }

    public RelevanceCalculator(List<String> originalTerms) {
        this();
        originalSearchTerms = originalTerms;
        searchTerms = searchQueryStemmer.stemmQuery(String.join(" ", originalTerms));
    }

    /**
     * Calculates the relevance score for all documents
     *
     * @param totalDocuments Amount of total documents from the iteration. Will
     * be added to the total document number
     * @param averageDocLength
     * @param docsToUpdate documents which shall be updated
     * @return updated documents
     */
    public List<Document> calculateRelevanceFactors(
            int totalDocuments,
            double averageDocLength,
            List<Document> docsToUpdate) {

        for (Document d : docsToUpdate) {
            d.setRelevance(0);
            for (String word : searchTerms.getAllStemmedWords()) {
                double docsWithHit = getDocAmountWithHit(docsToUpdate, word);
                logger.info("DWH " + docsWithHit);
                logger.info("T " + totalDocuments);
                double idf = Math.log10(1 + (totalDocuments / docsWithHit));
                int fq = d.getTermFreqList().getFreqOf(word);
                logger.info("FQ " + fq);
                if (fq > 0) {
                    double nf = (double) (d.getWordCount() / averageDocLength);
                    logger.info("NF " + nf);
                    double rh = (fq * (k1 + 1)) / (fq + k1 * (1 - b + b * (nf)));
                    logger.info("RH " + rh);
                    d.setRelevance(d.getRelevance() + (idf * rh));
                }
            }

        }
        return docsToUpdate;
    }

    /**
     * Calculates the amount from found documents with the search terms.
     *
     * @param docsToUpdate
     * @return amount of docs with at least one search term hit
     */
    private double getDocAmountWithHit(List<Document> docsToUpdate, String term) {
        double docs = 0;
        for (Document d : docsToUpdate) {
            if (d.getTermFreqList().getFreqOf(term) > 0) {
                docs++;
            }
        }

        return docs;
    }

    public StemmedWordGroup getSearchTerms() {
        return searchTerms;
    }

    public void setSearchTerms(StemmedWordGroup searchTerms) {
        this.searchTerms = searchTerms;
    }

    public List<String> getOriginalSearchTerms() {
        return originalSearchTerms;
    }

    public void setOriginalSearchTerms(List<String> originalSearchTerms) {
        this.originalSearchTerms = originalSearchTerms;
    }

}
