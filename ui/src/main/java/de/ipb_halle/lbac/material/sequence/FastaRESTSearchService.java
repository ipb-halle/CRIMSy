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
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.ipb_halle.fasta_search_service.models.endpoint.FastaSearchRequest;

/**
 * Service that handles calls to the fasta-search-service REST API.
 * 
 * @author flange
 */
/*
 * It seems that no connection pooling is used for the REST client.
 * How could a strategy for reusing a client instance look like?
 * - client is a field of this class: This is safe at first glance, because an EJB 
 *   instance can only be entered by a single thread.
 * - build client in @PostConstruct
 * - close client in @PreDestroy
 *
 * Considerations:
 * - What happens when there are several Response objects around and for one of 
 *   them Response.close() is called?
 */
@Stateless
public class FastaRESTSearchService {
    private String uri = "http://fasta:8080/fasta-search-service";
    private static final String ENDPOINT = "searchPostgres";

    public FastaRESTSearchService() {
    }

    FastaRESTSearchService(String uri) {
        this.uri = uri;
    }

    /**
     * Executes a REST API call to the fasta-search-service.
     * 
     * @param request request object
     * @return {@link Response} from the JAX-RS client, needs to be closed by the
     *         caller if necessary
     * @throws ResponseProcessingException in case processing of a received HTTP
     *                                     response fails
     * @throws ProcessingException         in case the request processing or
     *                                     subsequent I/O operation fails
     */
    public Response execSearch(FastaSearchRequest request) throws ResponseProcessingException, ProcessingException {
        Client client = ClientBuilder.newClient();
        return client.target(uri).path(ENDPOINT).request().accept(MediaType.APPLICATION_JSON)
                .post(Entity.json(request));
    }
}