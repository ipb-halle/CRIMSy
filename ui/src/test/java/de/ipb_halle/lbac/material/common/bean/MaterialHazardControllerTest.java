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
import de.ipb_halle.lbac.material.common.service.IndexService;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.MaterialDeployment;
import de.ipb_halle.lbac.material.biomaterial.BioMaterial;
import de.ipb_halle.lbac.material.common.HazardType;
import de.ipb_halle.lbac.material.common.service.HazardService;
import de.ipb_halle.lbac.material.consumable.Consumable;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import java.util.List;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class MaterialHazardControllerTest extends TestBase {

    private static final long serialVersionUID = 1L;

    @Inject
    private HazardService hazardService;
    @Inject
    private ProjectService projectService;

    private BioMaterial bioMaterial;
    private Structure structure;
    private Consumable consumable;
    private Project project;

    @Before
    public void init() {
        creationTools = new CreationTools("", "", "", memberService, projectService);
        project = creationTools.createAndSaveProject("MaterialHazardControllerTest_Project");
        structure = creationTools.createEmptyStructure(project.getId());
        bioMaterial = creationTools.createBioMaterial(project, "bioMaterial", null, null);
        consumable = creationTools.createConsumable(project, "consumable");

    }

    @Test
    public void test001_checkPossibleHazardCategories() {
        //Check Structure
        MaterialHazardController controller = new MaterialHazardController(hazardService, structure,true);
        Assert.assertFalse(controller.isHazardCategoryRendered(HazardType.Category.BSL.toString()));
        Assert.assertTrue(controller.isHazardCategoryRendered(HazardType.Category.CUSTOM.toString()));
        Assert.assertTrue(controller.isHazardCategoryRendered(HazardType.Category.STATEMENTS.toString()));
        Assert.assertTrue(controller.isHazardCategoryRendered(HazardType.Category.GHS.toString()));
        Assert.assertTrue(controller.isHazardCategoryRendered(HazardType.Category.RADIOACTIVITY.toString()));
        //Check Biomaterial
        controller = new MaterialHazardController(hazardService, bioMaterial,true);
        Assert.assertTrue(controller.isHazardCategoryRendered(HazardType.Category.BSL.toString()));
        Assert.assertTrue(controller.isHazardCategoryRendered(HazardType.Category.CUSTOM.toString()));
        Assert.assertFalse(controller.isHazardCategoryRendered(HazardType.Category.STATEMENTS.toString()));
        Assert.assertFalse(controller.isHazardCategoryRendered(HazardType.Category.GHS.toString()));
        Assert.assertFalse(controller.isHazardCategoryRendered(HazardType.Category.RADIOACTIVITY.toString()));
        //Check Consumable
        controller = new MaterialHazardController(hazardService, consumable,true);
        Assert.assertFalse(controller.isHazardCategoryRendered(HazardType.Category.BSL.toString()));
        Assert.assertTrue(controller.isHazardCategoryRendered(HazardType.Category.CUSTOM.toString()));
        Assert.assertFalse(controller.isHazardCategoryRendered(HazardType.Category.STATEMENTS.toString()));
        Assert.assertFalse(controller.isHazardCategoryRendered(HazardType.Category.GHS.toString()));
        Assert.assertFalse(controller.isHazardCategoryRendered(HazardType.Category.RADIOACTIVITY.toString()));
    }

    @Test
    public void test002_getImageLocation() {
        MaterialHazardController controller = new MaterialHazardController(hazardService, consumable,true);
        Assert.assertEquals(
                "/resources/img/hazards/GHS01.png",
                controller.getImageLocation(new HazardType(1, true, "GHS01", 1)));
    }

    @Test
    public void test003_getHazardsOfType() {
        MaterialHazardController controller = new MaterialHazardController(hazardService, consumable,true);
        List<HazardType> hazards = controller.getHazardsOfType(HazardType.Category.BSL.toString());
        Assert.assertEquals(4, hazards.size());
        Assert.assertEquals(12, hazards.get(0).getId());
        Assert.assertEquals(13, hazards.get(1).getId());
        Assert.assertEquals(14, hazards.get(2).getId());
        Assert.assertEquals(15, hazards.get(3).getId());

        hazards = controller.getHazardsOfType(HazardType.Category.CUSTOM.toString());
        Assert.assertEquals(1, hazards.size());
        Assert.assertEquals(17, hazards.get(0).getId());

        hazards = controller.getHazardsOfType(HazardType.Category.GHS.toString());
        Assert.assertEquals(9, hazards.size());
        for (int i = 1; i < 10; i++) {
            Assert.assertEquals(i, hazards.get(i - 1).getId());
        }

        hazards = controller.getHazardsOfType(HazardType.Category.RADIOACTIVITY.toString());
        Assert.assertEquals(1, hazards.size());
        Assert.assertEquals(16, hazards.get(0).getId());

        hazards = controller.getHazardsOfType(HazardType.Category.STATEMENTS.toString());
        Assert.assertEquals(2, hazards.size());
        Assert.assertEquals(10, hazards.get(0).getId());
        Assert.assertEquals(11, hazards.get(1).getId());
    }

    @Test
    public void test004_createHazardMap() {
        MaterialHazardController controller = new MaterialHazardController(hazardService, consumable,true);
        Assert.assertTrue(controller.createHazardMap().isEmpty());

        controller.getSelectedHazards().add(hazardService.getHazardOf(HazardType.Category.GHS).get(0));

        Assert.assertEquals(1, controller.createHazardMap().size());

        controller.setRadioctive(true);
        Assert.assertEquals(2, controller.createHazardMap().size());

        controller.setpStatements("P-Statemnet");
        Assert.assertEquals(3, controller.createHazardMap().size());

        controller.sethStatements("H-Statemnet");
        Assert.assertEquals(4, controller.createHazardMap().size());

        controller.setCustomText("C-Statemnet");
        Assert.assertEquals(5, controller.createHazardMap().size());
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment
                = prepareDeployment("MaterialHazardControllerTest.war")
                        .addClass(IndexService.class);
        deployment = ItemDeployment.add(deployment);
        deployment = UserBeanDeployment.add(deployment);
        return MaterialDeployment.add(PrintBeanDeployment.add(deployment));
    }
}
