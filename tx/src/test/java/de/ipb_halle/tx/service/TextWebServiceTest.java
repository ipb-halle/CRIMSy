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
package de.ipb_halle.tx.service;

import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.io.IOException;
import java.net.URL;
import javax.inject.Inject;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.eclipse.jetty.http.HttpStatus;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;



/**
 *
 * @author fblocal
 */

@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class TextWebServiceTest {
   

//  @Inject
//  private TextWebService textWebService;
    
    
    @Deployment
    public static WebArchive createDeployment() {
        System.setProperty("log4j.configurationFile", "log4j2-test.xml");

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "TextWebServiceTest.war")
                .addClass(TextWebService.class)
                .addAsWebInfResource("test-persistence.xml", "persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
//              .addAsResource("javax.enterprise.inject.spi.Extension",
//                  "META-INF/services/javax.enterprise.inject.spi.Extension");
        return archive;
    }
    
    // @BeforeEach
    public void init() {
    
    }
    
    @Test
    @RunAsClient
    public void test001_TextWebService() throws IOException {
        // port number must match the arquillian setting
        HttpUriRequest request = new HttpGet("http://localhost:8800/tx/process/");
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        assertEquals(HttpStatus.OK_200, response.getStatusLine().getStatusCode());
    }
    
}
