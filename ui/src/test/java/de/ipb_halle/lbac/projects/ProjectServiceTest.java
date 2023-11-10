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

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.SystemSettings;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.Group;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.base.ProjectCreator;
import de.ipb_halle.lbac.material.common.MaterialDetailType;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectSearchRequestBuilder;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.project.ProjectType;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class ProjectServiceTest extends TestBase {

    @Inject
    private ProjectService instance;

    @Inject
    private ACListService aclistService;

    private User publicUser;

    @BeforeEach
    public void init() {
        cleanAllProjectsFromDb();
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
    }

    @Test
    public void test001_saveLoadProject() {
        Project p = new Project(ProjectType.BIOCHEMICAL_PROJECT, "biochemical-test-project");
        p.setBudget(1000d);
        p.setDescription("Description of biochemical test project");

        Group g = memberService.loadGroupById(GlobalAdmissionContext.PUBLIC_GROUP_ID);
        User u = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);

        ACList projectAcList = new ACList();

        projectAcList.addACE(u, new ACPermission[]{
            ACPermission.permREAD,
            ACPermission.permCHOWN,
            ACPermission.permCREATE,
            ACPermission.permDELETE,
            ACPermission.permEDIT,
            ACPermission.permGRANT,
            ACPermission.permSUPER});

        projectAcList.addACE(g, new ACPermission[]{
            ACPermission.permREAD});

        p.setOwner(u);
        p.setACList(projectAcList);
        projectAcList = aclistService.save(projectAcList);

        p.getDetailTemplates().put(MaterialDetailType.COMMON_INFORMATION, projectAcList);
        p.getDetailTemplates().put(MaterialDetailType.STORAGE_CLASSES, projectAcList);
        p.getDetailTemplates().put(MaterialDetailType.STRUCTURE_INFORMATION, projectAcList);
        p.getDetailTemplates().put(MaterialDetailType.HAZARD_INFORMATION, projectAcList);
        p.getDetailTemplates().put(MaterialDetailType.INDEX, projectAcList);
        p.getDetailTemplates().put(MaterialDetailType.TAXONOMY, projectAcList);

        instance.saveProjectToDb(p);
        ProjectSearchRequestBuilder requestBuilder = new ProjectSearchRequestBuilder(u, 0, 25);

        SearchResult result = instance.loadProjects(requestBuilder.build());
        List<Project> projectsOfPublicUser = result.getAllFoundObjects(Project.class, nodeService.getLocalNode());
        Assert.assertEquals("Only 1 project should be found", 1, projectsOfPublicUser.size());

        User user2 = createUser("UserWithoutPermission", "no name");
        requestBuilder = new ProjectSearchRequestBuilder(user2, 0, 25);
        result = instance.loadProjects(requestBuilder.build());
        List<Project> projectsOfUser2 = result.getAllFoundObjects(Project.class, nodeService.getLocalNode());
        Assert.assertEquals("No project must be found", 0, projectsOfUser2.size());

        requestBuilder = new ProjectSearchRequestBuilder(u, 0, 25);
        requestBuilder.setProjectName("biochemical-test-project");
        result = instance.loadProjects(requestBuilder.build());
        List<Project> loadedByName = result.getAllFoundObjects(Project.class, nodeService.getLocalNode());
        Assert.assertNotNull("test001: loaded by name should not be null ", loadedByName);
        Assert.assertEquals("test001: loaded by name wrong project loaded", p.getId(), loadedByName.get(0).getId());

        requestBuilder = new ProjectSearchRequestBuilder(u, 0, 25);
        requestBuilder.setProjectName("biochemical-test-project-XXX");
        result = instance.loadProjects(requestBuilder.build());
        loadedByName = result.getAllFoundObjects(Project.class, nodeService.getLocalNode());
        Assert.assertEquals(0, loadedByName.size());

        cleanUp(user2, projectAcList, p);
    }

    @Test
    public void test002_saveEditedProject() {
        //Prepare a project and save it to the database
        Project p = new Project(ProjectType.BIOCHEMICAL_PROJECT, "test002_project-beforeEdit");
        p.setBudget(1000d);
        p.setDescription("Description of biochemical test project");
        Group g = memberService.loadGroupById(GlobalAdmissionContext.PUBLIC_GROUP_ID);
        User u = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        ACList emptyACList = new ACList();
        p.setACList(emptyACList);
        p.setOwner(u);
        p.getDetailTemplates().put(MaterialDetailType.COMMON_INFORMATION, emptyACList);
        p = instance.saveProjectToDb(p);

        //Modify the project and save it
        p.setBudget(0d);
        p.setName("test002_project-afterEdit");
        p.setACList(GlobalAdmissionContext.getPublicReadACL());
        p.getDetailTemplates().put(MaterialDetailType.COMMON_INFORMATION, GlobalAdmissionContext.getPublicReadACL());
        p.getDetailTemplates().put(MaterialDetailType.HAZARD_INFORMATION, GlobalAdmissionContext.getPublicReadACL());
        instance.saveEditedProjectToDb(p);

        //Load the edited Project and check its changed properties
        Project loadedProject = instance.loadProjectById(p.getId());
        Assert.assertEquals(0d, (double) loadedProject.getBudget(), 0);
        Assert.assertTrue(loadedProject.getBudgetReservation().isEmpty());
        Assert.assertEquals("Description of biochemical test project", loadedProject.getDescription());
        Assert.assertEquals(GlobalAdmissionContext.getPublicReadACL().getId(), loadedProject.getDetailTemplates().get(MaterialDetailType.COMMON_INFORMATION).getId());
        Assert.assertEquals(GlobalAdmissionContext.getPublicReadACL().getId(), loadedProject.getDetailTemplates().get(MaterialDetailType.HAZARD_INFORMATION).getId());
        Assert.assertNull(loadedProject.getDetailTemplates().get(MaterialDetailType.INDEX));
        Assert.assertEquals("test002_project-afterEdit", loadedProject.getName());
        Assert.assertEquals(u.getId(), loadedProject.getOwnerID());
        Assert.assertEquals(GlobalAdmissionContext.getPublicReadACL().getId(), loadedProject.getUserGroups().getId());
        loadedProject.hashCode();
    }

    @Test
    public void test003_isProjectNameAvailable() {
        Assert.assertTrue(instance.isProjectNameAvailable("Test_project"));
        saveProjectEntry("Test_project");
        Assert.assertFalse(instance.isProjectNameAvailable("Test_project"));
        Assert.assertFalse(instance.isProjectNameAvailable("test_Project"));
        entityManagerService.doSqlUpdate("DELETE FROM projects WHERE name='Test_project'");
    }

    @Test
    public void test004_changeDeactivationState() {
        saveProjectEntry("Test_project");
        int id = (int) ((List) entityManagerService.doSqlQuery("SELECT id FROM projects")).get(0);
        boolean deactivated = (boolean) ((List) entityManagerService.doSqlQuery("SELECT deactivated FROM projects")).get(0);
        Assert.assertFalse(deactivated);
        instance.changeDeactivationState(id, true);
        deactivated = (boolean) ((List) entityManagerService.doSqlQuery("SELECT deactivated FROM projects")).get(0);
        Assert.assertTrue(deactivated);
        entityManagerService.doSqlUpdate("DELETE FROM projects WHERE name='Test_project'");
    }

    @Test
    public void test005_getSimilarProjectNames() {
        ProjectCreator creator = new ProjectCreator(instance, GlobalAdmissionContext.getPublicReadACL());
        //Normal project
        creator.setProjectName("ProjectServiceTest:test005");
        creator.createAndSaveProject(publicUser);
        //project with similar name
        creator.setProjectName("ProjectServiceTest:test005_similarName");
        creator.createAndSaveProject(publicUser);
        // project with another name
        creator.setProjectName("ProjectServiceTest:test0XX05");
        creator.createAndSaveProject(publicUser);
        //Not readable project
        creator.setProjectAcl(context.getNoAccessACL());
        creator.setProjectName("ProjectServiceTest:test005_notReadable");
        creator.createAndSaveProject(publicUser);
        // Deactivated project
        creator.setProjectAcl(GlobalAdmissionContext.getPublicReadACL());
        creator.setProjectName("ProjectServiceTest:test005_deactivated");
        Project p = creator.createAndSaveProject(publicUser);
        instance.changeDeactivationState(p.getId(), true);

        List<String> names = instance.getSimilarProjectNames("test005", publicUser);
        Assert.assertEquals(2, names.size());
    }

    private void cleanUp(User user2, ACList projectAcList, Project p) {
        Map<String, Object> params = new HashMap<> ();
        entityManagerService.doSqlUpdate("delete from projecttemplates");
        entityManagerService.doSqlUpdate("delete from budgetreservations");
        params.put("aclist", projectAcList.getId());
        entityManagerService.doSqlUpdate("delete from acentries where aclist_id=:aclist", params);
        params.clear();
        params.put("id", p.getId());
        entityManagerService.doSqlUpdate("delete from projects where id=:id", params);
        params.clear();
        params.put("aclist", projectAcList.getId());
        entityManagerService.doSqlUpdate("delete from aclists where id=:aclist", params);
        params.clear();
        params.put("id", user2.getId());
        entityManagerService.doSqlUpdate("delete from memberships where member_id=:id", params);
        params.clear();
        params.put("name", user2.getName());
        entityManagerService.doSqlUpdate("delete from usersgroups where name=:name", params);
    }

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("ProjectServiceTest.war")
                .addClass(SystemSettings.class)
                .addClass(ACListService.class)
                .addClass(ProjectService.class);
    }

    private void saveProjectEntry(String name) {
        entityManagerService.doSqlUpdate(
                String.format("INSERT INTO "
                        + "projects (name,projecttypeid,owner_id,aclist_id) "
                        + "VALUES('%s',1,1,1)",
                        name));
    }
}
