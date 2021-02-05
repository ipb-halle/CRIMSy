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
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.base.ItemCreator;
import de.ipb_halle.lbac.base.MaterialCreator;
import de.ipb_halle.lbac.base.ProjectCreator;
import de.ipb_halle.lbac.exp.assay.Assay;
import de.ipb_halle.lbac.exp.assay.AssayService;
import de.ipb_halle.lbac.exp.search.ExperimentSearchRequestBuilder;
import de.ipb_halle.lbac.exp.text.Text;
import de.ipb_halle.lbac.exp.text.TextService;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchResult;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Before
    @Override
    public void setUp() {
        super.setUp();
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        publicReadAcl = GlobalAdmissionContext.getPublicReadACL();
        nothingAcl = new ACList();
        nothingAcl = aclistService.save(nothingAcl);

        projectCreator = new ProjectCreator(projectService, publicReadAcl);
        project1 = projectCreator.createAndSaveProject(publicUser);

        materialCreator = new MaterialCreator(entityManagerService);
        int materialId = materialCreator.createStructure(publicUser.getId(), publicReadAcl.getId(), project1.getId(), "Benzol");
        material1 = materialService.loadMaterialById(materialId);
        itemCreator = new ItemCreator(entityManagerService);
        int itemId = itemCreator.createItem(publicUser.getId(), publicReadAcl.getId(), materialId, "100 ml Benzol in einer Flasche", project1);
        item1 = itemService.loadItemById(itemId);

    }

    @After
    public void finish() {
        entityManagerService.doSqlUpdate("DELETE FROM experiments");
    }

    @Test
    public void test01_deleteAssayRecord() {
        recordService.deleteAssayRecord(1);
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("ExpRecordServiceTest.war");
        return ExperimentDeployment.add(UserBeanDeployment.add(ItemDeployment.add(deployment)));
    }

}
