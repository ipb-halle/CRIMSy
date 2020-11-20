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
package de.ipb_halle.lbac.material.common.bean;

import de.ipb_halle.lbac.EntityManagerService;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.UserBeanMock;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.device.print.PrintBeanDeployment;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.collections.CollectionBean;
import de.ipb_halle.lbac.collections.CollectionOrchestrator;
import de.ipb_halle.lbac.collections.CollectionWebClient;
import de.ipb_halle.lbac.container.service.ContainerNestingService;
import de.ipb_halle.lbac.container.service.ContainerPositionService;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.items.bean.ItemBean;
import de.ipb_halle.lbac.items.bean.ItemOverviewBean;
import de.ipb_halle.lbac.items.service.ArticleService;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyNestingService;
import de.ipb_halle.lbac.material.mocks.MateriaBeanMock;
import de.ipb_halle.lbac.material.common.entity.index.MaterialIndexHistoryEntity;
import de.ipb_halle.lbac.material.common.service.IndexService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.structure.MoleculeService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectBean;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.project.ProjectType;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.search.wordcloud.WordCloudBean;
import de.ipb_halle.lbac.search.wordcloud.WordCloudWebClient;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.project.ProjectEditBean;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.webservice.Updater;
import java.util.HashMap;
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
public class MaterialBeanTest extends TestBase {

    @Inject
    private ACListService aclistService;

    @Inject
    private MaterialService materialService;

    @Inject
    private ProjectService projectService;

    MateriaBeanMock instance;
    CreationTools creationTools;
    User publicUser;
    User customUser;
    ACList acl;
    Material material;
    UserBeanMock userBean;
    Project project;

    @Inject
    private IndexService indexService;

    @Before
    public void init() {
        instance = new MateriaBeanMock();
        instance.setAcListService(aclistService);

        creationTools = new CreationTools("", "", "", memberService, projectService);
        project = new Project(ProjectType.BIOCHEMICAL_PROJECT, "Test-Project");
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        acl = new ACList();
        customUser = createUser("testUser", "testUser");
        acl.addACE(customUser, new ACPermission[]{ACPermission.permEDIT});
        acl = aclistService.save(acl);
        project.setOwner(publicUser);
        project.setACList(acl);
        projectService.saveProjectToDb(project);

        userBean = new UserBeanMock();
        userBean.setCurrentAccount(publicUser);
        instance.setUserBean(userBean);

        material = creationTools.createStructure(project);
        Structure s = (Structure) material;
        s.getMolecule().setStructureModel(null);
        material.setOwner(publicUser);
        materialService.setUserBean(userBean);

        materialService.saveMaterialToDB(material, acl.getId(), new HashMap<>());

        instance.setMaterialIndexBean(new MaterialIndexBean());
        instance.setMaterialNameBean(new MaterialNameBean());

        instance.getMaterialEditState().setMaterialToEdit(material);
        instance.getMaterialEditState().setMaterialBeforeEdit(material);
        instance.setMaterialService(materialService);
    }

    @After
    public void finish() {
        cleanMaterialsFromDB();
        cleanProjectFromDB(project, false);
        // cleanUserFromDB(customUser);

    }

    @Test
    public void test001_checkRights() {

        instance.setMode(MaterialBean.Mode.HISTORY);
        Assert.assertFalse("testcase 001: In history mode edit must be false ", instance.isProjectEditEnabled());
        instance.setMode(MaterialBean.Mode.CREATE);
        Assert.assertTrue("testcase 001: In creation mode edit must be true ", instance.isProjectEditEnabled());
        instance.setMode(MaterialBean.Mode.EDIT);
        Assert.assertTrue("testcase 001: Owner must be able to edit project", instance.isProjectEditEnabled());
        User admin = memberService.loadUserById(GlobalAdmissionContext.OWNER_ACCOUNT_ID);
        userBean.setCurrentAccount(admin);
        Assert.assertFalse("testcase 001: No priviliged user must not  be able to edit project", instance.isProjectEditEnabled());
        userBean.setCurrentAccount(customUser);
        Assert.assertTrue("testcase 001: Priviliged user must   be able to edit project", instance.isProjectEditEnabled());
    }

    @Test
    public void test002_navigateInHistory() throws Exception {
        MaterialIndexBean indexBean = new MaterialIndexBean();
        indexBean.setIndexService(indexService);
        instance.setMaterialIndexBean(indexBean);

        instance.setProjectBean(new ProjectBean());
        Material originalMaterial = materialService.loadMaterialById(material.getId());

        MaterialEditState materialEditState = new MaterialEditState(project, null, originalMaterial.copyMaterial(), originalMaterial, originalMaterial.getHazards());
        materialEditState.getMaterialToEdit().getNames().add(new MaterialName("Edited-name-1", "de", 3));
        materialEditState.getMaterialToEdit().getNames().add(new MaterialName("Edited-name-2", "en", 4));

        materialService.saveEditedMaterial(
                materialEditState.getMaterialToEdit(),
                materialEditState.getMaterialBeforeEdit(),
                materialEditState.getCurrentProject().getUserGroups().getId(),
                userBean.getCurrentAccount().getId());

        instance.setMaterialIndexBean(indexBean);
        originalMaterial = materialService.loadMaterialById(material.getId());
        instance.startMaterialEdit(originalMaterial);
        instance.switchOneVersionBack();

        Assert.assertEquals(2, instance.getMaterialNameBean().getNames().size());
        Assert.assertEquals("Test-Struktur", instance.getMaterialNameBean().getNames().get(0).getValue());
        Assert.assertEquals("Test-Structure", instance.getMaterialNameBean().getNames().get(1).getValue());

        instance.switchOneVersionForward();
        Assert.assertEquals(4, instance.getMaterialNameBean().getNames().size());
        Assert.assertEquals("Test-Struktur", instance.getMaterialNameBean().getNames().get(0).getValue());
        Assert.assertEquals("Test-Structure", instance.getMaterialNameBean().getNames().get(1).getValue());
        Assert.assertEquals("Edited-name-1", instance.getMaterialNameBean().getNames().get(2).getValue());
        Assert.assertEquals("Edited-name-2", instance.getMaterialNameBean().getNames().get(3).getValue());
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment
                = prepareDeployment("MaterialBeanTest.war")
                        .addClass(IndexService.class);
        deployment = ItemDeployment.add(deployment);
        deployment = UserBeanDeployment.add(deployment);
        return PrintBeanDeployment.add(deployment);
    }
}
