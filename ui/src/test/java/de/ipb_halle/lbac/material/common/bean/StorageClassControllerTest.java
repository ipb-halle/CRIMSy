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

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.device.print.PrintBeanDeployment;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.material.common.service.IndexService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.MaterialDeployment;
import de.ipb_halle.lbac.material.common.StorageCondition;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.material.mocks.StructureInformationSaverMock;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
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
public class StorageClassControllerTest extends TestBase {

    private static final long serialVersionUID = 1L;

    @Inject
    private MaterialService materialService;

    @Inject
    private ProjectService projectService;

    private Project project;

    @Before
    public void init() {
        super.setUp();
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        creationTools = new CreationTools("", "", "", memberService, projectService);
        project = creationTools.createAndSaveProject("StorageClassControllerTest_project");
        materialService.setStructureInformationSaver(new StructureInformationSaverMock(materialService.getEm()));
    }

    @After
    public void finish() {
        cleanMaterialsFromDB();
        cleanProjectFromDB(project, false);

    }

    @Test
    public void test001_initStorageClassController() {
        Structure s = creationTools.createStructure(project);
        s.getStorageInformation().getStorageConditions().add(StorageCondition.awayFromOxidants);
        ACList publicReadAcl = GlobalAdmissionContext.getPublicReadACL();
        materialService.saveMaterialToDB(s, publicReadAcl.getId(), new HashMap<>(), publicUser);

        //Create a controller without a material. The use case is: creating a new material
        StorageClassBuilder controller = new StorageClassBuilder(new MessagePresenterMock(), materialService);
        Assert.assertEquals(23, controller.getPossibleStorageClasses().size());
        Assert.assertNotNull(controller.build());
        Assert.assertNull(controller.build().getStorageClass());
        Assert.assertTrue(controller.build().getStorageConditions().isEmpty());

        //Create a controller with a material. The use case is editing a material
        controller = new StorageClassBuilder(new MessagePresenterMock(), materialService, s);
        Assert.assertEquals(23, controller.getPossibleStorageClasses().size());
        Assert.assertNotNull(controller.build());
        Assert.assertNotNull(controller.build().getStorageClass());
        Assert.assertEquals(1, controller.build().getStorageClass().id, 0);

        Assert.assertEquals(1, controller.build().getStorageConditions().size(), 3);

    }

    @Test
    public void test002_selectStorageClass() {
        Structure s = creationTools.createStructure(project);
        s.getStorageInformation().getStorageConditions().add(StorageCondition.awayFromOxidants);
        ACList publicReadAcl = GlobalAdmissionContext.getPublicReadACL();
        materialService.saveMaterialToDB(s, publicReadAcl.getId(), new HashMap<>(), publicUser);

        //Activate storage class selection and set a storage class
        StorageClassBuilder controller = new StorageClassBuilder(new MessagePresenterMock(), materialService);
        controller.setStorageClassActivated(true);
        controller.setChoosenStorageClass(materialService.loadStorageClasses().get(0));
        controller.setRemarks("storage class remark");
        Assert.assertEquals(materialService.loadStorageClasses().get(0), controller.getChoosenStorageClass());
        Assert.assertTrue(controller.isStorageClassActivated());
        //Deactivate storage class selection
        controller.setStorageClassActivated(false);

    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment
                = prepareDeployment("StorageClassControllerTest.war")
                        .addClass(IndexService.class);
        deployment = ItemDeployment.add(deployment);
        deployment = UserBeanDeployment.add(deployment);
        return MaterialDeployment.add(PrintBeanDeployment.add(deployment));
    }
}