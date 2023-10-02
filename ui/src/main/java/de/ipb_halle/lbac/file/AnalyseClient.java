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
package de.ipb_halle.lbac.file;

import de.ipb_halle.kx.service.TextWebRequestType;
import de.ipb_halle.kx.service.TextWebStatus;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;


public class AnalyseClient {

    private final String baseURL = "http://localhost:8080/kx/";

    /**
     * make a GET request to the remote knowledge extractor 
     * endpoint.
     *
     * @param fileId id of the fileObject to be inspected
     * @param type the request type: initially SUBMIT; QUERY thereafter
     * @return ideally BUSY or DONE; can also return error codes
     *
     * @throws Exception if either an exception occurs during the request 
     * process or if the result cannot be converted to TextWebStatus.
     */
    public TextWebStatus analyseFile(Integer fileId, TextWebRequestType type) throws Exception {
        HttpUriRequest request = new HttpGet(
                new URL(
                    new URL(baseURL),
                    String.format("process?fileId=%d&type=%s", fileId, type.toString())
                ).toExternalForm());
        HttpResponse response = HttpClientBuilder.create().build().execute(request);

        int httpStatus = response.getStatusLine().getStatusCode();
        if (httpStatus != HttpStatus.SC_OK) {
            throw new Exception(String.format("Unexpected HTTP status: %d", httpStatus));
        }
        return TextWebStatus.valueOf(streamToString(response.getEntity().getContent()));
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
}
