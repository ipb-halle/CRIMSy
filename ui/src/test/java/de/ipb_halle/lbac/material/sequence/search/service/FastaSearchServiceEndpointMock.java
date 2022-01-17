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

import java.util.function.Function;

import javax.ejb.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import de.ipb_halle.fasta_search_service.models.endpoint.FastaSearchRequest;

/**
 * @author flange
 */
@Path("fasta-search-service")
@Singleton
public class FastaSearchServiceEndpointMock {
    private Function<FastaSearchRequest, Response> behaviour;

    public void setBehaviour(Function<FastaSearchRequest, Response> behaviour) {
        this.behaviour = behaviour;
    }

    @POST
    @Path("searchPostgres")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchPostgres(FastaSearchRequest request) {
        return behaviour.apply(request);
    }
}