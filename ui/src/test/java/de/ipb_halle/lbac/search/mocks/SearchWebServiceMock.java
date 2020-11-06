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
package de.ipb_halle.lbac.search.mocks;

import de.ipb_halle.lbac.search.document.Document;
import de.ipb_halle.lbac.file.TermFrequency;
import de.ipb_halle.lbac.search.document.DocumentSearchRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author fmauz
 */
@Path("rest/search/")
public class DocumentSearchEndpointMock {

    public static int SLEEPTIME_BETWEEN_REQUESTS = 500;
    private static int request = 0;

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response search(DocumentSearchRequest request) {
        try {
            System.out.println("REST Endpunkt angesprochen: " + request.getCollectionId());
            DocumentSearchEndpointMock.request++;
            Thread.sleep(SLEEPTIME_BETWEEN_REQUESTS * DocumentSearchEndpointMock.request);
            Document d = new Document();
            d.setCollectionId(request.getCollectionId());
            d.setNodeId(request.getNodeId());
            d.setOriginalName("Document from Remote Server");
            d.setWordCount(100);
            d.getTermFreqList().getTermFreq().add(new TermFrequency("java", 3));
            request.add(d);
        } catch (Exception e) {

        }
        return Response.ok(request).build();
    }

}
