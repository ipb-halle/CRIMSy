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

import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.NodeService;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.util.Base64;
import java.util.Properties;
import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

/**
 *
 * @author fmauz
 */
@Stateless
public class KeyManager {

    protected String LBAC_PROPERTIES_PATH = "/install/etc/lbac_properties.xml";

    protected Logger logger;

    @Inject
    private NodeService nodeService;

    @Inject
    private CloudNodeService cloudNodeService;

    public KeyManager() {
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    public PrivateKey getLocalPrivateKey(String cloudName) throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        KeyStoreFactory ksf = KeyStoreFactory.getInstance();
        Key key = ksf.getKeyStore(cloudName).getKey(
                ksf.getLOCAL_KEY_ALIAS(),
                ksf.getKeyPass()
        );
        if (key instanceof PrivateKey) {
            return (PrivateKey) key;
        }
        throw new KeyStoreException("Could not get private key with alias " + ksf.getLOCAL_KEY_ALIAS());
    }

    public PublicKey getLocalPublicKey(String cloudName) throws KeyStoreException, NoSuchAlgorithmException, NoSuchFileException, UnrecoverableKeyException {
        KeyStoreFactory ksf = KeyStoreFactory.getInstance();
        Certificate cert = ksf.getKeyStore(cloudName).getCertificate(ksf.getLOCAL_KEY_ALIAS());
        if (cert != null) {
            return cert.getPublicKey();
        }
        throw new KeyStoreException("Could not get public key with alias " + ksf.getLOCAL_KEY_ALIAS());
    }

    public void updatePublicKeyOfLocalNode() throws KeyStoreException, NoSuchAlgorithmException, UnrecoverableKeyException {
        for(CloudNode cn : this.cloudNodeService.load(null, this.nodeService.getLocalNode())) {
            try {
                PublicKey publicKey = getLocalPublicKey(cn.getCloud().getName());
                cn.setPublicKey(Base64.getEncoder().encodeToString(publicKey.getEncoded()));
                this.cloudNodeService.save(cn);
            } catch(NoSuchFileException e) {
                this.logger.warn("updatePublicKeyOfLocalNode(): missing keystore for cloud " + cn.getCloud().getName());
            }
        }
    }
}
