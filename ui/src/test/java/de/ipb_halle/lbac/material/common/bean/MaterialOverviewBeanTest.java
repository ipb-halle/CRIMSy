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
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.common.HazardType;
import de.ipb_halle.lbac.material.common.service.HazardService;
import de.ipb_halle.lbac.material.composition.Concentration;
import de.ipb_halle.lbac.material.mocks.MaterialOverviewBeanMock;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.project.ProjectType;
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
    Project project;

    @Inject
    private HazardService hazardService;

    @Inject
    private IndexService indexService;

    @BeforeEach
    public void init() {
        creationTools = new CreationTools("h-statement", "p-statement", "", memberService, projectService);
        project = new Project(ProjectType.BIOCHEMICAL_PROJECT, "Test-Project");
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        User ownerAccount = memberService.loadUserById(GlobalAdmissionContext.OWNER_ACCOUNT_ID);
        acl = new ACList();
        customUser = createUser("testUser", "testUser");
        acl.addACE(customUser, new ACPermission[]{ACPermission.permEDIT});
        acl.addACE(ownerAccount, ACPermission.values());
        acl = aclistService.save(acl);
        project.setOwner(publicUser);
        project.setACList(acl);
        projectService.saveProjectToDb(project);

        material = creationTools.createStructure(project);
        Structure s = (Structure) material;
        s.getMolecule().setStructureModel(null);
        material.setOwner(publicUser);
        materialService.saveMaterialToDB(material, acl.getId(), new HashMap<>(), publicUser);

        instance = new MaterialOverviewBeanMock();
        instance.hazardService = hazardService;
        instance.materialService = materialService;
        instance.init();
    }

    @AfterEach
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

    @Test
    public void test002_isRadioactive() {
        Material m = materialService.loadMaterialById(material.getId());

        Assert.assertFalse(instance.isRadioactive(m));
        m.getHazards().getHazards().put(new HazardType(16, false, "R1", 3), null);

        Assert.assertEquals("img/hazards/R1.png", instance.getRadioactiveImageLocation());
        Assert.assertTrue(instance.isRadioactive(m));
    }

    @Test
    public void test003_getImageLocationOfHazards() {
        Material m = materialService.loadMaterialById(material.getId());

        Assert.assertEquals(2, instance.getImageLocationOfHazards(m).size());
        Assert.assertTrue(instance.getImageLocationOfHazards(m).contains("img/hazards/GHS02.png"));
        Assert.assertTrue(instance.getImageLocationOfHazards(m).contains("img/hazards/GHS08.png"));
    }

    @Test
    public void test004_getHazardsRemark() {
        Material m = materialService.loadMaterialById(material.getId());
        Assert.assertEquals("h-statement", instance.getHazardRemark(m, 10));
        Assert.assertEquals("p-statement", instance.getHazardRemark(m, 11));
        Assert.assertNull(instance.getHazardRemark(m, 2));
        Assert.assertNull(instance.getHazardRemark(m, 8));
        //What happens if request for not existing hazard
        Assert.assertEquals("", instance.getHazardRemark(m, 1));
    }

    @Test
    public void test005_deactivateMaterial() {
        Material m = materialService.loadMaterialById(material.getId());
        instance.setCurrentAccount(new LoginEvent(publicUser));
        instance.actionDeactivateMaterial(m);
        List o = (List) entityManagerService.doSqlQuery("SELECT deactivated FROM materials WHERE materialid=" + m.getId());
        Assert.assertTrue(((Boolean) o.get(0)));

        entityManagerService.doSqlUpdate("UPDATE materials SET deactivated='false' WHERE materialid=" + m.getId());
    }

    @Test
    public void test006_hasAccessRight() {
        Material m = materialService.loadMaterialById(material.getId());
        instance.setCurrentAccount(new LoginEvent(publicUser));

        Assert.assertFalse(instance.hasAccessRight(m, "no valide right"));
        // Owner account
        Assert.assertTrue(instance.hasAccessRight(m, ACPermission.permDELETE.toString()));
        Assert.assertTrue(instance.hasAccessRight(m, ACPermission.permEDIT.toString()));
        Assert.assertTrue(instance.hasAccessRight(m, ACPermission.permGRANT.toString()));
        Assert.assertTrue(instance.hasAccessRight(m, ACPermission.permREAD.toString()));
        // another user
        instance.setCurrentAccount(new LoginEvent(customUser));
        Assert.assertFalse(instance.hasAccessRight(m, ACPermission.permDELETE.toString()));
        Assert.assertTrue(instance.hasAccessRight(m, ACPermission.permEDIT.toString()));
        Assert.assertFalse(instance.hasAccessRight(m, ACPermission.permGRANT.toString()));
        Assert.assertFalse(instance.hasAccessRight(m, ACPermission.permREAD.toString()));
    }

    @Test
    public void test007_getConcentrationString() {
        Concentration c = new Concentration(material);

        Assert.assertEquals("- Test-Struktur", instance.getComponentOfComposition(c));

        c.setConcentration(24.83748237);
        Assert.assertEquals("- 24.8375% Test-Struktur", instance.getComponentOfComposition(c));

        c.setConcentration(Double.POSITIVE_INFINITY);
        Assert.assertEquals("- Test-Struktur", instance.getComponentOfComposition(c));

        c.setConcentration(Double.NEGATIVE_INFINITY);
        Assert.assertEquals("- Test-Struktur", instance.getComponentOfComposition(c));

        c.setConcentration(Double.NaN);
        Assert.assertEquals("- Test-Struktur", instance.getComponentOfComposition(c));

        c.setConcentration(20d);
        material.getNames().clear();
        Assert.assertEquals("- 20.0000% Materialid: "+material.getId(), instance.getComponentOfComposition(c));
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
