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
package de.ipb_halle.lbac.device.job;


import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;

/**
 * This class represents the client side of the job processing 
 * machinery.
 */
public class JobWebClient {

    private static final Integer CONNECTION_TIMEOUT_IN_MS = 10 * 1000;
    private static final Integer READ_TIMEOUT_IN_MS = 30 * 1000;


    /**
     * Create a WebClient
     *
     * @param url url of the job service 
     * @return a WebClient with timeouts configured. This WebClient does NOT
     * use certificate based authentication. Instead an access token is sent 
     * as part of the request.
     */
    public static WebClient createWebClient(String url) {

        WebClient wc = WebClient.create(url); 

        ClientConfiguration cc = WebClient.getConfig(wc);

        HTTPConduit hc = cc.getHttpConduit();
        hc.getClient().setConnectionTimeout(CONNECTION_TIMEOUT_IN_MS);
        hc.getClient().setReceiveTimeout(READ_TIMEOUT_IN_MS);
        return wc;
    }


    /**
     * send a request to the JobWebService at the given url
     */
    public NetJob processRequest(NetJob job, String url) {
        try {
            WebClient wc = createWebClient(url);
            wc.accept(MediaType.APPLICATION_XML_TYPE);
            wc.type(MediaType.APPLICATION_XML_TYPE);
            
            NetJob response = wc.post(job, NetJob.class);
            return response;
        } catch(Exception e) {
        }
        return null;
    }
}
