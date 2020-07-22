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
package de.ipb_halle.lbac.material.common.bean;

import de.ipb_halle.lbac.EntityManagerService;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.LdapProperties;
import de.ipb_halle.lbac.admission.SystemSettings;
import de.ipb_halle.lbac.admission.MembershipOrchestrator;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.device.print.PrintBeanDeployment;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.cloud.solr.SolrAdminService;
import de.ipb_halle.lbac.collections.CollectionBean;
import de.ipb_halle.lbac.collections.CollectionOrchestrator;
import de.ipb_halle.lbac.collections.CollectionWebClient;
import de.ipb_halle.lbac.container.service.ContainerNestingService;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.ACPermission;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.items.bean.ItemBean;
import de.ipb_halle.lbac.items.bean.ItemOverviewBean;
import de.ipb_halle.lbac.items.service.ArticleService;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyNestingService;
import de.ipb_halle.lbac.material.common.bean.mock.MateriaBeanMock;
import de.ipb_halle.lbac.material.common.entity.index.MaterialIndexHistoryEntity;
import de.ipb_halle.lbac.material.mocks.UserBeanMock;
import de.ipb_halle.lbac.material.common.service.IndexService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.structure.MoleculeService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectBean;
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

    @Before
    public void init() {
        instance = new MateriaBeanMock();
        instance.setAcListService(aclistService);

        creationTools = new CreationTools("", "", "", memberService, projectService);
        project = new Project(ProjectType.BIOCHEMICAL_PROJECT, "Test-Project");
        publicUser = memberService.loadUserById(UUID.fromString(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID));
        acl = new ACList();
        customUser = createUser("testUser", "testUser");
        acl.addACE(customUser, new ACPermission[]{ACPermission.permEDIT});
        acl = aclistService.save(acl);
        project.setOwner(publicUser);
        project.setUserGroups(acl);
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
        User admin = memberService.loadUserById(UUID.fromString(GlobalAdmissionContext.OWNER_ACCOUNT_ID));
        userBean.setCurrentAccount(admin);
        Assert.assertFalse("testcase 001: No priviliged user must not  be able to edit project", instance.isProjectEditEnabled());
        userBean.setCurrentAccount(customUser);
        Assert.assertTrue("testcase 001: Priviliged user must   be able to edit project", instance.isProjectEditEnabled());
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = 
                prepareDeployment("MaterialEditBeanTest.war")
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
                .addClass(ProjectBean.class)
                .addClass(IndexService.class)
                .addClass(MaterialNameBean.class)
                .addClass(MaterialIndexBean.class)
                .addClass(TaxonomyNestingService.class)
                .addClass(LdapProperties.class)
                .addClass(ProjectService.class)
                .addClass(SystemSettings.class)
                .addClass(CollectionWebClient.class)
                .addClass(DocumentSearchOrchestrator.class)
                .addClass(Updater.class)
                .addClass(TissueService.class)
                .addClass(TaxonomyService.class)
                .addClass(Navigator.class)
                .addClass(WordCloudBean.class)
                .addClass(ACListService.class)
                .addClass(WordCloudWebClient.class)
                .addClass(MateriaBeanMock.class)
                .addClass(MaterialOverviewBean.class)
                .addClass(ContainerService.class)
                .addClass(ItemService.class)
                .addClass(ArticleService.class)
                .addClass(ItemOverviewBean.class)
                .addClass(ContainerNestingService.class)
                .addClass(ItemBean.class)
                .addClass(MaterialIndexHistoryEntity.class)
                .addClass(MaterialService.class);
        return PrintBeanDeployment.add(deployment);
    }
}
