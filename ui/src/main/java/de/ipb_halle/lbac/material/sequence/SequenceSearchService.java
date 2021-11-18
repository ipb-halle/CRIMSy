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
import de.ipb_halle.lbac.material.common.service.MaterialService;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;

/**
 *
 * @author fmauz
 */
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@Stateless
public class SequenceSearchService implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    NodeService nodeService;

    @Inject
    FastaRESTSearchService fastaService;

    @Inject
    MaterialService materialService;

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    public SearchResult searchSequences(SearchRequest request) {
        SearchResult result = new SearchResultImpl(nodeService.getLocalNode());
        String sql = createSqlStatement();
        UUID processID = generateProcessId();
        saveParameterInDataBase();

        FastaSearchRequest fastaRequest = createRestRequest();
        try {
            Response response = fastaService.execSearch(fastaRequest);
            FastaSearchResult fastaResults = response.readEntity(FastaSearchResult.class);
            for (FastaResult singleResult : fastaResults.getResults()) {
                int sequenceId = getSequenceIdFromResult(singleResult);
                Sequence loadedSequence = (Sequence) materialService.loadMaterialById(sequenceId);
            }

        } catch (Exception e) {

        }

        //Hier service für request
        cleanParameter();
        return result;
    }

    private int getSequenceIdFromResult(FastaResult result) {
        return Integer.parseInt(result.getSubjectSequenceDescription());
    }

    private String createSqlStatement() {
        return "";
    }

    private UUID generateProcessId() {
        return UUID.randomUUID();
    }

    private void saveParameterInDataBase() {

    }

    private FastaSearchRequest createRestRequest() {
        return null;
    }

    private void cleanParameter() {

    }

}
