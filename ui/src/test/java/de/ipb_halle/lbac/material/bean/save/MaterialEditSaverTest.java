/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.bean.save;

import de.ipb_halle.lbac.EntityManagerService;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.LdapProperties;
import de.ipb_halle.lbac.admission.SystemSettings;
import de.ipb_halle.lbac.announcement.membership.MembershipOrchestrator;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.cloud.solr.SolrAdminService;
import de.ipb_halle.lbac.collections.CollectionBean;
import de.ipb_halle.lbac.collections.CollectionOrchestrator;
import de.ipb_halle.lbac.collections.CollectionWebClient;
import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.ACPermission;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.component.StorageClass;
import de.ipb_halle.lbac.material.component.StorageCondition;
import de.ipb_halle.lbac.material.entity.MaterialIndexHistoryEntity;
import de.ipb_halle.lbac.material.mocks.MaterialServiceMock;
import de.ipb_halle.lbac.material.mocks.UserBeanMock;
import de.ipb_halle.lbac.material.service.MaterialService;
import de.ipb_halle.lbac.material.service.MoleculeService;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.project.ProjectType;
import de.ipb_halle.lbac.search.SolrSearcher;
import de.ipb_halle.lbac.search.document.DocumentSearchBean;
import de.ipb_halle.lbac.search.document.DocumentSearchOrchestrator;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.search.termvector.SolrTermVectorSearch;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.search.wordcloud.WordCloudBean;
import de.ipb_halle.lbac.search.wordcloud.WordCloudWebClient;
import de.ipb_halle.lbac.service.ACListService;
import de.ipb_halle.lbac.service.CollectionService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.webservice.Updater;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class MaterialEditSaverTest extends TestBase {

    private CreationTools creationTools;

    @Inject
    private ACListService acListService;

    @Inject
    private MaterialServiceMock materialService;

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

    UserBeanMock userBean = new UserBeanMock();

    @Before
    public void init() {
        creationTools = new CreationTools(hazardStatement, precautionaryStatement, storageClassRemark, memberService, projectService);
        p = new Project(ProjectType.BIOLOGICAL_PROJECT, "testProject");
        u = memberService.loadUserById(UUID.fromString(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID));
        aclist = new ACList();
        aclist.addACE(u, new ACPermission[]{ACPermission.permREAD});
        aclist = acListService.save(aclist);
        p.setOwner(u);
        p.setUserGroups(aclist);
        projectService.saveProjectToDb(p);
        mOld = creationTools.createEmptyStructure(p.getId());
        mOld.getStorageInformation().setStorageClass(new StorageClass(1, "storageClass-1"));
        userBean.setCurrentAccount(u);
        materialService.setUserBean(userBean);
    }

    @After
    public void finish() {
        cleanMaterialsFromDB();
        cleanProjectFromDB(p, false);
        cleanAcListFromDB(aclist);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test001_saveStorageDiffs() throws Exception {

        mOld.getStorageInformation().setLightSensitive(true);
        mOld.getStorageInformation().setKeepCool(true);

        materialService.saveMaterialToDB(mOld, aclist.getId(), new HashMap<>());
        mNew = mOld.copyMaterial();
        mNew.getStorageInformation().setStorageClass(new StorageClass(2, "storageClass-2"));
        mNew.getStorageInformation().setRemarks("new storage remarks");
        mNew.getStorageInformation().setAcidSensitive(true);

        materialService.saveEditedMaterial(mNew, mOld, aclist.getId(), u.getId());

        // check the new storage class and its remarks
        List<Object[]> storageClass = (List) entityManagerService.doSqlQuery("select storageclass,description from storages");
        Assert.assertTrue(!storageClass.isEmpty());
        Assert.assertEquals("Testcase 001 - new storageclass must be 2", (Integer) 2, (Integer) storageClass.get(0)[0]);
        Assert.assertEquals("Testcase 001 - new description must be 'new storage remarks'", "new storage remarks", (String) storageClass.get(0)[1]);

        //  Check the new storage conditions
        List<Integer> newStorageConditions = (List) entityManagerService.doSqlQuery("select conditionid from storageconditions_storages where materialid=" + mNew.getId() + " order by conditionid");
        Assert.assertEquals("Testcase 001 - 3 storage conditions must be found", 3, newStorageConditions.size());
        Assert.assertEquals("Testcase 001 - First new condition must be light sensitive", (Integer) StorageCondition.lightSensitive.getId(), newStorageConditions.get(0));
        Assert.assertEquals("Testcase 001 - Second new condition must be acid sensitive", (Integer) StorageCondition.acidSensitive.getId(), newStorageConditions.get(1));
        Assert.assertEquals("Testcase 001 - Third new condition must be keep cool", (Integer) StorageCondition.keepCool.getId(), newStorageConditions.get(2));

        // Check the history of the storage classes 
        List<Object[]> storageClassHist = (List) entityManagerService.doSqlQuery("select description_old,description_new,storageclass_old, storageclass_new from storages_hist where materialid=" + mNew.getId());
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
        mOld.getStorageInformation().setAcidSensitive(true);
        mOld.getStorageInformation().setKeepCool(true);
        materialService.saveMaterialToDB(mOld, aclist.getId(), new HashMap<>());
        mNew = mOld.copyMaterial();
        mNew.getStorageInformation().setKeepCool(false);
        mNew.getStorageInformation().setLightSensitive(true);
        materialService.saveEditedMaterial(mNew, mOld, aclist.getId(), u.getId());

        // check the new storage class and its remarks
        List<Object[]> storageClass = (List) entityManagerService.doSqlQuery("select storageclass,description from storages");
        Assert.assertTrue(!storageClass.isEmpty());
        Assert.assertEquals("Testcase 002 - new storageclass must be 1", (Integer) 1, (Integer) storageClass.get(0)[0]);
        Assert.assertNull("Testcase 002 - new description must be null", storageClass.get(0)[1]);

        //  Check the new storage conditions
        List<Integer> newStorageConditions = (List) entityManagerService.doSqlQuery("select conditionid from storageconditions_storages where materialid=" + mNew.getId() + " order by conditionid");
        Assert.assertEquals("Testcase 002 - 2 storage conditions must be found", 2, newStorageConditions.size());
        Assert.assertEquals("Testcase 002 - First new condition must be light sensitive", (Integer) StorageCondition.lightSensitive.getId(), newStorageConditions.get(0));
        Assert.assertEquals("Testcase 002 - Second new condition must be acid sensitive", (Integer) StorageCondition.acidSensitive.getId(), newStorageConditions.get(1));

        // Check the history of the storage classes 
        List<Object[]> storageClassHist = (List) entityManagerService.doSqlQuery("select description_old,description_new,storageclass_old, storageclass_new from storages_hist where materialid=" + mNew.getId());
        Assert.assertTrue("Testcase 002 - history of storageclass must  be empty", storageClassHist.isEmpty());

        // Check the history of the storage conditions
        List<Object[]> storageConditionsHist = (List) entityManagerService.doSqlQuery("select conditionId_old,conditionId_new from storagesconditions_storages_hist where materialid=" + mNew.getId() + " order by conditionId_old,conditionId_new");
        Assert.assertEquals("Testcase 002 - One history entry in storageconditions must be found ", 2, storageConditionsHist.size());
        Assert.assertNull("Testcase 002 - Old storagecondition must be null ", storageConditionsHist.get(0)[0]);
        Assert.assertEquals("Testcase 002 - New storagecondition must be 3 ", StorageCondition.lightSensitive.getId(), storageConditionsHist.get(0)[1]);
        Assert.assertNull("Testcase 002 - Old storagecondition must be null", storageConditionsHist.get(1)[1]);
        Assert.assertEquals("Testcase 002 - New storagecondition must be 5 ", StorageCondition.keepCool.getId(), storageConditionsHist.get(1)[0]);

    }

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("MaterialEditSaverTest.war")
                .addClass(UserBeanMock.class)
                .addClass(ACListService.class)
                .addClass(CollectionBean.class)
                .addClass(CollectionService.class)
                .addClass(SolrAdminService.class)
                .addClass(FileService.class)
                .addClass(FileEntityService.class)
                .addClass(SolrTermVectorSearch.class)
                .addClass(CollectionOrchestrator.class)
                .addClass(EntityManagerService.class)
                .addClass(TermVectorEntityService.class)
                .addClass(DocumentSearchBean.class)
                .addClass(DocumentSearchService.class)
                .addClass(SolrSearcher.class)
                .addClass(MembershipOrchestrator.class)
                .addClass(MoleculeService.class)
                .addClass(KeyManager.class)
                .addClass(SystemSettings.class)
                .addClass(LdapProperties.class)
                .addClass(ProjectService.class)
                .addClass(CollectionWebClient.class)
                .addClass(DocumentSearchOrchestrator.class)
                .addClass(Updater.class)
                .addClass(Navigator.class)
                .addClass(WordCloudBean.class)
                .addClass(ACListService.class)
                .addClass(WordCloudWebClient.class)
                .addClass(MaterialIndexHistoryEntity.class)
                .addClass(MaterialService.class)
                .addClass(MaterialServiceMock.class);
    }
}
