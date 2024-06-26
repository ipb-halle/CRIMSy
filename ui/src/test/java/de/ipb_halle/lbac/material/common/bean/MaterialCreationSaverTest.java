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

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.mock.UserBeanMock;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.device.print.PrintBeanDeployment;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.structure.StructureInformation;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.common.MaterialDetailType;
import de.ipb_halle.lbac.material.common.search.MaterialSearchRequestBuilder;
import de.ipb_halle.lbac.material.consumable.Consumable;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.ArrayList;
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
/**
 *
 * @author fmauz
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class MaterialCreationSaverTest extends TestBase {

    @Inject
    private MaterialService materialService;

    private CreationTools creationTools;
    @Inject
    private ProjectService projectService;

    @BeforeEach
    public void init() {
        creationTools = new CreationTools("", "", "", memberService, projectService);
        cleanItemsFromDb();
        cleanMaterialsFromDB();
    }

    @AfterEach
    public void after() {
        cleanItemsFromDb();
        cleanMaterialsFromDB();
    }

    @Test
    public void test001_saveNewStructure() {
        MaterialNameBean nameBean = new MaterialNameBean();
        MaterialCreationSaver saver = new MaterialCreationSaver(nameBean, materialService);
        Project p = creationTools.createProject();
        StructureInformation structureInfos = new StructureInformation();
        StorageInformation sci = new StorageInformation();
        sci.setRemarks("test-remark");
        //sci.setStorageClass(storageClass);
        saver.saveNewStructure(true, structureInfos, p, new HazardInformation(), sci, new ArrayList<>(), publicUser);

        List<Object> o = entityManagerService.doSqlQuery("SELECT * FROM materials");
        Assert.assertEquals(1, o.size());
        o = entityManagerService.doSqlQuery("SELECT * FROM structures");
        Assert.assertEquals(1, o.size());

        o = entityManagerService.doSqlQuery("SELECT * FROM storages");
        Assert.assertEquals(0, o.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test002_saveConsumable() {
        MaterialNameBean nameBean = new MaterialNameBean();
        MaterialCreationSaver saver = new MaterialCreationSaver(nameBean, materialService);
        UserBeanMock userBean = new UserBeanMock();
        userBean.setCurrentAccount(publicUser);

        Project p = creationTools.createProject();
        p.setName("test002_saveConsumable");
        p.getDetailTemplates().put(MaterialDetailType.COMMON_INFORMATION, p.getACList());
        projectService.saveProjectToDb(p);
        StorageInformation sci = new StorageInformation();
        saver.saveConsumable(p, new HazardInformation(), sci, new ArrayList<>(), publicUser);
        List<Object> o = entityManagerService.doSqlQuery("SELECT * FROM materials");
        Assert.assertEquals(1, o.size());

        MaterialSearchRequestBuilder b = new MaterialSearchRequestBuilder(userBean.getCurrentAccount(), 0, 25);
        b.addMaterialType(MaterialType.CONSUMABLE);

        List<Consumable> c = materialService.loadReadableMaterials(b.build()).getAllFoundObjects(Consumable.class, nodeService.getLocalNode());

        c.get(0);
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment
                = prepareDeployment("MaterialCreationSaverTest.war");
        deployment = ItemDeployment.add(deployment);
        deployment = UserBeanDeployment.add(deployment);
        return PrintBeanDeployment.add(deployment);
    }
}
