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
import de.ipb_halle.lbac.material.MaterialBeanDeployment;
import de.ipb_halle.lbac.material.MaterialDeployment;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.bean.MaterialIndexBean;
import de.ipb_halle.lbac.material.common.bean.MaterialEditState;
import de.ipb_halle.lbac.material.common.history.HistoryOperation;
import de.ipb_halle.lbac.material.structure.Molecule;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.HazardType;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.common.bean.MaterialHazardBuilder;
import de.ipb_halle.lbac.material.common.history.MaterialHazardDifference;
import de.ipb_halle.lbac.material.common.history.MaterialIndexDifference;
import de.ipb_halle.lbac.material.common.service.HazardService;
import de.ipb_halle.lbac.material.common.service.IndexService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.composition.MaterialCompositionBean;
import de.ipb_halle.lbac.material.mocks.MateriaBeanMock;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import jakarta.inject.Inject;
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
public class HistoryOperationHazardsTest extends TestBase {

    private static final long serialVersionUID = 1L;

    @Inject
    private HazardService hazardService;
    @Inject
    private MaterialService materialService;
    List<IndexEntry> indices;
    Structure s;
    Date currentDate;
    MaterialEditState mes;
    HistoryOperation instance;
    MaterialIndexDifference mid;
    MaterialIndexBean mib;
    Random random = new Random();

    private MaterialCompositionBean compositionBean;

    @BeforeEach
    public void init() {
        indices = new ArrayList<>();
        s = new Structure("H2O", 0d, 0d, 0, new ArrayList<>(), 0, new HazardInformation(), new StorageInformation(), new Molecule("h2o", 0));
        currentDate = new Date();
        mes = new MaterialEditState(MessagePresenterMock.getInstance());
        mes.setMaterialBeforeEdit(s);
        mes = new MaterialEditState(
                new Project(),
                currentDate,
                s,
                s,
                new MaterialHazardBuilder(hazardService, MaterialType.BIOMATERIAL, true, new HashMap<>(), MessagePresenterMock.getInstance()), MessagePresenterMock.getInstance());

        mes.setCurrentVersiondate(currentDate);
        mib = new MaterialIndexBean();
        s.setIndices(indices);
        mid = new MaterialIndexDifference();
        List<HazardType> possibleHazards = new ArrayList<>();
        possibleHazards.add(new HazardType(5, false, "GHS05", 1));
        possibleHazards.add(new HazardType(7, false, "GHS05", 1));
        possibleHazards.add(new HazardType(8, false, "GHS05", 1));
        possibleHazards.add(new HazardType(10, false, "GHS05", 1));
        possibleHazards.add(new HazardType(11, false, "GHS05", 1));
        mid.initialise(0, random.nextInt(100000), currentDate);
        MateriaBeanMock mock = new MateriaBeanMock();
        mock.setMaterialEditState(mes);
        mock.setHazardService(hazardService);
        instance = new HistoryOperation(mock);
    }

    /**
     * Description: The material is created without a Hazard. After that the
     * Hazard 'corrosive' is added and then at a later time the hazards
     * 'irritant' and 'unhealthy' are added and 'corrosive' removed, h and
     * p-statements are added.
     */
    @Test
    public void test01_HazardDifferenceOperations() {
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        Integer userID = random.nextInt(100000);

        //First edit: add corrosive
        MaterialHazardDifference d1 = new MaterialHazardDifference();
        d1.addDifference(null, 5, null, null);
        c.add(Calendar.MONTH, -2);
        Date date1 = c.getTime();
        d1.initialise(s.getId(), userID, date1);
        s.getHistory().addDifference(d1);

        //Second edit: add irritant and unhealthy, remove corrosive and 
        // add a precautionary statement with a remark
        MaterialHazardDifference d2 = new MaterialHazardDifference();
        d2.addDifference(null, 8, null, null);
        d2.addDifference(null, 7, null, null);
        d2.addDifference(null, 10, null, "h - test statement");
        d2.addDifference(null, 11, null, "p - test statement");
        d2.addDifference(5, null, null, null);
        c.add(Calendar.MONTH, 1);
        Date date2 = c.getTime();
        d2.initialise(s.getId(), userID, date2);
        s.getHistory().addDifference(d2);

        //create current state 
        mes.getHazardController().addHazardType(new HazardType(7, false, "GHS07", 1), null);
        mes.getHazardController().addHazardType(new HazardType(8, false, "GHS08", 1), null);

        mes.getHazardController().addHazardType(new HazardType(10, true, "h", 2), "H-Statement");
        mes.getHazardController().addHazardType(new HazardType(11, true, "p", 2), "P-Statement");
        mes.setCurrentVersiondate(date2);

        // apply second edit: (irritant,unhealthy) -> (corrosive)
        instance.applyNextNegativeDifference();
        Assert.assertEquals(1, mes.getHazardController().buildHazardsMap().size());
        Assert.assertTrue(mes.getHazardController().buildHazardsMap().keySet().contains(new HazardType(5, false, "GHS05", 1)));

        // apply first edit: (corrosive) -> ( )
        instance.applyNextNegativeDifference();
        Assert.assertTrue(mes.getHazardController().buildHazardsMap().isEmpty());

        // apply first edit: ( )  -> (corrosive) 
        instance.applyNextPositiveDifference();
        Assert.assertEquals(1, mes.getHazardController().buildHazardsMap().size());
        Assert.assertTrue(mes.getHazardController().buildHazardsMap().keySet().contains(new HazardType(5, false, "GHS05", 1)));

        // apply second edit: (corrosive) -> (irritant,unhealthy)
        instance.applyNextPositiveDifference();
        Assert.assertEquals(4, mes.getHazardController().buildHazardsMap().size());
        Assert.assertTrue(mes.getHazardController().buildHazardsMap().keySet().contains(new HazardType(7, false, "GHS05", 1)));
        Assert.assertTrue(mes.getHazardController().buildHazardsMap().keySet().contains(new HazardType(8, false, "GHS05", 1)));
        Assert.assertTrue(mes.getHazardController().buildHazardsMap().keySet().contains(new HazardType(10, false, "GHS05", 1)));
        Assert.assertTrue(mes.getHazardController().buildHazardsMap().keySet().contains(new HazardType(11, false, "GHS05", 1)));

    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment
                = prepareDeployment("HistoryOperationHazardsTest.war")
                        .addClass(IndexService.class);
        deployment = ItemDeployment.add(deployment);
        deployment = UserBeanDeployment.add(deployment);
        return MaterialDeployment.add(PrintBeanDeployment.add(deployment));
    }
}
