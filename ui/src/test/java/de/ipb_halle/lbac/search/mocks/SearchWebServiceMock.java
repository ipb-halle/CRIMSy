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

import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.search.SearchResultImpl;
import de.ipb_halle.lbac.search.SearchWebRequest;
import de.ipb_halle.lbac.search.SearchWebResponse;
import java.util.Arrays;
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
public class SearchWebServiceMock {

    public static int SLEEPTIME_BETWEEN_REQUESTS = 500;
    private static int request = 0;

    @POST
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response search(SearchWebRequest request) {
        SearchWebResponse response = new SearchWebResponse();
        try {

            SearchResult results = new SearchResultImpl(new Node());
            response.setItem(new Item());

            response.setResult(results);
        } catch (Exception e) {
            int i = 0;
        }
        return Response.ok(response).build();
    }

}
