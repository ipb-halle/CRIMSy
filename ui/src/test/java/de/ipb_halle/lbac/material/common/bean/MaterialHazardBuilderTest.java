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
public class MaterialHazardBuilderTest extends TestBase {

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
        project = creationTools.createAndSaveProject("MaterialHazardBuilderTest_Project");
        structure = creationTools.createEmptyStructure(project.getId());
        bioMaterial = creationTools.createBioMaterial(project, "bioMaterial", null, null);
        consumable = creationTools.createConsumable(project, "consumable");

    }

    @Test
    public void test001_checkPossibleHazardCategories() {
        //Check Structure
        MaterialHazardBuilder builder = new MaterialHazardBuilder(hazardService, structure.getType(), true, new HashMap<>(), MessagePresenterMock.getInstance());
        Assert.assertFalse(builder.isHazardCategoryRendered(HazardType.Category.BSL.toString()));
        Assert.assertTrue(builder.isHazardCategoryRendered(HazardType.Category.CUSTOM.toString()));
        Assert.assertTrue(builder.isHazardCategoryRendered(HazardType.Category.STATEMENTS.toString()));
        Assert.assertTrue(builder.isHazardCategoryRendered(HazardType.Category.GHS.toString()));
        Assert.assertTrue(builder.isHazardCategoryRendered(HazardType.Category.RADIOACTIVITY.toString()));
        //Check Biomaterial
        builder = new MaterialHazardBuilder(hazardService, bioMaterial.getType(), true, new HashMap<>(), MessagePresenterMock.getInstance());
        Assert.assertTrue(builder.isHazardCategoryRendered(HazardType.Category.BSL.toString()));
        Assert.assertTrue(builder.isHazardCategoryRendered(HazardType.Category.CUSTOM.toString()));
        Assert.assertFalse(builder.isHazardCategoryRendered(HazardType.Category.STATEMENTS.toString()));
        Assert.assertFalse(builder.isHazardCategoryRendered(HazardType.Category.GHS.toString()));
        Assert.assertFalse(builder.isHazardCategoryRendered(HazardType.Category.RADIOACTIVITY.toString()));
        //Check Consumable
        builder = new MaterialHazardBuilder(hazardService, consumable.getType(), true, new HashMap<>(), MessagePresenterMock.getInstance());
        Assert.assertFalse(builder.isHazardCategoryRendered(HazardType.Category.BSL.toString()));
        Assert.assertTrue(builder.isHazardCategoryRendered(HazardType.Category.CUSTOM.toString()));
        Assert.assertFalse(builder.isHazardCategoryRendered(HazardType.Category.STATEMENTS.toString()));
        Assert.assertFalse(builder.isHazardCategoryRendered(HazardType.Category.GHS.toString()));
        Assert.assertFalse(builder.isHazardCategoryRendered(HazardType.Category.RADIOACTIVITY.toString()));
    }

    @Test
    public void test002_getImageLocation() {
        MaterialHazardBuilder builder = new MaterialHazardBuilder(hazardService, consumable.getType(), true, new HashMap<>(), MessagePresenterMock.getInstance());
        Assert.assertEquals(
                "img/hazards/GHS01.png",
                builder.getImageLocation(new HazardType(1, true, "GHS01", 1)));
    }

    @Test
    public void test003_getHazardsOfType() {
        MaterialHazardBuilder builder = new MaterialHazardBuilder(hazardService, consumable.getType(), true, new HashMap<>(), MessagePresenterMock.getInstance());
        List<HazardType> hazards = builder.getHazardsOfType(HazardType.Category.BSL.toString());
        Assert.assertEquals(4, hazards.size());
        Assert.assertEquals(12, hazards.get(0).getId());
        Assert.assertEquals(13, hazards.get(1).getId());
        Assert.assertEquals(14, hazards.get(2).getId());
        Assert.assertEquals(15, hazards.get(3).getId());

        hazards = builder.getHazardsOfType(HazardType.Category.CUSTOM.toString());
        Assert.assertEquals(1, hazards.size());
        Assert.assertEquals(17, hazards.get(0).getId());

        hazards = builder.getHazardsOfType(HazardType.Category.GHS.toString());
        Assert.assertEquals(11, hazards.size());
        for (int i = 1; i < 10; i++) {
            Assert.assertEquals(i, hazards.get(i - 1).getId());
        }

        hazards = builder.getHazardsOfType(HazardType.Category.RADIOACTIVITY.toString());
        Assert.assertEquals(1, hazards.size());
        Assert.assertEquals(16, hazards.get(0).getId());

        hazards = builder.getHazardsOfType(HazardType.Category.STATEMENTS.toString());
        Assert.assertEquals(2, hazards.size());
        Assert.assertEquals(10, hazards.get(0).getId());
        Assert.assertEquals(11, hazards.get(1).getId());
    }

    @Test
    public void test004_createHazardMap() {
        MaterialHazardBuilder builder = new MaterialHazardBuilder(hazardService, consumable.getType(), true, new HashMap<>(), MessagePresenterMock.getInstance());
        Assert.assertTrue(builder.buildHazardsMap().isEmpty());
        HazardType explosive = hazardService.getHazardOf(HazardType.Category.GHS).get(0);
        builder.setSelectedHazards(new HazardType[]{explosive});

        Assert.assertEquals(1, builder.buildHazardsMap().size());

        builder.setRadioctive(true);
        Assert.assertEquals(2, builder.buildHazardsMap().size());

        builder.setpStatements("P-Statemnet");
        Assert.assertEquals(3, builder.buildHazardsMap().size());

        builder.sethStatements("H-Statemnet");
        Assert.assertEquals(4, builder.buildHazardsMap().size());

        builder.setCustomText("C-Statemnet");
        Assert.assertEquals(5, builder.buildHazardsMap().size());
    }

    @Test
    public void test005_initBuilderwithExistingHazards() {

        Map<HazardType, String> types = new HashMap<>();
        types.put(hazardService.getHazardOf(HazardType.Category.GHS).get(0), null);
        types.put(hazardService.getHazardOf(HazardType.Category.CUSTOM).get(0), "customText");
        types.put(hazardService.getHazardOf(HazardType.Category.STATEMENTS).get(0), "h-statement");
        MaterialHazardBuilder builder = new MaterialHazardBuilder(hazardService, consumable.getType(), true, types, MessagePresenterMock.getInstance());

        Assert.assertEquals(3, builder.buildHazardsMap().size());
    }

    @Test
    public void test006_createHazardMapWithBioSavetyLevel() {
        MaterialHazardBuilder builder = new MaterialHazardBuilder(hazardService, bioMaterial.getType(), true, new HashMap<>(), MessagePresenterMock.getInstance());
        Map<HazardType, String> hazards = builder.buildHazardsMap();
        Assert.assertEquals(0, hazards.size());
        //set level 0
        builder.setBioSavetyLevel(builder.getPossibleBioSavetyLevels().get(4));
        hazards = builder.buildHazardsMap();
        Assert.assertEquals(0, hazards.size());
        // Set level 1
        builder.setBioSavetyLevel(builder.getPossibleBioSavetyLevels().get(0));
        hazards = builder.buildHazardsMap();
        Assert.assertEquals(1, hazards.size());
        hazards.keySet().iterator().next().equals(hazardService.getHazardById(12));
        // Set level 2
        builder.setBioSavetyLevel(builder.getPossibleBioSavetyLevels().get(1));
        hazards = builder.buildHazardsMap();
        Assert.assertEquals(1, hazards.size());
        hazards.keySet().iterator().next().equals(hazardService.getHazardById(13));
        // Set level 3
        builder.setBioSavetyLevel(builder.getPossibleBioSavetyLevels().get(2));
        hazards = builder.buildHazardsMap();
        Assert.assertEquals(1, hazards.size());
        hazards.keySet().iterator().next().equals(hazardService.getHazardById(14));
        // Set level 4
        builder.setBioSavetyLevel(builder.getPossibleBioSavetyLevels().get(3));
        hazards = builder.buildHazardsMap();
        Assert.assertEquals(1, hazards.size());
        hazards.keySet().iterator().next().equals(hazardService.getHazardById(15));
    }

    @Test
    public void test007_createBuilderWithSavetyLevel() {
        Map<HazardType, String> hazards = new HashMap<>();
        hazards.put(hazardService.getHazardById(12), null);
        MaterialHazardBuilder builder = new MaterialHazardBuilder(hazardService, bioMaterial.getType(), true, hazards, MessagePresenterMock.getInstance());
        Assert.assertEquals(builder.getPossibleBioSavetyLevels().get(1), builder.getBioSavetyLevel());

        hazards.clear();
        hazards.put(hazardService.getHazardById(13), null);
        builder = new MaterialHazardBuilder(hazardService, bioMaterial.getType(), true, hazards, MessagePresenterMock.getInstance());
        Assert.assertEquals(builder.getPossibleBioSavetyLevels().get(2), builder.getBioSavetyLevel());

        hazards.clear();
        hazards.put(hazardService.getHazardById(14), null);
        builder = new MaterialHazardBuilder(hazardService, bioMaterial.getType(), true, hazards, MessagePresenterMock.getInstance());
        Assert.assertEquals(builder.getPossibleBioSavetyLevels().get(3), builder.getBioSavetyLevel());

        hazards.clear();
        hazards.put(hazardService.getHazardById(15), null);
        builder = new MaterialHazardBuilder(hazardService, bioMaterial.getType(), true, hazards, MessagePresenterMock.getInstance());
        Assert.assertEquals(builder.getPossibleBioSavetyLevels().get(4), builder.getBioSavetyLevel());
    }

    @Test
    public void test008_getImageLocationOfBls() {
        MaterialHazardBuilder builder = new MaterialHazardBuilder(hazardService, consumable.getType(), true, new HashMap<>(), MessagePresenterMock.getInstance());
        Assert.assertEquals("img/hazards/Empty.png", builder.getImageLocationOfBls(0));
        Assert.assertEquals("img/hazards/BIOHAZARD.png", builder.getImageLocationOfBls(1));
        Assert.assertEquals("img/hazards/BIOHAZARD.png", builder.getImageLocationOfBls(2));
        Assert.assertEquals("img/hazards/BIOHAZARD.png", builder.getImageLocationOfBls(3));
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment
                = prepareDeployment("MaterialHazardBuilderTest.war")
                        .addClass(IndexService.class);
        deployment = ItemDeployment.add(deployment);
        deployment = UserBeanDeployment.add(deployment);
        return MaterialDeployment.add(PrintBeanDeployment.add(deployment));
    }
}
