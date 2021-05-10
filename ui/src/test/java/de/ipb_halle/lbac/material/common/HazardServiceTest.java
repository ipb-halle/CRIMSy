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
package de.ipb_halle.lbac.material.common;

import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.material.MaterialDeployment;
import de.ipb_halle.lbac.material.common.service.HazardService;
import java.util.List;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class HazardServiceTest extends TestBase {

    @Inject
    private HazardService hazardService;

    @Test
    public void test001_loadHazards() {
        List<HazardType> hazards = hazardService.getHazardOf(HazardType.Category.GHS);
        Assert.assertEquals(9, hazards.size());
        hazards = hazardService.getHazardOf(HazardType.Category.STATEMENTS);
        Assert.assertEquals(2, hazards.size());
        hazards = hazardService.getHazardOf(HazardType.Category.BSL);
        Assert.assertEquals(4, hazards.size());
        hazards = hazardService.getHazardOf(HazardType.Category.RADIOACTIVITY);
        Assert.assertEquals(1, hazards.size());
        hazards = hazardService.getHazardOf(HazardType.Category.CUSTOM);
        Assert.assertEquals(1, hazards.size());
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("HazardServiceTest.war");
        return MaterialDeployment.add(deployment);
    }
}
