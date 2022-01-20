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
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;

import static org.junit.Assert.assertEquals;

/**
 * This class will provide some test cases for the CloudService class.
 */
@ExtendWith(ArquillianExtension.class)
public class CloudServiceTest extends TestBase {

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("CloudServiceTest.war");
    }
    
    /**
     */
    @Test
    public void testCloudService() {
        Cloud c = new Cloud();
        c.setName("unitTestCloud");
        cloudService.save(c);

        Cloud d = cloudService.loadByName(TESTCLOUD);

        assertEquals("testCloudService(): mismatch in cloud name",
                TESTCLOUD, d.getName());
    }

}
