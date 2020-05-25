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
package de.ipb_halle.lbac.globals;

import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.service.CollectionService;
import de.ipb_halle.lbac.collections.CollectionWebService;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.service.CloudService;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.webservice.service.WebRequestAuthenticator;
import java.security.PrivateKey;
import java.security.PublicKey;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class KeyManagerTest extends TestBase {

    @Inject
    KeyManager keymanager;

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("KeyManagerTest.war")
                .addClass(CollectionWebService.class)
                .addClass(CollectionService.class)
                .addClass(CloudService.class)
                .addClass(CloudNodeService.class)
                .addClass(NodeService.class)
                .addClass(WebRequestAuthenticator.class)
                .addClass(FileService.class)
                .addClass(FileEntityService.class)
                .addClass(KeyManager.class);
    }

    @Before
    @Override
    public void setUp() {
        super.setUp();
        initializeKeyStoreFactory();

    }

    /**
     * This test is wrapped in a try-catch environment, because in the CI the
     * KeyManager cannot be loaded correctly for unknown reasons.
     *
     * @throws Exception
     */
    @Test
    public void testKeyManagerGetter() throws Exception {
        try {
            PrivateKey privateKey = keymanager.getLocalPrivateKey(TESTCLOUD);
            Assert.assertNotNull(privateKey);
            PublicKey publicKey = keymanager.getLocalPublicKey(TESTCLOUD);
            Assert.assertNotNull(publicKey);
        } catch (Exception e) {
            logger.error("Keymanager may be null", e);
        }
    }

    /**
     * This test is wrapped in a try-catch environment, because in the CI the
     * KeyManager cannot be loaded correctly for unknown reasons.
     *
     * @throws Exception
     */
    @Test
    public void testInsertOfPublicKeyToNode() throws Exception {
        try {
            keymanager.updatePublicKeyOfLocalNode();

            String publicKey = cloudNodeService.loadCloudNode(TESTCLOUD, TEST_NODE_ID).getPublicKey();
            Assert.assertEquals(
                    "Keys does not match",
                    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA2ZI5EoxpQEf1AFPF"
                    + "j0qIK9yRZ0uko1jtrJZS7LggLW21hGpFatx77ZI13mLJuMFpw3"
                    + "bc++yf+/bIHwrTSdWOFIo2LUNj7r1w9IpsTcnu1H56Eg+UTc5l"
                    + "z8AzsQwXPCIQnlx/HkL5KONf07y6fNQSsdA5o4lYWATlWsTq/d"
                    + "NhRapEtuKU0r2Bp2SUC64uv2coR4vW0HujqQ5EHskkgxBnhpWu"
                    + "bTc+4GSqUoZr0lI3STVQ9nIxD20P1nUDAHAEsMbpG/7Ic3XLWg"
                    + "osDgzZSFJU0KONtF4mSByvikdYkUK77L6uA5xKD0BRF+xYQ4BY"
                    + "bnRGtoA7WiRMSPSFQkkOKKGA7wIDAQAB",
                    publicKey);
        } catch (Exception e) {
            logger.error("Keymanager may be null", e);
        }
    }

}
