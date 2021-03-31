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
package de.ipb_halle.lbac.exp.images;

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.exp.ExpRecord;
import de.ipb_halle.lbac.exp.ExpRecordEntity;
import de.ipb_halle.lbac.exp.ExpRecordService;
import de.ipb_halle.lbac.exp.ExpRecordType;
import de.ipb_halle.lbac.exp.Experiment;
import de.ipb_halle.lbac.exp.ExperimentDeployment;
import de.ipb_halle.lbac.exp.ExperimentService;
import de.ipb_halle.lbac.items.ItemDeployment;
import java.math.BigInteger;
import java.util.Date;
import java.util.List;
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
public class ImageServiceTest extends TestBase {

    private final String SQL_LOAD_IMAGES = "SELECT id,title,preview,image,aclist_id,owner_id from images";

    @Inject
    private ImageService imageService;

    @Inject
    private ExperimentService expService;

    @Inject
    private ExpRecordService recordService;

    private User publicUser;
    private ACList publicACL;
    private ACList noAccessACL;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("ImageServiceTest.war").addClass(ImageService.class);
        return ExperimentDeployment
                .add(UserBeanDeployment.add(ItemDeployment.add(deployment)));
    }

    @Before
    public void init() {
        publicUser = context.getPublicAccount();
        publicACL = GlobalAdmissionContext.getPublicReadACL();
        noAccessACL = context.getNoAccessACL();
    }

    @After
    public void clean() {
        cleanExperimentsFromDB();
    }

    @Test
    public void test001_saveImage() {
        Experiment exp = new Experiment(null, "test001_saveImage_exp", "test001_saveImage_desc", true, publicACL, publicUser, new Date());
        exp = expService.save(exp);

        Image image = new Image("title", "preview", "image", publicUser, publicACL);
        image.setExperiment(exp);
        recordService.save(image, publicUser);

        Assert.assertNotNull(image.getExpRecordId());

        List<Object> images = (List<Object>) entityManagerService.doSqlQuery(SQL_LOAD_IMAGES);
        Assert.assertEquals(1, images.size());
        Object[] o = (Object[]) images.get(0);
        Assert.assertEquals("title", o[1]);
        Assert.assertEquals("preview", o[2]);
        Assert.assertEquals("image", o[3]);
        Assert.assertEquals(publicACL.getId(), o[4]);
        Assert.assertEquals(publicUser.getId(), o[5]);

        image.setTitle("title-edited");
        image.setImage("image-edited");
        image.setPreview("preview-edited");
        imageService.saveEditedImage(image);
        images = (List<Object>) entityManagerService.doSqlQuery(SQL_LOAD_IMAGES);
        Assert.assertEquals(1, images.size());
        o = (Object[]) images.get(0);
        Assert.assertEquals(image.getExpRecordId(), ((BigInteger) o[0]).longValue(), 0);
        Assert.assertEquals("title-edited", o[1]);
        Assert.assertEquals("preview-edited", o[2]);
        Assert.assertEquals("image-edited", o[3]);
        Assert.assertEquals(publicACL.getId(), o[4]);
        Assert.assertEquals(publicUser.getId(), o[5]);
    }

    @Test
    public void test002_loadImage() {
        Experiment exp = new Experiment(null, "test002_loadImage_exp", "test002_loadImage_desc", true, publicACL, publicUser, new Date());
        exp = expService.save(exp);
        Image image = new Image("title", "preview", "image", publicUser, publicACL);
        image.setExperiment(exp);
        ExpRecord record = recordService.save(image, publicUser);

        Image loadedImage = imageService.loadImage(exp, record.createExpRecordEntity());
        Assert.assertEquals("title", loadedImage.getTitle());
        Assert.assertEquals("preview", loadedImage.getPreview());
        Assert.assertEquals("image", loadedImage.getImage());
        Assert.assertEquals(publicACL.getId(), loadedImage.getAclist().getId());
        Assert.assertEquals(publicUser.getId(), loadedImage.getUser().getId());

    }

}
