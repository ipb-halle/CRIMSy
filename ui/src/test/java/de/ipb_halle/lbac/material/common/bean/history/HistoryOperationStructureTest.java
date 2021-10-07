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
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.common.bean.MaterialEditState;
import de.ipb_halle.lbac.material.common.bean.MaterialIndexBean;
import de.ipb_halle.lbac.material.common.bean.MaterialNameBean;
import de.ipb_halle.lbac.material.common.bean.StorageInformationBuilder;
import de.ipb_halle.lbac.material.common.history.HistoryOperation;
import de.ipb_halle.lbac.material.common.service.IndexService;
import de.ipb_halle.lbac.material.structure.MaterialStructureDifference;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.material.mocks.ProjectBeanMock;
import de.ipb_halle.lbac.material.structure.Molecule;
import de.ipb_halle.lbac.material.structure.StructureInformation;
import de.ipb_halle.lbac.material.structure.Structure;
import java.util.ArrayList;
import java.util.Date;
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
public class HistoryOperationStructureTest extends TestBase {

    private static final long serialVersionUID = 1L;

    StructureInformation strucInfo;
    Structure s;
    Date currentDate;
    MaterialEditState mes;
    HistoryOperation instance;
    MaterialStructureDifference sdiff;

    @Inject
    private MaterialService materialService;

    @Before
    public void init() {

        s = new Structure("H2O", 0d, 0d, 0, new ArrayList<>(), 0, new HazardInformation(), new StorageInformation(), new Molecule("h2o", 0));
        currentDate = new Date();
        mes = new MaterialEditState();
        mes.setMaterialBeforeEdit(s);
        mes.setCurrentVersiondate(currentDate);
        strucInfo = new StructureInformation();

        instance = new HistoryOperation(mes, new ProjectBeanMock(), new MaterialNameBean(), new MaterialIndexBean(), strucInfo, new StorageInformationBuilder(MessagePresenterMock.getInstance(), materialService), null, new ArrayList<>(), null);
    }

    @Test
    public void test01_strcutureDifferenceOperations() {

        //################
        //Testcase 1: remove a molecule from the strucutre
        sdiff = new MaterialStructureDifference();
        sdiff.setModificationTime(currentDate);
        Molecule m = new Molecule("H20", 1);

        strucInfo.setAverageMolarMass(12d);

        strucInfo.setAverageMolarMass(12d);
        strucInfo.setExactMolarMass(11d);
        strucInfo.setSumFormula("H2O");
        strucInfo.setStructureModel("xxx-xxx");
        sdiff.setMoleculeId_old(null);
        sdiff.setMoleculeId_new(m);

        s.getHistory().addDifference(sdiff);
        instance.applyNextNegativeDifference();

        Assert.assertNull("Testcase 1 - no molecule found", strucInfo.getStructureModel());
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
