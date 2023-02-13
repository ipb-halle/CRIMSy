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
package de.ipb_halle.lbac.forum.postings;

import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.forum.Topic;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.util.ssl.SecureWebClientBuilder;
import de.ipb_halle.lbac.webclient.LbacWebClient;
import static de.ipb_halle.lbac.webservice.RestApiHelper.getRestApiDefaultPath;
import java.io.Serializable;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 *
 * @author fmauz
 */
public class PostingWebClient extends
        LbacWebClient implements Serializable {

    private final static String REST_PATH = getRestApiDefaultPath(PostingWebService.class);
    private Logger logger = LogManager.getLogger(PostingWebClient.class);

    @Inject 
    private CloudNodeService cloudNodeService;

    @Inject
    private KeyManager keyManager;

    @Inject
    private NodeService nodeService;

    public void announcePostingToRemoteNode(Topic t, User u, CloudNode cn) {
        PostingWebRequest webRequest = new PostingWebRequest();
        webRequest.setCloudName(cn.getCloud().getName());
        webRequest.setNodeIdOfRequest(nodeService.getLocalNodeId());
        u.obfuscate();
        t.getOwner().obfuscate();
        try {
            webRequest.setSignature(createWebRequestSignature(
                    keyManager.getLocalPrivateKey(cn.getCloud().getName())));
            webRequest.setUser(u);
            WebClient wc = SecureWebClientBuilder.createWebClient(
                    cn,
                    REST_PATH
            );

            webRequest.setTopic(t);

            wc.accept(MediaType.APPLICATION_XML_TYPE);
            wc.type(MediaType.APPLICATION_XML_TYPE);
            Response response = wc.post(webRequest);
            if (response.getStatus() != 200) {
                cn.fail();
                logger.error("Could not announce posting. Got Response with status " + response.getStatus());
            } else {
                cn.recover();
            }
        } catch (Exception e) {
            cn.fail();
            logger.error("announcePostingToRemoteNode() caught an exception:", (Throwable) e);
        }
        this.cloudNodeService.save(cn);
    }
}
