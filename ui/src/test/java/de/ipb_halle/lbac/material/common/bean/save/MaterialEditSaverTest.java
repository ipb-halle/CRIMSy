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
package de.ipb_halle.lbac.material.common.bean.save;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.StorageClass;
import de.ipb_halle.lbac.material.common.StorageCondition;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.mocks.StructureInformationSaverMock;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.project.ProjectType;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.material.MaterialDeployment;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.HashMap;
import java.util.List;
import jakarta.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
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
public class MaterialEditSaverTest extends TestBase {

    private static final long serialVersionUID = 1L;

    private CreationTools creationTools;

    @Inject
    private ACListService acListService;

    @Inject
    private MaterialService materialService;

    @Inject
    private ProjectService projectService;

    Project p;
    User u;
    ACList aclist;
    Material mOld;
    Material mNew;

    String hazardStatement = "HazardStatement - Text";
    String precautionaryStatement = "PrecautionaryStatement - Text";
    String storageClassRemark = "storageClassRemark";

    @BeforeEach
    public void init() {
        creationTools = new CreationTools(hazardStatement, precautionaryStatement, storageClassRemark, memberService, projectService);
        p = new Project(ProjectType.BIOLOGICAL_PROJECT, "testProject");
        u = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        aclist = new ACList();
        aclist.addACE(u, new ACPermission[]{ACPermission.permREAD});
        aclist = acListService.save(aclist);
        p.setOwner(u);
        p.setACList(aclist);
        projectService.saveProjectToDb(p);
        mOld = creationTools.createEmptyStructure(p.getId());
        mOld.getStorageInformation().setStorageClass(new StorageClass(1, "storageClass-1"));


        materialService.setStructureInformationSaver(new StructureInformationSaverMock());
    }

    @AfterEach
    public void finish() {
        cleanMaterialsFromDB();
        cleanProjectFromDB(p, false);
        cleanAcListFromDB(aclist);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test001_saveStorageDiffs() throws Exception {

        mOld.getStorageInformation().getStorageConditions().add(StorageCondition.lightSensitive);
        mOld.getStorageInformation().getStorageConditions().add(StorageCondition.keepCool);

        materialService.saveMaterialToDB(mOld, aclist.getId(), new HashMap<>(),publicUser);
        mNew = mOld.copyMaterial();
        mNew.getStorageInformation().setStorageClass(new StorageClass(2, "storageClass-2"));
        mNew.getStorageInformation().setRemarks("new storage remarks");
        mNew.getStorageInformation().getStorageConditions().add(StorageCondition.acidSensitive);

        materialService.saveEditedMaterial(mNew, mOld, aclist.getId(), u.getId());

        // check the new storage class and its remarks
        List<Object[]> storageClass = (List) entityManagerService.doSqlQuery("select storageclass,description from storages");
        Assert.assertTrue(!storageClass.isEmpty());
        Assert.assertEquals("Testcase 001 - new storageclass must be 2", (Integer) 2, (Integer) storageClass.get(0)[0]);
        Assert.assertEquals("Testcase 001 - new description must be 'new storage remarks'", "new storage remarks", (String) storageClass.get(0)[1]);

        //  Check the new storage conditions
        List<Integer> newStorageConditions = (List) entityManagerService.doSqlQuery("select conditionid from storageconditions_material where materialid=" + mNew.getId() + " order by conditionid");
        Assert.assertEquals("Testcase 001 - 3 storage conditions must be found", 3, newStorageConditions.size());
        Assert.assertEquals("Testcase 001 - First new condition must be light sensitive", (Integer) StorageCondition.lightSensitive.getId(), newStorageConditions.get(0));
        Assert.assertEquals("Testcase 001 - Second new condition must be acid sensitive", (Integer) StorageCondition.acidSensitive.getId(), newStorageConditions.get(1));
        Assert.assertEquals("Testcase 001 - Third new condition must be keep cool", (Integer) StorageCondition.keepCool.getId(), newStorageConditions.get(2));

        // Check the history of the storage classes
        List<Object[]> storageClassHist = (List) entityManagerService.doSqlQuery("select description_old,description_new,storageclass_old, storageclass_new from storages_hist where id=" + mNew.getId());
        Assert.assertTrue("Testcase 001 - history of storageclass must not be empty", !storageClassHist.isEmpty());
        Assert.assertNull("Testcase 001 - old remarks of storage class must be null", storageClassHist.get(0)[0]);
        Assert.assertEquals("Testcase 001 - new remarks of storage class must be 'new storage remarks'", "new storage remarks", storageClassHist.get(0)[1]);
        Assert.assertEquals("Testcase 001 - old storageClass of storage class must be 1", 1, storageClassHist.get(0)[2]);
        Assert.assertEquals("Testcase 001 - new storageClass of storage class must be 2", 2, storageClassHist.get(0)[3]);

        // Check the history of the storage conditions
        List<Object[]> storageConditionsHist = (List) entityManagerService.doSqlQuery("select conditionId_old,conditionId_new from storagesconditions_storages_hist where materialid=" + mNew.getId() + " order by conditionId_old,conditionId_new");
        Assert.assertEquals("Testcase 001 - One history entry in storageconditions must be found ", 1, storageConditionsHist.size());
        Assert.assertNull("Testcase 001 - Old storagecondition must be null ", storageConditionsHist.get(0)[0]);
        Assert.assertEquals("Testcase 001 - New storagecondition must be 5 ", StorageCondition.acidSensitive.getId(), storageConditionsHist.get(0)[1]);

    }

    @SuppressWarnings("unchecked")
    @Test
    public void test002_saveStorageConditionsDiffsWithoutStorageClassDiffs() throws Exception {
        mOld.getStorageInformation().getStorageConditions().add(StorageCondition.acidSensitive);
        mOld.getStorageInformation().getStorageConditions().add(StorageCondition.keepCool);
        materialService.saveMaterialToDB(mOld, aclist.getId(), new HashMap<>(), publicUser);
        mNew = mOld.copyMaterial();
        mNew.getStorageInformation().getStorageConditions().remove(StorageCondition.keepCool);
        mNew.getStorageInformation().getStorageConditions().add(StorageCondition.lightSensitive);
        materialService.saveEditedMaterial(mNew, mOld, aclist.getId(), u.getId());

        // check the new storage class and its remarks
        List<Object[]> storageClass = (List) entityManagerService.doSqlQuery("select storageclass,description from storages");
        Assert.assertTrue(!storageClass.isEmpty());
        Assert.assertEquals("Testcase 002 - new storageclass must be 1", (Integer) 1, (Integer) storageClass.get(0)[0]);
        Assert.assertNull("Testcase 002 - new description must be null", storageClass.get(0)[1]);

        //  Check the new storage conditions
        List<Integer> newStorageConditions = (List) entityManagerService.doSqlQuery("select conditionid from storageconditions_material where materialid=" + mNew.getId() + " order by conditionid");
        Assert.assertEquals("Testcase 002 - 2 storage conditions must be found", 2, newStorageConditions.size());
        Assert.assertEquals("Testcase 002 - First new condition must be light sensitive", (Integer) StorageCondition.lightSensitive.getId(), newStorageConditions.get(0));
        Assert.assertEquals("Testcase 002 - Second new condition must be acid sensitive", (Integer) StorageCondition.acidSensitive.getId(), newStorageConditions.get(1));

        // Check the history of the storage classes
        List<Object[]> storageClassHist = (List) entityManagerService.doSqlQuery("select description_old,description_new,storageclass_old, storageclass_new from storages_hist where id=" + mNew.getId());
        Assert.assertTrue("Testcase 002 - history of storageclass must  be empty", storageClassHist.isEmpty());

        // Check the history of the storage conditions
        List<Object[]> storageConditionsHist = (List) entityManagerService.doSqlQuery("select conditionId_old,conditionId_new from storagesconditions_storages_hist where materialid=" + mNew.getId() + " order by conditionId_old ASC NULLS FIRST,conditionId_new ASC NULLS FIRST");
        Assert.assertEquals("Testcase 002 - One history entry in storageconditions must be found ", 2, storageConditionsHist.size());
        Assert.assertNull("Testcase 002 - Old storagecondition must be null ", storageConditionsHist.get(0)[0]);
        Assert.assertEquals("Testcase 002 - New storagecondition must be 3 ", StorageCondition.lightSensitive.getId(), storageConditionsHist.get(0)[1]);
        Assert.assertNull("Testcase 002 - Old storagecondition must be null", storageConditionsHist.get(1)[1]);
        Assert.assertEquals("Testcase 002 - New storagecondition must be 5 ", StorageCondition.keepCool.getId(), storageConditionsHist.get(1)[0]);

    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("MaterialEditSaverTest.war");
        return MaterialDeployment.add(UserBeanDeployment.add(deployment));
    }
}
