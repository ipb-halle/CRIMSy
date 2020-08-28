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
package de.ipb_halle.lbac.search.wordcloud;

import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.util.ssl.SecureWebClientBuilder;
import de.ipb_halle.lbac.webclient.LbacWebClient;
import de.ipb_halle.lbac.webclient.WebRequestSignature;

import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

import static de.ipb_halle.lbac.webservice.RestApiHelper.getRestApiDefaultPath;
import java.util.Set;
import java.util.UUID;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.apache.cxf.jaxrs.client.WebClient;

@Stateless
public class WordCloudWebClient extends LbacWebClient {

    private final static String REST_PATH = getRestApiDefaultPath(WordCloudWebService.class);

    private Logger logger = LogManager.getLogger(WordCloudWebClient.class);

    @Inject
    private KeyManager keyManager;

    @Inject
    private NodeService nodeService;

    public WordCloudWebRequest getWordCloudResponse(
            User user,
            CloudNode cloudNode,
            Set<String> tags,
            Set<Integer> collectionIDs) {
        WordCloudWebRequest request = new WordCloudWebRequest();;
        try {
            WebRequestSignature signature = createWebRequestSignature(
                    keyManager.getLocalPrivateKey(cloudNode.getCloud().getName()));
            request.setSignature(signature);
            request.setCloudName(cloudNode.getCloud().getName());
            request.setNodeIdOfRequest(nodeService.getLocalNodeId());
            request.setUser(user);
            request.setTerms(tags);
            request.setIdsOfReadableCollections(collectionIDs);

            WebClient wc = SecureWebClientBuilder.createWebClient(cloudNode, REST_PATH);
            wc.accept(MediaType.APPLICATION_XML_TYPE);
            wc.type(MediaType.APPLICATION_XML_TYPE);
            return (WordCloudWebRequest) wc.post(request, WordCloudWebRequest.class);
        } catch (Exception e) {
            this.logger.warn("call() caught an exception: ", e);
        }
        return request;

    }
}
