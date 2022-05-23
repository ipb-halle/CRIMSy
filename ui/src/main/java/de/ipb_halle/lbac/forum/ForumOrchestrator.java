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
package de.ipb_halle.lbac.forum;

import de.ipb_halle.lbac.forum.topics.TopicsWebClient;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.NodeService;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.inject.Inject;

/**
 *
 * @author fmauz
 */
@Stateless
public class ForumOrchestrator implements Serializable{

    @Resource(name = "lbacManagedExecutorService")
    ManagedExecutorService managedExecutorService;

    @Inject
    private CloudNodeService cloudNodeService;

    @Inject
    private NodeService nodeService;

    @Inject
    private ForumService forumService;

    @Inject
    private TopicsWebClient forumWebClient;

    public void startRemoteSearch(
            ForumSearchState state,
            User user,
            String... keywords) {

        for (Node n : nodeService.load(null, Boolean.FALSE)) {

            List<CloudNode> cnl = cloudNodeService.load(null, n);
            if ((cnl == null) || (cnl.size() == 0)) {
                continue;
            }

            CompletableFuture.supplyAsync(() -> {
                return forumWebClient.getTopicsFromRemoteNode(cnl.get(0), user, keywords);
            }, managedExecutorService)
                    .thenApply(
                            foundTopics -> updateSearchState(foundTopics, state)
                    );
        }

    }

    private List<Topic> updateSearchState(
            List<Topic> foundTopics,
            ForumSearchState state) {

        for (Topic t : foundTopics) {
            state.setReadableTopics(
                    forumService.upsertTopicList(t, state.getReadableTopics())
            );
        }

        return foundTopics;
    }
}
