/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2023 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.kx.service;

import de.ipb_halle.kx.StemmingRequest;
import de.ipb_halle.kx.StemmingResponse;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


/**
 *
 * @author fblocal
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class QueryWebServiceTest {

    private final static String ENDPOINT = "/QueryWebServiceTest/kx/query";

    @ArquillianResource
    URL baseURL;

    @Inject
    private QueryWebService queryWebService;
    
    @Deployment
    public static WebArchive createDeployment() {
        System.setProperty("log4j.configurationFile", "log4j2-test.xml");

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "QueryWebServiceTest.war")
                .addClass(QueryWebService.class)
                .addClass(KxApplication.class);
        return archive;
    }
    
    private StemmingResponse doRequest(StemmingRequest request) throws Exception {
        try (Client client = ClientBuilder.newClient()) {
            URL url = new URL(baseURL, ENDPOINT);
            System.out.println(url.toExternalForm());
            Response response = client.target(url.toURI())
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.entity(request, MediaType.APPLICATION_JSON));
            Assertions.assertEquals(200, response.getStatus());
            return response.readEntity(StemmingResponse.class);
        }
    }
    
    @Test
    @RunAsClient
    public void test001_QueryWebService() throws Exception {
        StemmingRequest request = new StemmingRequest("Werkzeuge gebrauchen");
        StemmingResponse response = doRequest(request);
        Set<String> expected = new HashSet<> (Arrays.asList("werkzeug", "gebrauch", "gebrauchen"));
        Set<String> actual = new HashSet<> (Arrays.asList(response.getStems()));
        Assertions.assertEquals(expected, actual);

    }
}
