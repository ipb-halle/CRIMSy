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
import de.ipb_halle.lbac.entity.Cloud;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.inject.Inject;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This class will provide some test cases for the CloudService class.
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class CloudNodeServiceTest extends TestBase {

    public static int TEST_MASTER_NODE_RANK = 10;
    public static String TEST_CLOUDNODESERVICE_CLOUD = "CloudNodeServiceTestCloud";

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("CloudNodeServiceTest.war");
    }
   
    /**
     */
    @Test
    public void testCloudNodeService() {
        
        Cloud cloud = cloudService.save(new Cloud(TEST_CLOUDNODESERVICE_CLOUD));
        Node node = nodeService.loadById(TEST_NODE_ID);
        CloudNode cloudNode = cloudNodeService.save(new CloudNode(cloud, node));

        assertEquals("testCloudNodeService(): number of clouds for builtin node mismatch", 2, cloudNodeService.load(null, node).size());

        CloudNode cn =  cloudNodeService.save(new CloudNode(cloud, createNode(nodeService, "No Key!")));

        cloudNode.setRank(TEST_MASTER_NODE_RANK);
        cloudNode = cloudNodeService.save(cloudNode);

        /* cloud node record from init.sql file (builtin) */
        Cloud d = cloudService.loadByName(TESTCLOUD);
        List<CloudNode> cnl = cloudNodeService.load(d, null);
        assertEquals("testCloudNodeService(): name mismatch for builtin cloud", TESTCLOUD, d.getName());
        assertTrue("testCloudNodeService(): number cloud nodes to small for builtin cloud", 1 <= cnl.size());
        cn = cnl.get(0);
        cn.setRank(TEST_MASTER_NODE_RANK);
        cn = cloudNodeService.save(cn);

        d = cloudService.loadByName(TEST_CLOUDNODESERVICE_CLOUD);
        cnl = cloudNodeService.load(d, null);
        assertEquals("testCloudNodeService(): number of nodes in cloud 'CloudNodeServiceTestCloud' mismatch", 2, cnl.size());
        ListIterator<CloudNode> li = cnl.listIterator();
        while(li.hasNext()) {
            cn = li.next();
            if (cn.getNode().getId().equals(TEST_NODE_ID)) {
                node = cn.getNode();
                assertEquals("testCloudNodeService(): mismatch in master node rank",
                    TEST_MASTER_NODE_RANK, (int) cn.getRank());
            } else {
                assertEquals("testCloudNodeService(): mismatch in slave node rank",
                    1, (int) cn.getRank());
            }
        }
        assertEquals("testCloudNodeService(): master node mismatch", node,
            cloudNodeService.loadMasterNode(d));
    }

}
