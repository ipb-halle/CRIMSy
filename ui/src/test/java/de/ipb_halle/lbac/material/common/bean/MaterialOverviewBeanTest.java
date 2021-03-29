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

import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.UserBeanMock;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.device.print.PrintBeanDeployment;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.service.IndexService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.mocks.MaterialOverviewBeanMock;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.project.ProjectType;
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
public class MaterialOverviewBeanTest extends TestBase {

    @Inject
    private ACListService aclistService;

    @Inject
    private MaterialService materialService;

    @Inject
    private ProjectService projectService;

    MaterialOverviewBeanMock instance;
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
    public void setUp() {
        super.setUp();
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

        material = creationTools.createStructure(project);
        Structure s = (Structure) material;
        s.getMolecule().setStructureModel(null);
        material.setOwner(publicUser);
        materialService.setUserBean(userBean);

        materialService.saveMaterialToDB(material, acl.getId(), new HashMap<>());
    }

    @After
    public void finish() {
        cleanMaterialsFromDB();
        cleanProjectFromDB(project, false);

    }

    @Test
    public void test001_getNamesforUiAndToolTip() {
        instance = new MaterialOverviewBeanMock();
        Material m = materialService.loadMaterialById(material.getId());
        instance.init();
        Assert.assertEquals("Test-Struktur<br>...", instance.getWrappedNames(m, 1));
        Assert.assertEquals("Test-Struktur<br>Test-Structure", instance.getWrappedNames(m, 2));
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment
                = prepareDeployment("MaterialOverviewBeanTest.war")
                .addClass(IndexService.class);
        deployment = UserBeanDeployment.add(deployment);
        deployment = ItemDeployment.add(deployment);
        return PrintBeanDeployment.add(deployment);
    }
}
