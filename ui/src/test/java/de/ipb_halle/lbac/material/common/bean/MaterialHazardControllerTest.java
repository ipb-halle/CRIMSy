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
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.common.HazardType;
import de.ipb_halle.lbac.material.common.service.HazardService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.consumable.Consumable;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
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
    public void test001_checkPossibleHauardCategories() {
        //Check Structure
        MaterialHazardController controller = new MaterialHazardController(hazardService, structure);
        Assert.assertFalse(controller.isHazardCategoryRendered(HazardType.Category.BSL.toString()));
        Assert.assertTrue(controller.isHazardCategoryRendered(HazardType.Category.CUSTOM.toString()));
        Assert.assertTrue(controller.isHazardCategoryRendered(HazardType.Category.STATEMENTS.toString()));
        Assert.assertTrue(controller.isHazardCategoryRendered(HazardType.Category.GHS.toString()));
        Assert.assertTrue(controller.isHazardCategoryRendered(HazardType.Category.RADIOACTIVITY.toString()));
        //Check Biomaterial
        controller = new MaterialHazardController(hazardService, bioMaterial);
        Assert.assertTrue(controller.isHazardCategoryRendered(HazardType.Category.BSL.toString()));
        Assert.assertTrue(controller.isHazardCategoryRendered(HazardType.Category.CUSTOM.toString()));
        Assert.assertFalse(controller.isHazardCategoryRendered(HazardType.Category.STATEMENTS.toString()));
        Assert.assertFalse(controller.isHazardCategoryRendered(HazardType.Category.GHS.toString()));
        Assert.assertFalse(controller.isHazardCategoryRendered(HazardType.Category.RADIOACTIVITY.toString()));
        //Check Consumable
        controller = new MaterialHazardController(hazardService, consumable);
        Assert.assertFalse(controller.isHazardCategoryRendered(HazardType.Category.BSL.toString()));
        Assert.assertTrue(controller.isHazardCategoryRendered(HazardType.Category.CUSTOM.toString()));
        Assert.assertFalse(controller.isHazardCategoryRendered(HazardType.Category.STATEMENTS.toString()));
        Assert.assertFalse(controller.isHazardCategoryRendered(HazardType.Category.GHS.toString()));
        Assert.assertFalse(controller.isHazardCategoryRendered(HazardType.Category.RADIOACTIVITY.toString()));
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
