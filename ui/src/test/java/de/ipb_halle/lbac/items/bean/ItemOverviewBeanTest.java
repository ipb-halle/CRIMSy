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
package de.ipb_halle.lbac.items.bean;

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.UserBeanMock;
import de.ipb_halle.lbac.base.ContainerCreator;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.service.ContainerPositionService;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.device.print.PrintBean;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.mocks.ItemBeanMock;
import de.ipb_halle.lbac.items.mocks.ItemOverviewBeanMock;
import de.ipb_halle.lbac.items.mocks.NavigatorMock;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.navigation.Navigator;
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
public class ItemOverviewBeanTest extends TestBase {

    private ItemOverviewBeanMock itemOverviewBean;
    private ItemBeanMock itemBean;
    private UserBeanMock userBean;
    private User user;
    @Inject
    private ItemService itemService;
    @Inject
    private MaterialService materialService;
    @Inject
    private ProjectService projectService;
    @Inject
    private ContainerService containerService;
    @Inject
    private ContainerPositionService containerPositionService;
    @Inject
    private ACListService aclistService;

    private ACList aclist;
    private Integer materialid_1, materialid_2, materialid_3;
    private Integer itemid_1, itemid_2, itemid_3, restrictedItemId, itemid_project, itemid_container;
    protected ContainerCreator containerCreator;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        creationTools = new CreationTools("", "", "", memberService, projectService);
        user = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        containerCreator = new ContainerCreator(entityManagerService, containerService);
        aclist = new ACList();
        aclist.addACE(user, ACPermission.values());
        aclist = aclistService.save(aclist);
        userBean = new UserBeanMock();
        userBean.setCurrentAccount(user);
        itemBean = new ItemBeanMock();

        itemOverviewBean = new ItemOverviewBeanMock()
                .setItemBean(itemBean)
                .setItemService(itemService)
                .setMaterialService(materialService)
                .setNavigator(new NavigatorMock(userBean))
                .setProjectService(projectService)
                .setUser(user);

        itemBean.setItemService(itemService);
        itemBean.setPrintBean(new PrintBean());
        itemBean.setItemOverviewBean(itemOverviewBean);
        itemBean.setProjectService(projectService);
        itemBean.setContainerService(containerService);
        itemBean.setContainerPositionService(containerPositionService);
        itemBean.setNavigator(new NavigatorMock(userBean));
        itemBean.setUserBean(userBean);

        createAndSaveItems();

    }

    @Test
    public void test001_reloadItems() {
        //Load items without restrictions
        itemOverviewBean.reloadItems();
        Assert.assertEquals(3, itemOverviewBean.getItems().size());

        //Load items with restriction to materialname
        itemOverviewBean.getSearchMaskValues().setMaterialName("Wasserstoff");
        itemOverviewBean.reloadItems();
        Assert.assertEquals(1, itemOverviewBean.getItems().size());

        //Load items after clearing restrictions
        itemOverviewBean.actionClearSearchFilter();
        itemOverviewBean.reloadItems();
        Assert.assertEquals(3, itemOverviewBean.getItems().size());

        //Load items with a restricted item
        createAndSaveRestrictedItem();
        itemOverviewBean.reloadItems();
        Assert.assertEquals(3, itemOverviewBean.getItems().size());

        //Load item by id
        itemOverviewBean.getSearchMaskValues().setItemId(itemid_1.toString());
        itemOverviewBean.reloadItems();
        Assert.assertEquals(1, itemOverviewBean.getItems().size());
        itemOverviewBean.actionClearSearchFilter();

        //Load items by user
        itemOverviewBean.getSearchMaskValues().setUserName(user.getName());
        itemOverviewBean.reloadItems();
        Assert.assertEquals(3, itemOverviewBean.getItems().size());
        itemOverviewBean.actionClearSearchFilter();

        //Load items by project
        createItemWithProject();
        itemOverviewBean.getSearchMaskValues().setProjectName("biochemical-test-project");
        itemOverviewBean.reloadItems();
        Assert.assertEquals(1, itemOverviewBean.getItems().size());
        itemOverviewBean.actionClearSearchFilter();

        //Load items by direct location
        createItemWithContainer();
        itemOverviewBean.getSearchMaskValues().setLocation("BOX");
        itemOverviewBean.reloadItems();
        Assert.assertEquals(1, itemOverviewBean.getItems().size());
        itemOverviewBean.actionClearSearchFilter();

        //Load items by nested location
        itemOverviewBean.getSearchMaskValues().setLocation("ROOM");
        itemOverviewBean.reloadItems();
        Assert.assertEquals(1, itemOverviewBean.getItems().size());
        itemOverviewBean.actionClearSearchFilter();

        //Load items by description 
        itemOverviewBean.getSearchMaskValues().setDescription("TestItem");
        itemOverviewBean.reloadItems();
        Assert.assertEquals(3, itemOverviewBean.getItems().size());
        itemOverviewBean.actionClearSearchFilter();
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("ItemOverviewBeanTest.war")
                .addClass(Navigator.class)
                .addClass(ProjectService.class);
        return UserBeanDeployment.add(ItemDeployment.add(deployment));
    }

    private void createAndSaveItems() {
        materialid_1 = this.materialCreator.createStructure(
                user.getId(),
                aclist.getId(),
                null,
                "Wasser");

        materialid_2 = this.materialCreator.createStructure(
                user.getId(),
                aclist.getId(),
                null,
                "Sulfure");

        materialid_3 = this.materialCreator.createStructure(
                user.getId(),
                aclist.getId(),
                null,
                "Wasserstoff");
        itemid_1 = itemCreator.createItem(user.getId(), aclist.getId(), materialid_1, "TestItem1");
        itemid_2 = itemCreator.createItem(user.getId(), aclist.getId(), materialid_2, "TestItem2");
        itemid_3 = itemCreator.createItem(user.getId(), aclist.getId(), materialid_3, "TestItem3");

    }

    private void createAndSaveRestrictedItem() {
        User user2 = new User();
        user2.setName("ItemOverviewBeanTestUser");
        user2.setNode(nodeService.getLocalNode());
        user2 = memberService.save(user2);
        ACList acl = new ACList();
        acl.addACE(user2, ACPermission.values());
        acl = aclistService.save(acl);
        restrictedItemId = itemCreator.createItem(user2.getId(), acl.getId(), materialid_1, "RestrictedItem");
    }

    protected void createItemWithProject() {
        Project p = creationTools.createProject();
        itemid_project = itemCreator.createItem(user.getId(), aclist.getId(), materialid_1, "ProjectItem", p);
    }

    protected void createItemWithContainer() {
        Container room = containerCreator.createAndSaveContainer("ROOM", null);
        Container box = containerCreator.createAndSaveContainer("BOX", room);
        room.getContainerHierarchy().add(box);
        itemid_container = itemCreator.createItem(user.getId(), aclist.getId(), materialid_1, "ContainerItem", box);
    }
}
