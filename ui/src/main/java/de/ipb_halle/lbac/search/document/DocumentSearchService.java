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

import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.file.TermFrequency;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.file.FileObject;
import de.ipb_halle.lbac.file.FileObjectEntity;
import de.ipb_halle.lbac.file.FileSearchRequest;
import de.ipb_halle.lbac.search.SearchCategory;
import de.ipb_halle.lbac.search.SearchQueryStemmer;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.search.SearchResultImpl;
import de.ipb_halle.lbac.search.Searchable;
import de.ipb_halle.lbac.search.lang.Attribute;
import de.ipb_halle.lbac.search.lang.AttributeType;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.ConditionValueFetcher;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import de.ipb_halle.lbac.search.lang.Operator;
import de.ipb_halle.lbac.search.lang.SqlBuilder;
import de.ipb_halle.lbac.search.lang.Value;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.faces.application.ProjectStage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author fmauz
 */
@Stateless
public class DocumentSearchService {

    private final Logger LOGGER = LogManager.getLogger(DocumentSearchService.class);

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    @Inject
    private FileEntityService fileEntityService;

    @Inject
    private NodeService nodeService;

    @Inject
    private CollectionService collectionService;

    @Inject
    private MemberService memberService;

    @Inject
    private TermVectorEntityService termVectorEntityService;

    private final int MAX_TERMS = Integer.MAX_VALUE;

    private boolean development = false;
    private ConditionValueFetcher fetcher;

    private String uriOfPublicColl;
    protected SearchQueryStemmer searchQueryStemmer;
    private DocumentEntityGraphBuilder graphBuilder;

    @PostConstruct
    public void init() {
        graphBuilder = new DocumentEntityGraphBuilder();
        if (FacesContext.getCurrentInstance() != null && FacesContext.getCurrentInstance().getApplication().getProjectStage() == ProjectStage.Development) {
            development = true;
        }
        fetcher = new ConditionValueFetcher();

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
        searchQueryStemmer = new SearchQueryStemmer();
        // fetches all documents of the collection and adds the total 
        // number of documents in the collection to the search state
        StemmedWordGroup normalizedTerms = searchQueryStemmer.stemmQuery(searchText);
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

        TermOcurrence totalTerms = termVectorEntityService.getTermVectorForSearch(
                docIds,
                normalizedTerms.getAllStemmedWords());

        for (Document d : searchState.getFoundDocuments()) {
            d.setWordCount(getLengthOfDocument(d.getId()));
            Map<String, Integer> words = totalTerms.getTermsOfDocument(d.getId());
            for (String s : words.keySet()) {
                d.getTermFreqList().getTermFreq().add(new TermFrequency(s, words.get(s)));
            }
        }
        return searchState;
    }

    private int loadTotalCountOfFiles() {
        Query q = em.createNativeQuery(SQL_LOAD_DOCUMENT_COUNT);
        List<BigInteger> result = q.getResultList();
        return result.get(0).intValue();
    }

    public SearchResult loadDocuments(SearchRequest request) {
        List<Searchable> foundDocs = new ArrayList<>();
        SearchResult result = new SearchResultImpl(nodeService.getLocalNode());
        if (!hasWordRoots(request)) {
            return result;
        }
        SqlBuilder sqlBuilder = new SqlBuilder(createEntityGraph());
        DocumentSearchConditionBuilder conBuilder=new DocumentSearchConditionBuilder(createEntityGraph(),"documents");
        String sql = sqlBuilder.query(conBuilder.convertRequestToCondition(request, ACPermission.permREAD));
        Query q = em.createNativeQuery(sql, FileObjectEntity.class);
        for (Value param : sqlBuilder.getValueList()) {
            q.setParameter(param.getArgumentKey(), param.getValue());
        }
        q.setFirstResult(request.getFirstResult());
        q.setMaxResults(request.getMaxResults());
        List<FileObjectEntity> entities = q.getResultList();
        for (FileObjectEntity entity : entities) {
            foundDocs.add(convertFileObjectToDocument(
                    new FileObject(
                            entity,
                            collectionService.loadById(entity.getCollection()),
                            memberService.loadUserById(entity.getUser()))));

        }
        result.addResults(foundDocs);
        List<Integer> docIds = getDocIds(foundDocs);

        TermOcurrence totalTerms = termVectorEntityService.getTermVectorForSearch(
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
        boolean hasRoot = false;
        for (SearchCategory c : request.getSearchValues().keySet()) {
            if (c == SearchCategory.WORDROOT) {
                hasRoot = true;
            }
        }
        return hasRoot;

    }

    private Set<String> getWordRoots(SearchRequest request) {
        for (SearchCategory c : request.getSearchValues().keySet()) {
            if (c == SearchCategory.WORDROOT) {
                return request.getSearchValues().get(c);
            }
        }
        return new HashSet<>();
    }

    private void calculateWordCountOfDocs(List<Searchable> foundDocs, TermOcurrence totalTerms) {
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
        return termVectorEntityService.getSumOfAllWordsFromAllDocs();
    }

    private int getLengthOfDocument(int documentId) {
        return ((BigInteger) em.createNativeQuery(SQL_LOAD_DOCUMENT_LENGTH)
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
        List<FileObjectEntity> results = this.em.createNativeQuery(SQL_LOAD_DOCUMENTS, FileObjectEntity.class)
                .setParameter("collectionid", request.holder.getId())
                .setParameter("termvectorLength", request.wordsToSearchFor.getAllStemmedWords().size())
                .setParameter("termvector", request.wordsToSearchFor.getAllStemmedWords())
                .getResultList();

        int count = 0;
        for (FileObjectEntity foe : results) {
            if (count < limit) {
                documents.add(
                        convertFileObjectToDocument(
                                new FileObject(
                                        foe,
                                        collectionService.loadById(foe.getCollection()),
                                        memberService.loadUserById(foe.getUser())))
                );
            }
        }
        return documents;
    }

    private Document convertFileObjectToDocument(FileObject fo) {
        Document d = new Document();
        d.setId(fo.getId());
        d.setCollectionId(fo.getCollection().getId());
        d.setNodeId(nodeService.getLocalNodeId());
        d.setNode(nodeService.getLocalNode());
        d.setLanguage(fo.getDocument_language());
        d.setCollection((Collection) fo.getCollection());
        d.setPath(fo.getFileLocation());
        d.setContentType(fo.getName().split("\\.")[fo.getName().split("\\.").length - 1]);
        d.setOriginalName(fo.getName());
        return d;

    }

    public String createSqlReplaceString(Map<String, Set<String>> stemmedWords) {
        if (stemmedWords.isEmpty()) {
            return "";
        }
        List<String> subClauses = new ArrayList<>();
        for (String word : stemmedWords.keySet()) {
            List<String> stemmedWordsWithQuotationMark = new ArrayList<>();
            for (String w : stemmedWords.get(word)) {
                stemmedWordsWithQuotationMark.add("'" + w + "'");
            }

            subClauses.add(
                    String.format(" tv.wordroot IN (%s) ",
                            String.join(",", stemmedWordsWithQuotationMark)));
        }

        return " AND (" + String.join(" OR ", subClauses) + ")";
    }

    private EntityGraph createEntityGraph() {
        graphBuilder = new DocumentEntityGraphBuilder();
        return graphBuilder.buildEntityGraph(true);
    }
}
