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
import de.ipb_halle.lbac.exp.RemoteExperiment;
import de.ipb_halle.lbac.items.RemoteItem;
import de.ipb_halle.lbac.material.RemoteMaterial;
import de.ipb_halle.lbac.search.document.Document;
import de.ipb_halle.lbac.webclient.LbacWebClient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.ejb.DependsOn;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
@ApplicationScoped
@Startup
@DependsOn({"NodeService"})
public class SearchWebClient extends LbacWebClient {

    protected Logger logger = LogManager.getLogger(this.getClass().getName());

    public SearchResult getRemoteSearchResult(
            CloudNode cn,
            User user,
            List<SearchRequest> requests) {
        try {

            SearchWebRequest webRequest = new SearchWebRequest();
            for (SearchRequest r : requests) {
                webRequest.addRequest(Arrays.asList((SearchRequestImpl) r));
            }

            signWebRequest(webRequest, cn.getCloud().getName(), user);
            WebClient wc = createWebclient(cn, SearchWebService.class);
            webRequest.switchToTransferMode();
            SearchWebResponse result = wc.post(webRequest, SearchWebResponse.class);
            if (result != null) {
                cn.recover();
                cloudNodeService.save(cn);
                cn.getNode().setLocal(false);
                SearchResult searchResult = new SearchResultImpl(cn.getNode());
                searchResult.addResults(convertRemoteObjectsToSearchable(result));
                searchResult
                        .getDocumentStatistic().setAverageWordLength(result.getAverageWordLength());
                searchResult.getDocumentStatistic().setTotalDocsInNode(result.getTotalDocsInNode());
                return searchResult;

            } else {
                return new SearchResultImpl(nodeService.getLocalNode());
            }
        } catch (Exception e) {
            return new SearchResultImpl(nodeService.getLocalNode());
        }
    }

    private List<Searchable> convertRemoteObjectsToSearchable(SearchWebResponse result) {
        List<Searchable> searchables = new ArrayList<>();
        for (Document d : result.getRemoteDocuments()) {
            searchables.add(d);
        }
        for (RemoteMaterial d : result.getRemoteMaterials()) {
            searchables.add(d);
        }
        for (RemoteItem d : result.getRemoteItem()) {
            searchables.add(d);
        }
        for (RemoteExperiment d : result.getRemoteExperiments()) {
            searchables.add(d);
        }
        return searchables;
    }
}
