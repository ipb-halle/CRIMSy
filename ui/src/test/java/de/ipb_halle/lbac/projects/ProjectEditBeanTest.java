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
package de.ipb_halle.lbac.projects;

import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.collections.CollectionBean;
import de.ipb_halle.lbac.collections.CollectionOrchestrator;
import de.ipb_halle.lbac.collections.CollectionWebClient;
import de.ipb_halle.lbac.admission.ACEntry;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.AdmissionSubSystemType;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.Group;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.exp.ExperimentDeployment;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.mocks.NavigatorMock;
import de.ipb_halle.lbac.material.common.MaterialDetailType;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectBean;
import jakarta.inject.Inject;
import de.ipb_halle.lbac.project.ProjectEditBean;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.project.ProjectType;
import de.ipb_halle.lbac.search.SearchService;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.search.SearchWebService;
import de.ipb_halle.lbac.search.wordcloud.WordCloudBean;
import de.ipb_halle.lbac.search.wordcloud.WordCloudWebClient;
import de.ipb_halle.lbac.webservice.Updater;
import de.ipb_halle.lbac.webservice.service.WebRequestAuthenticator;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.HashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
public class ProjectEditBeanTest extends TestBase {

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    @Inject
    protected ProjectEditBean instance;

    private User publicUser;

    private Group group1, group2;

    @Inject
    private ProjectEditBean projectEditBean;

    @Inject
    private ProjectService projectService;

    @Inject
    private ProjectBean projectBean;

    @Inject
    private Navigator navigator;

    @BeforeEach
    public void init() {
        this.publicUser = context.getPublicAccount();
        projectBean.setCurrentAccount(new LoginEvent(publicUser));
        group1 = createGroup("test02_removeAceFromProjectACL_group_1",
                nodeService.getLocalNode(),
                memberService,
                membershipService);

        group2 = createGroup("test02_removeAceFromProjectACL_group_2",
                nodeService.getLocalNode(),
                memberService,
                membershipService);

        projectEditBean.setNavigator(new NavigatorMock(null));
        projectEditBean.init();
        projectEditBean.setCurrentAccount(new LoginEvent(publicUser));
    }

    @AfterEach
    public void cleanUp() {
        entityManagerService.doSqlUpdate("DELETE FROM usersgroups WHERE id=" + group1.getId());
        entityManagerService.doSqlUpdate("DELETE FROM usersgroups WHERE id=" + group2.getId());
    }

    @Test
    public void test01_addGroupToProjectACL() {
        projectEditBean.addGroupToProjectACL(group1);
        int sizeBeforeAdding = projectEditBean.getAddableGroupsForProject().size();
        projectEditBean.addGroupToProjectACL(group2);
        Assert.assertEquals(
                sizeBeforeAdding,
                projectEditBean.getAddableGroupsForProject().size() + 1);
    }

    @Test
    public void test02_removeAceFromProjectACL() {
        projectEditBean.addGroupToProjectACL(group1);
        int sizeBeforeAction = projectEditBean.getACEntriesOfProject().size();
        projectEditBean.addGroupToProjectACL(group2);
        projectEditBean.removeAceFromProjectACL(projectEditBean.getACEntriesOfProject().get(0));
        Assert.assertEquals(
                sizeBeforeAction,
                projectEditBean.getACEntriesOfProject().size());
    }

    @Test
    public void test03_roleTemplate() {
        String materialDetail = MaterialDetailType.COMMON_INFORMATION.toString();
        int sizeBeforeAction = projectEditBean
                .getAddableGroupsForRoleTemplates(materialDetail).size();
        projectEditBean.addAceToRoleTemplate(group1, materialDetail);
        int sizeAfterAction = projectEditBean
                .getAddableGroupsForRoleTemplates(materialDetail).size();
        Assert.assertEquals(
                sizeBeforeAction,
                sizeAfterAction + 1);
        ACEntry ace = projectEditBean.getACEntriesForDetailRole(materialDetail).get(0);
        projectEditBean.removeAceFromRoleTemplateACL(ace, materialDetail);
        sizeAfterAction = projectEditBean
                .getAddableGroupsForRoleTemplates(materialDetail).size();
        Assert.assertEquals(
                sizeBeforeAction,
                sizeAfterAction);
    }

    @Test
    public void test004_createProject() {
        projectEditBean.startProjectCreation();
        int originalProjectCount = projectBean.getReadableProjects().size();
        Assert.assertTrue(projectEditBean.getProjectName().isEmpty());
        Assert.assertEquals(2, projectEditBean.getACEntriesOfProject().size());
        Assert.assertEquals(ProjectType.CHEMICAL_PROJECT, projectEditBean.getCurrentProjectType());
        Assert.assertTrue(projectEditBean.getProjectDescription().isEmpty());
        Assert.assertEquals(publicUser.getId(), projectEditBean.getProjectOwner().getId());

        projectEditBean.setProjectName("test004_createProject");

        projectEditBean.setCurrentProjectType(ProjectType.BIOCHEMICAL_PROJECT);
        projectEditBean.setProjectDescription("test004_createProject_desc");

        for (ACEntry ace : projectEditBean.getACEntriesOfProject()) {
            projectEditBean.removeAceFromRoleTemplateACL(ace, MaterialDetailType.COMMON_INFORMATION.toString());
        }
        for (ACEntry ace : projectEditBean.getACEntriesOfProject()) {
            projectEditBean.removeAceFromProjectACL(ace);
        }

        projectEditBean.saveProject();
        
        Assert.assertEquals(originalProjectCount + 1, projectBean.getReadableProjects().size());
        Project newProject = projectBean.getReadableProjects().get(originalProjectCount);
        Assert.assertEquals(1, newProject.getACList().getACEntries().size());
        Assert.assertEquals(1, newProject.getDetailTemplates().get(MaterialDetailType.COMMON_INFORMATION).getACEntries().size());
        Assert.assertEquals("test004_createProject",newProject.getName());
        Assert.assertEquals("test004_createProject_desc",newProject.getDescription());
        Assert.assertEquals(ProjectType.BIOCHEMICAL_PROJECT,newProject.getProjectType());
    }

    @Test
    public void test005_startProjectEdit() {
        Project p = new Project();
        p.setName("test005_startProjectEdit");
        p.setDescription("test005_startProjectEdit_description");
        p.setOwner(context.getAdminAccount());
        p.setDetailTemplates(new HashMap<>());
        p.setProjectType(ProjectType.IT_PROJECT);
        p.setACList(new ACList());

        projectEditBean.startProjectEdit(p);
        Assert.assertEquals("test005_startProjectEdit", projectEditBean.getProjectName());
        Assert.assertEquals(0, projectEditBean.getACEntriesOfProject().size());
        Assert.assertEquals(ProjectType.IT_PROJECT, projectEditBean.getCurrentProjectType());
        Assert.assertEquals("test005_startProjectEdit_description", projectEditBean.getProjectDescription());
        Assert.assertEquals(context.getAdminAccount().getId(), projectEditBean.getProjectOwner().getId());
    }

    @Test
    public void test006_testLocalUsers() {
        try {
            User u = new User();
            u.setName("test006_testLocalUsers");
            u.setLogin("test006_testLocalUsers");
            u.setPassword("test006_testLocalUsers");
            u.setNode(nodeService.getLocalNode());
            u = memberService.save(u);
            int currentLocalUsers = projectEditBean.getLocalUsers().size();
            Assert.assertEquals(currentLocalUsers, projectEditBean.getLocalUsers().size());
            u.setLogin(GlobalAdmissionContext.NAME_OF_DEACTIVATED_USER);
            u.setEmail("");
            u.setPassword("");
            u.setPhone("");
            u.setName(GlobalAdmissionContext.NAME_OF_DEACTIVATED_USER);
            u.setSubSystemData("");
            u.setSubSystemType(AdmissionSubSystemType.LOCAL);
            memberService.save(u);
            Assert.assertEquals(currentLocalUsers - 1, projectEditBean.getLocalUsers().size());
        } catch (Exception e) {
            logger.error("test006_testLocalUsers() caught an exception:", (Throwable) e);
        }

    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("ProjectEditBeanTest.war")
                .addClass(ProjectBean.class)
                .addClass(Navigator.class)
                .addClass(ProjectService.class)
                .addClass(WordCloudBean.class)
                .addClass(WordCloudWebClient.class)
                .addClass(CollectionBean.class)
                .addClass(WebRequestAuthenticator.class)
                .addClass(SearchWebService.class)
                .addClass(DocumentSearchService.class)
                .addClass(DocumentSearchService.class)
                .addClass(CollectionOrchestrator.class)
                .addClass(CollectionWebClient.class)
                .addClass(SearchService.class)
                .addClass(SearchWebService.class)
                .addClass(Updater.class)
                .addClass(Navigator.class)
                .addClass(ProjectEditBean.class);
        return ExperimentDeployment.add(ItemDeployment.add(UserBeanDeployment.add(deployment)));
    }

}
