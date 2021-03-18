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
package de.ipb_halle.lbac.exp.link;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.exp.*;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.ProjectCreator;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.exp.assay.AssayService;
import de.ipb_halle.lbac.exp.text.TextService;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.project.ProjectType;
import java.util.ArrayList;
import javax.faces.component.UIComponentBase;
import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.primefaces.event.FlowEvent;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class LinkCreationProcessTest extends TestBase {

    private LinkCreationProcess linkCreationProcess;
    private ExperimentBean experimentBean;
    @Inject
    private MaterialAgent materialAgent;
    @Inject
    private ItemAgent itemAgent;
    @Inject
    private GlobalAdmissionContext context;
    @Inject
    private ProjectService projectService;

    @Inject
    private ExperimentService expService;

    @Inject
    private ExpRecordService expRecService;
    @Inject
    private MaterialService materialService;
    @Inject
    private ItemService itemService;

    private User publicUser;
    private int materialId;
    private int itemId;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        Project project = new ProjectCreator(projectService, GlobalAdmissionContext.getPublicReadACL())
                .setProjectName("LinkCreationProcessTest_Project")
                .setType(ProjectType.BIOCHEMICAL_PROJECT)
                .createAndSaveProject(publicUser);
        experimentBean = new ExperimentBean(itemAgent, materialAgent, context, projectService, expService, new MessagePresenterMock(), expRecService);
        LoginEvent event = new LoginEvent(publicUser);
        experimentBean.setCurrentAccount(event);
        linkCreationProcess = new LinkCreationProcess(materialAgent, itemAgent, experimentBean);
        linkCreationProcess.init();
        materialId = materialCreator.createStructure(publicUser.getId(), GlobalAdmissionContext.getPublicReadACL().getId(), null, "LinkCreationProcessTest_M1");
        itemId = itemCreator.createItem(
                publicUser.getId(),
                GlobalAdmissionContext.getPublicReadACL().getId(),
                materialId,
                "LinkCreationProcessTest_I1",
                project.getId());

    }

    @Test
    public void test001_createMaterialLink() {
        prepareLinkCreation();

        linkCreationProcess.setLinkText("test001_createMaterialLink");
        linkCreationProcess.onFlowProcess(new FlowEvent(new TestUIComponent(), "step1", "step2"));
        linkCreationProcess.setMaterial(materialService.loadMaterialById(materialId));
        experimentBean.getExpRecordController().actionSaveRecord();

        ArrayList o = (ArrayList) entityManagerService.doSqlQuery("SELECT * from exp_linked_data");
        Assert.assertEquals(1, o.size());

        cleanUp();
    }

    @Test
    public void test002_createItemLink() {
        prepareLinkCreation();

        linkCreationProcess.setLinkText("test001_createItemLink");
        linkCreationProcess.onFlowProcess(new FlowEvent(new TestUIComponent(), "step1", "step2"));
        linkCreationProcess.setItem(itemService.loadItemById(itemId));
        experimentBean.getExpRecordController().actionSaveRecord();

        ArrayList o = (ArrayList) entityManagerService.doSqlQuery("SELECT * from exp_linked_data");
        Assert.assertEquals(1, o.size());

        cleanUp();
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("LinkCreationProcessTest.war")
                .addClass(ExperimentService.class)
                .addClass(ExpRecordService.class)
                .addClass(ItemAgent.class)
                .addClass(MaterialAgent.class)
                .addClass(AssayService.class)
                .addClass(TextService.class)
                .addClass(MaterialService.class)
                .addClass(ProjectService.class);
        return UserBeanDeployment.add(ItemDeployment.add(deployment));
    }

    private void prepareLinkCreation() {
        linkCreationProcess.startLinkCreation();
        experimentBean.actionNewExperiment();
        experimentBean.getExperiment().setCode("LinkCreationProcessTest_EXP1");
        experimentBean.getProjectController().setChoosenProject(experimentBean.getProjectController().getChoosableProjects().get(0));
        experimentBean.actionSaveExperiment();
        experimentBean.actionNewExperimentRecord("TEXT", 0);
    }

    private void cleanUp() {
        entityManagerService.doSqlUpdate("DELETE FROM exp_linked_data");
        entityManagerService.doSqlUpdate("DELETE FROM exp_records");
        entityManagerService.doSqlUpdate("DELETE FROM experiments");
    }

    private class TestUIComponent extends UIComponentBase {

        @Override
        public String getFamily() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

    }

}
