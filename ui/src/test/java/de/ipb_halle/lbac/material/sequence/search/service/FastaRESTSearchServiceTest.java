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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

import javax.inject.Inject;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.ipb_halle.fasta_search_service.models.endpoint.FastaSearchRequest;
import de.ipb_halle.fasta_search_service.models.endpoint.FastaSearchResult;
import de.ipb_halle.fasta_search_service.models.fastaresult.FastaResult;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.webservice.RestApiHelper;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;

/**
 * @author flange
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class FastaRESTSearchServiceTest extends TestBase {
    private static final long serialVersionUID = 1L;
    private FastaRESTSearchService searchService;

    @ArquillianResource
    private URL url;

    @Inject
    private FastaSearchServiceEndpointMock mockEndpoint;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("FastaRESTSearchServiceTest.war")
                .addClass(FastaRESTSearchService.class)
                .addClass(FastaSearchServiceEndpointMock.class);
        return UserBeanDeployment.add(deployment);
    }

    @BeforeEach
    public void init() throws URISyntaxException {
        mockEndpoint.setBehaviour(null);

        String base = url.toURI().toString();
        String uri = base + RestApiHelper.getRestApiPath(FastaSearchServiceEndpointMock.class);
        searchService = new FastaRESTSearchService(uri);
    }

    @Test
    @RunAsClient
    public void test001_endpointReturns404() {
        mockEndpoint.setBehaviour(r -> {
            throw new NotFoundException();
        });
        Response response = searchService.execSearch(new FastaSearchRequest());
        assertEquals(Status.NOT_FOUND, Status.fromStatusCode(response.getStatus()));
    }

    @Test
    @RunAsClient
    public void test002_endpointReturns500() {
        mockEndpoint.setBehaviour(r -> {
            throw new InternalServerErrorException();
        });
        Response response = searchService.execSearch(new FastaSearchRequest());
        assertEquals(Status.INTERNAL_SERVER_ERROR, Status.fromStatusCode(response.getStatus()));
    }

    @Test
    @RunAsClient
    public void test003_wrongUri() {
        searchService = new FastaRESTSearchService("http://test.example");
        assertThrows(ProcessingException.class, () -> searchService.execSearch(new FastaSearchRequest()));
    }

    @Test
    @RunAsClient
    public void test004_endpointReturnsCorrectFastaSearchResult() {
        mockEndpoint.setBehaviour(r -> {
            FastaResult fastaResult = new FastaResult();
            fastaResult.setQueryAlignmentLine("abc");

            FastaSearchResult result = new FastaSearchResult();
            result.setProgramOutput("def");
            result.setResults(Arrays.asList(fastaResult, new FastaResult()));

            return Response.ok(result).build();
        });
        Response response = searchService.execSearch(new FastaSearchRequest());
        assertEquals(Status.OK, Status.fromStatusCode(response.getStatus()));
        FastaSearchResult searchResult = response.readEntity(FastaSearchResult.class);
        assertEquals(2, searchResult.getResults().size());
        assertEquals("abc", searchResult.getResults().get(0).getQueryAlignmentLine());
        assertEquals("def", searchResult.getProgramOutput());
    }
}