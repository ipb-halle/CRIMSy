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
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.mock.ContainerEditBeanMock;
import de.ipb_halle.lbac.container.mock.ContainerOverviewBeanMock;
import de.ipb_halle.lbac.container.mock.ContainerSearchMaskBeanMock;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.items.ItemDeployment;
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
public class ContainerEditBeanTest extends TestBase {
    
    @Inject
    private ProjectService projectService;
    
    @Inject
    private ContainerService containerService;
    
    private ContainerEditBean bean;
    private User publicUser;
    
    private ContainerOverviewBeanMock overviewBean;
    private ContainerSearchMaskBean searchMaskBean;
    
    @Test
    public void test001_logIn() {
        bean.setCurrentAccount(new LoginEvent(publicUser));
        Assert.assertEquals(5, bean.getGvoClasses().size());
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
        bean.setSecurityLevel("Section-1");
        bean.setGvoClass(bean.getGvoClasses().get(0));
        
        Assert.assertTrue(bean.isSecurityLevelVisible());
        
        overviewBean.saveNewContainer();
        
        Container loadedContainer = containerService.loadContainerById(bean.getContainerToCreate().getId());
        
        bean.startNewContainerCreation();
        checkCleanState();
        
        bean.setContainerName("container-2-cupboard");
        bean.setContainerType(bean.getContainerTypesWithRankGreaterZero().get(1));
        bean.setContainerHeight(10);
        bean.setContainerWidth(20);
        bean.setFireSection("FireSection A");
        bean.setContainerLocation(loadedContainer);
        
        overviewBean.saveNewContainer();
        loadedContainer = containerService.loadContainerById(bean.getContainerToCreate().getId());
        Assert.assertNotNull(loadedContainer.getParentContainer());
        
        bean.startNewContainerCreation();
        bean.setContainerName("container-3-FREEZER");
        bean.setContainerType(bean.getContainerTypesWithRankGreaterZero().get(2));
        bean.setContainerHeight(10);
        bean.setContainerWidth(20);
        bean.setContainerLocation(loadedContainer);
        
        overviewBean.saveNewContainer();
        loadedContainer = containerService.loadContainerById(bean.getContainerToCreate().getId());
        Assert.assertEquals(2, loadedContainer.getContainerHierarchy().size());
        
        Assert.assertTrue(bean.isEditable());
        Assert.assertFalse(bean.isSecurityLevelVisible());
        
    }
    
    @Before
    @Override
    public void setUp() {
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        bean = new ContainerEditBeanMock()
                .setContainerService(containerService)
                .setProjectService(projectService);
        
        overviewBean = new ContainerOverviewBeanMock()
                .setContainerEditBean(bean)
                .setContainerService(containerService)
                .setProjectService(projectService);
        overviewBean.setCurrentAccount(new LoginEvent(publicUser));
        searchMaskBean = new ContainerSearchMaskBeanMock()
                .setContainerOverviewBean(overviewBean)
                .setContainerService(containerService)
                .setProjectService(projectService);
        searchMaskBean.setCurrentAccount(new LoginEvent(publicUser));
        
        overviewBean.setContainerSearchMaskBean(searchMaskBean);
        
    }
    
    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("ContainerEditBeanTest.war");
        return ItemDeployment.add(UserBeanDeployment.add(deployment));
    }
    
    private void checkCleanState() {
        Assert.assertEquals(4, bean.getContainerTypesWithRankGreaterZero().size());
        Assert.assertNull(bean.getContainerName());
        Assert.assertNull(bean.getContainerHeight());
        Assert.assertNull(bean.getContainerWidth());
        Assert.assertNull(bean.getContainerLocation());
        Assert.assertEquals(100, bean.getContainerType().getRank());
        Assert.assertNull(bean.getFireSection());
        Assert.assertNull(bean.getGvoClass());
        Assert.assertNull(bean.getPreferredProjectName());
        Assert.assertNull(bean.getSecurityLevel());
        
    }
}
