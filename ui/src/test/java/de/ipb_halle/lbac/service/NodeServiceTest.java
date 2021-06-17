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
package de.ipb_halle.lbac.service;

import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.entity.Node;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.util.UUID;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import static org.junit.Assert.assertEquals;

/**
 * This class will provide some test cases for the NodeService class.
 */
@RunWith(Arquillian.class)
public class NodeServiceTest extends TestBase {

    public final static String TEST_NODE_INSTITUTION = "TEST";

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("NodeServiceTest.war");
    }
    
   

    @Inject
    private NodeService nodeService;

   
    /**
     */
    @Test
    public void testNodeService() {
        UUID id = UUID.randomUUID();
        Node n = new Node();
        n.setId(id);
        n.setVersion("TEST-VERSION");
        n.setBaseUrl("http://localhost");
        n.setInstitution("TEST: NodeServiceTest");
        n.setLocal(false);

        this.nodeService.save(n);

        Node m = this.nodeService.loadById(TEST_NODE_ID);

        assertEquals("testNodeService(): mismatch in institution",
                TEST_NODE_INSTITUTION, m.getInstitution());
    }

}
