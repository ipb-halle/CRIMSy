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

import de.ipb_halle.lbac.entity.Document;
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
    private boolean develop;
    private List<String> originalSearchTerms;

    public RelevanceCalculator() {
        this.searchTerms = new StemmedWordGroup();
        this.logger = LogManager.getLogger(this.getClass().getName());
        this.develop = false;
        this.originalSearchTerms = new ArrayList<>();
    }

    public RelevanceCalculator(List<String> originalTerms) {
        this();
        originalSearchTerms = originalTerms;
    }

    private final float k1 = 1.2f;
    private final float b = 0.75f;

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

        double docsWithHit = getDocAmountWithHit(docsToUpdate);
        double idf = Math.log10(1 + (totalDocuments / docsWithHit));

        if (develop) {
            infoStart(docsWithHit, idf, averageDocLength, totalDocuments);
        }

        for (Document d : docsToUpdate) {
            d.setRelevance(0);
            for (String word : searchTerms.getAllStemmedWords()) {
                int fq = d.getTermFreqList().getFreqOf(word);
                if (fq > 0) {
                    double nf = (double) (d.getWordCount() / averageDocLength);
                    double rh = (fq * (k1 + 1)) / (fq + k1 * (1 - b + b * (nf)));
                    d.setRelevance(d.getRelevance() + (idf * rh));
                    if (develop) {
                        infoDocument(d.getOriginalName(), word, nf, rh, d.getRelevance());
                    }
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
    private double getDocAmountWithHit(List<Document> docsToUpdate) {
        double docs = 0;
        for (Document d : docsToUpdate) {

            for (String s : searchTerms.getAllStemmedWords()) {
                int fq = d.getTermFreqList().getFreqOf(s);
                if (fq > 0) {
                    docs++;
                    break;
                }
            }
        }

        return docs;
    }

    public void setDevelop(boolean develop) {
        this.develop = develop;
    }

    private void infoStart(double docsWithHit, double idf, double averageDocLength, int totalDocuments) {
        logger.info("------");
        logger.info("Start Calculation of DocumentRelevance");
        logger.info("Docs with term " + docsWithHit);
        logger.info("IDF " + idf);
        logger.info("AWL " + averageDocLength);
        logger.info("TOTAL " + totalDocuments);
        logger.info("TERMS");
        logger.info("-- ORIGINAL");
        for (String o : originalSearchTerms) {
            logger.info(o);
        }
    }

    private void infoDocument(String doc, String term, double nf, double rh, double relevance) {
        logger.info("-- Term: " + doc + ":" + term);
        logger.info("---- NF: " + nf);
        logger.info("---- RH: " + rh);
        logger.info("---- RELEVANCE: " + relevance);
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
