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
package de.ipb_halle.lbac.admission.group;

import de.ipb_halle.lbac.admission.*;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.util.ssl.SecureWebClientBuilder;
import de.ipb_halle.lbac.webclient.LbacWebClient;
import de.ipb_halle.lbac.webservice.RestApiHelper;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SignatureException;
import java.util.UUID;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * MembershipWebClient an instance of this object is constructed upon user login
 */
public class DeactivateGroupWebClient
        extends LbacWebClient {

    private final static String REST_PATH
            = RestApiHelper.getRestApiDefaultPath(DeactivateGroupWebService.class);

    private Logger LOGGER = LogManager.getLogger(this.getClass().getName());

    /**
     * Triggers a REST call which deactivate a group the target node
     *
     * @param u obfuscated user to be announced
     * @param cloudNode remote node where the user and groups will be announced
     * @param groupToDeactivate
     * @param localNodeId
     * @param privateKey private rsa key for authentificating the webrequest
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public void deactivateGroupAtRemoteNodes(
            User u,
            CloudNode cloudNode,
            Group groupToDeactivate,
            UUID localNodeId,
            PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        DeactivateGroupWebRequest membershipAnnouncement = new DeactivateGroupWebRequest(groupToDeactivate);
        membershipAnnouncement.setUser(u);
        membershipAnnouncement.setSignature(
                createWebRequestSignature(privateKey)
        );
        membershipAnnouncement.setCloudName(cloudNode.getCloud().getName());
        membershipAnnouncement.setNodeIdOfRequest(localNodeId);

        WebClient wc = SecureWebClientBuilder.createWebClient(
                cloudNode,
                REST_PATH);
        wc.accept(MediaType.APPLICATION_XML_TYPE);
        wc.type(MediaType.APPLICATION_XML_TYPE);
        Response response = wc.post(membershipAnnouncement);

        if (response.getStatus() != 200) {
            logFailureResponse(
                    String.format("%d:%s", response.getStatus(), ""),
                    LOGGER,
                    String.format("Failed to deactivate group at %s", cloudNode.getNode().getInstitution()));
        }
    }
}
