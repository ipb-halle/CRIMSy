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
package de.ipb_halle.lbac.webservice.service;

import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.webclient.WebRequestSignature;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * Takes a WebRequestSignature tries to authentificate it. Authentification
 * contains 2 steps: <ol><li>(1) Validating the signature by the given algorithm
 * </li><li>(2) Checking that the timestamp of the request is not too
 * old.</li></ol>
 *
 * @author fmauz
 */
@Stateless
public class WebRequestAuthenticator {

    protected int intervallOfAcceptanceInMilliSec = 1000 * 60 * 5; // 5 Minutes
    protected String KEY_ALGORITHM = "RSA"; // Algorithm for deserialising the public key
    protected Logger logger;


    /**
     * default constructor
     */
    public WebRequestAuthenticator() {
        logger = LogManager.getLogger(WebRequestAuthenticator.class.getName());
    }

    /**
     * Tries to authentificate a WebRequest from another node.
     *
     * @param webReqSig Signature of the request
     * @param cloudNode the origin CloudNode (with public key) of the request 
     * @return Is the authentificated and not too old
     * @throws Exception Error at authentification
     */
    public boolean isAuthentificated(
            WebRequestSignature webReqSig,
            CloudNode cloudNode) throws Exception {

        // Get the public key of the requestnode from the database
        PublicKey pubKey = getKeyFromString(cloudNode.getPublicKey());

        // Is the signature valide to the clear message
        Signature sig = Signature.getInstance(webReqSig.getSignatureAlgorithm());
        sig.initVerify(pubKey);
        sig.update(webReqSig.getDecryptedMessage().getBytes());
        if (!sig.verify(Base64.getDecoder().decode(webReqSig.getCryptedMessage()))) {
            logger.error("Could not validate signature");
            return false;
        }

        //Check that the request is not too old to prevent replay attacks.
        long timeOfCreation = Long.parseLong(webReqSig.getDecryptedMessage().split("#")[1]);
        Date d = new Date();

        long validThru = d.getTime() - intervallOfAcceptanceInMilliSec;

        if (timeOfCreation < validThru) {
            logger.warn("Timeout WebRequest from Node  " + cloudNode.getNode().getId().toString()
                    + ". Request timestamp " + new Date(timeOfCreation + intervallOfAcceptanceInMilliSec)
            );
            return false;
        }

        // Request is authentificated
        return true;
    }


    /**
     * Creates the public key from a Base64 String represantation
     *
     * @param key public key in Base64 String represantation
     * @return Public key object
     * @throws Exception
     */
    public PublicKey getKeyFromString(String key) throws Exception {
        byte[] byteKey = Base64.getDecoder().decode(key.getBytes());
        X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
        KeyFactory kf = KeyFactory.getInstance(KEY_ALGORITHM);
        return kf.generatePublic(X509publicKey);
    }

    /**
     * Gets the tolerance time intervall of requests in milliseconds
     *
     * @return
     */
    public int getIntervallOfAcceptanceInMilliSec() {
        return intervallOfAcceptanceInMilliSec;
    }

    /**
     * Sets the tolerance time intervall of requests in milliseconds
     *
     * @param intervallOfAcceptanceInMilliSec in milliseconds
     */
    public void setIntervallOfAcceptanceInMilliSec(int intervallOfAcceptanceInMilliSec) {
        this.intervallOfAcceptanceInMilliSec = intervallOfAcceptanceInMilliSec;
    }

}
