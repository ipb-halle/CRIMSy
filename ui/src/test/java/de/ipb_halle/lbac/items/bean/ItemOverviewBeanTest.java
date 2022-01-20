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
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.mock.UserBeanMock;
import de.ipb_halle.lbac.base.ContainerCreator;
import de.ipb_halle.lbac.base.ProjectCreator;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.service.ContainerPositionService;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.device.print.PrintBean;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.ItemHistory;
import de.ipb_halle.lbac.items.mocks.ItemBeanMock;
import de.ipb_halle.lbac.items.mocks.ItemOverviewBeanMock;
import de.ipb_halle.lbac.items.mocks.NavigatorMock;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.project.ProjectType;
import java.util.List;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author fmauz
 */
@ExtendWith(ArquillianExtension.class)
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
    private Integer itemid_1, itemid_2, itemid_3, restrictedItemId, itemid_project, itemid_container, itemid_containerAndProject;
    private String item1_Label;
    private Project project;
    protected ContainerCreator containerCreator;

    @BeforeEach
    public void init() {
        creationTools = new CreationTools("", "", "", memberService, projectService);
        user = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        containerCreator = new ContainerCreator(entityManagerService, containerService);
        aclist = new ACList();
        aclist.addACE(user, ACPermission.values());
        aclist = aclistService.save(aclist);
        userBean = new UserBeanMock();
        userBean.setCurrentAccount(user);
        itemBean = new ItemBeanMock();
        
        project= new ProjectCreator(projectService,GlobalAdmissionContext.getPublicReadACL())
                .setProjectName("ItemOverviewBeanTest")
                .createAndSaveProject(user);

        itemOverviewBean = new ItemOverviewBeanMock()
                .setItemBean(itemBean)
                .setItemService(itemService)
                .setMaterialService(materialService)
                .setNavigator(new NavigatorMock(userBean))
                .setProjectService(projectService)
                .setMemberService(memberService)
                .setContainerService(containerService)
                .setNodeService(nodeService)
                .setUser(user);

        itemBean.setItemService(itemService);
        itemBean.setPrintBean(new PrintBean());
        itemBean.setItemOverviewBean(itemOverviewBean);
        itemBean.setProjectService(projectService);
        itemBean.setContainerService(containerService);
        itemBean.setContainerPositionService(containerPositionService);
        itemBean.setNavigator(new NavigatorMock(userBean));
        itemBean.setUserBean(userBean);

    }
    
    @Test
    public void test001_reloadItems() {
        createAndSaveItems();
        //Load items without restrictions
        itemOverviewBean.setCurrentAccount(new LoginEvent(user));

        itemOverviewBean.actionApplySearchFilter();
        Assert.assertEquals(3, itemOverviewBean.getItems().size());

        //Load items with restriction to materialname
        itemOverviewBean.getSearchMaskValues().setMaterialName("Wasserstoff");
        itemOverviewBean.actionApplySearchFilter();
        Assert.assertEquals(1, itemOverviewBean.getItems().size());

        //Load items after clearing restrictions
        itemOverviewBean.actionClearSearchFilter();
        itemOverviewBean.actionApplySearchFilter();
        Assert.assertEquals(3, itemOverviewBean.getItems().size());

        //Load items with a restricted item
        createAndSaveRestrictedItem();
        itemOverviewBean.actionApplySearchFilter();
        Assert.assertEquals(3, itemOverviewBean.getItems().size());

        //Load item by label
        itemOverviewBean.getSearchMaskValues().setLabel(item1_Label);
        itemOverviewBean.actionApplySearchFilter();
        Assert.assertEquals(1, itemOverviewBean.getItems().size());
        itemOverviewBean.actionClearSearchFilter();

        //Load items by user
        itemOverviewBean.getSearchMaskValues().setUserName(user.getName());
        itemOverviewBean.actionApplySearchFilter();
        Assert.assertEquals(3, itemOverviewBean.getItems().size());
        itemOverviewBean.actionClearSearchFilter();

        //Load items by project
        createItemWithProject();
        itemOverviewBean.getSearchMaskValues().setProjectName("biochemical-test-project");
        itemOverviewBean.actionApplySearchFilter();
        Assert.assertEquals(1, itemOverviewBean.getItems().size());
        itemOverviewBean.actionClearSearchFilter();

        //Load items by direct location
        createItemWithContainer();
        itemOverviewBean.getSearchMaskValues().setLocation("BOX");
        itemOverviewBean.actionApplySearchFilter();
        Assert.assertEquals(1, itemOverviewBean.getItems().size());
        itemOverviewBean.actionClearSearchFilter();

        //Load items by nested location
        itemOverviewBean.getSearchMaskValues().setLocation("ROOM");
        itemOverviewBean.actionApplySearchFilter();
        Assert.assertEquals(1, itemOverviewBean.getItems().size());
        itemOverviewBean.actionClearSearchFilter();

        //Load items by description 
        itemOverviewBean.getSearchMaskValues().setDescription("TestItem");
        itemOverviewBean.actionApplySearchFilter();
        Assert.assertEquals(3, itemOverviewBean.getItems().size());
        itemOverviewBean.actionClearSearchFilter();

        itemOverviewBean.getSearchMaskValues().setDescription("X");
        itemOverviewBean.actionApplySearchFilter();
        Assert.assertEquals(0, itemOverviewBean.getItemAmount());
    }

 
    @Test
    public void test002_itemTableNavigation() {
        materialid_1 = this.materialCreator.createStructure(
                user.getId(),
                aclist.getId(),
                null,
                "Wasser");
        for (int i = 0; i < 106; i++) {
            
            itemCreator.createItem(user.getId(), aclist.getId(), materialid_1, "TestItem " + i,project.getId());
        }

        //Initial table content
        itemOverviewBean.actionApplySearchFilter();
        Assert.assertEquals(10, itemOverviewBean.getItems().size());
        Assert.assertEquals(106, itemOverviewBean.getItemAmount());
        Assert.assertEquals("TestItem 0", itemOverviewBean.getItems().get(0).getDescription());
        Assert.assertTrue(itemOverviewBean.isBackDeactivated());
        Assert.assertFalse(itemOverviewBean.isForwardDeactivated());
        Assert.assertEquals(1, itemOverviewBean.getLeftBorder());
        Assert.assertEquals(10, itemOverviewBean.getRightBorder());
        Assert.assertEquals(106, itemOverviewBean.getItemAmount());

        //go one step forward
        itemOverviewBean.actionNextItems();
        Assert.assertEquals(10, itemOverviewBean.getItems().size());
        Assert.assertEquals("TestItem 10", itemOverviewBean.getItems().get(0).getDescription());
        Assert.assertFalse(itemOverviewBean.isBackDeactivated());
        Assert.assertFalse(itemOverviewBean.isForwardDeactivated());
        Assert.assertEquals(11, itemOverviewBean.getLeftBorder());
        Assert.assertEquals(20, itemOverviewBean.getRightBorder());
        Assert.assertEquals(106, itemOverviewBean.getItemAmount());

        //go to the end of the list
        itemOverviewBean.actionEndItems();
        Assert.assertEquals(10, itemOverviewBean.getItems().size());
        Assert.assertEquals("TestItem 96", itemOverviewBean.getItems().get(0).getDescription());
        Assert.assertFalse(itemOverviewBean.isBackDeactivated());
        Assert.assertTrue(itemOverviewBean.isForwardDeactivated());
        Assert.assertEquals(97, itemOverviewBean.getLeftBorder());
        Assert.assertEquals(106, itemOverviewBean.getRightBorder());
        Assert.assertEquals(106, itemOverviewBean.getItemAmount());

        //go one step back
        itemOverviewBean.actionLastItems();
        Assert.assertEquals(10, itemOverviewBean.getItems().size());
        Assert.assertEquals("TestItem 86", itemOverviewBean.getItems().get(0).getDescription());
        Assert.assertFalse(itemOverviewBean.isBackDeactivated());
        Assert.assertFalse(itemOverviewBean.isForwardDeactivated());
        Assert.assertEquals(87, itemOverviewBean.getLeftBorder());
        Assert.assertEquals(96, itemOverviewBean.getRightBorder());
        Assert.assertEquals(106, itemOverviewBean.getItemAmount());

        //go to first item
        itemOverviewBean.actionFirstItems();
        Assert.assertEquals(10, itemOverviewBean.getItems().size());
        Assert.assertEquals("TestItem 0", itemOverviewBean.getItems().get(0).getDescription());
        Assert.assertTrue(itemOverviewBean.isBackDeactivated());
        Assert.assertFalse(itemOverviewBean.isForwardDeactivated());
        Assert.assertEquals(1, itemOverviewBean.getLeftBorder());
        Assert.assertEquals(10, itemOverviewBean.getRightBorder());
        Assert.assertEquals(106, itemOverviewBean.getItemAmount());
    }

   
    @Test
    public void test003_applyAclChanges() {
        materialid_1 = this.materialCreator.createStructure(user.getId(), aclist.getId(), null, "Wasser");
        itemid_1 = itemCreator.createItem(user.getId(), aclist.getId(), materialid_1, "TestItem1",project.getId());
        Item item = itemService.loadItemById(itemid_1);

        itemOverviewBean.actionStartAclChange(item);
        ACList acl = new ACList();
        item.setACList(acl);
        itemOverviewBean.getAcObjectController().actionApplyChanges();

        Item loadedItem = itemService.loadItemById(item.getId());
        Assert.assertTrue(loadedItem.getACList().getACEntries().isEmpty());
        ItemHistory history = (ItemHistory) loadedItem.getHistory().get(loadedItem.getHistory().firstKey()).get(0);
        Assert.assertEquals(aclist.getId(), history.getAcListOld().getId());

        itemOverviewBean.cancelAclChanges();
    }

  
    @Test
    public void test004_startItemEdit() {
        materialid_1 = this.materialCreator.createStructure(user.getId(), aclist.getId(), null, "Wasser");
        itemid_1 = itemCreator.createItem(user.getId(), aclist.getId(), materialid_1, "TestItem1",project.getId());
        Item item = itemService.loadItemById(itemid_1);
        itemOverviewBean.actionStartItemEdit(item);
    }

    @Test
    public void test005_getSimilarNames() {

        materialid_1 = this.materialCreator.createStructure(user.getId(), aclist.getId(), null, "Wasser", "water");
        materialid_2 = this.materialCreator.createStructure(user.getId(), aclist.getId(), null, "Wasserstoff");

        Project p1 = creationTools.createAndSaveProject("Project_X");
        Project p2 = creationTools.createAndSaveProject("Project_XY");
        Project p3 = creationTools.createAndSaveProject("Project_Z");

        Container room = containerCreator.createAndSaveContainer("ROOM", null);
        Container box = containerCreator.createAndSaveContainer("BOX", room);

        List<String> containerNames = itemOverviewBean.getSimilarContainerNames("BO");
        Assert.assertEquals(1, containerNames.size());
        Assert.assertEquals("BOX", containerNames.get(0));

        List<String> projectNames = itemOverviewBean.getSimilarProjectNames("Project_X");
        Assert.assertEquals(2, projectNames.size());
        Assert.assertEquals("Project_X", projectNames.get(0));
        Assert.assertEquals("Project_XY", projectNames.get(1));

        List<String> usernames = itemOverviewBean.getSimilarUserNames("public");
        Assert.assertEquals(1, usernames.size());
        Assert.assertEquals("Public Account", usernames.get(0));

        List<String> materialNames = itemOverviewBean.getSimilarMaterialNames("Wasser");
        Assert.assertEquals(2, materialNames.size());
        Assert.assertEquals("Wasser", materialNames.get(0));
        Assert.assertEquals("Wasserstoff", materialNames.get(1));
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
        itemid_1 = itemCreator.createItem(user.getId(), aclist.getId(), materialid_1, "TestItem1",project.getId());

        item1_Label = itemService.loadItemById(itemid_1).getLabel();
        itemid_2 = itemCreator.createItem(user.getId(), aclist.getId(), materialid_2, "TestItem2",project.getId());
        itemid_3 = itemCreator.createItem(user.getId(), aclist.getId(), materialid_3, "TestItem3",project.getId());

    }

    private void createAndSaveRestrictedItem() {
        User user2 = new User();
        user2.setName("ItemOverviewBeanTestUser");
        user2.setNode(nodeService.getLocalNode());
        user2 = memberService.save(user2);
        ACList acl = new ACList();
        acl.addACE(user2, ACPermission.values());
        acl = aclistService.save(acl);
        restrictedItemId = itemCreator.createItem(user2.getId(), acl.getId(), materialid_1, "RestrictedItem",project.getId());
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

    protected int createItemWithProjectAndContainer() {
        Container room = containerCreator.createAndSaveContainer("ROOM", null);
        Container box = containerCreator.createAndSaveContainer("BOX", room);
        room.getContainerHierarchy().add(box);
        Project p = creationTools.createProject();
        return itemCreator.createItem(user.getId(), aclist.getId(), materialid_1, "ContainerProjectItem", p, box);

    }
}
