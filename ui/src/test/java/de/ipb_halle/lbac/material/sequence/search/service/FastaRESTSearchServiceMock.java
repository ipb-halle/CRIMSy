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
package de.ipb_halle.lbac.material.sequence.search.service;

import jakarta.ejb.Singleton;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.ResponseProcessingException;
import jakarta.ws.rs.core.Response;

import de.ipb_halle.fasta_search_service.models.endpoint.FastaSearchRequest;
import java.util.function.Function;

/**
 * @author fmauz
 */
@Singleton
public class FastaRESTSearchServiceMock extends FastaRESTSearchService {
    private Function<FastaSearchRequest, Response> behaviour;

    public void setBehaviour(Function<FastaSearchRequest, Response> behaviour) {
        this.behaviour = behaviour;
    }

    @Override
    public Response execSearch(FastaSearchRequest request) throws ResponseProcessingException, ProcessingException {
        return behaviour.apply(request);
    }
}
