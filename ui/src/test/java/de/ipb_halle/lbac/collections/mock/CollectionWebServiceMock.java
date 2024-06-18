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
package de.ipb_halle.lbac.collections.mock;

import de.ipb_halle.lbac.collections.CollectionWebRequest;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.collections.CollectionList;
import de.ipb_halle.lbac.service.NodeService;
import java.util.Arrays;
import java.util.UUID;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 *
 * @author fmauz
 */
@Path("rest/collections/")
@Stateless
public class CollectionWebServiceMock {
    
    @Inject
    private NodeService nodeService;
    
    private int latenceTimeInMs = 500;
    
    public static boolean SUCCESS = true;
    
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public Response handleRequest(CollectionWebRequest request) {
        System.out.println("Hallo aus WebService");
        if (!SUCCESS) {
            request.setStatusCode("500:Failure from Mocking Server");
        }
        try {
            Thread.sleep(latenceTimeInMs);
        } catch (Exception e) {
            
        }
        CollectionList result = new CollectionList();
        Collection c = new Collection();
        c.setNode(nodeService.getLocalNode());
        c.setId(-100000);
        
        c.setName("testCollectionFromRemote");
        result.setCollectionList(Arrays.asList(c));
        return Response.ok(result).build();
    }
    
    public void setLatenceTimeInMs(int latenceTimeInMs) {
        this.latenceTimeInMs = latenceTimeInMs;
    }
    
}
