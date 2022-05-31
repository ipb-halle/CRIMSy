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
package de.ipb_halle.lbac.admission.group;

import de.ipb_halle.lbac.admission.AdmissionSubSystemType;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.admission.Group;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.group.mock.DeactivateGroupWebClientMock;
import de.ipb_halle.lbac.admission.group.mock.DeactivateGroupWebServiceMock;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.webservice.Updater;
import de.ipb_halle.lbac.webservice.service.WebRequestAuthenticator;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import javax.inject.Inject;

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
public class DeactivateGroupOrchestratorTest extends TestBase {

    @Inject
    private DeactivateGroupOrchestrator orchestrator;

    @Test
    public void deactivateGroup() throws Exception {
        User user =createUser();
        Group group =createGroup();
        orchestrator.startGroupDeactivation(group, user);
    }

    @BeforeEach
    public void init() {
        initializeBaseUrl();
        initializeKeyStoreFactory();
    }

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("DeactivateGroupOrchestratorTest.war")
                .addClass(WebRequestAuthenticator.class)
                .addClass(DeactivateGroupWebClientMock.class)
                .addClass(Updater.class)
                .addClass(KeyManager.class)
                .addClass(DeactivateGroupOrchestrator.class)
                .addClass(FileService.class)
                .addClass(FileEntityService.class);
    }

    private Group createGroup() {
        Group g = new Group();
        g.setId(-1000);
        g.setName("testGroup");
        g.setNode(nodeService.getLocalNode());
        g.setSubSystemData("G");
        g.setSubSystemType(AdmissionSubSystemType.LOCAL);
        return g;
    }

    private User createUser() {
        User u = new User();
        u.setId(-1001);
        u.setName("testUser");
        u.setLogin("testUserLogIn");
        return u;
    }
}
