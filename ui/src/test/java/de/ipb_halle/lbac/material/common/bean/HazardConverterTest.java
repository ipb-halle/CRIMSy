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
package de.ipb_halle.lbac.material.common.bean;

import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.device.print.PrintBeanDeployment;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.material.common.service.IndexService;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.MaterialDeployment;
import de.ipb_halle.lbac.material.common.HazardType;
import de.ipb_halle.lbac.material.common.service.HazardService;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author fmauz
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class HazardConverterTest extends TestBase {

    private static final long serialVersionUID = 1L;

    @Inject
    private HazardService hazardService;

    @BeforeEach
    public void init() {

    }

    @Test
    public void test001_stringToObject() {
        HazardConverter conv = new HazardConverter();
        Assert.assertNull(conv.getAsObject(null, null, null));
        Assert.assertNull(conv.getAsObject(null, null, "non existing"));
        Assert.assertNotNull(conv.getAsObject(null, null, "GHS01"));
    }

    @Test
    public void test002_objectToString() {
        HazardType hazard = hazardService.getHazardById(3);
        HazardConverter conv = new HazardConverter();
        Assert.assertEquals("", conv.getAsString(null, null, null));
        Assert.assertEquals("", conv.getAsString(null, null, new Integer(2)));
        Assert.assertEquals(hazard.getName(), conv.getAsString(null, null, hazard));
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment
                = prepareDeployment("MaterialBeanTest.war")
                        .addClass(IndexService.class);
        deployment = ItemDeployment.add(deployment);
        deployment = UserBeanDeployment.add(deployment);
        return MaterialDeployment.add(PrintBeanDeployment.add(deployment));
    }
}
