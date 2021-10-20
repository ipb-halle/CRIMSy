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
package de.ipb_halle.lbac.material.common.bean.history;

import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.mock.UserBeanMock;
import de.ipb_halle.lbac.base.MaterialCreator;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.device.print.PrintBeanDeployment;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.MaterialDeployment;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.biomaterial.BioMaterial;
import de.ipb_halle.lbac.material.biomaterial.TaxonomySelectionController;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.material.common.bean.MaterialIndexBean;
import de.ipb_halle.lbac.material.common.bean.MaterialEditState;
import de.ipb_halle.lbac.material.common.history.HistoryOperation;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.common.bean.MaterialHazardBuilder;
import de.ipb_halle.lbac.material.common.history.MaterialDifference;
import de.ipb_halle.lbac.material.common.history.MaterialIndexDifference;
import de.ipb_halle.lbac.material.common.service.HazardService;
import de.ipb_halle.lbac.material.common.service.IndexService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.composition.CompositionDifference;
import de.ipb_halle.lbac.material.composition.CompositionType;
import de.ipb_halle.lbac.material.composition.Concentration;
import de.ipb_halle.lbac.material.composition.MaterialComposition;
import de.ipb_halle.lbac.material.composition.MaterialCompositionBean;
import de.ipb_halle.lbac.material.mocks.MaterialBeanMock;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectType;
import de.ipb_halle.lbac.util.Unit;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
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
public class HistoryOperationCompositionTest extends HistoryOperationTest {

    private static final long serialVersionUID = 1L;

    /**
     * ([S2:g:0.7]) -> ([S1:null,0.75],[S2,null,null],[B1,null,0.3]) ->
     * ([S1:g:0.5],[B1:null,null])
     */
    @Test
    public void test01_chechHistoryOperation() {
        checkCurrentState();
        //Go one step back (20.12.2000)
        instance.applyNextNegativeDifference();
        checkStateAt20001220();
        //Go one step back (20.10.2000)
        instance.applyNextNegativeDifference();
        checkStateAt20001020();
        //Go one step back (20.12.2000)
        instance.applyNextPositiveDifference();
        checkStateAt20001220();
        //Go one step back (now)
        instance.applyNextPositiveDifference();
        checkCurrentState();
    }

    @Override
    protected void checkCurrentState() {
        Assert.assertEquals(2, compositionBean.getConcentrationsInComposition().size());
        Assert.assertEquals(0.5d, getConcentrationByMaterialId(structureId1).getConcentration(), 0.01);
        Assert.assertEquals("g", getConcentrationByMaterialId(structureId1).getUnit().toString());
        Assert.assertNull(getConcentrationByMaterialId(biomaterialId).getConcentration());
        Assert.assertNull(getConcentrationByMaterialId(biomaterialId).getUnit());

    }

    @Override
    protected void checkStateAt20001020() {
        Assert.assertEquals(1, compositionBean.getConcentrationsInComposition().size());
        Assert.assertEquals(0.7d, getConcentrationByMaterialId(structureId2).getConcentration(), 0.01);
        Assert.assertEquals("g", getConcentrationByMaterialId(structureId2).getUnit().toString());
    }

    @Override
    protected void checkStateAt20001220() {
        Assert.assertEquals(3, compositionBean.getConcentrationsInComposition().size());
        Assert.assertEquals(0.75d, getConcentrationByMaterialId(structureId1).getConcentration(), 0.01);
        Assert.assertNull(getConcentrationByMaterialId(structureId1).getUnit());
        Assert.assertEquals(0.3d, getConcentrationByMaterialId(biomaterialId).getConcentration(), 0.01);
        Assert.assertNull(getConcentrationByMaterialId(biomaterialId).getUnit());
        Assert.assertNull(getConcentrationByMaterialId(structureId2).getConcentration());
        Assert.assertNull(getConcentrationByMaterialId(structureId2).getUnit());

    }

    private Concentration getConcentrationByMaterialId(int id) {
        for (Concentration c : compositionBean.getConcentrationsInComposition()) {
            if (c.getMaterialId() == id) {
                return c;
            }
        }
        return null;
    }

    @Override
    protected CompositionDifference createDiffAt20001020() {
        CompositionDifference diff = new CompositionDifference("EDIT");
        diff.initialise(0, publicUser.getId(), d_20001020);

        diff.addDifference(null, structureId1, null, 0.75d, null, null);
        diff.addDifference(null, biomaterialId, null, 0.3d, null, null);
        diff.addDifference(structureId2, structureId2, 0.7d, null, "g", null);
        return diff;
    }

    @Override
    protected MaterialDifference createDiffAt20001220() {
        CompositionDifference diff = new CompositionDifference("EDIT");
        diff.initialise(0, publicUser.getId(), d_20001220);
        diff.addDifference(structureId1, structureId1, 0.75d, 0.5d, null, "g");
        diff.addConcentrationDifference(biomaterialId, 0.3d, null);
        diff.addDifference(structureId2, null, null, null, null, null);
        return diff;
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment
                = prepareDeployment("HistoryOperationBiomaterialTest.war")
                        .addClass(IndexService.class);
        deployment = ItemDeployment.add(deployment);
        deployment = UserBeanDeployment.add(deployment);

        return MaterialDeployment.add(PrintBeanDeployment.add(deployment));
    }
}
