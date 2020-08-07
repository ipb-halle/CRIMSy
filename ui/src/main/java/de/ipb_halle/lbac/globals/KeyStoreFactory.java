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
package de.ipb_halle.lbac.globals;

import de.ipb_halle.lbac.entity.Cloud;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.NodeService;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

/**
 *
 * @author fbroda
 */
@Stateless
public class KeyStoreFactory {

    // xxxxx LBAC_PROPERTIES_PATH
    // xxxxx this definition should move to some global location
    // xxxxx or should be replaced by a more general approach.
    protected static String LBAC_PROPERTIES_PATH = "/install/etc/lbac_properties.xml";

    private Map<String, KeyStore> keyStore;
    private Map<String, KeyStore> trustStore;
    private char[] keyPass;
    private char[] trustPass;
    protected Logger logger;
    protected String LOCAL_KEY_ALIAS;
    protected String SSL_PROTOCOL;
    protected Properties prop;

    private static KeyStoreFactory instance;

    /**
     * Default Constructor
     */
    private KeyStoreFactory() {
        this.keyStore = new HashMap<String, KeyStore> ();
        this.trustStore = new HashMap<String, KeyStore> ();
        this.logger = LogManager.getLogger(this.getClass().getName());
        init();
    }

    public KeyStoreFactory init() {
        try {
            this.prop = new Properties();
            prop.loadFromXML(Files.newInputStream(Paths.get(LBAC_PROPERTIES_PATH), StandardOpenOption.READ));
            this.keyPass = prop.getProperty("SecureWebClient.KEYSTORE_PASSWORD").toCharArray();
            this.trustPass = prop.getProperty("SecureWebClient.TRUSTSTORE_PASSWORD").toCharArray();
            this.LOCAL_KEY_ALIAS = prop.getProperty("LBAC_INTERNET_FQHN");
            this.SSL_PROTOCOL =  prop.getProperty("SecureWebClient.SSL_PROTOCOL");

        } catch(Exception e) {
            logger.error("Could not initialise KeyStoreFactory", e);
        }
        return this;
    }

    /**
     * Initialize a keystore / truststore pair
     */
    private void load(String cloudName) {
        try {
            KeyStore ks = java.security.KeyStore.getInstance(
                    prop.getProperty("SecureWebClient.KEYSTORE_TYPE"));
            ks.load(Files.newInputStream(Paths.get(
                    prop.getProperty("SecureWebClient.KEYSTORE_PATH"), 
                    cloudName + ".keystore"), StandardOpenOption.READ), keyPass);

            this.keyStore.put(cloudName, ks);

            KeyStore ts = java.security.KeyStore.getInstance(
                    prop.getProperty("SecureWebClient.TRUSTSTORE_TYPE"));
            ts.load(Files.newInputStream(Paths.get( 
                    prop.getProperty("SecureWebClient.TRUSTSTORE_PATH"),
                    cloudName + ".truststore"), StandardOpenOption.READ), trustPass);

            this.trustStore.put(cloudName, ts);

        } catch (Exception e) {
            logger.error("Could not load keystores", e);
        }

    }

    public static KeyStoreFactory getInstance() {
        if (KeyStoreFactory.instance == null) {
            synchronized(KeyStoreFactory.class) {
                if (KeyStoreFactory.instance == null) {
                    KeyStoreFactory.instance = new KeyStoreFactory();
                }
            }
        }
        return KeyStoreFactory.instance;
    }

    public KeyStore getKeyStore(String cloudName) {
        KeyStore ks = this.keyStore.get(cloudName);
        if (ks == null) {
            synchronized(KeyStoreFactory.class) {
                ks = this.keyStore.get(cloudName);
                if (ks == null) {
                    load(cloudName);
                    ks = this.keyStore.get(cloudName);
                }
            }
        }
        return ks;
    }

    public char[] getKeyPass() { 
        return this.keyPass; 
    }

    public KeyStore getTrustStore(String cloudName) {
        KeyStore ts = this.trustStore.get(cloudName);
        if (ts == null) {
            synchronized(KeyStoreFactory.class) {
                ts = this.trustStore.get(cloudName);
                if (ts == null) {
                    load(cloudName);
                    ts = this.trustStore.get(cloudName);
                }
            }
        }
        return ts;
    }


    public char[] getTrustPass() { 
        return this.trustPass; 
    }

    /*
     * @return per convention Internet-FQHN
     */
    public String getLOCAL_KEY_ALIAS() {
        return this.LOCAL_KEY_ALIAS;
    }

    public KeyStoreFactory setLOCAL_KEY_ALIAS(String lka) {
        this.LOCAL_KEY_ALIAS = lka;
        return this;
    }

    public String getSSL_PROTOCOL() {
        return this.SSL_PROTOCOL;
    }

    public static void setLBAC_PROPERTIES_PATH(String path) {
        KeyStoreFactory.LBAC_PROPERTIES_PATH = path;
    }
}
