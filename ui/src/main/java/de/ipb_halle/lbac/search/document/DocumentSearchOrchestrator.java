/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.search.document;

import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.Collection;
import de.ipb_halle.lbac.entity.Document;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.util.ssl.SecureWebClientBuilder;
import de.ipb_halle.lbac.webclient.LbacWebClient;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.PostConstruct;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.faces.application.ProjectStage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.log4j.Logger;

/**
 * Triggers remote search requests and puts the results into the @see
 * DocumentSearchState Object.
 *
 */
@Stateless
public class DocumentSearchOrchestrator implements Serializable {

    private final String REST_PATH = "/rest/search";
    private final Logger logger = Logger.getLogger(this.getClass());

    final List<CompletableFuture<DocumentSearchRequest>> taskList = new ArrayList<>();

    @Resource(name = "lbacManagedExecutorService")
    private ManagedExecutorService managedExecutorService;

    @Inject
    private KeyManager keyManager;

    @Inject
    private CloudNodeService cloudNodeService;

    @Inject
    private NodeService nodeService;

    private boolean development = false;

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }

    /**
     * Trigger asynchronus searches on remote nodes. They will be excecuted as
     * asynchronus taks and the results are written to futures.
     *
     * @param collections List of collections which will be searched for. Every
     * collection triggers an seperate search.
     * @param queryString String of search terms
     * @param documentState Container in which the found documents and global
     * informations are stored
     */
    public void orchestrate(
            List<Collection> collections,
            String queryString,
            DocumentSearchState documentState) {
        if (development) {
            infoStart(collections, queryString, documentState);
        }
        for (Collection collection : collections) {

            if (!collection.getNode().getLocal()) {
                // any of the clouds is ok
                List<CloudNode> cnl = this.cloudNodeService.load(null, collection.getNode()); 
                if ((cnl == null || cnl.isEmpty())) {
                    continue;
                }
                CloudNode cloudNode = cnl.get(0);

                documentState.getUnfinishedCollectionRequests().add(collection.getId());
                final CompletableFuture<DocumentSearchRequest> doCall = CompletableFuture.supplyAsync(() -> {
                    // Triggering the remote searches
                    if (development) {
                        this.logger.info("orchestrate() collection: " + collection.getName());
                    }
                    return startRemoteSearch(
                            createSearchRequest(queryString, collection, cloudNode), 
                            cloudNode);

                }, managedExecutorService)
                        .thenApply(searchResult -> {
                            // update searchState with the results from future
                            return updateSearchRequest(
                                    documentState,
                                    searchResult,
                                    collection);
                        });
                // store running tasks to be able to kill them later
                taskList.add(doCall);
            }
        }

    }

    /**
     * Creates a Search Request. The limit of resulting documents is set to
     * Integer.MAX_VALUE
     *
     * @param queryString
     * @param collection
     * @return
     */
    private DocumentSearchRequest createSearchRequest(String queryString, Collection collection, CloudNode cn) {
        final DocumentSearchRequest searchRequest = new DocumentSearchRequest();
        try {

            LbacWebClient webClient = new LbacWebClient();
            searchRequest.setSignature(
                    webClient.createWebRequestSignature(
                            keyManager.getLocalPrivateKey(cn.getCloud().getName())
                    )
            );
            searchRequest.setSearchQuery(new DocumentSearchQuery(queryString));
            searchRequest.setNodeId(collection.getNode().getId());
            searchRequest.setCloudName(cn.getCloud().getName());
            searchRequest.setNodeIdOfRequest(nodeService.getLocalNodeId());
            searchRequest.setCollectionId(collection.getId());
            searchRequest.setLimit(Integer.MAX_VALUE);
        } catch (InvalidKeyException | KeyStoreException | NoSuchAlgorithmException | SignatureException | UnrecoverableKeyException e) {
            logger.error("Could not create WebRequest", e);
        }
        return searchRequest;
    }

    /**
     * Starts a search on a remote node. If the node of the searchRequest is a
     * local node the search will not bee excecuted and the search request is
     * given back with an empty reult list.
     *
     * @param searchRequest
     * @param cloudNode
     * @return
     */
    private DocumentSearchRequest startRemoteSearch(
            DocumentSearchRequest searchRequest,
            CloudNode cloudNode) {
        if (!cloudNode.getNode().getLocal()) {
            if (development) {
                logger.info("startRemoteSearch() making request: " + cloudNode.getNode().getBaseUrl());
            }
            // any cloud is okay here.
            final WebClient wc = SecureWebClientBuilder.createWebClient(
                    cloudNode,
                    REST_PATH
            );
            //*** prepare XML format in header ***
            //*** JSON possible too ***
            wc.accept(MediaType.APPLICATION_XML_TYPE);
            wc.type(MediaType.APPLICATION_XML_TYPE);
            DocumentSearchRequest result = wc.post(searchRequest, DocumentSearchRequest.class);
            if (development) {
                logger.info("startRemoteSearch() finished request: " + cloudNode.getNode().getBaseUrl()); 
            }
            return result;
        } else {
            return searchRequest;
        }
    }

    /**
     * Fetches the Results from a remote request and puts the found documents
     * into the @see DocumentSearchState object and actualises the global
     * Information e.g. the total amount of found documents.
     *
     * @param documentState
     * @param searchResult
     * @param collection
     * @return
     */
    private DocumentSearchRequest updateSearchRequest(
            DocumentSearchState documentState,
            DocumentSearchRequest searchResult,
            Collection collection) {

        documentState.addToTotalDocs((int) searchResult.getTotalDocsInCollection());

        for (Document document : searchResult.getResultList()) {
            document.setCollection(collection);
            document.setNode(collection.getNode());
            documentState.getFoundDocuments().add(document);
        }

        documentState.getUnfinishedCollectionRequests().remove(collection.getId());
        documentState.getStats().addSearchResult(
                searchResult.getNodeId(),
                searchResult.getTotalDocsInCollection(),
                searchResult.getSumOfWordsOfNode());
        return searchResult;
    }

    @PostConstruct
    public void init() {
        try {
            if (FacesContext.getCurrentInstance().getApplication().getProjectStage() == ProjectStage.Development) {
                development = true;
            }
        } catch (Exception e) {

        }
    }

    private void infoStart(List<Collection> colls, String searchTerm, DocumentSearchState state) {
        logger.info("");
        logger.info("Start Orchestrating with term: " + searchTerm);
        for (Collection c : colls) {
            logger.info(" -" + c.getName() + ":" + c.getNode().getInstitution());
        }
    }

}
