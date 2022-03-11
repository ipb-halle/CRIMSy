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
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.ModificationType;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.common.bean.MaterialEditState;
import de.ipb_halle.lbac.material.common.history.HistoryOperation;
import de.ipb_halle.lbac.material.common.service.IndexService;
import de.ipb_halle.lbac.material.structure.MaterialStructureDifference;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.mocks.MateriaBeanMock;
import de.ipb_halle.lbac.material.structure.Molecule;
import de.ipb_halle.lbac.material.structure.StructureInformation;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
public class HistoryOperationStructureTest extends TestBase {

    private static final long serialVersionUID = 1L;

    StructureInformation strucInfo;
    Structure s;
    Date currentDate;
    MaterialEditState mes;
    HistoryOperation instance;
    private Date d_20001220, d_20001020;

    @Inject
    private MaterialService materialService;

    private MateriaBeanMock materialBean;

    @BeforeEach
    public void init() {
        Calendar c = new GregorianCalendar();
        c.set(2000, 12, 20);
        d_20001220 = c.getTime();
        c.set(2000, 10, 20);
        d_20001020 = c.getTime();
        materialBean = new MateriaBeanMock();
        s = new Structure("H2O", 0d, 0d, 0, new ArrayList<>(), 0, new HazardInformation(), new StorageInformation(), new Molecule("h2o", 0));
        currentDate = new Date();
        mes = new MaterialEditState();
        mes.setMaterialBeforeEdit(s);
        mes.setCurrentVersiondate(currentDate);
        strucInfo = new StructureInformation();

    }

    @Test
    public void test01_strcutureDifferenceOperations() {
        materialBean.getStructureInfos().setAverageMolarMass(18.01528);
        materialBean.getStructureInfos().setExactMolarMass(18.01528);
        materialBean.getStructureInfos().setSumFormula("H2O");
        materialBean.getStructureInfos().setStructureModel("MOL-H2O");
        materialBean.getMaterialEditState().setMaterialToEdit(s);
        materialBean.getMaterialEditState().setMaterialBeforeEdit(s);
        materialBean.getMaterialEditState().setCurrentVersiondate(d_20001220);

        s.getHistory().addDifference(createDiff1());
        s.getHistory().addDifference(createDiff2());

        instance = new HistoryOperation(materialBean);

        instance.applyNextNegativeDifference();

        Assert.assertEquals("MOL-H3O", materialBean.getStructureInfos().getStructureModel());
        Assert.assertEquals("H3O", materialBean.getStructureInfos().getSumFormula());
        Assert.assertEquals(18.01528, materialBean.getStructureInfos().getAverageMolarMass(), 0.001);
        Assert.assertEquals(18.01528, materialBean.getStructureInfos().getExactMolarMass(), 0.001);

        instance.applyNextNegativeDifference();

        Assert.assertNull(materialBean.getStructureInfos().getStructureModel());
        Assert.assertEquals("H3O", materialBean.getStructureInfos().getSumFormula());
        Assert.assertEquals(10, materialBean.getStructureInfos().getAverageMolarMass(), 0.001);
        Assert.assertEquals(11, materialBean.getStructureInfos().getExactMolarMass(), 0.001);

        instance.applyNextPositiveDifference();

        Assert.assertEquals("MOL-H3O", materialBean.getStructureInfos().getStructureModel());
        Assert.assertEquals("H3O", materialBean.getStructureInfos().getSumFormula());
        Assert.assertEquals(18.01528, materialBean.getStructureInfos().getAverageMolarMass(), 0.001);
        Assert.assertEquals(18.01528, materialBean.getStructureInfos().getExactMolarMass(), 0.001);

        instance.applyNextPositiveDifference();

        Assert.assertEquals("MOL-H2O", materialBean.getStructureInfos().getStructureModel());
        Assert.assertEquals("H2O", materialBean.getStructureInfos().getSumFormula());
        Assert.assertEquals(18.01528, materialBean.getStructureInfos().getAverageMolarMass(), 0.001);
        Assert.assertEquals(18.01528, materialBean.getStructureInfos().getExactMolarMass(), 0.001);
    }

    private MaterialStructureDifference createDiff1() {

        MaterialStructureDifference difference = new MaterialStructureDifference();
        difference.setModificationTime(d_20001220);
        difference.setMoleculeId_old(new Molecule("MOL-H3O", 1));
        difference.setMoleculeId_new(new Molecule("MOL-H2O", 2));
        difference.setSumFormula_old("H3O");
        difference.setSumFormula_new("H2O");
        difference.setActorId(1);
        difference.setAction(ModificationType.EDIT);
        difference.setMaterialId(1);
        return difference;
    }

    private MaterialStructureDifference createDiff2() {

        MaterialStructureDifference difference = new MaterialStructureDifference();
        difference.setModificationTime(d_20001020);
        difference.setExactMolarMass_new(18.01528);
        difference.setExactMolarMass_old(11d);
        difference.setMolarMass_new(18.01528);
        difference.setMolarMass_old(10d);
        difference.setMoleculeId_old(null);
        difference.setMoleculeId_new(new Molecule("MOL-H3O", 2));

        difference.setActorId(1);
        difference.setAction(ModificationType.EDIT);
        difference.setMaterialId(1);
        return difference;
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment
                = prepareDeployment("HistoryOperationStructureTest.war")
                        .addClass(IndexService.class);
        deployment = ItemDeployment.add(deployment);
        deployment = UserBeanDeployment.add(deployment);
        return MaterialDeployment.add(PrintBeanDeployment.add(deployment));
    }

}
