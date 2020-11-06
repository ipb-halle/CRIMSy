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

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.search.document.SearchWebService;
import de.ipb_halle.lbac.webclient.LbacWebClient;
import java.util.List;
import javax.ejb.DependsOn;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import org.apache.cxf.jaxrs.client.WebClient;

/**
 *
 * @author fmauz
 */
@ApplicationScoped
@Startup
@DependsOn({"NodeService"})
public class SearchWebClient extends LbacWebClient {

    public SearchResult getRemoteSearchResult(
            CloudNode cn,
            User user,
            List<SearchRequest> requests) {
        try {
            SearchWebRequest webRequest = new SearchWebRequest();
            webRequest.addRequest(requests);
            signWebRequest(webRequest, cn.getCloud().getName(), user);
            WebClient wc = createWebclient(cn, SearchWebService.class);

            SearchWebResponse result = wc.post(webRequest, SearchWebResponse.class);
            if (result != null && result.getSearchResult() != null) {
                cn.recover();
                cloudNodeService.save(cn);
                return result.getSearchResult();

            } else {
                return new SearchResultImpl(nodeService.getLocalNode());
            }
        } catch (Exception e) {
            return new SearchResultImpl(nodeService.getLocalNode());
        }
    }
}
