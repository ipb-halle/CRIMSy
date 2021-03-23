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
package de.ipb_halle.lbac.exp.assay;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.exp.ExperimentBean;
import de.ipb_halle.lbac.exp.ExperimentDeployment;
import de.ipb_halle.lbac.exp.ItemAgent;
import de.ipb_halle.lbac.exp.MaterialAgent;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
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
public class AssayControllerTest extends TestBase {

    @Test
    public void test001_isDiagrammButtonVisible() {

        ExperimentBean bean = new ExperimentBean(
                new ItemAgent(),
                new MaterialAgent(),
                context,
                null, null, new MessagePresenterMock(), null
        );

        bean.init();
        AssayController controller = new AssayController(bean);
        Assay assay = new Assay();
        Assert.assertFalse(controller.isDiagrammButtonVisible(assay));
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("AssayControllerTest.war");
        return ExperimentDeployment.add(UserBeanDeployment.add(ItemDeployment.add(deployment)));
    }
}