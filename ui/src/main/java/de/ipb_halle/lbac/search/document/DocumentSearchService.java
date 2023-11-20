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
package de.ipb_halle.lbac.search.document;

import de.ipb_halle.kx.file.FileObject;
import de.ipb_halle.kx.file.FileObjectEntity;
import de.ipb_halle.kx.termvector.TermFrequency;
import de.ipb_halle.kx.termvector.TermVectorService;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.file.FileSearchRequest;
import de.ipb_halle.lbac.search.SearchCategory;
import de.ipb_halle.lbac.search.SearchQueryStemmer;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.search.SearchResultImpl;
import de.ipb_halle.lbac.search.Searchable;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import de.ipb_halle.lbac.search.lang.SqlBuilder;
import de.ipb_halle.lbac.search.lang.Value;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.webclient.XmlSetWrapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.faces.application.ProjectStage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author fmauz
 */
@Stateless
public class DocumentSearchService {

    private final Logger logger = LogManager.getLogger(DocumentSearchService.class);

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    @Inject
    private NodeService nodeService;

    @Inject
    private CollectionService collectionService;

    @Inject
    private MemberService memberService;

    @Inject
    private TermVectorService termVectorService;

    private final int MAX_TERMS = Integer.MAX_VALUE;

    private boolean development = false;

    private String uriOfPublicColl;
    private SearchQueryStemmer searchQueryStemmer = new SearchQueryStemmer();
    private DocumentEntityGraphBuilder graphBuilder;

    @PostConstruct
    public void init() {
        graphBuilder = new DocumentEntityGraphBuilder();
        if (FacesContext.getCurrentInstance() != null && FacesContext.getCurrentInstance().getApplication().getProjectStage() == ProjectStage.Development) {
            development = true;
        }
    }

    protected String SQL_LOAD_DOCUMENTS = "SELECT DISTINCT "
            + "f.id, "
            + "f.name,"
            + "f.filename,"
            + "f.hash,"
            + "f.created,"
            + "f.user_id,"
            + "f.collection_id,"
            + "f.document_language "
            + "FROM files f "
            + "JOIN termvectors tv ON tv.file_id=f.id "
            + "WHERE f.collection_id=:collectionid "
            + "AND (:termvectorLength=0 OR tv.wordroot IN (:termvector))";

    protected String SQL_LOAD_DOCUMENT_LENGTH
            = "SELECT sum(termfrequency) "
            + "FROM termvectors "
            + "WHERE file_id=:file_id";

    protected String SQL_LOAD_DOCUMENT_COUNT
            = "SELECT count(*) "
            + "FROM files";

    public DocumentSearchState actionStartDocumentSearch(
            DocumentSearchState searchState,
            List<Collection> collsToSearchIn,
            String searchText,
            int limit,
            int offSet,
            String uriOfPublicColl) throws Exception {

        this.uriOfPublicColl = uriOfPublicColl;
        searchState.clearState();
        // fetches all documents of the collection and adds the total 
        // number of documents in the collection to the search state
        Set<String> normalizedTerms = searchQueryStemmer.stemmQuery(searchText);
        searchState.setSearchWords(normalizedTerms);
        for (Collection coll : collsToSearchIn) {
            if (coll.getNode().equals(nodeService.getLocalNode())) {
                FileSearchRequest searchRequest = new FileSearchRequest();
                searchRequest.holder = coll;
                searchRequest.wordsToSearchFor = normalizedTerms;
                Set<Document> foundDocs = loadDocuments(searchRequest, limit);
                searchState.getFoundDocuments().addAll(foundDocs);
                searchState.addToTotalDocs(foundDocs.size());
            }

        }

        List<Integer> docIds = new ArrayList<>();
        for (Document d : searchState.getFoundDocuments()) {
            docIds.add(d.getId());
        }

        TermOccurrence totalTerms = getTermOccurrence(
                docIds,
                normalizedTerms);

        for (Document d : searchState.getFoundDocuments()) {
            d.setWordCount(getLengthOfDocument(d.getId()));
            Map<String, Integer> words = totalTerms.getTermsOfDocument(d.getId());
            for (String s : words.keySet()) {
                d.getTermFreqList().getTermFreq().add(new TermFrequency(s, words.get(s)));
            }
        }
        return searchState;
    }

    /**
     * getTermOccurrence with aggregation, grouping and order result
     *
     * @param fileIds - document ids
     * @param searchTerms
     * @return - list (String word, Integer wordCount)
     * 
     */
    @SuppressWarnings("unchecked")
    public TermOccurrence getTermOccurrence(
            List<Integer> fileIds,
            Set<String> searchTerms) {
        TermOccurrence occurrence = new TermOccurrence();
        for (Integer fileId : fileIds) {
            List<TermFrequency> frequencies = termVectorService.getTermFrequencies(fileId, searchTerms);
            for (TermFrequency freq : frequencies) {
                occurrence.addOccurrence(fileId, freq.getTerm(), freq.getFrequency());
            }
        }
        return occurrence;
    }

    private int loadTotalCountOfFiles() {
        Query q = em.createNativeQuery(SQL_LOAD_DOCUMENT_COUNT);
        @SuppressWarnings("unchecked")
        List<Long> result = q.getResultList();
        return result.get(0).intValue();
    }

    public SearchResult loadDocuments(SearchRequest request) {
        List<Searchable> foundDocs = new ArrayList<>();
        SearchResult result = new SearchResultImpl(nodeService.getLocalNode());
        if (!hasWordRoots(request)) {
            return result;
        }
        SqlBuilder sqlBuilder = new SqlBuilder(createEntityGraph());
        DocumentSearchConditionBuilder conBuilder = new DocumentSearchConditionBuilder(createEntityGraph(), "files");
        conBuilder.convertRequestToCondition(request, ACPermission.permREAD);
        String sql = sqlBuilder.query(conBuilder.convertRequestToCondition(request, ACPermission.permREAD));
        Query q = em.createNativeQuery(sql, FileObjectEntity.class);
        for (Value param : sqlBuilder.getValueList()) {
            q.setParameter(param.getArgumentKey(), param.getValue());
        }
        q.setFirstResult(request.getFirstResult());
        q.setMaxResults(request.getMaxResults());
        @SuppressWarnings("unchecked")
        List<FileObjectEntity> entities = q.getResultList();
        for (FileObjectEntity entity : entities) {
            foundDocs.add(convertFileObjectToDocument(
                    new FileObject(entity)));

        }
        result.addResults(foundDocs);
        List<Integer> docIds = getDocIds(foundDocs);

        TermOccurrence totalTerms = getTermOccurrence(
                docIds,
                getWordRoots(request));
        calculateWordCountOfDocs(foundDocs, totalTerms);
        result.getDocumentStatistic().setTotalDocsInNode(loadTotalCountOfFiles());
        if (result.getDocumentStatistic().getTotalDocsInNode() > 0) {
            result.getDocumentStatistic().setAverageWordLength(getSumOfWordsOfAllDocs() / result.getDocumentStatistic().getTotalDocsInNode());
        }
        return result;
    }

    private boolean hasWordRoots(SearchRequest request) {
        return request.getSearchValues().get(SearchCategory.WORDROOT) != null
                && request.getSearchValues().get(SearchCategory.WORDROOT).getValues() != null
                && !request.getSearchValues().get(SearchCategory.WORDROOT).getValues().isEmpty();

    }

    private Set<String> getWordRoots(SearchRequest request) {
        XmlSetWrapper wrapper = request.getSearchValues().get(SearchCategory.WORDROOT);
        if (wrapper != null) {
            return wrapper.getValues();
        }
        return new HashSet<>();
    }

    private void calculateWordCountOfDocs(List<Searchable> foundDocs, TermOccurrence totalTerms) {
        for (Searchable searchable : foundDocs) {
            Document d = (Document) searchable;
            d.setWordCount(getLengthOfDocument(d.getId()));
            Map<String, Integer> words = totalTerms.getTermsOfDocument(d.getId());
            for (String s : words.keySet()) {
                d.getTermFreqList().getTermFreq().add(new TermFrequency(s, words.get(s)));
            }
        }
    }

    private List<Integer> getDocIds(List<Searchable> foundDocs) {
        List<Integer> ids = new ArrayList<>();
        for (Searchable s : foundDocs) {
            ids.add(((Document) s).getId());
        }
        return ids;
    }

    /**
     * Gets the total sum of all words over all collections and all documents
     *
     * @return
     */
    public long getSumOfWordsOfAllDocs() {
        return termVectorService.getSumOfAllWordsFromAllDocs();
    }

    private int getLengthOfDocument(int documentId) {
        return ((Long) em.createNativeQuery(SQL_LOAD_DOCUMENT_LENGTH)
                .setParameter("file_id", documentId).getResultList()
                .get(0)).intValue();
    }

    /**
     * Concatinates the terms with and AND for the solR querry.If there is only
     * one term the term is given back
     *
     * @param tagList
     * @return string for solr querry
     */
    public String getTagStringForSeachRequest(Set<String> tagList) {
        if (tagList == null || tagList.isEmpty()) {
            return "";
        }
        if (tagList.size() == 1) {
            return new ArrayList<>(tagList).get(0);
        }
        String back = "";
        for (String s : tagList) {
            back += " AND " + s;
        }

        back = back.substring(4, back.length());
        return back.trim();
    }

    public String getUriOfPublicCollection() {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put("name", "public");
        List<Collection> colls = collectionService.load(cmap);

        String restUri = null;
        if (colls != null && colls.size() > 0) {
            restUri = colls.get(0).getIndexPath();
        }
        return restUri;
    }

    public Set<Document> loadDocuments(FileSearchRequest request, int limit) {
        Set<Document> documents = new HashSet<>();
        @SuppressWarnings("unchecked")
        List<FileObjectEntity> results = this.em.createNativeQuery(SQL_LOAD_DOCUMENTS, FileObjectEntity.class)
                .setParameter("collectionid", request.holder.getId())
                .setParameter("termvectorLength", request.wordsToSearchFor.size())
                .setParameter("termvector", request.wordsToSearchFor)
                .getResultList();

        int count = 0;
        for (FileObjectEntity foe : results) {
            if (count < limit) {
                documents.add(
                        convertFileObjectToDocument(
                                new FileObject(foe))
                );
            }
        }
        return documents;
    }

    private Document convertFileObjectToDocument(FileObject fo) {
        Document d = new Document();
        d.setId(fo.getId());
        d.setCollectionId(fo.getCollectionId());
        d.setNodeId(nodeService.getLocalNodeId());
        d.setNode(nodeService.getLocalNode());
        d.setLanguage(fo.getDocumentLanguage());
        d.setCollection(collectionService.loadById(fo.getCollectionId()));
        d.setPath(fo.getFileLocation());
        d.setContentType(fo.getName().split("\\.")[fo.getName().split("\\.").length - 1]);
        d.setOriginalName(fo.getName());
        return d;

    }

    private EntityGraph createEntityGraph() {
        graphBuilder = new DocumentEntityGraphBuilder();
        return graphBuilder.buildEntityGraph(true);
    }

    // for test purposes
    public void setSearchQueryStemmer(SearchQueryStemmer stemmer) {
        searchQueryStemmer = stemmer;
    }
}
