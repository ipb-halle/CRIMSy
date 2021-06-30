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

import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.datalink.LinkedData;
import de.ipb_halle.lbac.datalink.LinkedDataType;
import de.ipb_halle.lbac.exp.ExperimentBean;
import de.ipb_halle.lbac.exp.ExperimentDeployment;
import de.ipb_halle.lbac.exp.ItemAgent;
import de.ipb_halle.lbac.exp.MaterialAgent;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.biomaterial.BioMaterial;
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
        Assay assay;
        BioMaterial bioMaterial = AssayTest.createBioMaterial("Arabidopsis leaf");
        LinkedData data = new LinkedData(null,
                LinkedDataType.ASSAY_SINGLE_POINT_OUTCOME,
                42);

        // Our assay is not in edit mode, thus the button is not visible.
        assay = new Assay();
        assay.setTarget(bioMaterial);
        assay.getLinkedData().add(data);
        Assert.assertFalse(assay.getEdit());
        Assert.assertFalse(controller.isDiagrammButtonVisible(assay));

        // Assay is now in edit mode, but it has neither results nor a target material.
        assay = new Assay();
        assay.setEdit(true);
        Assert.assertFalse(controller.isDiagrammButtonVisible(assay));

        // Add material to target, but no results.
        assay = new Assay();
        assay.setEdit(true);
        assay.setTarget(bioMaterial);
        Assert.assertFalse(controller.isDiagrammButtonVisible(assay));

        // Add a result record, but no material to the target.
        assay = new Assay();
        assay.setEdit(true);
        assay.getLinkedData().add(data);
        Assert.assertFalse(controller.isDiagrammButtonVisible(assay));

        // All conditions fulfilled, button is visible.
        assay = new Assay();
        assay.setEdit(true);
        assay.setTarget(bioMaterial);
        assay.getLinkedData().add(data);
        Assert.assertTrue(controller.isDiagrammButtonVisible(assay));
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("AssayControllerTest.war");
        return ExperimentDeployment.add(UserBeanDeployment.add(ItemDeployment.add(deployment)));
    }
}
