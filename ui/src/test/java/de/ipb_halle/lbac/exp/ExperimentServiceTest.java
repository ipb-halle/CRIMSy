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
import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.exp.assay.AssayService;
import de.ipb_halle.lbac.exp.text.Text;
import de.ipb_halle.lbac.exp.text.TextService;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.service.ItemService;
import java.util.Date;
import java.util.UUID;
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

    private User publicUser;
    private ACList publicReadAcl;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        publicUser = memberService.loadUserById(UUID.fromString(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID));
        publicReadAcl = GlobalAdmissionContext.getPublicReadACL();
    }

    @After
    public void finish() {

    }

    @Test
    public void test001_saveAndLoadExp() {
        Date creationDate=new Date();
        Experiment exp = new Experiment(null, "TEST-EXP-001", "Testexperiment", false, publicReadAcl, publicUser, creationDate);
        exp=experimentService.save(exp);
        Text text1 = new Text();
        text1.setChangeTime(new Date());
        text1.setCreationTime(new Date());
        text1.setExperiment(exp);
        text1.setRevision(1);
        text1.setText("Hallo");
        recordService.save(text1);
        
        Experiment loadedExperiment=experimentService.loadById(exp.getExperimentId());
        Assert.assertEquals(exp.getExperimentId(),loadedExperiment.getExperimentId());
        Assert.assertEquals(exp.getCode(),loadedExperiment.getCode());
        Assert.assertEquals(exp.getDescription(),loadedExperiment.getDescription());
        Assert.assertEquals(exp.getTemplate(),loadedExperiment.getTemplate());
        Assert.assertEquals(publicReadAcl.getId(),loadedExperiment.getACList().getId());
        Assert.assertEquals(publicUser.getId(),loadedExperiment.getOwner().getId());
        Assert.assertEquals(creationDate,loadedExperiment.getCreationTime());

    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("ExperimentServiceTest.war")
                .addClass(ExpRecordService.class)
                .addClass(TextService.class)
                .addClass(AssayService.class)
                .addClass(ItemService.class)
                .addClass(ExperimentService.class);
        return UserBeanDeployment.add(ItemDeployment.add(deployment));
    }

}
