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
package de.ipb_halle.lbac.datalink;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.exp.*;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.ProjectCreator;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.exp.assay.AssayService;
import de.ipb_halle.lbac.exp.text.TextService;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.project.ProjectType;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.ArrayList;
import jakarta.faces.component.UIComponentBase;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.primefaces.event.FlowEvent;

/**
 *
 * @author fmauz
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
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
    private int materialId, materialId2, materialId3;
    private int itemId;

    @BeforeEach
    public void init() {
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        Project project = new ProjectCreator(projectService, GlobalAdmissionContext.getPublicReadACL())
                .setProjectName("LinkCreationProcessTest_Project")
                .setType(ProjectType.BIOCHEMICAL_PROJECT)
                .createAndSaveProject(publicUser);
        experimentBean = new ExperimentBean(itemAgent, materialAgent, context, projectService, expService, MessagePresenterMock.getInstance(), expRecService);
        LoginEvent event = new LoginEvent(publicUser);
        experimentBean.setCurrentAccount(event);
        linkCreationProcess = new LinkCreationProcess(materialAgent, itemAgent, experimentBean);
        linkCreationProcess.init();
        materialId = materialCreator.createStructure(publicUser.getId(), GlobalAdmissionContext.getPublicReadACL().getId(), null, "LinkCreationProcessTest_M1");
        materialId2 = materialCreator.createStructure(publicUser.getId(), GlobalAdmissionContext.getPublicReadACL().getId(), null, "LinkCreationProcessTest_M2");
        materialId3 = materialCreator.createStructure(publicUser.getId(), GlobalAdmissionContext.getPublicReadACL().getId(), null, "LinkCreationProcessTest_M3");
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
        triggerWizardStep();
        linkCreationProcess.setMaterial(materialService.loadMaterialById(materialId));
        experimentBean.getExpRecordController().actionSaveRecord();

        ArrayList o = (ArrayList) entityManagerService.doSqlQuery("SELECT * from linked_data");
        Assert.assertEquals(1, o.size());

        cleanUp();
    }

    @Test
    public void test002_createItemLink() {
        prepareLinkCreation();

        linkCreationProcess.setLinkText("test001_createItemLink");
        triggerWizardStep();
        linkCreationProcess.setItem(itemService.loadItemById(itemId));
        experimentBean.getExpRecordController().actionSaveRecord();

        ArrayList o = (ArrayList) entityManagerService.doSqlQuery("SELECT * from linked_data");
        Assert.assertEquals(1, o.size());

        cleanUp();
    }

    /**
     * This test is created because a bug was found. The rank of the third
     * material was wrong due to an unknown reason.
     */
    @Test
    public void test003_createMultipleMaterialLinks() {
        prepareLinkCreation();

        linkCreationProcess.setLinkText("test003_M1");
        triggerWizardStep();
        linkCreationProcess.setMaterial(materialService.loadMaterialById(materialId));
        experimentBean.getExpRecordController().actionSaveRecord();

        experimentBean.actionEditRecord(experimentBean.getExpRecords().get(0));
        linkCreationProcess.setLinkText("test003_M2");
        triggerWizardStep();
        linkCreationProcess.setMaterial(materialService.loadMaterialById(materialId2));
        experimentBean.getExpRecordController().actionSaveRecord();

        experimentBean.actionEditRecord(experimentBean.getExpRecords().get(0));
        linkCreationProcess.setLinkText("test003_M3");
        triggerWizardStep();
        linkCreationProcess.setMaterial(materialService.loadMaterialById(materialId3));
        experimentBean.getExpRecordController().actionSaveRecord();

        Assert.assertEquals(1, experimentBean.getExpRecords().size());
        Assert.assertEquals(3, experimentBean.getExpRecords().get(0).getLinkedData().size());
        Assert.assertEquals(0, experimentBean.getExpRecords().get(0).getLinkedData().get(0).getRank());
        Assert.assertEquals(1, experimentBean.getExpRecords().get(0).getLinkedData().get(1).getRank());
        Assert.assertEquals(2, experimentBean.getExpRecords().get(0).getLinkedData().get(2).getRank());

    }

    private void triggerWizardStep() {
        linkCreationProcess.onFlowProcess(new FlowEvent(new TestUIComponent(), "step1", "step2"));
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
        return ExperimentDeployment.add(UserBeanDeployment.add(ItemDeployment.add(deployment)));
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
        entityManagerService.doSqlUpdate("DELETE FROM linked_data");
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
