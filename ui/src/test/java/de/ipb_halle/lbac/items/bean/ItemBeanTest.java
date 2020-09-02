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

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.UserBeanMock;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.container.service.ContainerPositionService;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.device.print.PrintBean;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.mocks.ItemBeanMock;
import de.ipb_halle.lbac.items.mocks.ItemOverviewBeanMock;
import de.ipb_halle.lbac.items.mocks.NavigatorMock;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.mocks.MaterialEditSaverMock;
import de.ipb_halle.lbac.material.structure.StructureInformationSaverMock;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import java.util.HashMap;
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
public class ItemBeanTest extends TestBase {

    private ItemBeanMock itemBean;
    private PrintBean printBean;
    private ItemOverviewBean overviewBean;
    private UserBeanMock userBean;

    @Inject
    private ItemService itemService;
    @Inject
    private ProjectService projectService;
    @Inject
    private ContainerService containerService;
    @Inject
    private ContainerPositionService containerPositionService;

    @Inject
    protected MaterialService materialService;
    protected Project project;
    protected Material structure;
    protected User user;

    protected ItemOverviewBean itemOverviewBean;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        creationTools = new CreationTools("", "", "", memberService, projectService);
        project = creationTools.createProject();
        structure = creationTools.createStructure(project);
        structure.setACList(project.getACList());
        user = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        materialService.setStructureInformationSaver(new StructureInformationSaverMock(materialService.getEm()));
        materialService.setEditedMaterialSaver(new MaterialEditSaverMock(materialService));
        materialService.saveMaterialToDB(structure, project.getUserGroups().getId(), new HashMap<>(), user.getId());

        userBean = new UserBeanMock();
        userBean.setCurrentAccount(user);

        itemBean = new ItemBeanMock();
        printBean = new PrintBean();
        overviewBean = new ItemOverviewBeanMock()
                .setItemBean(itemBean)
                .setItemService(itemService)
                .setMaterialService(materialService)
                .setMemberService(memberService)
                .setNavigator(new NavigatorMock(userBean))
                .setProjectService(projectService)
                .setUser(user);

        itemBean.setItemService(itemService);
        itemBean.setPrintBean(printBean);
        itemBean.setItemOverviewBean(overviewBean);
        itemBean.setProjectService(projectService);
        itemBean.setContainerService(containerService);
        itemBean.setContainerPositionService(containerPositionService);
        itemBean.setNavigator(new NavigatorMock(userBean));
        itemBean.setUserBean(userBean);

    }

    @Test
    public void test001_createNewItem() {
        itemBean.actionStartItemCreation(structure);
        itemBean.getState().getEditedItem().setAmount(20d);
        itemBean.getState().getEditedItem().setUnit("g");
        itemBean.setBasicContainerType(new ContainerType("GLAS_FLASK", 0, false, false));
        itemBean.getState().getEditedItem().setConcentration(.5d);
        itemBean.getState().getEditedItem().setContainerSize(40d);
        itemBean.setProject(project);
        itemBean.getState().getEditedItem().setPurity("pure");

        Assert.assertEquals(ItemBean.Mode.CREATE, itemBean.mode);

        itemBean.actionSave();

    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("ItemBeanTest.war")
                .addClass(Navigator.class)
                .addClass(ProjectService.class);
        return UserBeanDeployment.add(ItemDeployment.add(deployment));
    }
}
