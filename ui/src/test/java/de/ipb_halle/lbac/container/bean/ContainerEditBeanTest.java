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
package de.ipb_halle.lbac.container.bean;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.container.mock.ContainerEditBeanMock;
import de.ipb_halle.lbac.container.mock.ContainerOverviewBeanMock;
import de.ipb_halle.lbac.container.mock.ContainerSearchMaskBeanMock;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author fmauz
 */
@ExtendWith(ArquillianExtension.class)
public class ContainerEditBeanTest extends TestBase {

    @Inject
    private ProjectService projectService;

    @Inject
    private ContainerService containerService;

    private ContainerEditBean bean;
    private User publicUser;

    private ContainerOverviewBeanMock overviewBean;
    private ContainerSearchMaskBean searchMaskBean;
    private CreationTools creationTools;

    @Test
    public void test001_logIn() {
        bean.setCurrentAccount(new LoginEvent(publicUser));
        Assert.assertEquals(5, bean.getGmoSafetyLevels().size());
        bean.getContainerTypesWithRankGreaterZero();
    }

    @Test
    public void test002_createNewContainer() {
        bean.setCurrentAccount(new LoginEvent(publicUser));
        bean.startNewContainerCreation();

        Assert.assertNotNull(bean.getContainerToCreate());
        Assert.assertEquals("container_edit_titel_create", bean.getDialogTitle());
        checkCleanState();
        bean.setContainerName("container-1-room");
        bean.setContainerType(bean.getContainerTypesWithRankGreaterZero().get(0));
        bean.setFireArea("Section-1");
        bean.setGmoSafetyLevel(bean.getGmoSafetyLevels().get(0));

        Assert.assertTrue(bean.isGmoSafetyLevelVisible());

        overviewBean.saveNewContainer();

        Container loadedContainer = containerService.loadContainerById(bean.getContainerToCreate().getId());

        bean.startNewContainerCreation();
        checkCleanState();

        bean.setContainerName("container-2-cupboard");
        bean.setContainerType(bean.getContainerTypesWithRankGreaterZero().get(1));
        bean.setContainerHeight(10);
        bean.setContainerWidth(20);
        bean.setFireArea("FireArea A");
        bean.setContainerLocation(loadedContainer);

        overviewBean.saveNewContainer();
        loadedContainer = containerService.loadContainerById(bean.getContainerToCreate().getId());
        Assert.assertNotNull(loadedContainer.getParentContainer());

        Project project = creationTools.createAndSaveProject("test002_createNewContainer - testproject");

        bean.startNewContainerCreation();
        bean.setContainerName("container-3-FREEZER");
        bean.setContainerType(bean.getContainerTypesWithRankGreaterZero().get(2));
        bean.setPreferredProjectName(project.getName());
        bean.setContainerHeight(10);
        bean.setContainerWidth(20);
        bean.setContainerLocation(loadedContainer);

        overviewBean.saveNewContainer();
        loadedContainer = containerService.loadContainerById(bean.getContainerToCreate().getId());
        Assert.assertEquals(Integer.valueOf(10), loadedContainer.getRows());
        Assert.assertEquals(Integer.valueOf(20), loadedContainer.getColumns());
        Assert.assertEquals("container-3-FREEZER", loadedContainer.getLabel());
        Assert.assertEquals(
                bean.getContainerTypesWithRankGreaterZero().get(2).getName(),
                loadedContainer.getType().getName());
        Assert.assertEquals(2, loadedContainer.getContainerHierarchy().size());
        Assert.assertNotNull(loadedContainer.getProject());
        Assert.assertTrue(bean.isEditable());
        Assert.assertFalse(bean.isGmoSafetyLevelVisible());
    }

    @Test
    public void test003_editContainer() {
        bean.setCurrentAccount(new LoginEvent(publicUser));

        bean.startNewContainerCreation();
        ContainerValues values = new ContainerValues();
        values.label = "container-1-room";
        values.type = bean.getContainerTypesWithRankGreaterZero().get(0);
        Container room1 = createAnsSaveContainer(values);
        bean.startNewContainerCreation();

        bean.startNewContainerCreation();
        values = new ContainerValues();
        values.label = "container-2-room";
        values.type = bean.getContainerTypesWithRankGreaterZero().get(0);
        Container room2 = createAnsSaveContainer(values);
        bean.startNewContainerCreation();

        bean.startNewContainerCreation();
        values = new ContainerValues();
        values.label = "container-1-board";
        values.type = bean.getContainerTypesWithRankGreaterZero().get(1);
        values.parent = room1;
        Container board1 = createAnsSaveContainer(values);

        bean.startNewContainerCreation();
        bean.startNewContainerCreation();
        values = new ContainerValues();
        values.label = "container-1-freezer";
        values.type = bean.getContainerTypesWithRankGreaterZero().get(2);
        values.parent = board1;
        Container freezer1 = createAnsSaveContainer(values);

        overviewBean.actionContainerEdit(board1);
        Assert.assertEquals("container_edit_titel_edit", bean.getDialogTitle());
        bean.setContainerLocation(room2);

        overviewBean.actionTriggerContainerSave();

        Container loadedContainer = containerService.loadContainerById(board1.getId());
        Assert.assertEquals(1, loadedContainer.getContainerHierarchy().size());
        Assert.assertEquals("container-2-room", loadedContainer.getContainerHierarchy().get(0).getLabel());
        loadedContainer = containerService.loadContainerById(freezer1.getId());
        Assert.assertEquals(2, loadedContainer.getContainerHierarchy().size());
        Assert.assertEquals("container-1-board", loadedContainer.getContainerHierarchy().get(0).getLabel());
        Assert.assertEquals("container-2-room", loadedContainer.getContainerHierarchy().get(1).getLabel());

        overviewBean.actionContainerEdit(containerService.loadContainerById(board1.getId()));
        bean.setContainerLocation(null);
        overviewBean.actionTriggerContainerSave();

        loadedContainer = containerService.loadContainerById(board1.getId());
        Assert.assertEquals(0, loadedContainer.getContainerHierarchy().size());
        loadedContainer = containerService.loadContainerById(freezer1.getId());
        Assert.assertEquals(1, loadedContainer.getContainerHierarchy().size());
        Assert.assertEquals("container-1-board", loadedContainer.getContainerHierarchy().get(0).getLabel());

        Project project = creationTools.createAndSaveProject("test002_editContainer() - testproject");
        overviewBean.actionContainerEdit(board1);
        bean.setPreferredProjectName(project.getName());
        bean.setContainerName("container-1-board EDITED");
        overviewBean.actionTriggerContainerSave();

        loadedContainer = containerService.loadContainerById(board1.getId());
        Assert.assertEquals("container-1-board EDITED", loadedContainer.getLabel());
        Assert.assertNotNull(loadedContainer.getProject());

        overviewBean.actionContainerEdit(loadedContainer);

        Assert.assertEquals("test002_editContainer() - testproject", bean.getPreferredProjectName());
        Assert.assertEquals("container_edit_titel_edit", bean.getDialogTitle());
        Assert.assertNotNull(bean.getOriginalContainer());

        overviewBean.actionContainerEdit(containerService.loadContainerById(room1.getId()));
        Assert.assertNull(bean.getContainerLocation());
        Assert.assertNull(bean.getContainerHeight());
        Assert.assertNull(bean.getContainerWidth());

    }

    private Container createAnsSaveContainer(
            ContainerValues values) {
        bean.startNewContainerCreation();
        bean.setContainerName(values.label);
        bean.setContainerType(values.type);
        bean.setFireArea(values.fireArea);
        bean.setGmoSafetyLevel(values.gmoSafetyLevel);
        bean.setPreferredProjectName(values.projectName);
        bean.setContainerLocation(values.parent);
        overviewBean.saveNewContainer();
        return containerService.loadContainerById(bean.getContainerToCreate().getId());
    }

    @BeforeEach
    public void init() {
        entityManagerService.doSqlUpdate("DELETE FROM containers");
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        bean = new ContainerEditBeanMock()
                .setContainerService(containerService)
                .setProjectService(projectService);

        overviewBean = new ContainerOverviewBeanMock(containerService)
                .setContainerEditBean(bean)
                .setProjectService(projectService);

        overviewBean.setCurrentAccount(new LoginEvent(publicUser));
        searchMaskBean = new ContainerSearchMaskBeanMock()
                .setContainerOverviewBean(overviewBean)
                .setContainerService(containerService)
                .setProjectService(projectService);
        searchMaskBean.setCurrentAccount(new LoginEvent(publicUser));

        overviewBean.setContainerSearchMaskBean(searchMaskBean);
        creationTools = new CreationTools("", "", "", memberService, projectService);
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("ContainerEditBeanTest.war");
        return ItemDeployment.add(UserBeanDeployment.add(deployment));
    }

    private void checkCleanState() {
        Assert.assertEquals(6, bean.getContainerTypesWithRankGreaterZero().size());
        Assert.assertNull(bean.getContainerName());
        Assert.assertNull(bean.getContainerHeight());
        Assert.assertNull(bean.getContainerWidth());
        Assert.assertNull(bean.getContainerLocation());
        Assert.assertEquals(100, bean.getContainerType().getRank());
        Assert.assertNull(bean.getFireArea());
        Assert.assertNull(bean.getGmoSafetyLevel());
        Assert.assertNull(bean.getPreferredProjectName());
    }

    class ContainerValues {

        public String label;
        public ContainerType type;
        public String fireArea;
        public String gmoSafetyLevel;
        public Container parent;
        public String projectName;
    }
}
