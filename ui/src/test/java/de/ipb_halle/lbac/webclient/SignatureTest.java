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

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Properties;
import javax.crypto.NoSuchPaddingException;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

/**
 * Testclass for examples of usage of signature, public and private keys
 *
 * @author fmauz
 */
public class SignatureTest {

    String LBAC_PROPERTIES_PATH = "target/test-classes/keystore/lbac_properties.xml";
    String KEYSTORE_FILE = "TESTCLOUD.keystore";

    @Test
    public void signatureTest()
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();

        String algoType = "SHA256withRSA";
        String message = "Hello World";

        Signature rsa = Signature.getInstance(algoType);
        rsa.initSign(privateKey);
        rsa.update(message.getBytes());
        byte[] signature = rsa.sign();

        rsa = Signature.getInstance(algoType);
        rsa.initVerify(publicKey);
        rsa.update(message.getBytes());

        Assert.assertTrue(rsa.verify(signature));

    }

    /**
     * Test for fetching the public RSA key from the keystore
     *
     * @throws Exception
     */
    @Test
    public void getPublicKeyFromKeyStore()
            throws Exception {

        Properties prop = new Properties();
        InputStream is = new FileInputStream(LBAC_PROPERTIES_PATH);
        prop.loadFromXML(is);

        char[] keyPass = prop.getProperty("SecureWebClient.KEYSTORE_PASSWORD").toCharArray();

        KeyStore ks = java.security.KeyStore.getInstance(
                prop.getProperty("SecureWebClient.KEYSTORE_TYPE"));
        ks.load(Files.newInputStream(Paths.get(
                prop.getProperty("SecureWebClient.KEYSTORE_PATH"), 
                KEYSTORE_FILE), StandardOpenOption.READ), keyPass);
    }

    /**
     * Get hexadecimal representation of the bytearray of secure random number
     *
     * @param bytes secureRandomNumber in byte represantation
     * @return SecureRandomNumber in hexadecimal represantation
     */
    protected String getGuidFromByteArray(byte[] bytes) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            buffer.append(String.format("%02x", bytes[i]));
        }
        return buffer.toString();
    }

}
