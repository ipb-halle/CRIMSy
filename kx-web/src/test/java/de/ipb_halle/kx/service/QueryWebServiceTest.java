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

import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import jakarta.inject.Inject;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


/**
 *
 * @author fblocal
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class QueryWebServiceTest {

    @ArquillianResource
    URL baseURL;

    @Inject
    private QueryWebService queryWebService;

    @Deployment
    public static WebArchive createDeployment() {
        System.setProperty("log4j.configurationFile", "log4j2-test.xml");

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "QueryWebServiceTest.war")
                .addClass(QueryWebService.class)
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        return archive;
    }

    @BeforeEach
    public void init() {
    }

    private String doRequest(String query) {
        // port number must match the arquillian setting
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {;
            HttpPost post = new HttpPost(
                    new URL(
                        baseURL, "query").toExternalForm());

            HttpEntity entity = new ByteArrayEntity(query.getBytes("UTF-8"));
            post.setEntity(entity);
            HttpResponse response = client.execute(post);
            String result = EntityUtils.toString(response.getEntity());

            assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode(), "match HTTP status code");
            return result;
        } catch (Exception e) {
            assertTrue(false, "unexpected exception");
        }
        return "";
    }

    @Test
    @RunAsClient
    public void test001_QueryWebService() throws IOException {

        Set<String> expected = new HashSet<> (Arrays.asList("werkzeug", "gebrauch", "gebrauchen"));
        Set<String> actual = new HashSet<> (Arrays.asList(doRequest("Werkzeuge gebrauchen").trim().split(" ")));
        assertEquals(expected, actual);

    }
}
