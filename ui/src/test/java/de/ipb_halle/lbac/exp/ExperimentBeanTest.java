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

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.exp.assay.AssayController;
import de.ipb_halle.lbac.exp.assay.AssayService;
import de.ipb_halle.lbac.exp.mocks.ExperimentBeanMock;
import de.ipb_halle.lbac.exp.text.Text;
import de.ipb_halle.lbac.exp.text.TextController;
import de.ipb_halle.lbac.exp.text.TextService;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.project.ProjectService;
import java.util.Date;
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
public class ExperimentBeanTest extends TestBase {

    @Inject
    private ProjectService projectService;

    @Inject
    private ExperimentService experimentService;

    @Inject
    private ExpRecordService expRecordService;
    @Inject
    private GlobalAdmissionContext globalAdmissionContext;

    private ExperimentBeanMock experimentBean;
    private User publicUser;
    private ACList publicReadAcl;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        creationTools = new CreationTools("", "", "", memberService, projectService);
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        publicReadAcl = GlobalAdmissionContext.getPublicReadACL();
        experimentBean = new ExperimentBeanMock()
                .setExpRecordService(expRecordService)
                .setExperimentService(experimentService)
                .setGlobalAdmissionContext(globalAdmissionContext);
    }

    @After
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
        Assert.assertEquals("expRecordOdd", experimentBean.getExpRecordStyle(false, false));
        Assert.assertEquals("expRecordEdit", experimentBean.getExpRecordStyle(true, false));
        Assert.assertEquals("expRecordEven", experimentBean.getExpRecordStyle(false, true));
        Assert.assertEquals("expRecordEdit", experimentBean.getExpRecordStyle(true, true));
    }

    @Test
    public void test003_loadExperimentsAndRecords() {
        experimentBean.experimentBeanInit();

        Experiment exp1 = createAndSaveExperiment("EXP-1", "EXP-1-DESC", publicReadAcl, publicUser, false);
        Experiment exp2 = createAndSaveExperiment("EXP-2", "EXP-2-DESC", publicReadAcl, publicUser, false);
        Experiment exp3 = createAndSaveExperiment("EXP-3", "EXP-3-DESC", publicReadAcl, publicUser, true);
        experimentBean.setExperiment(exp1);

        experimentBean.saveExpRecord(createTextrecord(exp1, "Overview"));
        experimentBean.saveExpRecord(createTextrecord(exp1, "Section 1"));
        experimentBean.saveExpRecord(createTextrecord(exp1, "Section 2"));
        experimentBean.saveExpRecord(createTextrecord(exp1, "Ending"));

        experimentBean.setTemplateMode(false);
        Assert.assertFalse(experimentBean.getTemplateMode());
        Assert.assertEquals(2, experimentBean.getExperiments().size());
        experimentBean.setTemplateMode(true);
        Assert.assertTrue(experimentBean.getTemplateMode());
        Assert.assertEquals(1, experimentBean.getExperiments().size());
        experimentBean.loadExpRecords();
        Assert.assertEquals(4, experimentBean.getExpRecords().size());
        experimentBean.setTemplateMode(false);
        experimentBean.actionToggleExperiment(exp1);
        //Deselect the experiment 
        Assert.assertEquals(0, experimentBean.getExpRecords().size());
        experimentBean.actionToggleExperiment(exp2);
        Assert.assertEquals(0, experimentBean.getExpRecords().size());
        experimentBean.actionToggleExperiment(exp1);
        experimentBean.reIndex();

        experimentBean.setNewRecordType("TEXT");
        //Append at last position
        experimentBean.actionAppendRecord(-1);
        Assert.assertEquals(5, experimentBean.getExpRecords().size());
        experimentBean.actionCancel();
       
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("ItemBeanTest.war")
                .addClass(ExperimentService.class)
                .addClass(ExpRecordService.class)
                .addClass(AssayService.class)
                .addClass(TextService.class)
                .addClass(ProjectService.class);
        return UserBeanDeployment.add(ItemDeployment.add(deployment));
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
        //  return (Text) expRecordService.save(text1);
    }
}