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
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        MaterialHazardBuilder controller = new MaterialHazardBuilder(hazardService, structure.getType(), true, new HashMap<>(), new MessagePresenterMock());
        Assert.assertFalse(controller.isHazardCategoryRendered(HazardType.Category.BSL.toString()));
        Assert.assertTrue(controller.isHazardCategoryRendered(HazardType.Category.CUSTOM.toString()));
        Assert.assertTrue(controller.isHazardCategoryRendered(HazardType.Category.STATEMENTS.toString()));
        Assert.assertTrue(controller.isHazardCategoryRendered(HazardType.Category.GHS.toString()));
        Assert.assertTrue(controller.isHazardCategoryRendered(HazardType.Category.RADIOACTIVITY.toString()));
        //Check Biomaterial
        controller = new MaterialHazardBuilder(hazardService, bioMaterial.getType(), true, new HashMap<>(), new MessagePresenterMock());
        Assert.assertTrue(controller.isHazardCategoryRendered(HazardType.Category.BSL.toString()));
        Assert.assertTrue(controller.isHazardCategoryRendered(HazardType.Category.CUSTOM.toString()));
        Assert.assertFalse(controller.isHazardCategoryRendered(HazardType.Category.STATEMENTS.toString()));
        Assert.assertFalse(controller.isHazardCategoryRendered(HazardType.Category.GHS.toString()));
        Assert.assertFalse(controller.isHazardCategoryRendered(HazardType.Category.RADIOACTIVITY.toString()));
        //Check Consumable
        controller = new MaterialHazardBuilder(hazardService, consumable.getType(), true, new HashMap<>(), new MessagePresenterMock());
        Assert.assertFalse(controller.isHazardCategoryRendered(HazardType.Category.BSL.toString()));
        Assert.assertTrue(controller.isHazardCategoryRendered(HazardType.Category.CUSTOM.toString()));
        Assert.assertFalse(controller.isHazardCategoryRendered(HazardType.Category.STATEMENTS.toString()));
        Assert.assertFalse(controller.isHazardCategoryRendered(HazardType.Category.GHS.toString()));
        Assert.assertFalse(controller.isHazardCategoryRendered(HazardType.Category.RADIOACTIVITY.toString()));
    }

    @Test
    public void test002_getImageLocation() {
        MaterialHazardBuilder controller = new MaterialHazardBuilder(hazardService, consumable.getType(), true, new HashMap<>(), new MessagePresenterMock());
        Assert.assertEquals(
                "/resources/img/hazards/GHS01.png",
                controller.getImageLocation(new HazardType(1, true, "GHS01", 1)));
    }

    @Test
    public void test003_getHazardsOfType() {
        MaterialHazardBuilder controller = new MaterialHazardBuilder(hazardService, consumable.getType(), true, new HashMap<>(), new MessagePresenterMock());
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
        Assert.assertEquals(11, hazards.size());
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
        MaterialHazardBuilder controller = new MaterialHazardBuilder(hazardService, consumable.getType(), true, new HashMap<>(), new MessagePresenterMock());
        Assert.assertTrue(controller.buildHazardsMap().isEmpty());
        HazardType explosive = hazardService.getHazardOf(HazardType.Category.GHS).get(0);
        controller.setSelectedHazards(new HazardType[]{explosive});

        Assert.assertEquals(1, controller.buildHazardsMap().size());

        controller.setRadioctive(true);
        Assert.assertEquals(2, controller.buildHazardsMap().size());

        controller.setpStatements("P-Statemnet");
        Assert.assertEquals(3, controller.buildHazardsMap().size());

        controller.sethStatements("H-Statemnet");
        Assert.assertEquals(4, controller.buildHazardsMap().size());

        controller.setCustomText("C-Statemnet");
        Assert.assertEquals(5, controller.buildHazardsMap().size());
    }

    @Test
    public void test005_initControllerwithExistingHazards() {

        Map<HazardType, String> types = new HashMap<>();
        types.put(hazardService.getHazardOf(HazardType.Category.GHS).get(0), null);
        types.put(hazardService.getHazardOf(HazardType.Category.CUSTOM).get(0), "customText");
        types.put(hazardService.getHazardOf(HazardType.Category.STATEMENTS).get(0), "h-statement");
        MaterialHazardBuilder controller = new MaterialHazardBuilder(hazardService, consumable.getType(), true, types, new MessagePresenterMock());

        Assert.assertEquals(3, controller.buildHazardsMap().size());
    }

    @Test
    public void test006_createHazardMapWithBioSavetyLevel() {
        MaterialHazardBuilder controller = new MaterialHazardBuilder(hazardService, bioMaterial.getType(), true, new HashMap<>(), new MessagePresenterMock());
        Map<HazardType, String> hazards = controller.buildHazardsMap();
        Assert.assertEquals(0, hazards.size());
        //set level 0
        controller.setBioSavetyLevel(controller.getPossibleBioSavetyLevels().get(4));
        hazards = controller.buildHazardsMap();
        Assert.assertEquals(0, hazards.size());
        // Set level 1
        controller.setBioSavetyLevel(controller.getPossibleBioSavetyLevels().get(0));
        hazards = controller.buildHazardsMap();
        Assert.assertEquals(1, hazards.size());
        hazards.keySet().iterator().next().equals(hazardService.getHazardById(12));
        // Set level 2
        controller.setBioSavetyLevel(controller.getPossibleBioSavetyLevels().get(1));
        hazards = controller.buildHazardsMap();
        Assert.assertEquals(1, hazards.size());
        hazards.keySet().iterator().next().equals(hazardService.getHazardById(13));
        // Set level 3
        controller.setBioSavetyLevel(controller.getPossibleBioSavetyLevels().get(2));
        hazards = controller.buildHazardsMap();
        Assert.assertEquals(1, hazards.size());
        hazards.keySet().iterator().next().equals(hazardService.getHazardById(14));
        // Set level 4
        controller.setBioSavetyLevel(controller.getPossibleBioSavetyLevels().get(3));
        hazards = controller.buildHazardsMap();
        Assert.assertEquals(1, hazards.size());
        hazards.keySet().iterator().next().equals(hazardService.getHazardById(15));
    }

    @Test
    public void test007_createControllerWithSavetyLevel() {
        Map<HazardType, String> hazards = new HashMap<>();
        hazards.put(hazardService.getHazardById(12), null);
        MaterialHazardBuilder controller = new MaterialHazardBuilder(hazardService, bioMaterial.getType(), true, hazards, new MessagePresenterMock());
        Assert.assertEquals(controller.getPossibleBioSavetyLevels().get(1), controller.getBioSavetyLevel());

        hazards.clear();
        hazards.put(hazardService.getHazardById(13), null);
        controller = new MaterialHazardBuilder(hazardService, bioMaterial.getType(), true, hazards, new MessagePresenterMock());
        Assert.assertEquals(controller.getPossibleBioSavetyLevels().get(2), controller.getBioSavetyLevel());

        hazards.clear();
        hazards.put(hazardService.getHazardById(14), null);
        controller = new MaterialHazardBuilder(hazardService, bioMaterial.getType(), true, hazards, new MessagePresenterMock());
        Assert.assertEquals(controller.getPossibleBioSavetyLevels().get(3), controller.getBioSavetyLevel());

        hazards.clear();
        hazards.put(hazardService.getHazardById(15), null);
        controller = new MaterialHazardBuilder(hazardService, bioMaterial.getType(), true, hazards, new MessagePresenterMock());
        Assert.assertEquals(controller.getPossibleBioSavetyLevels().get(4), controller.getBioSavetyLevel());
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
