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
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.HashMap;
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
public class StorageInformationBuilderTest extends TestBase {

    private static final long serialVersionUID = 1L;

    @Inject
    private MaterialService materialService;

    @Inject
    private ProjectService projectService;

    private Project project;

    @BeforeEach
    public void init() {
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        creationTools = new CreationTools("", "", "", memberService, projectService);
        project = creationTools.createAndSaveProject("StorageClassControllerTest_project");
        materialService.setStructureInformationSaver(new StructureInformationSaverMock());
    }

    @AfterEach
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
        StorageInformationBuilder controller = new StorageInformationBuilder(MessagePresenterMock.getInstance(), materialService);
        Assert.assertEquals(23, controller.getPossibleStorageClasses().size());
        Assert.assertNotNull(controller.build());
        Assert.assertNull(controller.build().getStorageClass());
        Assert.assertTrue(controller.build().getStorageConditions().isEmpty());

        //Create a controller with a material. The use case is editing a material
        controller = new StorageInformationBuilder(MessagePresenterMock.getInstance(), materialService, s);
        Assert.assertEquals(23, controller.getPossibleStorageClasses().size());
        Assert.assertNotNull(controller.build());
        Assert.assertNotNull(controller.build().getStorageClass());
        Assert.assertEquals(1, controller.build().getStorageClass().id, 0);

        Assert.assertEquals(1, controller.build().getStorageConditions().size(), 3);

        controller = new StorageInformationBuilder(MessagePresenterMock.getInstance(), materialService, s);
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
        StorageInformationBuilder controller = new StorageInformationBuilder(MessagePresenterMock.getInstance(), materialService);
        controller.setStorageClassActivated(true);
        controller.setChoosenStorageClass(materialService.loadStorageClasses().get(0));
        controller.setRemarks("storage class remark");
        Assert.assertEquals(materialService.loadStorageClasses().get(0), controller.getChoosenStorageClass());
        Assert.assertTrue(controller.isStorageClassActivated());
        //Deactivate storage class selection
        controller.setStorageClassActivated(false);
    }

    @Test
    public void isStorageClassDisabled() {
        StorageInformationBuilder controller = new StorageInformationBuilder(MessagePresenterMock.getInstance(), materialService);

        Assert.assertTrue(controller.isStorageClassDisabled());

        controller.setStorageClassActivated(true);
        Assert.assertFalse(controller.isStorageClassDisabled());

        controller.setInHistoryMode(true);
        Assert.assertTrue(controller.isStorageClassDisabled());
        controller.setStorageClassActivated(false);
        Assert.assertTrue(controller.isStorageClassDisabled());

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
