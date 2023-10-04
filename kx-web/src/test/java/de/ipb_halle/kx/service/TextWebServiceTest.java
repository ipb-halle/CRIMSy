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

import de.ipb_halle.test.ManagedExecutorServiceMock;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import de.ipb_halle.kx.file.FileObject;
import de.ipb_halle.kx.file.FileObjectService;
import de.ipb_halle.kx.termvector.StemmedWordOrigin;
import de.ipb_halle.kx.termvector.TermVector;
import de.ipb_halle.kx.termvector.TermVectorService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.Arrays;
import javax.inject.Inject;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

//  @Resource(name = "DefaultManagedExecutorService")
//  private ManagedExecutorService executor;

    @ArquillianResource
    URL baseURL;

    @Inject
    private TextWebService textWebService;

    @Inject
    private FileObjectService fileObjectService;

    @Inject
    private JobTracker jobTracker;

    @Deployment
    public static WebArchive createDeployment() {
        System.setProperty("log4j.configurationFile", "log4j2-test.xml");

        WebArchive archive = ShrinkWrap.create(WebArchive.class, "TextWebServiceTest.war")
                .addClass(FileObjectService.class)
                .addClass(JobTracker.class)
                .addClass(TermVectorService.class)
                .addClass(TextWebService.class)
                .addAsResource("PostgresqlContainerSchemaFiles")
                .addAsWebInfResource("test-persistence.xml", "persistence.xml")
                .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
        return archive;
    }

    @BeforeEach
    public void init() {
        textWebService.setFileAnalyserFactory(new FileAnalyserFactoryMock());
        textWebService.setExecutorService(new ManagedExecutorServiceMock(2));
    }

    private String streamToString(InputStream inputStream) {
        try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            for (int length; (length = inputStream.read(buffer)) != -1; ) {
                result.write(buffer, 0, length);
            }
            // StandardCharsets.UTF_8.name() > JDK 7
            return result.toString("UTF-8");
        } catch (IOException e) {
            // ignore
        }
        return "";
    }

    private FileObject createFileObject(String location) {
        FileObject fo = new FileObject();
        fo.setFileLocation(location);
        fo.setName("dummy");
        return fileObjectService.save(fo);
    }

    private String doRequest(Integer fileId, TextWebRequestType type) throws IOException {
        // port number must match the arquillian setting
        HttpUriRequest request = new HttpGet(
                new URL(
                    baseURL, 
                    String.format("process?fileId=%d&type=%s", fileId, type.toString())
                ).toExternalForm());
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode(), "match HTTP status code");

        return streamToString(response.getEntity().getContent());
    }

    @Test
    @RunAsClient
    public void test001_TextWebService() throws IOException {
        FileObject fo = createFileObject("some_invalid_path");

        String result = doRequest(-1, TextWebRequestType.SUBMIT);
        assertEquals(TextWebStatus.NO_INPUT_ERROR.toString(), result, "error on invalid fileId");

        result = doRequest(fo.getId(), TextWebRequestType.QUERY);
        assertEquals(TextWebStatus.NO_SUCH_JOB_ERROR.toString(), result, "error on non-existent job");

        result = doRequest(fo.getId(), TextWebRequestType.SUBMIT);
        assertEquals(TextWebStatus.BUSY.toString(), result, "successful job submission");

        FileAnalyserMock mock = (FileAnalyserMock) jobTracker.getJob(fo.getId());
        mock.setStatus(TextWebStatus.DONE);
        mock.setLanguage("de");
        mock.setTermVectors(Arrays.asList(
                    // root, file, freq
                    new TermVector ("kapsel", fo.getId(), 1), 
                    new TermVector ("gefund", fo.getId(), 2)));
        mock.setWordOrigins(Arrays.asList(
                    // stem, origin
                    new StemmedWordOrigin ("kapsel", "kapselung"),
                    new StemmedWordOrigin ("gefund", "gefunden")));
        result = doRequest(fo.getId(), TextWebRequestType.QUERY);
        assertEquals(TextWebStatus.DONE.toString(), result, "successful job completion");

        assertNull((Object) jobTracker.getJob(fo.getId()), "job got removed");
    }
}
