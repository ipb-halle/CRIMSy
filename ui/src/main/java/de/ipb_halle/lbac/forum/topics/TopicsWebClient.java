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
package de.ipb_halle.lbac.forum.topics;

import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.forum.Topic;
import de.ipb_halle.lbac.forum.TopicsList;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.util.ssl.SecureWebClientBuilder;
import de.ipb_halle.lbac.webclient.LbacWebClient;
import static de.ipb_halle.lbac.webservice.RestApiHelper.getRestApiDefaultPath;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author fmauz
 */
@ApplicationScoped
@Startup
public class TopicsWebClient extends
        LbacWebClient {
    
    private final static String REST_PATH = getRestApiDefaultPath(TopicsWebService.class);
    private Logger logger = LogManager.getLogger(TopicsWebClient.class);
    
    @Inject
    private CloudNodeService cloudNodeService;
    
    @Inject
    private NodeService nodeService;
    
    @Inject
    private KeyManager keyManager;

    /**
     * Fetches all topics which are readable by the user u from the node n
     * filtered by keywords via a REST call
     *
     * @param cn
     * @param u
     * @param keywords
     * @return
     */
    public List<Topic> getTopicsFromRemoteNode(
            CloudNode cn,
            User u,
            String... keywords) {
        
        TopicsWebRequest webRequest = new TopicsWebRequest();
        webRequest.setCloudName(cn.getCloud().getName());
        webRequest.setCloud(cn.getCloud());
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
            
            TopicsList result = wc.post(webRequest, TopicsList.class);
            if (result == null) {
                throw new Exception("Result from REST call is null");
            }
            for (Topic c : result.getTopics()) {
                c.getNode().setLocal(false);
            }
            cn.recover();
            cloudNodeService.save(cn);
            return result.getTopics();
            
        } catch (Exception e) {
            logger.warn("getTopicsFromRemoteNode() caught an exception", (Throwable) e);
            cn.fail();
            cloudNodeService.save(cn);
            return new ArrayList<>();
        }
        
    }
    
}
