/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.exp.image;

import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.exp.ExperimentBean;
import de.ipb_halle.lbac.exp.ExperimentDeployment;
import de.ipb_halle.lbac.exp.ExperimentService;
import de.ipb_halle.lbac.exp.ItemAgent;
import de.ipb_halle.lbac.exp.MaterialAgent;
import de.ipb_halle.lbac.exp.image.Image;
import de.ipb_halle.lbac.exp.image.ImageController;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.lbac.util.WebXml;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;

/**
 * 
 * @author flange
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class ImageControllerTest extends TestBase {
    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("ImageControllerTest.war");
        return ExperimentDeployment
                .add(UserBeanDeployment.add(ItemDeployment.add(deployment)));
    }

    ExperimentBean experimentBean;
    User publicUser;
    ACList publicACL;

    @Inject
    private ExperimentService experimentService;

    @BeforeEach
    public void init() {
        experimentBean = new ExperimentBean(new ItemAgent(),
                new MaterialAgent(), context, null, experimentService,
                getMessagePresenterMock(), null);
        experimentBean.init();

        publicUser = context.getPublicAccount();
        experimentBean.setCurrentAccount(new LoginEvent(publicUser));
        publicACL = GlobalAdmissionContext.getPublicReadACL();
    }

    @Test
    public void test001_getNewRecord() {
        ImageController controller = new ImageController(experimentBean);
        Image image = (Image) controller.getNewRecord();

        Assert.assertEquals("", image.getTitle());
        Assert.assertEquals("", image.getPreview());
        Assert.assertEquals("", image.getImage());
        Assert.assertEquals(publicUser, image.getUser());
        Assert.assertEquals(publicACL, image.getAclist());
        Assert.assertTrue(image.getEdit());
    }

    /**
     * Check contract of the parent method
     * {@link ExpRecordController#getSaveButtonOnClick}
     */
    @Test
    public void test002_getSaveButtonOnClick() {
        String onclick = new ImageController(experimentBean)
                .getSaveButtonOnClick();
        Assert.assertFalse(onclick.contains("ajax:"));
        Assert.assertFalse(onclick.contains("javascript:"));
    }

    @Test
    public void test003_getMaxUploadFileSize() {
        WebXml webXml = new WebXml() {
            @Override
            public String getContextParam(String paramName,
                    String defaultValue) {
                return "123";
            }

            @Override
            public String getContextParam(String paramName,
                    FacesContext context, String defaultValue) {
                return getContextParam(paramName, defaultValue);
            }
        };

        ImageController controller = new ImageController(experimentBean,
                webXml);
        Assert.assertEquals(123L, controller.getMaxUploadFileSize());
    }

    @Test
    public void test004_gettersAndSetters() {
        experimentBean.actionSaveExperiment();
        experimentBean.actionNewExperimentRecord("IMAGE", 0);
        ImageController controller = (ImageController) experimentBean
                .getExpRecordController();
        Image image = (Image) controller.getExpRecord();

        Assert.assertEquals("", controller.getJsonImage());
        controller.setJsonImage("abc");
        Assert.assertEquals("", controller.getJsonImage());

        image.setImage("image");
        Assert.assertEquals("image", controller.getJsonImage());

        Assert.assertEquals("", controller.getJsonFile());
        controller.setJsonFile("def");
        Assert.assertEquals("", controller.getJsonFile());
    }

    @Test
    public void test005_actionSaveRecord() {
        experimentBean.actionSaveExperiment();
        experimentBean.actionNewExperimentRecord("IMAGE", 0);
        ImageController controller = (ImageController) experimentBean
                .getExpRecordController();
        Image image = (Image) controller.getExpRecord();

        // same like in getNewRecord test
        Assert.assertEquals("", image.getTitle());
        Assert.assertEquals("", image.getPreview());
        Assert.assertEquals("", image.getImage());
        Assert.assertEquals(publicUser, image.getUser());
        Assert.assertEquals(publicACL, image.getAclist());
        Assert.assertTrue(image.getEdit());

        image.setTitle("title");
        image.setPreview("preview");
        image.setImage("image");
        controller.setJsonImage("jsonImage");
        controller.setJsonFile("jsonFile");

        controller.actionSaveRecord();
        Assert.assertEquals(1, experimentBean.getExpRecords().size());

        image = (Image) controller.getExpRecord();
        Assert.assertEquals("title", image.getTitle());
        Assert.assertEquals("preview", image.getPreview());
        Assert.assertEquals("jsonFile", image.getImage());
        Assert.assertEquals(publicUser, image.getUser());
        Assert.assertEquals(publicACL, image.getAclist());
        Assert.assertTrue(image.getEdit());

        // change image and save again
        image = (Image) controller.getExpRecord();
        image.setTitle("title2");
        image.setPreview("preview2");
        image.setImage("image2");
        controller.setJsonImage("jsonImage2");
        controller.setJsonFile("jsonFile2");

        controller.actionSaveRecord();
        Assert.assertEquals(1, experimentBean.getExpRecords().size());

        Assert.assertEquals("title2", image.getTitle());
        Assert.assertEquals("preview2", image.getPreview());
        Assert.assertEquals("jsonFile2", image.getImage());
        Assert.assertEquals(publicUser, image.getUser());
        Assert.assertEquals(publicACL, image.getAclist());
        Assert.assertTrue(image.getEdit());
    }
}
