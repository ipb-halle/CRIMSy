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
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.device.print.PrintBeanDeployment;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.MaterialDeployment;
import de.ipb_halle.lbac.material.common.StorageCondition;
import de.ipb_halle.lbac.material.common.history.MaterialStorageDifference;
import de.ipb_halle.lbac.material.common.service.IndexService;
import java.util.ArrayList;
import java.util.Arrays;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author fmauz
 */
@ExtendWith(ArquillianExtension.class)
public class HistoryOperationStorageInformationTest extends HistoryOperationTest {

    private static final long serialVersionUID = 1L;

    @Test
    public void test01_checkHistoryOperation() {
        checkCurrentState();
        //Go one step back (20.12.2000)
        materialBeanMock.switchOneVersionBack();
        checkStateAt20001220();
        //Go one step back (20.10.2000)
        materialBeanMock.switchOneVersionBack();
        checkStateAt20001020();
        //Go one step back (20.12.2000)
        materialBeanMock.switchOneVersionForward();
        checkStateAt20001220();
        //Go one step back (now)
        materialBeanMock.switchOneVersionForward();
        checkCurrentState();
    }

    @Override
    protected void checkCurrentState() {
        Assert.assertEquals(3, materialBeanMock.getStorageInformationBuilder().getChoosenStorageClass().getId(), 0);
        Assert.assertTrue(materialBeanMock.getStorageInformationBuilder().isStorageClassActivated());
        Assert.assertEquals("materialCreation_storageclass_2B",materialBeanMock.getStorageInformationBuilder().getChoosenStorageClass().getName());
        Assert.assertEquals(2, materialBeanMock.getStorageInformationBuilder().getSelectedConditions().length);
        Assert.assertTrue(Arrays.asList(materialBeanMock.getStorageInformationBuilder().getSelectedConditions()).contains(StorageCondition.keepTempBelowMinus80Celsius));
        Assert.assertTrue(Arrays.asList(materialBeanMock.getStorageInformationBuilder().getSelectedConditions()).contains(StorageCondition.keepFrozen));
        Assert.assertTrue(materialBeanMock.getStorageInformationBuilder().isConditionEditable());
        Assert.assertTrue(materialBeanMock.getStorageInformationBuilder().isStorageClassRendered());

    }

    @Override
    protected void checkStateAt20001020() {
        Assert.assertTrue(materialBeanMock.getStorageInformationBuilder().isStorageClassActivated());
        Assert.assertEquals(2, materialBeanMock.getStorageInformationBuilder().getChoosenStorageClass().getId(), 0);
        Assert.assertEquals("materialCreation_storageclass_2A",materialBeanMock.getStorageInformationBuilder().getChoosenStorageClass().getName());
        Assert.assertEquals(1, materialBeanMock.getStorageInformationBuilder().getSelectedConditions().length);
        Assert.assertTrue(Arrays.asList(materialBeanMock.getStorageInformationBuilder().getSelectedConditions()).contains(StorageCondition.keepMoist));
        Assert.assertFalse(materialBeanMock.getStorageInformationBuilder().isConditionEditable());
        Assert.assertTrue(materialBeanMock.getStorageInformationBuilder().isStorageClassRendered());
    }

    @Override
    protected void checkStateAt20001220() {
        Assert.assertTrue(materialBeanMock.getStorageInformationBuilder().isStorageClassDisabled());
        Assert.assertEquals(0, materialBeanMock.getStorageInformationBuilder().getSelectedConditions().length);
        Assert.assertFalse(materialBeanMock.getStorageInformationBuilder().isConditionEditable());
        Assert.assertFalse(materialBeanMock.getStorageInformationBuilder().isStorageClassRendered());
    }

    @Override
    protected MaterialStorageDifference createDiffAt20001020() {
        MaterialStorageDifference diff = new MaterialStorageDifference();
        diff.removeCondition(StorageCondition.keepMoist);
        diff.changeStorageClass(2, null);
        diff.initialise(0, publicUser.getId(), d_20001020);
        return diff;
    }

    @Override
    protected MaterialStorageDifference createDiffAt20001220() {
        MaterialStorageDifference diff = new MaterialStorageDifference();
        diff.addCondition(StorageCondition.keepFrozen);
        diff.addCondition(StorageCondition.keepTempBelowMinus80Celsius);
        diff.changeStorageClass(null, 3);
        diff.initialise(0, publicUser.getId(), d_20001220);
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
