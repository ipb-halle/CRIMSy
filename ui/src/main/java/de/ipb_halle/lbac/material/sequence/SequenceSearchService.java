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
package de.ipb_halle.lbac.material.sequence;

import de.ipb_halle.fasta_search_service.models.endpoint.FastaSearchQuery;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.search.SearchResultImpl;
import de.ipb_halle.lbac.service.NodeService;
import java.io.Serializable;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.ipb_halle.fasta_search_service.models.endpoint.FastaSearchRequest;
import de.ipb_halle.fasta_search_service.models.endpoint.FastaSearchResult;
import de.ipb_halle.fasta_search_service.models.fastaresult.FastaResult;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.search.MaterialSearchConditionBuilder;
import de.ipb_halle.lbac.material.common.search.MaterialSearchRequestBuilder;
import de.ipb_halle.lbac.material.common.service.MaterialEntityGraphBuilder;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.search.SearchCategory;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import de.ipb_halle.lbac.search.lang.SqlBuilder;
import de.ipb_halle.lbac.search.lang.Value;
import javax.ws.rs.core.Response;

/**
 *
 * @author fmauz
 */
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@Stateless
public class SequenceSearchService implements Serializable {

    private String dbUser;
    private String dbpass;
    private static final long serialVersionUID = 1L;

    @Inject
    NodeService nodeService;

    @Inject
    FastaRESTSearchService fastaService;

    @Inject
    MaterialService materialService;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    protected EntityManager em;

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    public SearchResult searchSequences(SearchRequest request) {
        SearchResult result = new SearchResultImpl(nodeService.getLocalNode());

        UUID processID = generateProcessId();
        String sql = createSqlString(request, processID);
//        saveParameterInDataBase();

        FastaSearchRequest fastaRequest = createRestRequest(request, sql);

        try {
            Response response = fastaService.execSearch(fastaRequest);
            FastaSearchResult fastaResults = response.readEntity(FastaSearchResult.class);
            for (FastaResult singleResult : fastaResults.getResults()) {
                int sequenceId = getSequenceIdFromResult(singleResult);
                Sequence loadedSequence = (Sequence) materialService.loadMaterialById(sequenceId);
                result.addResult(new SequenceAlignment(loadedSequence, singleResult));
            }

        } catch (Exception e) {
            int i=0;
        }
//
//        //Hier service für request
//        cleanParameter();
        return result;
    }

    private int getSequenceIdFromResult(FastaResult result) {
        return Integer.parseInt(result.getSubjectSequenceName());
    }

    private UUID generateProcessId() {
        return UUID.randomUUID();
    }

    private void saveParameterInDataBase() {

    }

    private FastaSearchRequest createRestRequest(SearchRequest request, String sql) {

        FastaSearchRequest fastaRequest = new FastaSearchRequest();

        FastaSearchQuery query = new FastaSearchQuery();
        String queryString = request.getSearchValues().get(SearchCategory.SEQUENCE_STRING).getValues().iterator().next();
        String libraryType = request.getSearchValues().get(SearchCategory.SEQUENCE_LIBRARY_TYPE).getValues().iterator().next();
        String seqType = request.getSearchValues().get(SearchCategory.SEQUENCE_TYPE).getValues().iterator().next();
        String translationTable = request.getSearchValues().get(SearchCategory.SEQUENCE_TRANSLATION_TABLE).getValues().iterator().next();
        query.setQuerySequence(queryString);
        query.setLibrarySequenceType(libraryType);
        query.setQuerySequenceType(seqType);
        query.setTranslationTable(Integer.parseInt(translationTable));
        query.setMaxResults(request.getMaxResults());
        fastaRequest.setSearchQuery(query);
        fastaRequest.setDatabaseQueries(sql);

        // Are the connection strings still relevant?
        return fastaRequest;
    }

    private void cleanParameter() {

    }

    public String createSqlString(SearchRequest searchRequest, UUID processId) {
        MaterialEntityGraphBuilder graphBuilder = new MaterialEntityGraphBuilder();
        EntityGraph graph = graphBuilder.buildEntityGraph(true);

        SequenceSearchConditionBuilder builder = new SequenceSearchConditionBuilder(graph, "materials");
        Condition condition = builder.convertRequestToCondition(searchRequest, ACPermission.permREAD);
        SqlBuilder sqlBuilder = new SqlBuilder(graph);
        String sql = sqlBuilder.query(condition);
        // This code block is only for testing purpose.
        // In the final version the substitute of the parameter must be 
        // done sql injection save, e.g by preparred statements
        for (Value param : sqlBuilder.getValueList()) {
            if (param.getValue() instanceof String) {
                sql = sql.replace(":" + param.getArgumentKey(), "'" + String.valueOf(param.getValue()) + "'");
            } else {
                sql = sql.replace(":" + param.getArgumentKey(), String.valueOf(param.getValue()));
            }

        }

        return sql;
    }

}