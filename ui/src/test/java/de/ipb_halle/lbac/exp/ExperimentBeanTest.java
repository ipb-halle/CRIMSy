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

import de.ipb_halle.lbac.admission.ACEntry;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.mock.UserBeanMock;
import de.ipb_halle.lbac.base.TestBase;
import static org.junit.Assert.assertEquals;

import de.ipb_halle.lbac.exp.assay.AssayController;
import de.ipb_halle.lbac.exp.assay.AssayService;
import de.ipb_halle.lbac.exp.mocks.ExperimentBeanMock;
import de.ipb_halle.lbac.exp.mocks.ItemAgentMock;
import de.ipb_halle.lbac.exp.mocks.MaterialAgentMock;
import de.ipb_halle.lbac.exp.virtual.NullController;
import de.ipb_halle.lbac.exp.text.Text;
import de.ipb_halle.lbac.exp.text.TextController;
import de.ipb_halle.lbac.exp.text.TextService;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
@ExtendWith(ArquillianExtension.class)
public class ExperimentBeanTest extends TestBase {

    @Inject
    private ProjectService projectService;

    @Inject
    private ExperimentService experimentService;

    @Inject
    private ExpRecordService expRecordService;

    @Inject
    private GlobalAdmissionContext globalAdmissionContext;

    @Inject
    private ItemService itemService;

    @Inject
    private MaterialService materialService;

    private ExperimentBeanMock experimentBean;
    private User publicUser;
    private ACList publicReadAcl;

    @BeforeEach
    public void init() {
        creationTools = new CreationTools("", "", "", memberService, projectService);
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        publicReadAcl = GlobalAdmissionContext.getPublicReadACL();
        UserBeanMock userBean = new UserBeanMock();
        userBean.setCurrentAccount(publicUser);

        MaterialAgent materialAgentMock = new MaterialAgentMock()
                .setGlobalAdmissionContext(globalAdmissionContext)
                .setMaterialService(this.materialService)
                .setUserBean(userBean);

        ItemAgent itemAgentMock = new ItemAgentMock()
                .setGlobalAdmissionContext(globalAdmissionContext)
                .setItemService(this.itemService)
                .setUserBean(userBean);

        experimentBean = new ExperimentBeanMock()
                .setExpRecordService(expRecordService)
                .setExperimentService(experimentService)
                .setGlobalAdmissionContext(globalAdmissionContext)
                .setMaterialAgent(materialAgentMock)
                .setMemberService(memberService)
                .setProjectService(projectService)
                .setMessagePresenter(MessagePresenterMock.getInstance())
                .setItemAgent(itemAgentMock);
        experimentBean.setCurrentAccount(new LoginEvent(publicUser));

        entityManagerService.doSqlUpdate("DELETE FROM usersgroups WHERE name NOT IN('Public Group','Admin Group') AND membertype='G'");

    }

    @AfterEach
    public void cleanUp() {
        entityManagerService.doSqlUpdate("DELETE FROM experiments");

    }

    @Test
    public void test001_createExpRecordController() {
        experimentBean.experimentBeanInit();
        experimentBean.createExpRecordController("ASSAY");
        Assert.assertTrue(experimentBean.getExpRecordController() instanceof AssayController);
        experimentBean.createExpRecordController("TEXT");
        Assert.assertTrue(experimentBean.getExpRecordController() instanceof TextController);
        experimentBean.createExpRecordController("XYZ");
        Assert.assertTrue(experimentBean.getExpRecordController() instanceof NullController);
    }

    @Test
    public void test002_getExpRecordStyle() {
        experimentBean.experimentBeanInit();
        Experiment exp1 = createAndSaveExperiment("EXP-1", "EXP-1-DESC",
                publicReadAcl, publicUser, false);
        ExpRecord record = createTextrecord(exp1, "Overview");
        Set<String> expectedStrings;

        /*
         * record is not yet saved
         */
        record.setEdit(false);
        Assert.assertEquals(ExperimentBean.expRecordOddCssClass,
                experimentBean.getExpRecordStyle(record, false));
        Assert.assertEquals(ExperimentBean.expRecordEvenCssClass,
                experimentBean.getExpRecordStyle(record, true));

        record.setEdit(true);
        expectedStrings = new HashSet<>();
        Collections.addAll(expectedStrings, ExperimentBean.expRecordOddCssClass,
                ExperimentBean.expRecordEditCssClass);
        Assert.assertEquals(expectedStrings, new HashSet<>(Arrays.asList(
                experimentBean.getExpRecordStyle(record, false).split(" "))));
        expectedStrings = new HashSet<>();
        Collections.addAll(expectedStrings,
                ExperimentBean.expRecordEvenCssClass,
                ExperimentBean.expRecordEditCssClass);
        Assert.assertEquals(expectedStrings, new HashSet<>(Arrays.asList(
                experimentBean.getExpRecordStyle(record, true).split(" "))));

        /*
         * record becomes lastSavedRecord
         */
        experimentBean.saveExpRecord(record);

        record.setEdit(false);
        expectedStrings = new HashSet<>();
        Collections.addAll(expectedStrings, ExperimentBean.expRecordOddCssClass,
                ExperimentBean.expRecordLastSavedCssClass);
        Assert.assertEquals(expectedStrings, new HashSet<>(Arrays.asList(
                experimentBean.getExpRecordStyle(record, false).split(" "))));
        expectedStrings = new HashSet<>();
        Collections.addAll(expectedStrings,
                ExperimentBean.expRecordEvenCssClass,
                ExperimentBean.expRecordLastSavedCssClass);
        Assert.assertEquals(expectedStrings, new HashSet<>(Arrays.asList(
                experimentBean.getExpRecordStyle(record, true).split(" "))));

        record.setEdit(true);
        expectedStrings = new HashSet<>();
        Collections.addAll(expectedStrings, ExperimentBean.expRecordOddCssClass,
                ExperimentBean.expRecordEditCssClass,
                ExperimentBean.expRecordLastSavedCssClass);
        Assert.assertEquals(expectedStrings, new HashSet<>(Arrays.asList(
                experimentBean.getExpRecordStyle(record, false).split(" "))));
        expectedStrings = new HashSet<>();
        Collections.addAll(expectedStrings,
                ExperimentBean.expRecordEvenCssClass,
                ExperimentBean.expRecordEditCssClass,
                ExperimentBean.expRecordLastSavedCssClass);
        Assert.assertEquals(expectedStrings, new HashSet<>(Arrays.asList(
                experimentBean.getExpRecordStyle(record, true).split(" "))));
    }

    @Test
    public void test003_loadExperimentsAndRecords() {
        experimentBean.experimentBeanInit();
        experimentBean.setSearchTerm("EXP");
        Experiment exp1 = createAndSaveExperiment("EXP-1", "EXP-1-DESC", publicReadAcl, publicUser, false);
        Experiment exp2 = createAndSaveExperiment("EXP-2", "EXP-2-DESC", publicReadAcl, publicUser, false);
        Experiment exp3 = createAndSaveExperiment("EXP-3", "EXP-3-DESC", publicReadAcl, publicUser, true);

        // whe have 4 records in experiment exp1
        experimentBean.saveExpRecord(createTextrecord(exp1, "Overview"));
        experimentBean.saveExpRecord(createTextrecord(exp1, "Section 1"));
        experimentBean.saveExpRecord(createTextrecord(exp1, "Section 2"));
        experimentBean.saveExpRecord(createTextrecord(exp1, "Ending"));

        experimentBean.setCurrentAccount(new LoginEvent(publicUser));

        experimentBean.setExperiment(exp1);
        experimentBean.setTemplateMode(false);
        experimentBean.loadExperiments(false);
        Assert.assertFalse(experimentBean.getTemplateMode());
        Assert.assertEquals(2, experimentBean.getExperiments().size());
        experimentBean.setTemplateMode(true);
        experimentBean.loadExperiments(true);
        Assert.assertTrue(experimentBean.getTemplateMode());
        Assert.assertEquals(1, experimentBean.getExperiments().size());
        experimentBean.loadExpRecords();

        // we get 5 records (4 + NullRecord)
        Assert.assertEquals(5, experimentBean.getExpRecordsWithNullRecord().size());
        experimentBean.setTemplateMode(false);
        experimentBean.actionToggleExperiment(exp1);

        // deselect the experiment (0 records)
        Assert.assertEquals(0, experimentBean.getExpRecordsWithNullRecord().size());

        // select exp2 (only NullRecord)
        experimentBean.actionToggleExperiment(exp2);
        Assert.assertEquals(1, experimentBean.getExpRecordsWithNullRecord().size());
        experimentBean.actionToggleExperiment(exp1);
        experimentBean.reIndex();

        experimentBean.setNewRecordType("TEXT");
        // append at last position
        experimentBean.actionAppendRecord(experimentBean.getExpRecordsWithNullRecord().size() - 1);
        Assert.assertEquals(6, experimentBean.getExpRecordsWithNullRecord().size());
        experimentBean.actionCancel();
    }

    @Test
    public void test004_testAclChange() {
        experimentBean.experimentBeanInit();
        Experiment exp1 = createAndSaveExperiment("EXP-1", "EXP-1-DESC", publicReadAcl, publicUser, false);
        experimentBean.actionStartAclChange(exp1);
        List<ACEntry> acentries = experimentBean.getAcObjectController().getAcEntries();
        Assert.assertEquals(2, acentries.size());
        ACEntry ace = getACEntryByName("Admin Group", acentries);
        Assert.assertEquals(127, ace.getPerm());
        ace = getACEntryByName("Public Group", acentries);
        Assert.assertEquals(1, ace.getPerm());

        ace.setPermEdit(true);

        experimentBean.getAcObjectController().saveNewAcList();

        Experiment loadedExp = experimentService.loadById((exp1.getId()));
        ACEntry loadedAce = getACEntryByName("Public Group", loadedExp.getACList().getACEntries().values());
        Assert.assertEquals(3, loadedAce.getPerm());

        experimentBean.actionStartAclChange(loadedExp);
        acentries = experimentBean.getAcObjectController().getAcEntries();
        ace = getACEntryByName("Public Group", acentries);
        ace.setPermEdit(false);
        ace.setPermRead(false);
        experimentBean.getAcObjectController().handleClose(null);
        loadedAce = getACEntryByName("Public Group", loadedExp.getACList().getACEntries().values());
        Assert.assertEquals(3, loadedAce.getPerm());

        experimentBean.actionStartAclChange(loadedExp);
        experimentBean.getAcObjectController().removeGroupFromAcList(loadedAce);
        experimentBean.getAcObjectController().saveNewAcList();

        loadedExp = experimentService.loadById((exp1.getId()));
        Assert.assertEquals(1, loadedExp.getACList().getACEntries().size());

        experimentBean.actionStartAclChange(loadedExp);
        Assert.assertEquals(1, experimentBean.getAcObjectController().getGroupsNotInAcList().size());
        Assert.assertEquals(2, experimentBean.getAcObjectController().getPossibleGroupsToAdd().size());
        experimentBean.getAcObjectController().addGroupToAcList(experimentBean.getAcObjectController().getGroupsNotInAcList().get(0));
        Assert.assertEquals(0, experimentBean.getAcObjectController().getGroupsNotInAcList().size());
        getACEntryByName("Public Group", experimentBean.getAcObjectController().getAcEntries()).setPermRead(true);
        experimentBean.getAcObjectController().saveNewAcList();
    }

    @Test
    public void test005_actionToogleExperiment() {
        creationTools.createAndSaveProject("ExperimentBeanTest-Test-Project");
        experimentBean.actionNewExperiment();
        List<Project> projects = experimentBean.getProjectController().getChoosableProjects();
        Assert.assertEquals(1, projects.size());
        setExperimentProperties("test005_actionNewExperiment()", false, projects.get(0));

        experimentBean.actionSaveExperiment();
        MessagePresenterMock mockedPresenter = (MessagePresenterMock) experimentBean.messagePresenter;
        int expId = experimentBean.getExperiment().getId();
        experimentBean.actionToggleExperiment(experimentBean.getExperiment());
        experimentBean.getExperiment();

        Assert.assertNull(experimentBean.getExperiment().getId());

        experimentBean.actionToggleExperiment(experimentService.loadById(expId));

        Assert.assertEquals(expId, experimentBean.getExperiment().getId(), 0);
    }

    @Test
    public void test006_actionCopyTemplate() {
        creationTools.createAndSaveProject("ExperimentBeanTest-Test-Project");
        experimentBean.experimentBeanInit();
        experimentBean.actionNewExperiment();
        List<Project> projects = experimentBean.getProjectController().getChoosableProjects();
        Assert.assertEquals(1, projects.size());
        setExperimentProperties("test006_actionCopyTemplate()", true, projects.get(0));
        experimentBean.actionSaveExperiment();
        experimentBean.actionCopyTemplate();

        Assert.assertEquals(2, entityManagerService.doSqlQuery("SELECT experimentid FROM experiments").size());
    }

    @Test
    public void test007_getSaveButtonOnClick() {
        experimentBean.setExpRecordController(new ExpRecordController(experimentBean) {
            @Override
            public ExpRecord getNewRecord() {
                return null;
            }

            @Override
            public String getSaveButtonOnClick() {
                return "abc";
            }
        });
        assertEquals("abc;ajax:experimentBean.actionDoNothing();javascript:return false;", experimentBean.getSaveButtonOnClick());

        experimentBean.setExpRecordController(new ExpRecordController(experimentBean) {
            @Override
            public ExpRecord getNewRecord() {
                return null;
            }

            @Override
            public String getSaveButtonOnClick() {
                return "abc;";
            }
        });
        assertEquals("abc;ajax:experimentBean.actionDoNothing();javascript:return false;", experimentBean.getSaveButtonOnClick());
    }

    @Test
    public void test008_isExpRecordButtonsDisabled() {
        experimentBean.experimentBeanInit();
        Experiment exp = createAndSaveExperiment("EXP-1", "EXP-1-DESC", publicReadAcl, publicUser, false);
        experimentBean.setExperiment(exp);

        // no records present and none in edit mode 
        Assert.assertFalse(experimentBean.isExpRecordButtonsDisabled());

        Text text1 = createTextrecord(exp, "Text 1");
        experimentBean.saveExpRecord(text1);
        experimentBean.saveExpRecord(createTextrecord(exp, "Text 2"));
        experimentBean.actionEditRecord(text1);

        // records present and one is in edit mode 
        Assert.assertTrue(experimentBean.isExpRecordButtonsDisabled());

        // cancel editing
        experimentBean.getExpRecordController().actionCancel();
        Assert.assertFalse(experimentBean.isExpRecordButtonsDisabled());
    }

    @Test
    public void test009_actionSaveExpriment() {
        creationTools.createAndSaveProject("ExperimentBeanTest-Test-Project");
        experimentBean.actionNewExperiment();
        List<Project> projects = experimentBean.getProjectController().getChoosableProjects();
        Assert.assertEquals(1, projects.size());
        setExperimentProperties("test009_actionSaveExpriment()", false, projects.get(0));

        experimentBean.actionSaveExperiment();
        MessagePresenterMock mockedPresenter = (MessagePresenterMock) experimentBean.messagePresenter;
        Assert.assertEquals("exp_save_new_experiment", mockedPresenter.getLastInfoMessage());
    }

    @Test
    public void test010_actionSaveTemplate() {
        creationTools.createAndSaveProject("ExperimentBeanTest-Test-Project");
        experimentBean.actionNewExperiment();
        experimentBean.setTemplateMode(true);
        List<Project> projects = experimentBean.getProjectController().getChoosableProjects();
        Assert.assertEquals(1, projects.size());
        setExperimentProperties("test010_actionSaveTemplate()", true, projects.get(0));

        experimentBean.actionSaveExperiment();
        MessagePresenterMock mockedPresenter = (MessagePresenterMock) experimentBean.messagePresenter;
        Assert.assertEquals("exp_save_new_template", mockedPresenter.getLastInfoMessage());

    }

    private ACEntry getACEntryByName(String name, Collection<ACEntry> aces) {
        for (ACEntry ace : aces) {
            if (ace.getMember().getName().equals(name)) {
                return ace;
            }
        }
        return null;

    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("ExperimentBeanTest.war")
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

    private Experiment createAndSaveExperiment(String name, String desc, ACList acl, User user, boolean template) {
        Date creationDate = new Date();
        Experiment exp = new Experiment(null, name, desc, template, acl, user, creationDate);
        return experimentService.save(exp);
    }

    private Text createTextrecord(Experiment exp, String text) {
        Text text1 = new Text();
        text1.setExperiment(exp);
        text1.setCreationTime(new Date());
        text1.setText(text);
        return text1;
    }

    private void setExperimentProperties(String text, boolean template, Project project) {
        experimentBean.getProjectController().setChoosenProject(project);
        experimentBean.getExperiment().setCode(text + "-expCode");
        experimentBean.getExperiment().setDescription(text + "-description");
        experimentBean.getExperiment().setOwner(publicUser);
        experimentBean.getExperiment().setACList(publicReadAcl);
        experimentBean.getExperiment().setTemplate(template);
    }

}
