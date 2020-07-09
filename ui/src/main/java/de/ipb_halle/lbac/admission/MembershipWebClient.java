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
package de.ipb_halle.lbac.admission;

import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.Group;
import de.ipb_halle.lbac.entity.User;
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
import java.util.Set;
import java.util.UUID;
import javax.ws.rs.core.MediaType;

/**
 * MembershipWebClient an instance of this object is constructed upon user login
 */
public class MembershipWebClient
        extends LbacWebClient {

    private final static String REST_PATH
            = RestApiHelper.getRestApiDefaultPath(MembershipWebService.class);

    private MembershipWebRequest membershipAnnouncement;
    private Logger LOGGER = LogManager.getLogger(this.getClass().getName());

    /**
     * Triggers a REST call which announces the user with all its groups.The
     * user and groups will be persited in the remote database with obfuscated
     * information. The user will also be included in the public group of the
     * remote node.
     *
     * @param u user to be announced
     * @param cloudNode remote node where the user and groups will be announced
     * @param groupsOfUser set with all groups of user
     * @param localNodeId
     * @param privateKey private rsa key for authentifivating the webrequest
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public void announceUserToRemoteNodes(
            User u,
            CloudNode cloudNode,
            Set<Group> groupsOfUser,
            UUID localNodeId,
            PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        membershipAnnouncement = new MembershipWebRequest();

        membershipAnnouncement.setGroups(groupsOfUser);
        membershipAnnouncement.setUser(u);
        membershipAnnouncement.setSignature(
                createWebRequestSignature(privateKey)
        );
        membershipAnnouncement.setUserToAnnounce(u);
        membershipAnnouncement.setCloudName(cloudNode.getCloud().getName());
        membershipAnnouncement.setNodeIdOfRequest(localNodeId);

        WebClient wc = SecureWebClientBuilder.createWebClient(
                cloudNode, REST_PATH);
        wc.accept(MediaType.APPLICATION_XML_TYPE);
        wc.type(MediaType.APPLICATION_XML_TYPE);

        MembershipWebRequest response
                = wc.post(membershipAnnouncement, MembershipWebRequest.class);

        logFailureResponse(
                response.getStatusCode(),
                LOGGER,
                "Error at announcing user at node: " + cloudNode.getNode().getId());
    }
}
