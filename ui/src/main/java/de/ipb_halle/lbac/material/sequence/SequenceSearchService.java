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

import com.fasterxml.jackson.core.JsonProcessingException;
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
import de.ipb_halle.lbac.material.common.service.MaterialEntityGraphBuilder;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.search.SearchCategory;
import de.ipb_halle.lbac.search.lang.Condition;
import de.ipb_halle.lbac.search.lang.EntityGraph;
import de.ipb_halle.lbac.search.lang.SqlBuilder;
import de.ipb_halle.lbac.search.lang.Value;
import java.util.List;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.apache.commons.lang.exception.ExceptionUtils;

/**
 *
 * @author fmauz
 */
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@Stateless
public class SequenceSearchService implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String ERROR_SAVE_PARAMETER = "Could not save temporary parameter in database";
    private final String ERROR_REST_CALL_FAILED = "SequenceSearchService: REST call returns: %s %s";
    private final String ERROR_REST_CALL_EXEPTION = "SequenceSearchService: Error at sequence search: %s";
    @Inject
    NodeService nodeService;

    @Inject
    FastaRESTSearchService fastaService;

    @Inject
    MaterialService materialService;

    @Inject
    private SearchParameterService searchParameterService;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    protected EntityManager em;

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    public SearchResult searchSequences(SearchRequest request) {
        SearchResult result = new SearchResultImpl(nodeService.getLocalNode());

        UUID processID = generateProcessId();
        String sql;
        try {
            sql = createSqlString(request, processID);
        } catch (Exception e) {
            logger.error(ERROR_SAVE_PARAMETER);
            result.addErrorMessage(ERROR_SAVE_PARAMETER);
            return result;
        }

        FastaSearchRequest fastaRequest = createRestRequest(request, sql);

        try {
            Response response = fastaService.execSearch(fastaRequest);
            if (Status.OK == Status.fromStatusCode(response.getStatus())) {
                FastaSearchResult fastaResults = response.readEntity(FastaSearchResult.class);
                for (FastaResult singleResult : fastaResults.getResults()) {
                    int sequenceId = getSequenceIdFromResult(singleResult);
                    Sequence loadedSequence = (Sequence) materialService.loadMaterialById(sequenceId);
                    result.addResult(new SequenceAlignment(loadedSequence, singleResult));
                }
            } else {
                String error = response.readEntity(String.class);
                String errorMessage = String.format(ERROR_REST_CALL_FAILED,
                        response.getStatus(), error);
                logger.error(errorMessage);
                result.addErrorMessage(errorMessage);
            }

        } catch (Exception e) {
            logger.error(ExceptionUtils.getStackTrace(e));
            result.addErrorMessage(
                    String.format(ERROR_REST_CALL_EXEPTION,
                            e.getMessage()));
        }

        cleanParameter(processID);
        return result;
    }

    private int getSequenceIdFromResult(FastaResult result) {
        return Integer.parseInt(result.getSubjectSequenceName());
    }

    private UUID generateProcessId() {
        return UUID.randomUUID();
    }

    private void saveParameterInDataBase(UUID processId, List<Value> valueList) throws JsonProcessingException {
        List<String> fields = valueList.stream().map(v -> v.getArgumentKey()).collect(Collectors.toList());
        List<Object> values = valueList.stream().map(v -> v.getValue()).collect(Collectors.toList());
        searchParameterService.saveParameter(processId, fields, values);
    }

    private FastaSearchRequest createRestRequest(SearchRequest request, String sql) {

        FastaSearchRequest fastaRequest = new FastaSearchRequest();

        FastaSearchQuery query = new FastaSearchQuery();
        String queryString = request.getSearchValues().get(SearchCategory.SEQUENCE_QUERY_STRING).getValues().iterator().next();
        String libraryType = request.getSearchValues().get(SearchCategory.SEQUENCE_LIBRARY_TYPE).getValues().iterator().next();
        String seqType = request.getSearchValues().get(SearchCategory.SEQUENCE_QUERY_TYPE).getValues().iterator().next();
        String translationTable = request.getSearchValues().get(SearchCategory.SEQUENCE_TRANSLATION_TABLE).getValues().iterator().next();

        query.setQuerySequence(queryString);
        query.setLibrarySequenceType(libraryType);
        query.setQuerySequenceType(seqType);
        query.setTranslationTable(Integer.parseInt(translationTable));
        query.setMaxResults(request.getMaxResults());

        fastaRequest.setSearchQuery(query);
        fastaRequest.setDatabaseQueries(sql);
        fastaRequest.setDatabaseConnectionString(null);

        return fastaRequest;
    }

    private void cleanParameter(UUID processID) {
        searchParameterService.removeParameter(processID);
    }

    private String createSqlString(SearchRequest searchRequest, UUID processId) throws JsonProcessingException {
        MaterialEntityGraphBuilder graphBuilder = new MaterialEntityGraphBuilder();
        EntityGraph graph = graphBuilder.buildEntityGraph(true);

        SequenceSearchConditionBuilder builder = new SequenceSearchConditionBuilder(graph, "materials");
        Condition condition = builder.convertRequestToCondition(searchRequest, ACPermission.permREAD);
        SqlBuilder sqlBuilder = new SqlBuilder(graph);
        String sql = sqlBuilder.query(condition);

        saveParameterInDataBase(processId, sqlBuilder.getValueList());
        // This code block is only for testing purpose.
        // In the final version the substitute of the parameter must be 
        // done sql injection save, e.g by preparred statements     
        String parameterPattern = "(select cast(parameter->'%s'->>0 as %s) from temp_search_parameter where processid='%s') ";
        for (Value param : sqlBuilder.getValueList()) {
            String parameterSubQuery;

            if (param.getValue() instanceof String) {
                parameterSubQuery = String.format(parameterPattern, param.getArgumentKey(), "VARCHAR", processId.toString());
            } else {
                parameterSubQuery = String.format(parameterPattern, param.getArgumentKey(), "int", processId.toString());
            }
            sql = sql.replaceFirst(":" + param.getArgumentKey(), parameterSubQuery);

        }
        String firstSqlPart = "DO SELECT 1;";
        String secondSqlPart = sql.replace("\n", " ") + ";";

        secondSqlPart = secondSqlPart.replace("[", "(");
        secondSqlPart = secondSqlPart.replace("]", ")");
        secondSqlPart = secondSqlPart.replace("SELECT DISTINCT a.aclist_id, a.owner_id, a.ctime, a.materialtypeid, a.materialid, a.projectid, a.deactivated",
                "SELECT DISTINCT a.materialid,a_0_0_3.sequencestring");
        String thirdSqlPart = "SELECT #;";
        String fourthSqlPart = "SELECT sequencestring FROM sequences WHERE id=#;";
        String finalString = String.join("\n", firstSqlPart, secondSqlPart, thirdSqlPart, fourthSqlPart);

        return finalString;
    }

}
