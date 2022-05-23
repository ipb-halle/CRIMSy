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
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.search.bean.SearchState;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.NodeService;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
@Stateless
public class SearchOrchestrator implements Serializable {
    
    @Inject
    private CloudNodeService cloudNodeService;
    
    @Inject
    private NodeService nodeService;
    
    @Inject
    private SearchWebClient searchWebClient;
    
    @Resource(name = "lbacManagedExecutorService")
    private ManagedExecutorService managedExecutorService;
    
    private static final Logger logger = LogManager.getLogger(SearchOrchestrator.class);
    
    public void startRemoteSearch(
            SearchState searchState,
            User user,
            List<SearchRequest> requests) {
        for (Node node : nodeService.load(null, false)) {
            List<CloudNode> cnl = cloudNodeService.load(null, node);
            if (cnl != null && !cnl.isEmpty()) {
                startRemoteSearchForCloudNode(
                        searchState,
                        cnl.get(0),
                        user,
                        requests);
            }
        }
    }
    
    private SearchState updateSearchState(
            SearchState searchState,
            SearchResult result) {
        searchState.removeNodeFromSearch(result.getNode());
        searchState.addNetObjects(result.getAllFoundObjects());
        searchState.addNewStats(
                result.getDocumentStatistic().getTotalDocsInNode(),
                result.getDocumentStatistic().getAverageWordLength());
        return searchState;
    }
    
    public void setSearchWebClient(SearchWebClient searchWebClient) {
        this.searchWebClient = searchWebClient;
    }
    
    private void startRemoteSearchForCloudNode(
            SearchState searchState,
            CloudNode cloudNode,
            User user,
            List<SearchRequest> requests) {
        searchState.addNoteToSearch(cloudNode.getNode().getId());
        CompletableFuture.supplyAsync(() -> {
            return searchWebClient.getRemoteSearchResult(cloudNode, user, requests);
        }, managedExecutorService)
                .thenApply(
                        remoteResult -> updateSearchState(
                                searchState,
                                remoteResult
                        )
                );
    }
    
}
