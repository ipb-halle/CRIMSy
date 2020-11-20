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
package de.ipb_halle.lbac.label;

import de.ipb_halle.lbac.items.service.*;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.EntityManagerService;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.container.service.ContainerNestingService;
import de.ipb_halle.lbac.container.service.ContainerPositionService;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.ItemDifference;
import de.ipb_halle.lbac.items.ItemHistory;
import de.ipb_halle.lbac.items.ItemPositionHistoryList;
import de.ipb_halle.lbac.items.ItemPositionsHistory;
import de.ipb_halle.lbac.items.search.ItemSearchRequestBuilder;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyNestingService;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.StorageClassInformation;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.structure.MoleculeService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.material.structure.Structure;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.SearchResult;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
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
public class LabelServiceTest extends TestBase {

    @Inject
    private ItemService itemService;

    @Inject
    private ProjectService projectService;

    @Inject
    private LabelService labelService;

    private User publicUser;
    private ACList publicACList;
    private int itemId;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        creationTools = new CreationTools("", "", "", memberService, projectService);
        publicUser = context.getPublicAccount();
        publicACList = GlobalAdmissionContext.getPublicReadACL();
        cleanItemsFromDb();
        cleanMaterialsFromDB();

    }

    @After
    public void finish() {
        cleanItemsFromDb();
        cleanMaterialsFromDB();
    }

    @Test
    public void createLabelTest() {
        createItem();

        String label = labelService.createLabel(1, Item.class);

        Assert.assertEquals("0000000017", label);
    }

    @Test
    public void saveItemLabelTest() {
        createItem();
        String label = labelService.createLabel(itemId, Item.class);
        labelService.saveItemLabel(label, itemId);
        
        

    }

    private void createItem() {
        Project p = creationTools.createAndSaveProject("LabelServiceTest_Project");
        int materialId = materialCreator.createStructure(
                publicUser.getId(),
                publicACList.getId(),
                p.getId(),
                "Test-Structure");
        itemId = itemCreator.createItem(publicUser.getId(), publicACList.getId(), materialId, "Test Item");
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("LabelServiceTest.war")
                .addClass(EntityManagerService.class)
                .addClass(ProjectService.class)
                .addClass(MaterialService.class)
                .addClass(ContainerService.class)
                .addClass(MoleculeService.class)
                .addClass(ArticleService.class)
                .addClass(TaxonomyService.class)
                .addClass(LabelService.class)
                .addClass(TissueService.class)
                .addClass(TaxonomyNestingService.class)
                .addClass(ContainerNestingService.class)
                .addClass(ContainerPositionService.class)
                .addClass(ItemService.class);
        return UserBeanDeployment.add(deployment);
    }
}
