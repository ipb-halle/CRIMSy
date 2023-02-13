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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Represents a signature with a timestamp for the communication between a @see
 * WebClient and a
 *
 * @see WebService.
 *
 * @author fmauz
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class WebRequestSignature {

    protected String decryptedMessage;
    protected String cryptedMessage;
    protected String signatureAlgorithm;

    /**
     * Default Constructor
     *
     * @param decryptedMessage message in creartext. The message contains 2
     * parts. Part one is a secure unique id, the second part a timestamp in
     * unixtime. They are seperated by an # .
     *
     * @param cryptedMessage Message crypted by the signature algorithm
     * @param signatureAlgorithm Used signature algorithm for signing
     */
    public WebRequestSignature(
            String decryptedMessage,
            String cryptedMessage,
            String signatureAlgorithm) {
        this.decryptedMessage = decryptedMessage;
        this.cryptedMessage = cryptedMessage;
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public WebRequestSignature() {
    }

    /**
     * Get the signature.
     *
     * @return Signature crypted by the specified signature algorithm
     */
    public String getCryptedMessage() {
        return cryptedMessage;
    }

    /**
     * Set the signature
     *
     * @param cryptedMessage
     */
    public void setCryptedMessage(String cryptedMessage) {
        this.cryptedMessage = cryptedMessage;
    }

    /**
     * Gets the clear message. The message contains 2 parts. Part one is a
     * secure unique id, the second part a timestamp in unixtime. They are
     * seperated by an # .
     *
     * @return clear message
     */
    public String getDecryptedMessage() {
        return decryptedMessage;
    }

    /**
     * Sets the clear message
     *
     * @param decryptedMessage
     */
    public void setDecryptedMessage(String decryptedMessage) {
        this.decryptedMessage = decryptedMessage;
    }

    /**
     * Gets the used signature algorithm.
     *
     * @return
     */
    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    /**
     * Sets the used signature algorithm. It will be used to design the
     * signature.
     *
     * @param signatureAlgorithm
     */
    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

}
