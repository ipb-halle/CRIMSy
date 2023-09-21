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
package de.ipb_halle.kx.termvector;

import de.ipb_halle.kx.file.FileObject;
import de.ipb_halle.kx.file.FileObjectService;

import java.io.Serializable;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class TermVectorService implements Serializable {

    protected final String SQL_TOTAL_WORD_COUNT
            = "SELECT SUM(t.termfrequency) FROM termvectors t";

    protected final String SQL_DELETE_UNSTEMMED_WORDS_BY_ID
            = "DELETE FROM unstemmed_words tv WHERE tv.file_id in(:fileId)";

    protected final String SQL_DELETE_TERMVECTOR_BY_ID
            = "DELETE FROM termvectors tv WHERE tv.file_id in(:fileId)";

    protected final String SQL_INSERT_UNSTEMMED_WORD
            = "INSERT INTO unstemmed_words (unstemmed_word, file_id, stemmed_word)"
            + " VALUES (:unstemmed_word,:fileId,:stemmed_word)";

    protected final String SQL_DELETE_ALL_UNSTEMMED_WORDS
            = "DELETE FROM unstemmed_words";

    protected final String SQL_DELETE_ALL_TERMVECTORS
            = "DELETE from termvectors";

    private final String SQL_DELETE_TERMVECTORS_OF_COLLECTION
            = "DELETE from termvectors AS tv ... "
            + " WHERE fo.collection_id=:collectionId";

    private final String SQL_DELETE_ORIGINAL_WORDS_OF_COLLECTION
            = "DELETE FROM unstemmed_words AS stem ... ";
            + " WHERE fo.collection_id=:collectionId";


    @PersistenceContext(name = "de.ipb_halle.lbac")
    public EntityManager em;

    private Logger logger;

    /**
     * init. check injection points
     */
    @PostConstruct
    private void init() {
        logger =  LoggerFactory.getLogger(this.getClass());
        if (em == null) {
            logger.error("Injection Entitymanager failed.");
        }
    }

    /**
     * getTermvector with aggregation, grouping and order result. Uses the 
     * TermVectorEntity.SQL_LOAD_TERMFREQUENCIES native query.
     *
     * @param fileId - document ids
     * @param maxResult - return top max. rows for result set
     * @return list of TermFrequency objects, ordered by descending frequency 
     */
    @SuppressWarnings("unchecked")
    public List<TermFrequency> getTermVector(List<Integer> fileIds, Integer maxResult) {
        try {
            if (fileIds.isEmpty()) {
                return new ArrayList<>();
            }

            return this.em.createNamedQuery(
                    TermVectorEntity.SQL_LOAD_TERMFREQUENCIES)
                    .setMaxResults(maxResult)
                    .setParameter("fileIds", fileIds)
                    .getResultList();

        } catch (Exception e) {
            logger.error("getTermVector() caught an Exception: ", (Throwable) e);
            return new ArrayList<>();
        }
    }

   /**
     * Get term frequencies with result ordering for a specific file and 
     * a set of words. Uses the TermVectorEntity.SQL_LOAD_TERMFREQUENCY_BY_WORD native query.
     *
     * @param fileId document ids
     * @param words set of words, the document should match
     * @return list of TermFrequency objects, ordered by descending frequency 
     */
    @SuppressWarnings("unchecked")
    public List<TermFrequency> getTermFrequencies(Integer fileId, Set<String> words) {
        try {
            return this.em.createNamedQuery(
                    TermVectorEntity.SQL_LOAD_TERMFREQUENCY_BY_WORD)
                    .setParameter("fileId", fileId)
                    .setParameter("words", words)
                    .getResultList();

        } catch (Exception e) {
            logger.error("getTermFrequencies() caught an Exception: ", (Throwable) e);
            return new ArrayList<>();
        }
    }


    /**
     * Sums all words from all documents of the local node
     *
     * @return
     */
    public int getSumOfAllWordsFromAllDocs() {
        java.math.BigInteger sum = (java.math.BigInteger) this.em.createNativeQuery(SQL_TOTAL_WORD_COUNT).getSingleResult();
        if (sum == null) {
            return 0;
        } else {
            return sum.intValue();
        }
    }

    /**
     * Delete all TermVectors for a given collectionId
     * NOTE: Future implementations should not have references to
     * Collections
     */
    @Deprecated
    public void deleteTermVectorsOfCollection(Integer collectionId) {
        this.em.createNativeQuery(SQL_DELETE_TERMVECTORS_OF_COLLECTION)
            .setParameter("collectionId", collectionId)
            .executeUpdate();
        this.em.createNativeQuery(SQL_DELETE_ORIGINAL_WORDS_OF_COLLECTION)
            .setParameter("collectionId", collectionId)
            .executeUpdate();
    }

    public void deleteTermVector(FileObject fileObject) {

        this.em.createNativeQuery(SQL_DELETE_UNSTEMMED_WORDS_BY_ID)
            .setParameter("fileId", fileObject.getId())
            .executeUpdate();

        this.em.createNativeQuery(SQL_DELETE_TERMVECTOR_BY_ID)
            .setParameter("fileId", fileObject.getId())
            .executeUpdate();

        this.em.flush();
    }

    /**
     * Saves the list of unstemmed words for a stemmed word of a file
     *
     * @param wordOrigins
     * @param fileId 
     */
    public void saveUnstemmedWordsOfDocument(
            List<StemmedWordOrigin> wordOrigins,
            Integer fileId) {
        for (StemmedWordOrigin swo : wordOrigins) {
            this.em.createNativeQuery(SQL_INSERT_UNSTEMMED_WORD)
                    .setParameter("unstemmed_word", swo.getOriginalWord())
                    .setParameter("fileId", fileId)
                    .setParameter("stemmed_word", swo.getStemmedWord())
                    .executeUpdate();
            this.em.flush();
        }
    }

    public void saveTermVectors(List<TermVector> vectors) {
        try {
            for(TermVector tv : vectors) {
                this.em.merge(tv.createEntity());
            }
        } catch (Exception e) {
            logger.error("saveTermVectors() caught an exception: ", (Throwable) e);
        }
    }

    /**
     * Loads the list of unstemmed words for a document and a stemmed word.
     *
     * @param fileId the file Id
     * @param wordRoot a single wordRoot (= stem of a word)
     * @return
     */
    public List<StemmedWordOrigin> loadUnstemmedWordsOfDocument(
            Integer fileId,
            String wordRoot) {

        return this.em.createNamedQuery(TermVectorEntity.SQL_LOAD_UNSTEMMED_WORD)
                .setParameter("fileId", fileId)
                .setParameter("wordroot", wordRoot)
                .getResultList();

    }

    /**
     * Loads the list of unstemmed words for a document and a stemmed word.
     *
     * @param fileId the file Id
     * @param wordRoot a list of wordRoots (= stems of words)
     * @return
     */
    public List<StemmedWordOrigin> loadUnstemmedWordsOfDocument(
            Integer fileId,
            List<String> wordRoot) {

        return this.em.createNamedQuery(TermVectorEntity.SQL_LOAD_UNSTEMMED_WORDS)
                .setParameter("fileId", fileId)
                .setParameter("wordroot", wordRoot)
                .getResultList();
    }

    /**
     * Deletes all termvectors and all unstemmed words from database.
     */
    public void deleteTermVectors() {
        this.em.createNativeQuery(SQL_DELETE_ALL_UNSTEMMED_WORDS).executeUpdate();
        this.em.createNativeQuery(SQL_DELETE_ALL_TERMVECTORS).executeUpdate();
        this.em.flush();
    }
}
