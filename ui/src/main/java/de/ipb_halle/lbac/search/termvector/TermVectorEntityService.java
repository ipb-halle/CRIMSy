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
package de.ipb_halle.lbac.search.termvector;

import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.file.FileObject;
import de.ipb_halle.lbac.file.TermVector;
import de.ipb_halle.lbac.file.TermVectorEntity;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.file.StemmedWordOrigin;
import de.ipb_halle.lbac.message.TermVectorMessage;
import de.ipb_halle.lbac.collections.CollectionService;

import java.io.Serializable;
import java.util.ArrayList;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Stateless
public class TermVectorEntityService implements Serializable {

    @PersistenceContext(name = "de.ipb_halle.lbac")
    public EntityManager em;

    private Logger logger;

    @Inject
    private CollectionService collectionService;

    @Inject
    private FileEntityService fileEntityService;

    protected final String SQL_TERMVECTORS_BY_ID
            = "select wordroot,file_id,termfrequency"
            + " from termvectors"
            + " where file_id in (:id)"
            + "order by termfrequency desc";

    protected final String SQL_TERMVECTORS_BY_ID_AND_WORDS
            = "select wordroot,file_id,termfrequency"
            + " from termvectors"
            + " where file_id in (:id)"
            + " and wordroot in(:words)"
            + "order by termfrequency desc";

    protected final String SQL_TOTAL_WORD_COUNT
            = "select sum(t.termfrequency) from termvectors t";

    protected final String SQL_DELETE_UNSTEMMED_WORDS_BY_ID
            = "delete from unstemmed_words tv where tv.file_id in(:fileId)";

    protected final String SQL_DELETE_TERMVECTOR_BY_ID
            = "delete from termvectors tv where tv.file_id in(:fileId)";

    protected final String SQL_INSERT_UNSTEMMED_WORD
            = "insert into  unstemmed_words (unstemmed_word, file_id, wordroot) values"
            + "(:unstemmed_word,:did,:stemmed_word)";

    protected final String SQL_LOAD_UNSTEMMED_WORD
            = "select unstemmed_word"
            + " from unstemmed_words"
            + " where file_id in (:id)"
            + "AND wordroot=:wr";

    protected final String SQL_LOAD_UNSTEMMED_WORDS
            = "select wordroot,unstemmed_word"
            + " from unstemmed_words"
            + " where file_id in (:id)"
            + "AND wordroot in (:wr)";

    protected final String SQL_DELETE_ALL_UNSTEMMED_WORDS
            = "delete from unstemmed_words";

    protected final String SQL_DELETE_ALL_TERMVECTORS
            = "delete from termvectors";

    /**
     * init. check injection points
     */
    @PostConstruct
    public void FileEntityServiceInit() {
        logger = LogManager.getLogger(this.getClass());
        if (em == null) {
            logger.error("Injection failed for Entitimanager em.");
        }
    }

    /**
     * getTermvector with aggregation, grouping and order result
     *
     *
     * @param docIds - document ids
     * @param maxResult - return top max. rows for result set
     * @return - list (String word, Integer wordCount)
     *
     * hibernate except UUIDs only, can't cast Strings to UUIDs
     */
    @SuppressWarnings("unchecked")
    public Map<String, Integer> getTermVector(List<Integer> docIds, Integer maxResult) {
        try {
            if (docIds.isEmpty()) {
                return new HashMap<>();
            }
            List<TermVector> tvList = loadTermvectorsForDocuments(docIds, maxResult);

            Map<String, Integer> results = mergeTermvectors(tvList);

            ArrayList<Integer> list = sortValues(results);

            return getMostFrequentTerms(list, maxResult, results);

        } catch (Exception e) {
            logger.error(e.getMessage());
            for (StackTraceElement el : e.getStackTrace()) {
                logger.error(el);
            }
            return new HashMap<>();
        }
    }

    /**
     * getTermvector with aggregation, grouping and order result
     *
     *
     * @param docIds - document ids
     * @param searchTerms
     * @return - list (String word, Integer wordCount)
     *
     * hibernate except UUIDs only, can't cast Strings to UUIDs
     */
    @SuppressWarnings("unchecked")
    public Map<Integer, Map<String, Integer>> getTermVectorForSearch(
            List<Integer> docIds,
            Set<String> searchTerms) {
        Map<Integer, Map<String, Integer>> back = new HashMap<>();

        try {
            if (docIds.isEmpty()) {
                return new HashMap<>();
            }

            List<TermVector> list = new ArrayList<>();
            List<TermVectorEntity> entities = this.em.createNativeQuery(
                    SQL_TERMVECTORS_BY_ID_AND_WORDS, TermVectorEntity.class)
                    .setParameter("id", docIds)
                    .setParameter("words", searchTerms)
                    .getResultList();
            for (TermVectorEntity entity : entities) {
                list.add(new TermVector(entity));
            }

            for (TermVector tv : list) {
                if (!back.containsKey(tv.getFileId())) {
                    back.put(tv.getFileId(), new HashMap<>());
                }
                Map<String, Integer> tempMap = back.get(tv.getFileId());
                tempMap.put(tv.getWordRoot(), tv.getTermFrequency());
                back.put(tv.getFileId(), tempMap);
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            for (StackTraceElement el : e.getStackTrace()) {
                logger.error(el);
            }
            return new HashMap<>();
        }
        return back;
    }

    /**
     * wrapper for getTermVector, set maxResult = 50 (see above)
     *
     * @param termVectorMessage - contains parameters
     * @return - map (String word, Integer wordCount), maxResult = 50
     */
    public Map<String, Integer> getTermVector(TermVectorMessage termVectorMessage) {
        return getTermVector(termVectorMessage.getDocIds(), termVectorMessage.getMaxResult());
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
     * Deletes the termvectors and unstemmed words for all documents in a
     * collection
     *
     * @param c
     */
    public void deleteTermVectorOfCollection(Collection c) {

        List<FileObject> files = fileEntityService.getAllFilesInCollection(c);

        for (FileObject f : files) {

            this.em.createNativeQuery(SQL_DELETE_UNSTEMMED_WORDS_BY_ID)
                    .setParameter("fileId", f.getId())
                    .executeUpdate();
            this.em.flush();
        }

        for (FileObject f : files) {
            this.em.createNativeQuery(SQL_DELETE_TERMVECTOR_BY_ID)
                    .setParameter("fileId", f.getId())
                    .executeUpdate();
            this.em.flush();
        }
    }

    /**
     * Saves the list of unstemmed words for a stemmed word of a document
     *
     * @param wordOrigins
     * @param documentId
     */
    public void saveUnstemmedWordsOfDocument(
            List<StemmedWordOrigin> wordOrigins,
            Integer documentId) {
        for (StemmedWordOrigin swo : wordOrigins) {
            for (String s : swo.getOriginalWord()) {
                this.em.createNativeQuery(SQL_INSERT_UNSTEMMED_WORD)
                        .setParameter("unstemmed_word", s)
                        .setParameter("did", documentId)
                        .setParameter("stemmed_word", swo.getStemmedWord())
                        .executeUpdate();
                this.em.flush();
            }
        }
    }

    /**
     * Loads the list of unstemmed words for a document and a stemmed word.
     *
     * @param documentId
     * @param wordRoot
     * @return
     */
    public List<StemmedWordOrigin> loadUnstemmedWordsOfDocument(
            Integer documentId,
            String wordRoot) {
        List<StemmedWordOrigin> stemmedWords = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<String> words = this.em.createNativeQuery(
                SQL_LOAD_UNSTEMMED_WORD)
                .setParameter("id", documentId)
                .setParameter("wr", wordRoot)
                .getResultList();
        StemmedWordOrigin swo = new StemmedWordOrigin();
        stemmedWords.add(swo);
        for (String o : words) {
            swo.getOriginalWord().add(o);
            swo.setStemmedWord(wordRoot);

        }
        return stemmedWords;
    }

    /**
     * Loads the list of unstemmed words for a document and a stemmed word.
     *
     * @param documentId
     * @param wordRoot
     * @return
     */
    public List<StemmedWordOrigin> loadUnstemmedWordsOfDocument(
            Integer documentId,
            List<String> wordRoot) {
        List<StemmedWordOrigin> stemmedWords = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Object[]> words = this.em.createNativeQuery(
                SQL_LOAD_UNSTEMMED_WORDS)
                .setParameter("id", documentId)
                .setParameter("wr", wordRoot)
                .getResultList();

        for (Object[] o : words) {
            StemmedWordOrigin swo = new StemmedWordOrigin();
            stemmedWords.add(swo);
            swo.getOriginalWord().add((String) o[1]);
            swo.setStemmedWord((String) o[0]);

        }
        return stemmedWords;
    }

    /**
     * Deletes all termvectors and all unstemmed words from database.
     */
    public void deleteTermVectors() {
        this.em.createNativeQuery(SQL_DELETE_ALL_UNSTEMMED_WORDS).executeUpdate();
        this.em.createNativeQuery(SQL_DELETE_ALL_TERMVECTORS).executeUpdate();
        this.em.flush();
    }

    /**
     * Loads the termvectors for the given list of documents. The length of the
     * results is limited by maxresults
     *
     * @param docIds
     * @param maxResult
     * @return
     */
    private List<TermVector> loadTermvectorsForDocuments(
            List<Integer> docIds,
            Integer maxResult) {
        List<TermVector> tvList = new ArrayList<>();
        for (Integer id : docIds) {
            // Loads the termvector for a documentid and limits 
            // the length by the given maximum result length
            @SuppressWarnings("unchecked")
            List<TermVectorEntity> entities = this.em.createNativeQuery(
                    SQL_TERMVECTORS_BY_ID,
                    TermVectorEntity.class)
                    .setParameter("id", id)
                    .getResultList();

            List<TermVector> list = new ArrayList<>();
            for (TermVectorEntity entity : entities) {
                list.add(new TermVector(entity));
            }
            tvList.addAll(list.subList(0, Math.min(maxResult, list.size())));
        }
        return tvList;
    }

    /**
     * Merges the termvectors from different documents to one hashmap
     *
     * @param tvList
     * @return
     */
    private Map<String, Integer> mergeTermvectors(List<TermVector> tvList) {
        Map<String, Integer> results = new HashMap<>();
        for (TermVector tve : tvList) {
            if (results.containsKey(tve.getWordRoot())) {
                results.put(tve.getWordRoot(), results.get(tve.getWordRoot()) + tve.getTermFrequency());
            } else {
                results.put(tve.getWordRoot(), tve.getTermFrequency());
            }
        }
        return results;
    }

    /**
     * Returns the sorted (desc) list of the values of the hashmap
     *
     * @param results
     * @return
     */
    private ArrayList<Integer> sortValues(Map<String, Integer> results) {
        java.util.Collection col = results.values();
        @SuppressWarnings("unchecked")
        ArrayList<Integer> list = new ArrayList<>(col);
        Collections.sort(list);
        Collections.reverse(list);
        return list;
    }

    /**
     * get the most frequent termvectors limit by maxResults. If the list is
     * shorter than max results the complete list is given back
     *
     * @param list
     * @param maxResult
     * @param results
     * @return
     */
    private Map<String, Integer> getMostFrequentTerms(
            ArrayList<Integer> list,
            Integer maxResult,
            Map<String, Integer> results) {
        if (list.size() < maxResult) {
            return results;
        }
        int treshhold = list.get(maxResult - 1);
        Map<String, Integer> filteredResults = new HashMap<>();
        int addedWords = 0;
        for (String s : results.keySet()) {
            if (results.get(s) >= treshhold) {
                addedWords++;
                filteredResults.put(s, results.get(s));
            }
            if (addedWords >= maxResult) {
                return filteredResults;
            }
        }
        return filteredResults;
    }

}
