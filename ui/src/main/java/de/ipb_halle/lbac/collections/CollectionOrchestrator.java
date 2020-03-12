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
package de.ipb_halle.lbac.collections;

import de.ipb_halle.lbac.entity.ACPermission;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.Collection;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.service.ACListService;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.CollectionService;
import de.ipb_halle.lbac.service.MembershipService;
import de.ipb_halle.lbac.service.NodeService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;
import org.apache.log4j.Logger;

/**
 * Service that triggers a search for collections. The search is divided into 2
 * steps: (1) Seach for local, readable collections (synchron) (2) Triggers the
 * search via a webclient for remote nodes. The search results will be inserted
 * into an @see de.ipb_halle.lbac.search.CollectionSearchStatus (asynchron)
 *
 * @author fmauz
 */
@Stateless
public class CollectionOrchestrator implements Serializable {

    @Inject
    private CloudNodeService cloudNodeService;

    @Inject
    private CollectionWebClient collectionWebClient;

    private static final long serialVersionUID = 1L;

    @Inject
    private NodeService nodeService;

    @Inject
    private CollectionService collectionService;

    @Inject
    private FileEntityService fileEntityService;

    @Inject
    private MembershipService membershipService;

    @Inject
    private ACListService acListService;

    @Resource
    ManagedExecutorService managedExecutorService;

    private static final Logger LOGGER = Logger.getLogger(CollectionOrchestrator.class);

    /**
     * Checking if all injected dependencies are present.
     */
    @PostConstruct
    public void checkInjectedFields() {
        assert (collectionWebClient != null);
        assert (nodeService != null);
        assert (collectionService != null);
        assert (managedExecutorService != null);
        assert (acListService != null);
    }

    /**
     * Starts the search for readable collections on local and remote nodes.
     * (1)Clears all old collections and seach requests in node. (2) Starts a
     * synchonized search for local collections. (3) Starts n remote searches on
     * remote nodes.
     *
     * All found collections of remote seaches are put into the
     *
     * @see CollectionSearchState Object.
     *
     * @param colState State fo readable collections of user
     * @param user Logged in user
     */
    public void startCollectionSearch(
            CollectionSearchState colState,
            User user) {

        //clear all searchrequests and old collections in status
        colState.getCollections().clear();
        colState.getUnfinishedNodeRequests().clear();

        //get local collections with at least read permmision
        List<Collection> readableList = removeCollsWithoutReadPerm(
                        collectionService.load(new HashMap<>()),
                        user);
        for(Collection col : readableList) {
            col.setCountDocs(this.fileEntityService.getDocumentCount(col));
        }
        colState.getCollections().addAll(readableList);

        // start a collection search for each node
        for (Node n : nodeService.load(null, false)) {   //Only remote nodes
            List<CloudNode> cnl = cloudNodeService.load(null, n);
            if ((cnl == null) || (cnl.size() == 0)) {
                continue;
            }
            colState.getUnfinishedNodeRequests().add(n.getId());
            CompletableFuture.supplyAsync(() -> {
                return collectionWebClient.getCollectionsFromRemoteNode(cnl.get(0), user);
            }, managedExecutorService)
                    .thenApply(
                            foundCollections -> updateCollectionStatus(
                                    colState,
                                    foundCollections,
                                    n.getId()
                            )
                    );
        }
    }

    /**
     * TO DO: transfer the removement to criteria api via the cmap
     *
     * @param rawColls
     * @param user
     * @return
     */
    private List<Collection> removeCollsWithoutReadPerm(
            List<Collection> rawColls,
            User user) {
        if (user == null) {
            return new ArrayList<>();
        }
        Set<Collection> filteredColls = new HashSet<>();
        for (Collection c : rawColls) {
            boolean gotPermission = acListService.isPermitted(ACPermission.permREAD, c, user);
            boolean isOwner = c.getOwner().equals(user);
            if (gotPermission || isOwner) {
                filteredColls.add(c);
            }
        }

        return new ArrayList<>(filteredColls);
    }

    /**
     * Updates the collectionstate by the found collections. Only adds the
     * collections if the node from which the collections are requested are in
     * the search list of the update state.
     *
     * @param colState
     * @param foundCollections
     * @param nodeId
     * @return
     */
    private List<Collection> updateCollectionStatus(
            CollectionSearchState colState,
            List<Collection> foundCollections,
            UUID nodeId) {
        colState.addCollections(foundCollections);
        colState.getUnfinishedNodeRequests().remove(nodeId);
        return new ArrayList<>();
    }

    public CollectionWebClient getCollectionWebClient() {
        return collectionWebClient;
    }

    public void setCollectionWebClient(CollectionWebClient collectionWebClient) {
        this.collectionWebClient = collectionWebClient;
    }

    public NodeService getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public CollectionService getCollectionService() {
        return collectionService;
    }

    public void setCollectionService(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    public ManagedExecutorService getManagedExecutorService() {
        return managedExecutorService;
    }

    public void setManagedExecutorService(ManagedExecutorService managedExecutorService) {
        this.managedExecutorService = managedExecutorService;
    }

    public MembershipService getMembershipService() {
        return membershipService;
    }

    public void setMembershipService(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

}
