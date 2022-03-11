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
package de.ipb_halle.lbac.exp;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.exp.image.Image;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.base.ItemCreator;
import de.ipb_halle.lbac.base.MaterialCreator;
import de.ipb_halle.lbac.base.ProjectCreator;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author fmauz
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class ExpRecordServiceTest extends TestBase {

    @Inject
    private ExperimentService experimentService;

    @Inject
    private ExpRecordService recordService;

    @Inject
    private ACListService aclistService;

    @Inject
    private ProjectService projectService;

    @Inject
    private MaterialService materialService;

    @Inject
    private ItemService itemService;

    @Inject
    private GlobalAdmissionContext context;

    private MaterialCreator materialCreator;
    private ItemCreator itemCreator;
    private ProjectCreator projectCreator;

    private Project project1;
    private Material material1;
    private Item item1;

    private User publicUser;
    private ACList publicReadAcl;
    private ACList nothingAcl;

    @BeforeEach
    public void init() {
        publicUser = memberService
                .loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        publicReadAcl = GlobalAdmissionContext.getPublicReadACL();
        nothingAcl = new ACList();
        nothingAcl = aclistService.save(nothingAcl);

        projectCreator = new ProjectCreator(projectService, publicReadAcl);
        project1 = projectCreator.createAndSaveProject(publicUser);

        materialCreator = new MaterialCreator(entityManagerService);
        int materialId = materialCreator.createStructure(publicUser.getId(),
                publicReadAcl.getId(), project1.getId(), "Benzol");
        material1 = materialService.loadMaterialById(materialId);
        itemCreator = new ItemCreator(entityManagerService);
        int itemId = itemCreator.createItem(publicUser.getId(),
                publicReadAcl.getId(), materialId,
                "100 ml Benzol in einer Flasche", project1);
        item1 = itemService.loadItemById(itemId);

    }

    @AfterEach
    public void finish() {
        entityManagerService.doSqlUpdate("DELETE FROM experiments");
    }

    @Test
    public void test001_deleteAssayRecord() {
        recordService.deleteAssayRecord(1);
    }

    @Test
    public void test002_saveLoadImageRecord() {
        Experiment experiment = new Experiment(null,
                "ExpRecordServiceTest:test002_saveLoadImageRecord()",
                "ExpRecordServiceTest:test002_saveLoadImageRecord()", false,
                publicReadAcl, publicUser, new Date());
        experiment = experimentService.save(experiment);
        
        Image image = new Image("abc", "def", "ghi", publicUser, publicReadAcl);
        image.setExperiment(experiment);
        image = (Image) recordService.save(image, publicUser);
                
        Experiment loadedExperiment = experimentService.loadById(experiment.getId());
        assertNotNull(loadedExperiment);
        
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(ExpRecordService.EXPERIMENT_ID, loadedExperiment.getId());
        List<ExpRecord> records = recordService.load(cmap, publicUser);
        assertEquals(1, records.size());
        Image loadedImage1 = (Image) records.get(0);
        assertEquals("abc", loadedImage1.getTitle());
        assertEquals("def", loadedImage1.getPreview());
        assertEquals("ghi", loadedImage1.getImage());
        
        Image loadedImage2 = (Image) recordService.loadById(image.getId(), publicUser);
        assertEquals("abc", loadedImage1.getTitle());
        assertEquals("def", loadedImage2.getPreview());
        assertEquals("ghi", loadedImage2.getImage());
    }
    
    // TODO: Test for saving/loading of Assay and Text records

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("ExpRecordServiceTest.war");
        return ExperimentDeployment
                .add(UserBeanDeployment.add(ItemDeployment.add(deployment)));
    }

}
