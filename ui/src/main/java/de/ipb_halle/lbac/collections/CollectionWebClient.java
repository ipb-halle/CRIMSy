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
package de.ipb_halle.lbac.collections;

import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.util.ssl.SecureWebClientBuilder;
import de.ipb_halle.lbac.webclient.LbacWebClient;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;
import jakarta.ejb.DependsOn;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import static de.ipb_halle.lbac.webservice.RestApiHelper.getRestApiDefaultPath;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.MediaType;

/**
 * CollectionWebClient
 *
 *
 */
@ApplicationScoped
@Startup
@DependsOn({"NodeService", "CollectionService"})
public class CollectionWebClient
        extends LbacWebClient {

    private final static String REST_PATH = getRestApiDefaultPath(CollectionWebService.class);

    @Inject
    private CloudNodeService cloudNodeService;

    @Inject
    private CollectionService collectionService;

    @Inject
    private NodeService nodeService;

    private Logger logger;

    @Inject
    protected KeyManager keyManager;

    public CollectionWebClient() {
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    /**
     * perform the post construction initialization after the necessary objects
     * have been injected.
     */
    @PostConstruct
    public void CollectionWebClientInit() {
        if (nodeService == null) {
            logger.error("Injection failed for nodeService.");
        }
        if (collectionService == null) {
            logger.error("Injection failed for collectionService.");
        }
    }

    /**
     * Gets all readable collections of one node for given user
     *
     * @param cn Cloudnode from which the collections are obtained
     * @param u User
     * @return List of readable collections of user u
     */
    public List<Collection> getCollectionsFromRemoteNode(CloudNode cn, User u) {
        CollectionWebRequest webRequest = new CollectionWebRequest();
        webRequest.setCloudName(cn.getCloud().getName());
        webRequest.setNodeIdOfRequest(nodeService.getLocalNodeId());
        try {
            webRequest.setSignature(createWebRequestSignature(
                    keyManager.getLocalPrivateKey(cn.getCloud().getName())));
            webRequest.setUser(u);

            WebClient wc = SecureWebClientBuilder.createWebClient(
                    cn,
                    REST_PATH
            );

            wc.accept(MediaType.APPLICATION_XML_TYPE);
            wc.type(MediaType.APPLICATION_XML_TYPE);
            CollectionList result = wc.post(webRequest, CollectionList.class);
            if (result != null && result.getCollectionList() != null) {
                for (Collection c : result.getCollectionList()) {
                    c.getNode().setLocal(false);
                }
                cn.recover();
                cloudNodeService.save(cn);
                return result.getCollectionList();

            }
        } catch (Exception e) {
            cn.fail();
            cloudNodeService.save(cn);
            logger.error("getCollectionsFromRemoteNode() caught an exception:", (Throwable) e);
            return new ArrayList<>();
        }
        return new ArrayList<>();
    }
}
