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

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.exp.assay.AssayService;
import de.ipb_halle.lbac.exp.search.ExperimentSearchRequestBuilder;
import de.ipb_halle.lbac.exp.text.Text;
import de.ipb_halle.lbac.exp.text.TextService;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.search.NetObject;
import de.ipb_halle.lbac.search.SearchResult;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class ExperimentServiceTest extends TestBase {

    @Inject
    private ExperimentService experimentService;

    @Inject
    private ExpRecordService recordService;

    @Inject
    private ACListService aclistService;

    private User publicUser;
    private ACList publicReadAcl;
    private ACList nothingAcl;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        publicReadAcl = GlobalAdmissionContext.getPublicReadACL();
        nothingAcl = new ACList();
        nothingAcl = aclistService.save(nothingAcl);

    }

    @After
    public void finish() {
        entityManagerService.doSqlUpdate("DELETE FROM experiments");
    }

    @Test
    public void test001_saveAndLoadExp() {

        Date creationDate = new Date();
        Experiment exp = new Experiment(null, "TEST-EXP-001", "Testexperiment x56df", false, publicReadAcl, publicUser, creationDate);
        exp = experimentService.save(exp);
        Experiment exp2 = new Experiment(null, "TEST-EXP-002", "Testexperiment", false, publicReadAcl, publicUser, creationDate);
        exp2 = experimentService.save(exp2);
        Experiment exp3 = new Experiment(null, "TEST-EXP-003", "not readable experiment", false, nothingAcl, publicUser, creationDate);
        exp3 = experimentService.save(exp3);

        Text text1 = new Text();
        text1.setExperiment(exp);
        text1.setCreationTime(creationDate);
        text1.setText("Test001");
        text1 = (Text) recordService.save(text1);

        Text text2 = new Text();
        text2.setExperiment(exp2);
        text2.setText("Test001-A");
        text2 = (Text) recordService.save(text2);

        ExperimentSearchRequestBuilder builder = new ExperimentSearchRequestBuilder(publicUser, 0, 25);
        Experiment loadedExperiment = experimentService.loadById(exp.getExperimentId());

        SearchResult loadedExp = experimentService.load(builder.buildSearchRequest());
        Assert.assertEquals(2, loadedExp.getAllFoundObjects().size());

        Assert.assertEquals(exp.getExperimentId(), loadedExperiment.getExperimentId());
        Assert.assertEquals(exp.getCode(), loadedExperiment.getCode());
        Assert.assertEquals(exp.getDescription(), loadedExperiment.getDescription());
        Assert.assertEquals(exp.getTemplate(), loadedExperiment.getTemplate());
        Assert.assertEquals(publicReadAcl.getId(), loadedExperiment.getACList().getId());
        Assert.assertEquals(publicUser.getId(), loadedExperiment.getOwner().getId());
        Assert.assertEquals(creationDate, loadedExperiment.getCreationTime());

        builder = new ExperimentSearchRequestBuilder(publicUser, 0, 25);
        builder.addId(exp.getExperimentId());
        loadedExp = experimentService.load(builder.buildSearchRequest());

        Assert.assertEquals(1, loadedExp.getAllFoundObjects().size());
        Experiment exp1 = (Experiment) loadedExp.getAllFoundObjects().get(0).getSearchable();
        Map<String, Object> cmap = new HashMap<>();
        cmap.put("ID", exp1.getId());
        List<ExpRecord> loadedRecords = recordService.load(cmap);
        Assert.assertEquals("Test001", ((Text) loadedRecords.get(0)).getText());
        Assert.assertEquals(exp.getExperimentId(), loadedRecords.get(0).getExperiment().getExperimentId());
        Assert.assertEquals(ExpRecordType.TEXT, loadedRecords.get(0).getType());
        Assert.assertEquals(creationDate, loadedRecords.get(0).getCreationTime());
        Assert.assertTrue(loadedRecords.get(0).getChangeTime().getTime() >= creationDate.getTime());
        Assert.assertNull(loadedRecords.get(0).getNext());
        Assert.assertEquals(1, loadedRecords.get(0).getRevision());

        Text loadedById = (Text) recordService.loadById(text1.getExpRecordId());
        Assert.assertEquals("Test001", loadedById.getText());

        Text text3 = new Text();
        text3.setExperiment(exp);
        text3.setCreationTime(creationDate);
        text3.setText("Test001-text3");
        text3 = (Text) recordService.save(text3);

        text1.setNext(text3.getExpRecordId());
        recordService.saveOnly(text1);
        loadedRecords = recordService.load(cmap);
        loadedRecords = recordService.orderList(loadedRecords);
        Assert.assertEquals(3, loadedRecords.size());
        Assert.assertEquals(text1.getExpRecordId(), loadedRecords.get(0).getExpRecordId());
        Assert.assertEquals(text3.getExpRecordId(), loadedRecords.get(1).getExpRecordId());
        Assert.assertEquals(text3.getExpRecordId(), loadedRecords.get(0).getNext());
        Assert.assertNull(loadedRecords.get(1).getNext());

        builder = new ExperimentSearchRequestBuilder(publicUser, 0, 25);
        builder.addDescription("x56df");
        loadedExp = experimentService.load(builder.buildSearchRequest());
        Assert.assertEquals(1, loadedExp.getAllFoundObjects().size());

        builder = new ExperimentSearchRequestBuilder(publicUser, 0, 25);
        builder.addUserName("public");
        loadedExp = experimentService.load(builder.buildSearchRequest());
        Assert.assertEquals(2, loadedExp.getAllFoundObjects().size());

        builder = new ExperimentSearchRequestBuilder(publicUser, 0, 25);
        builder.addUserName("no-valide-user");
        loadedExp = experimentService.load(builder.buildSearchRequest());
        Assert.assertEquals(0, loadedExp.getAllFoundObjects().size());
    }

    @Test
    public void test002_searchExperimentByDescription() {
        Date creationDate = new Date();
        Experiment exp = new Experiment(null, "TEST-EXP-001", "java is a fine language", false, publicReadAcl, publicUser, creationDate);
        exp = experimentService.save(exp);

        ExperimentSearchRequestBuilder builder = new ExperimentSearchRequestBuilder(publicUser, 0, 25);
        builder.addDescription("java");
        SearchResult loadedExp = experimentService.load(builder.buildSearchRequest());
        Assert.assertEquals(1, loadedExp.getAllFoundObjects().size());

        builder.addDescription("c#");
        loadedExp = experimentService.load(builder.buildSearchRequest());
        Assert.assertEquals(0, loadedExp.getAllFoundObjects().size());
    }

    @Test
    public void test003_searchExperimentByTextRecord() {
        Date creationDate = new Date();
        Experiment exp = new Experiment(null, "TEST-EXP-002", "java is a fine language", false, publicReadAcl, publicUser, creationDate);
        Text text1 = new Text();
        text1.setExperiment(exp);
        text1.setCreationTime(creationDate);
        text1.setText("C# is also good");
        text1 = (Text) recordService.save(text1);

        ExperimentSearchRequestBuilder builder = new ExperimentSearchRequestBuilder(publicUser, 0, 25);
        builder.addDescription("C#");
        SearchResult loadedExp = experimentService.load(builder.buildSearchRequest());
        Assert.assertEquals(1, loadedExp.getAllFoundObjects().size());

        builder = new ExperimentSearchRequestBuilder(publicUser, 0, 25);
        builder.addDescription("PROLOG");
        loadedExp = experimentService.load(builder.buildSearchRequest());
        Assert.assertEquals(0, loadedExp.getAllFoundObjects().size());
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("ExperimentServiceTest.war")
                .addClass(ExpRecordService.class)
                .addClass(TextService.class)
                .addClass(AssayService.class)
                .addClass(ItemService.class)
                .addClass(ExperimentBean.class)
                .addClass(ExperimentService.class)
                .addClass(ItemAgent.class)
                .addClass(MaterialAgent.class);
        return UserBeanDeployment.add(ItemDeployment.add(deployment));
    }

}
