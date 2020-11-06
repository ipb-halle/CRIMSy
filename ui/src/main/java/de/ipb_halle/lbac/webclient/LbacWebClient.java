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
package de.ipb_halle.lbac.webclient;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.search.document.SearchWebService;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.util.HexUtil;
import de.ipb_halle.lbac.util.ssl.SecureWebClientBuilder;
import static de.ipb_halle.lbac.webservice.RestApiHelper.getRestApiDefaultPath;
import de.ipb_halle.lbac.webservice.WebRequest;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.util.Base64;
import java.util.Date;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.logging.log4j.Logger;

/**
 * Basic class for webclient which provides basic funtionality e.g. creating
 * signatures for webrequests.
 *
 * @author fmauz
 */
public class LbacWebClient implements Serializable {

    protected String SIGNATURE_ALGORITHM = "SHA256withRSA";
    protected int RANDOM_VALUE_LENGTH = 8;
    
    @Inject
    protected NodeService nodeService;

    @Inject
    protected KeyManager keyManager;

    @Inject
    protected CloudNodeService cloudNodeService;

    /**
     * Creates a WebRequestSignature with the clear and crypted message. The
     * clear message contains an unix timestamp at the end.
     *
     * @param privateKey Private rsa key for signing
     * @return WebRequestSignature
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public WebRequestSignature createWebRequestSignature(
            PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        // Generate a secure random number and convert it to hexadecimal
        SecureRandom random = new SecureRandom();
        byte[] values = new byte[RANDOM_VALUE_LENGTH];
        random.nextBytes(values);
        String id = HexUtil.toHex(values);

        // Add the current unix time to the message, seperated by an #
        Date d = new Date();
        String decryptedSignature = id + "#" + String.valueOf(d.getTime());

        // Sign the message whith the defined algorithm
        Signature signatureAlgorithm = Signature.getInstance(SIGNATURE_ALGORITHM);
        signatureAlgorithm.initSign(privateKey);
        signatureAlgorithm.update(decryptedSignature.getBytes());
        byte[] signature = signatureAlgorithm.sign();

        //return the finished signature of the webrequest
        WebRequestSignature websignature = new WebRequestSignature(
                decryptedSignature,
                Base64.getEncoder().encodeToString(signature),
                SIGNATURE_ALGORITHM);

        return websignature;
    }

    protected void logFailureResponse(
            String responseStatus, Logger logger, String message) {
        if (!responseStatus.split(":")[0].equals("200")) {
            logger.error(message);
            logger.error(responseStatus);
        }
    }

    protected void signWebRequest(
            WebRequest webRequest,
            String cloudName,
            User user) throws NoSuchAlgorithmException, InvalidKeyException, KeyStoreException, UnrecoverableKeyException, SignatureException {
        webRequest.setCloudName(cloudName);
        webRequest.setNodeIdOfRequest(nodeService.getLocalNodeId());
        webRequest.setSignature(
                createWebRequestSignature(
                        keyManager.getLocalPrivateKey(cloudName)));
        webRequest.setUser(user);
    }

    protected WebClient createWebclient(CloudNode cn, Class webServiceClazz) {
        WebClient wc = SecureWebClientBuilder.createWebClient(cn, getRestApiDefaultPath(webServiceClazz));
        wc.accept(MediaType.APPLICATION_XML_TYPE);
        wc.type(MediaType.APPLICATION_XML_TYPE);
        return wc;
    }
}
