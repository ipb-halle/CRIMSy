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

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.collections.CollectionBean;
import de.ipb_halle.lbac.collections.CollectionOrchestrator;
import de.ipb_halle.lbac.collections.CollectionWebClient;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.exp.ExperimentDeployment;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectBean;
import javax.inject.Inject;
import de.ipb_halle.lbac.project.ProjectEditBean;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.SearchService;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.search.SearchWebService;
import de.ipb_halle.lbac.search.wordcloud.WordCloudBean;
import de.ipb_halle.lbac.search.wordcloud.WordCloudWebClient;
import de.ipb_halle.lbac.webservice.Updater;
import de.ipb_halle.lbac.webservice.service.WebRequestAuthenticator;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class ProjectBeanTest extends TestBase {

    @Inject
    protected ProjectBean instance;

    @Inject
    private ProjectService projectService;

    @Inject
    private ACListService aclistService;

    private User publicUser, adminUser;
    private ACList adminOnlyAcl, publicAcl;
    private int project1Id, project2Id, project3Id;

    @Ignore("Ignored until new API is implemented for requests")
    @Test
    public void test001_reloadReadableProjects() {
        instance.reloadReadableProjects();
        Assert.assertEquals(2, instance.getReadableProjects().size());
    }

    @Ignore("Ignored until new API is implemented for requests")
    @Test
    public void test002_getReadableProjectById() {
        Assert.assertNotNull(instance.getReadableProjectById(project1Id));
        Assert.assertNull(instance.getReadableProjectById(project3Id));
    }

    @Ignore("Ignored until new API is implemented for requests")
    @Test
    public void test003_changeAclOfProject() {
        Project projectToEdit = instance.getReadableProjectById(project1Id);
        instance.actionStartAclChange(projectToEdit);
        projectToEdit.setACList(adminOnlyAcl);
        instance.cancelAclChanges();
        Assert.assertEquals(2, instance.getReadableProjects().size());

        instance.actionStartAclChange(projectToEdit);
        projectToEdit.setACList(adminOnlyAcl);
        instance.applyAclChanges();
        Assert.assertEquals(1, instance.getReadableProjects().size());
    }

    @Ignore("Ignored until new API is implemented for requests")
    @Test
    public void test004_isPermissionEditAllowed() {

        Project projectToEdit = instance.getReadableProjectById(project1Id);
        Assert.assertFalse(instance.isPermissionAllowed(projectToEdit, "permEDIT"));

        instance.actionStartAclChange(projectToEdit);
        projectToEdit.getACList().getACEntries().get(context.getPublicGroup().getId()).setPermEdit(true);
        instance.applyAclChanges();
        Assert.assertTrue(instance.isPermissionAllowed(projectToEdit, "permEDIT"));
    }

    @Before
    @Override
    public void setUp() {
        super.setUp();

        this.publicUser = context.getPublicAccount();
        instance = new ProjectBean();
        instance.setProjectService(projectService);
        instance.setMemberService(memberService);
        instance.setAclistService(aclistService);

        adminOnlyAcl = context.getAdminOnlyACL();
        publicAcl = GlobalAdmissionContext.getPublicReadACL();
        adminUser = context.getAdminAccount();
        saveProjectEntry("ProjectBeanTest_p1_public", publicAcl.getId(), publicUser.getId());
        saveProjectEntry("ProjectBeanTest_p2_public", publicAcl.getId(), publicUser.getId());
        saveProjectEntry("ProjectBeanTest_p3_admin", adminOnlyAcl.getId(), adminUser.getId());

        project1Id = (Integer) entityManagerService.doSqlQuery("SELECT id FROM projects WHERE name='ProjectBeanTest_p1_public'").get(0);
        project2Id = (Integer) entityManagerService.doSqlQuery("SELECT id FROM projects WHERE name='ProjectBeanTest_p2_public'").get(0);
        project3Id = (Integer) entityManagerService.doSqlQuery("SELECT id FROM projects WHERE name='ProjectBeanTest_p3_admin'").get(0);

        instance.setCurrentAccount(new LoginEvent(publicUser));

    }

    @After
    public void cleanUp() {
        entityManagerService.doSqlUpdate("DELETE FROM projects");
    }

    private void saveProjectEntry(String name, int aclistId, int userId) {
        entityManagerService.doSqlUpdate(
                String.format("INSERT INTO "
                        + "projects (name,projecttypeid,owner_id,aclist_id) "
                        + "VALUES('%s',1,%d,%d)",
                        name, userId, aclistId));
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
                .addClass(ACListService.class)
                .addClass(ProjectEditBean.class);
        return ExperimentDeployment.add(ItemDeployment.add(UserBeanDeployment.add(deployment)));
    }

}
