/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2020 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.search;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.apache.http.HttpResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;


/**
 *
 * @author fmauz
 */
public class SearchQueryStemmer {

    private final String baseURL = "http://localhost:8080/kx-web/";
    private final Logger logger = LogManager.getLogger(SearchQueryStemmer.class.getName());

    public Set<String> stemmQuery(String queryString) {
        return new HashSet<> (Arrays.asList(doRequest(queryString).trim().split(" ")));
    } 

    private String doRequest(String query) {
        try (CloseableHttpClient client = HttpClientBuilder.create().build()) {;
            HttpPost post = new HttpPost(
                    new URL(new URL(baseURL), "query").toExternalForm());

            HttpEntity entity = new ByteArrayEntity(query.getBytes("UTF-8"));
            post.setEntity(entity);
            HttpResponse response = client.execute(post);
            String result = EntityUtils.toString(response.getEntity());

            int httpStatus = response.getStatusLine().getStatusCode();
            if (httpStatus != HttpStatus.SC_OK) {
                throw new Exception(String.format("Unexpected HTTP status: %d", httpStatus));
            }
            return result;
        } catch (Exception e) {
            logger.warn("doRequest caught an exception: ", (Throwable) e);
        }
        return "";
    }
}
