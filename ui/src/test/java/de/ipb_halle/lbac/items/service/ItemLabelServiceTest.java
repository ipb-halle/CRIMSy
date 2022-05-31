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
package de.ipb_halle.lbac.items.service;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import javax.inject.Inject;

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
public class ItemLabelServiceTest extends TestBase {

    private static final long serialVersionUID = 1L;

    @Inject
    private ItemService itemService;

    @Inject
    private ProjectService projectService;

    @Inject
    private ItemLabelService labelService;

    private User publicUser;
    private ACList publicACList;
    private int itemId;

    @BeforeEach
    public void init() {
        creationTools = new CreationTools("", "", "", memberService, projectService);
        publicUser = context.getPublicAccount();
        publicACList = GlobalAdmissionContext.getPublicReadACL();
        cleanItemsFromDb();
        cleanMaterialsFromDB();

    }

    @AfterEach
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

    @Test
    public void isLabelAvailable() {
        Assert.assertTrue(labelService.isLabelAvailable("00001"));

        createItem();
        Item item = itemService.loadItemById(itemId);
        Assert.assertFalse(labelService.isLabelAvailable(item.getLabel()));
    }

    private void createItem() {
        Project p = creationTools.createAndSaveProject("LabelServiceTest_Project");
        int materialId = materialCreator.createStructure(
                publicUser.getId(),
                publicACList.getId(),
                p.getId(),
                "Test-Structure");
        itemId = itemCreator.createItem(publicUser.getId(), publicACList.getId(), materialId, "Test Item",p.getId());
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("LabelServiceTest.war");
        return ItemDeployment.add(UserBeanDeployment.add(deployment));
    }
}
