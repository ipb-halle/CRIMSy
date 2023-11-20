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
package de.ipb_halle.lbac.announcement.membership;

import de.ipb_halle.lbac.admission.AdmissionSubSystemType;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.announcement.membership.mock.MembershipWebServiceMock;
import de.ipb_halle.lbac.admission.MembershipWebClient;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.admission.Group;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.webservice.Updater;
import de.ipb_halle.lbac.webservice.service.WebRequestAuthenticator;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.HashSet;
import java.util.Set;
import jakarta.inject.Inject;
import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.jaxrs.impl.ResponseImpl;


import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


/**
 *
 * @author fmauz
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class MembershipWebClientTest extends TestBase {
    
    @Deployment
    public static WebArchive createDeployment() {
        
        return prepareDeployment("MembershipWebClientTest.war")
                .addClass(WebRequestAuthenticator.class)
                .addPackage(MembershipWebServiceMock.class.getPackage())
                .addClass(Updater.class)
                .addClass(KeyManager.class);
    }
    
    @Inject
    KeyManager keymanager;
    
    @BeforeEach
    public void init() {
        initializeBaseUrl();
        initializeKeyStoreFactory();
    }
    
    @Test
    public void announceUserToRemoteNodesTest() throws Exception {
        MembershipWebClient client = new MembershipWebClient();
        Set<Group> groups = new HashSet<>();
        Group g = new Group();
        g.setId(-1000);
        g.setName("testGroup");
        g.setNode(nodeService.getLocalNode());
        g.setSubSystemData("G");
        g.setSubSystemType(AdmissionSubSystemType.LOCAL);
        
        User u = new User();
        u.setId(-1001);
        u.setName("testUser");
        u.setLogin("testUserLogIn");
        
        CloudNode cn = cloudNodeService.loadCloudNode(TESTCLOUD, TEST_NODE_ID);
        
        client.announceUserToRemoteNodes(
                u,
                cn,
                groups,
                nodeService.getLocalNodeId(),
                keymanager.getLocalPrivateKey(TESTCLOUD)
        );
        MembershipWebServiceMock.SUCCESS = false;
        
        client.announceUserToRemoteNodes(
                u,
                cn,
                groups,
                nodeService.getLocalNodeId(),
                keymanager.getLocalPrivateKey(TESTCLOUD)
        );
        
    }
}
