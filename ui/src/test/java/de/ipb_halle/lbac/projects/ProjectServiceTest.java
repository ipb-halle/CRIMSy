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
package de.ipb_halle.lbac.projects;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.SystemSettings;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.ACPermission;
import de.ipb_halle.lbac.entity.Group;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.material.common.MaterialDetailType;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.project.ProjectType;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class ProjectServiceTest extends TestBase {

    @Inject
    private ProjectService instance;

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("ProjectServiceTest.war")
                 .addClass(SystemSettings.class)
                .addClass(ProjectService.class);

    }

    @Test
    public void test01_saveLoadProject() {
        cleanMaterialsFromDB();
        cleanAllProjectsFromDb();
        Project p = new Project(ProjectType.BIOCHEMICAL_PROJECT, "biochemical-test-project");
        p.setBudget(1000d);
        p.setDescription("Description of biochemical test project");

        Group g = memberService.loadGroupById(UUID.fromString(GlobalAdmissionContext.PUBLIC_GROUP_ID));
        User u = memberService.loadUserById(UUID.fromString(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID));

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
        p.setUserGroups(projectAcList);

        p.getDetailTemplates().put(MaterialDetailType.COMMON_INFORMATION, projectAcList);
        p.getDetailTemplates().put(MaterialDetailType.STORAGE_CLASSES, projectAcList);
        p.getDetailTemplates().put(MaterialDetailType.STRUCTURE_INFORMATION, projectAcList);
        p.getDetailTemplates().put(MaterialDetailType.HAZARD_INFORMATION, projectAcList);
        p.getDetailTemplates().put(MaterialDetailType.INDEX, projectAcList);
        p.getDetailTemplates().put(MaterialDetailType.TAXONOMY, projectAcList);

        instance.saveProjectToDb(p);

        List<Project> projectsOfPublicUser = instance.loadReadableProjectsOfUser(u);
        Assert.assertEquals("Only 1 project should be found", 1, projectsOfPublicUser.size());

        User user2 = createUser("UserWithoutPermission", "no name", nodeService.getLocalNode(), memberService, membershipService);
        List<Project> projectsOfUser2 = instance.loadReadableProjectsOfUser(user2);
        Assert.assertEquals("No project must be found", 0, projectsOfUser2.size());

        // Clean up the database to ensure idempotence
        entityManagerService.doSqlUpdate("delete from projecttemplates");
        entityManagerService.doSqlUpdate("delete from budgetreservations");
        entityManagerService.doSqlUpdate("delete from acentries where aclist_id='" + projectAcList.getId().toString() + "'");
        entityManagerService.doSqlUpdate("delete from projects where id=" + p.getId());
        entityManagerService.doSqlUpdate("delete from aclists where id='" + projectAcList.getId().toString() + "'");
        entityManagerService.deleteUserWithAllMemberships(user2.getId().toString());
        entityManagerService.doSqlUpdate("delete from usersgroups where name='" + user2.getName() + "'");

    }
}
