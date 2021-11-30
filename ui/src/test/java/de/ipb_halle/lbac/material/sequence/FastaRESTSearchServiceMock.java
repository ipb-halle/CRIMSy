/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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

import javax.ejb.Stateless;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.Response;

import de.ipb_halle.fasta_search_service.models.endpoint.FastaSearchRequest;
import de.ipb_halle.fasta_search_service.models.endpoint.FastaSearchResult;
import de.ipb_halle.fasta_search_service.models.fastaresult.FastaResult;
import java.util.Arrays;

@Stateless
public class FastaRESTSearchServiceMock extends FastaRESTSearchService {

    public static int sequenceId;

    public Response execSearch(FastaSearchRequest request) throws ResponseProcessingException, ProcessingException {
        FastaResult fastaResult = new FastaResult();
        fastaResult.setQueryAlignmentLine("abc");
        fastaResult.setSubjectSequenceName(String.valueOf(sequenceId));

        FastaSearchResult result = new FastaSearchResult();
        result.setProgramOutput("def");
        result.setResults(Arrays.asList(fastaResult));

        return Response.ok(result).build();
    }
}