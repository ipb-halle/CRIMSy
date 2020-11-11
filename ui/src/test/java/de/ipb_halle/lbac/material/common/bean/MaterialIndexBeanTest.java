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

import de.ipb_halle.lbac.EntityManagerService;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.device.print.PrintBeanDeployment;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.collections.CollectionBean;
import de.ipb_halle.lbac.collections.CollectionOrchestrator;
import de.ipb_halle.lbac.collections.CollectionWebClient;
import de.ipb_halle.lbac.container.service.ContainerNestingService;
import de.ipb_halle.lbac.container.service.ContainerPositionService;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.items.bean.ItemBean;
import de.ipb_halle.lbac.items.bean.ItemOverviewBean;
import de.ipb_halle.lbac.items.service.ArticleService;
import de.ipb_halle.lbac.items.service.ItemService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyNestingService;
import de.ipb_halle.lbac.material.structure.MoleculeService;
import de.ipb_halle.lbac.material.mocks.IndexServiceMock;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.ProjectBean;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.document.DocumentSearchOrchestrator;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.search.wordcloud.WordCloudBean;
import de.ipb_halle.lbac.search.wordcloud.WordCloudWebClient;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.material.common.IndexEntry;
import de.ipb_halle.lbac.material.mocks.MateriaBeanMock;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.webservice.Updater;
import java.util.Arrays;
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
public class MaterialIndexBeanTest extends TestBase {

    @Inject
    private IndexServiceMock indexServiceMock;
    private MaterialIndexBean instance;
    private MateriaBeanMock materialEditBeanMock;

    @Before
    public void init() {
        instance = new MaterialIndexBean();
        instance.setIndexService(indexServiceMock);
        materialEditBeanMock = new MateriaBeanMock();
        materialEditBeanMock.setMode(MaterialBean.Mode.EDIT);
        instance.setMaterialEditBean(materialEditBeanMock);
        instance.init();
    }

    @Test
    public void test01_init() throws Exception {

        Assert.assertTrue(
                "value after initialisation must be empty",
                instance.getIndexValue().isEmpty());

        Assert.assertEquals(
                "First Indextype must be of GESTIS/ZVG",
                "GESTIS/ZVG", instance.getIndexCategories().get(0));
    }

    @Test
    public void test02_getIndexCategories() throws Exception {
        Assert.assertEquals(3, instance.getIndexCategories().size());
        Assert.assertEquals("GESTIS/ZVG", instance.getIndexCategories().get(0));
        Assert.assertEquals("CAS/RM", instance.getIndexCategories().get(1));
        Assert.assertEquals("Carl Roth Sicherheitsdatenblatt",
                instance.getIndexCategories().get(2)
        );

    }

    @Test
    public void test03_addNewIndex() throws Exception {
        //Add new index
        instance.setIndexValue("TestValue");
        Assert.assertEquals("TestValue", instance.getIndexValue());
        instance.setIndexCatergory("GESTIS/ZVG");
        Assert.assertEquals("GESTIS/ZVG", instance.getIndexCatergory());
        instance.addNewIndex();

        Assert.assertEquals(1, instance.getIndices().size());
        Assert.assertEquals(2, instance.getIndices().get(0).getTypeId());
        Assert.assertEquals("TestValue", instance.getIndices().get(0).getValue());
        Assert.assertEquals(2, instance.getIndexCategories().size());

        //Change the value of an existing index
        instance.setIndexCatergory("GESTIS/ZVG");
        instance.setIndexValue("Changed Value");
        instance.addNewIndex();
        Assert.assertEquals(1, instance.getIndices().size());
        Assert.assertEquals(2, instance.getIndices().get(0).getTypeId());
        Assert.assertEquals("Changed Value", instance.getIndices().get(0).getValue());
        Assert.assertEquals(2, instance.getIndexCategories().size());

        instance.init();
        instance.setIndices(Arrays.asList(new IndexEntry(2, "setIndex", "de")));
        Assert.assertEquals(1, instance.getIndices().size());
        Assert.assertEquals(2, instance.getIndices().get(0).getTypeId());
        Assert.assertEquals("setIndex", instance.getIndices().get(0).getValue());
        Assert.assertEquals(2, instance.getIndexCategories().size());

        Assert.assertNull(instance.getIndexName(new IndexEntry(1, "", "")));
        Assert.assertEquals("GESTIS/ZVG", instance.getIndexName(new IndexEntry(2, "", "")));
        Assert.assertEquals("CAS/RM", instance.getIndexName(new IndexEntry(3, "", "")));
        Assert.assertEquals("Carl Roth Sicherheitsdatenblatt", instance.getIndexName(new IndexEntry(4, "", "")));

    }

    @Test
    public void test04_removeIndex() throws Exception {
        instance.setIndexValue("TestValue");
        instance.setIndexCatergory("GESTIS/ZVG");
        instance.addNewIndex();
        Assert.assertEquals(1, instance.getIndices().size());
        Assert.assertEquals(2, instance.getIndexCategories().size());
        instance.removeIndex(instance.getIndices().get(0));
        Assert.assertEquals(0, instance.getIndices().size());
        Assert.assertEquals(3, instance.getIndexCategories().size());
    }

    @Test
    public void test005_checkIndexEnable() {
        materialEditBeanMock.setMode(MaterialBean.Mode.EDIT);
        materialEditBeanMock.setRightToEdit(false);
        Assert.assertTrue(instance.isIndexEditDisabled());

        materialEditBeanMock.setRightToEdit(true);
        Assert.assertFalse(instance.isIndexEditDisabled());

        materialEditBeanMock.setMode(MaterialBean.Mode.HISTORY);
        Assert.assertTrue(instance.isIndexEditDisabled());

        materialEditBeanMock.setMode(MaterialBean.Mode.CREATE);
        Assert.assertFalse(instance.isIndexEditDisabled());
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment
                = prepareDeployment("MaterialIndexBeanTest.war")
                        .addClass(ACListService.class)
                        .addClass(CollectionBean.class)
                        .addClass(CollectionService.class)
                        .addClass(FileService.class)
                        .addClass(FileEntityService.class)
                        .addClass(CollectionOrchestrator.class)
                        .addClass(EntityManagerService.class)
                        .addClass(TermVectorEntityService.class)
                        .addClass(DocumentSearchService.class)
                        .addClass(MoleculeService.class)
                        .addClass(ProjectService.class)
                        .addClass(CollectionWebClient.class)
                        .addClass(DocumentSearchOrchestrator.class)
                        .addClass(Updater.class)
                        .addClass(Navigator.class)
                        .addClass(WordCloudBean.class)
                        .addClass(WordCloudWebClient.class)
                        .addClass(MaterialIndexBean.class)
                        .addClass(IndexServiceMock.class)
                        .addClass(MaterialNameBean.class)
                        .addClass(ProjectBean.class)
                        .addClass(MaterialService.class)
                        .addClass(ContainerPositionService.class)
                        .addClass(MaterialBean.class)
                        .addClass(TissueService.class)
                        .addClass(TaxonomyService.class)
                        .addClass(MaterialOverviewBean.class)
                        .addClass(TaxonomyNestingService.class)
                        .addClass(ContainerService.class)
                        .addClass(ItemService.class)
                        .addClass(ArticleService.class)
                        .addClass(ItemOverviewBean.class)
                        .addClass(ContainerNestingService.class)
                        .addClass(ItemBean.class)
                        .addClass(IndexServiceMock.class);
        deployment = UserBeanDeployment.add(deployment);
        return PrintBeanDeployment.add(deployment);
    }

}
