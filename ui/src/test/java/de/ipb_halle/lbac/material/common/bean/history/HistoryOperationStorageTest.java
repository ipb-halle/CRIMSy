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

import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.device.print.PrintBeanDeployment;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.MaterialDeployment;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.HazardType;
import de.ipb_halle.lbac.material.common.bean.MaterialEditState;
import de.ipb_halle.lbac.material.common.bean.MaterialHazardBuilder;
import de.ipb_halle.lbac.material.common.history.HistoryOperation;
import de.ipb_halle.lbac.material.common.history.MaterialDifference;
import de.ipb_halle.lbac.material.common.history.MaterialHazardDifference;
import de.ipb_halle.lbac.material.common.service.HazardService;
import de.ipb_halle.lbac.material.common.service.IndexService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.composition.CompositionType;
import de.ipb_halle.lbac.material.composition.MaterialComposition;
import de.ipb_halle.lbac.material.mocks.MaterialBeanMock;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.material.structure.StructureInformation;
import de.ipb_halle.lbac.project.Project;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
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
public class HistoryOperationStorageTest extends TestBase {

    private static final long serialVersionUID = 1L;

    StructureInformation strucInfo;
    MaterialComposition composition;
    Date currentDate;
    MaterialEditState mes;
    HistoryOperation instance;
    private Date d_20001220, d_20001020;
    @Inject
    private HazardService hazardService;

    private MaterialBeanMock materialBean;
    private int projectId = 0;
    private HazardType radioactivity, explosive, flammable, toxic;
    private HazardType hStatement, pStatement;
    private HazardType bioSavetyLevel0, bioSavetyLevel1;
    private HazardType customRemarks;

    @Before
    public void init() {
        radioactivity = hazardService.getHazardOf(HazardType.Category.RADIOACTIVITY).get(0);
        hStatement = hazardService.getHazardOf(HazardType.Category.STATEMENTS).get(0);
        pStatement = hazardService.getHazardOf(HazardType.Category.STATEMENTS).get(1);
        toxic = hazardService.getHazardById(7);
        flammable = hazardService.getHazardById(3);
        explosive = hazardService.getHazardById(1);
        bioSavetyLevel0 = hazardService.getHazardOf(HazardType.Category.BSL).get(3);
        bioSavetyLevel1 = hazardService.getHazardOf(HazardType.Category.BSL).get(0);
        customRemarks = hazardService.getHazardOf(HazardType.Category.CUSTOM).get(0);
        Calendar c = new GregorianCalendar();
        c.set(2000, 12, 20);
        d_20001220 = c.getTime();
        c.set(2000, 10, 20);
        d_20001020 = c.getTime();
        materialBean = new MaterialBeanMock();
        composition = new MaterialComposition(projectId, CompositionType.EXTRACT);
        composition.getHazards().getHazards().put(explosive, null);
        composition.getHazards().getHazards().put(flammable, null);

        composition.getHazards().getHazards().put(hStatement, "H-Statement");
        composition.getHazards().getHazards().put(pStatement, "P-Statement");
        composition.getHazards().getHazards().put(radioactivity, null);
        composition.getHazards().getHazards().put(bioSavetyLevel0, null);
        composition.getHazards().getHazards().put(customRemarks, "custom Statement");

        composition.getHistory().addDifference(createDiffAt20001220());
        composition.getHistory().addDifference(createDiffAt20001020());

        currentDate = new Date();
        materialBean.setHazardService(hazardService);
        materialBean.setHazardController(new MaterialHazardBuilder(
                hazardService,
                MaterialType.COMPOSITION,
                true,
                composition.getHazards().getHazards(),
                MessagePresenterMock.getInstance()));

        mes = new MaterialEditState(
                new Project(),
                d_20001220,
                composition,
                composition,
                materialBean.getHazardController());

        materialBean.setMaterialEditState(mes);
        strucInfo = new StructureInformation();
        instance = new HistoryOperation(materialBean);

    }

    /**
     * current state -> [explosive,flammable,radioactive,h- & p-statements
     * set,custom remark set,biosavety X] 20.12.2000 ->[toxic, only p-statement
     * set, no custom remark, biosavety Y] 20.10.2000 ->[no hazards, no
     * statements, no custom remark, biosavety Z]
     */
    @Test
    public void test01_strcutureDifferenceOperations() {
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

    private void checkCurrentState() {
        Assert.assertEquals("H-Statement", materialBean.getHazardController().gethStatements());
        Assert.assertEquals("P-Statement", materialBean.getHazardController().getpStatements());
        Assert.assertTrue(materialBean.getHazardController().isRadioctive());
        Assert.assertEquals("hazard_S0", materialBean.getHazardController().getBioSavetyLevel());
        List hazardIds = Arrays.stream(materialBean.getHazardController().getSelectedHazards()).map(h -> h.getId()).collect(Collectors.toList());
        Assert.assertTrue(hazardIds.contains(1));
        Assert.assertTrue(hazardIds.contains(3));
    }

    private void checkStateAt20001020() {
        Assert.assertNull(materialBean.getHazardController().gethStatements());
        Assert.assertNull(materialBean.getHazardController().getpStatements());
        Assert.assertFalse(materialBean.getHazardController().isRadioctive());
        Assert.assertEquals("hazard_S2", materialBean.getHazardController().getBioSavetyLevel());
        List hazardIds = Arrays.stream(materialBean.getHazardController().getSelectedHazards()).map(h -> h.getId()).collect(Collectors.toList());
        Assert.assertEquals(1, hazardIds.size());
    }

    private void checkStateAt20001220() {
        Assert.assertNull(materialBean.getHazardController().gethStatements());
        Assert.assertEquals("P-Statement-20001220", materialBean.getHazardController().getpStatements());
        Assert.assertFalse(materialBean.getHazardController().isRadioctive());
        Assert.assertEquals("hazard_S0", materialBean.getHazardController().getBioSavetyLevel());
        List hazardIds = Arrays.stream(materialBean.getHazardController().getSelectedHazards()).map(h -> h.getId()).collect(Collectors.toList());
        Assert.assertTrue(hazardIds.contains(7));
    }

    private MaterialDifference createDiffAt20001020() {
        MaterialHazardDifference diff = new MaterialHazardDifference();
        diff.addHazardExpansion(toxic.getId(), null);
        diff.addHazardExpansion(bioSavetyLevel0.getId(), null);
        diff.addHazardRemovement(bioSavetyLevel1.getId(), null);
        diff.addHazardExpansion(customRemarks.getId(), "custom Statement");
        diff.addHazardExpansion(pStatement.getId(), "P-Statement-20001220");
        diff.initialise(0, publicUser.getId(), d_20001020);
        return diff;
    }

    private MaterialDifference createDiffAt20001220() {
        MaterialHazardDifference diff = new MaterialHazardDifference();
        diff.addHazardExpansion(explosive.getId(), null);
        diff.addHazardExpansion(flammable.getId(), null);
        diff.addHazardExpansion(hStatement.getId(), "H-Statement");
        diff.addHazardExpansion(radioactivity.getId(), null);
        diff.addHazardRemovement(toxic.getId(), null);
        diff.addDifference(pStatement.getId(), pStatement.getId(), "P-Statement-20001220", "P-Statement");
        diff.initialise(0, publicUser.getId(), d_20001220);
        return diff;
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment
                = prepareDeployment("HistoryOperationStorageTest.war")
                        .addClass(IndexService.class);
        deployment = ItemDeployment.add(deployment);
        deployment = UserBeanDeployment.add(deployment);
        return MaterialDeployment.add(PrintBeanDeployment.add(deployment));
    }

}
