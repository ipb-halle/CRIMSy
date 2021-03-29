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
package de.ipb_halle.lbac.material.common.bean;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.material.biomaterial.Taxonomy;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyBean;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyDifference;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyNestingService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageClassInformation;
import de.ipb_halle.lbac.material.common.history.MaterialDifference;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.mocks.TaxonomyBeanMock;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.ProjectBean;
import de.ipb_halle.lbac.project.ProjectEditBean;
import de.ipb_halle.lbac.project.ProjectService;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class TaxonomyRenderControllerTest extends TestBase {

    @Inject
    private TaxonomyService taxonomyService;

    TaxonomyBeanMock taxonomyBean;
    private Taxonomy taxonomy_1;
    private Taxonomy taxonomy_2;
    private User user_1;
    private User user_2;
    private Date creationDate1;
    private Date creationDate2;
    private SimpleDateFormat SDF = new SimpleDateFormat(" yyyy-MM-dd HH:mm");

    @Before
    @Override
    public void setUp() {
        user_1 = new User();
        user_1.setName("user_1");
        user_2 = new User();
        user_2.setName("user_2");
        Calendar cal = new GregorianCalendar();
        creationDate1 = cal.getTime();
        cal.add(Calendar.HOUR_OF_DAY, 1);
        creationDate2 = cal.getTime();
        taxonomy_1 = createTaxonomy(1, user_1, creationDate1, "Taxo1");
        taxonomy_2 = createTaxonomy(2, user_1, creationDate2, "Taxo2");
        MaterialDifference diff = new TaxonomyDifference();
        diff.initialise(1, user_1.getId(), creationDate1);
        taxonomy_2.getHistory().addDifference(diff);
    }

    private void init() {
        taxonomyBean = new TaxonomyBeanMock();
        TreeNode treeNode = new DefaultTreeNode(taxonomy_1, null);
        taxonomyBean.setSelectedTaxonomy(treeNode);
        taxonomyBean.init(memberService, taxonomyService);

        taxonomyBean.setTaxonomyBeforeEdit(taxonomy_2);
    }

    @Test
    public void test001_testInfoInShowMode() {
        init();
        taxonomyBean.setMode(TaxonomyBean.Mode.SHOW);
        Assert.assertEquals(
                "taxonomy_label_detail",
                taxonomyBean
                        .getRenderController()
                        .getInfoHeader());

        Assert.assertEquals(
                "taxonomy_label_created" + SDF.format(creationDate1),
                taxonomyBean
                        .getRenderController()
                        .getOwnerInfoForSelectedTaxonomy());

        Assert.assertTrue(
                taxonomyBean
                        .getRenderController()
                        .getEditInfoForSelectedTaxonomy()
                        .isEmpty());

        Assert.assertEquals(
                "Taxo1 (ID: 1)",
                taxonomyBean
                        .getRenderController()
                        .getInfoForSelectedTaxonomy());

    }

    @Test
    public void test002_testInfoInEditMode() {
        init();
        taxonomyBean.setMode(TaxonomyBean.Mode.EDIT);
        Assert.assertEquals("taxonomy_label_edit", taxonomyBean.getRenderController().getInfoHeader());
        Assert.assertEquals(
                "taxonomy_label_created" + SDF.format(creationDate2),
                taxonomyBean.getRenderController().getOwnerInfoForSelectedTaxonomy());
        Assert.assertEquals(
                "taxonomy_label_edit_by" + SDF.format(creationDate1),
                taxonomyBean
                        .getRenderController()
                        .getEditInfoForSelectedTaxonomy());
        Assert.assertEquals(
                "Taxo2 (ID: 2)",
                taxonomyBean
                        .getRenderController()
                        .getInfoForSelectedTaxonomy());

    }

    @Test
    public void test003_testInfoInHistoryMode() {
        init();
        taxonomyBean.setMode(TaxonomyBean.Mode.HISTORY);
        Assert.assertEquals("taxonomy_label_detail", taxonomyBean.getRenderController().getInfoHeader());
        Assert.assertEquals(
                "taxonomy_label_created" + SDF.format(creationDate1),
                taxonomyBean.getRenderController().getOwnerInfoForSelectedTaxonomy());
        Assert.assertTrue(
                taxonomyBean
                        .getRenderController()
                        .getEditInfoForSelectedTaxonomy()
                        .isEmpty());
        Assert.assertEquals(
                "Taxo1 (ID: 1)",
                taxonomyBean
                        .getRenderController()
                        .getInfoForSelectedTaxonomy());
    }

    @Test
    public void test004_testInfoInCreationMode() {
        init();
        taxonomyBean.setMode(TaxonomyBean.Mode.CREATE);
        Assert.assertEquals("taxonomy_label_new", taxonomyBean.getRenderController().getInfoHeader());
        Assert.assertTrue(
                taxonomyBean
                        .getRenderController()
                        .getOwnerInfoForSelectedTaxonomy()
                        .isEmpty());
        Assert.assertTrue(
                taxonomyBean
                        .getRenderController()
                        .getEditInfoForSelectedTaxonomy()
                        .isEmpty());
        Assert.assertTrue(
                "taxonomy_label_new",
                taxonomyBean
                        .getRenderController()
                        .getInfoForSelectedTaxonomy()
                        .isEmpty()
        );
    }

    private Taxonomy createTaxonomy(int id, User user, Date creationDate, String... names) {
        List<MaterialName> mnames = new ArrayList<>();
        for (String s : names) {
            mnames.add(new MaterialName(s, "de", 1));
        }
        return new Taxonomy(
                id,
                mnames,
                new HazardInformation(),
                new StorageClassInformation(),
                new ArrayList<>(),
                user,
                creationDate
        );
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("TaxonomyRenderControllerTest.war")
                .addClass(ProjectService.class)
                .addClass(MaterialService.class)
                .addClass(TaxonomyNestingService.class)
                .addClass(TissueService.class)
                .addClass(ProjectEditBean.class)
                .addClass(Navigator.class)
                .addClass(ProjectBean.class)
                .addClass(TaxonomyService.class);
        return UserBeanDeployment.add(deployment);
    }

}
