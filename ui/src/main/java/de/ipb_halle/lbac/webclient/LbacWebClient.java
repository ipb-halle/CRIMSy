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

import de.ipb_halle.lbac.util.HexUtil;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.Date;
import org.apache.logging.log4j.Logger;

/**
 * Basic class for webclient which provides basic funtionality e.g. creating
 * signatures for webrequests.
 *
 * @author fmauz
 */
public class LbacWebClient implements Serializable{

    protected String SIGNATURE_ALGORITHM = "SHA256withRSA";
    protected int RANDOM_VALUE_LENGTH = 8;

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
}
