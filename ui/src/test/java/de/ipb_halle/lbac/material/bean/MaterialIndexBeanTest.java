/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.material.bean;

import de.ipb_halle.lbac.EntityManagerService;
import de.ipb_halle.lbac.admission.LdapProperties;
import de.ipb_halle.lbac.admission.SystemSettings;
import de.ipb_halle.lbac.announcement.membership.MembershipOrchestrator;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.cloud.solr.SolrAdminService;
import de.ipb_halle.lbac.collections.CollectionBean;
import de.ipb_halle.lbac.collections.CollectionOrchestrator;
import de.ipb_halle.lbac.collections.CollectionWebClient;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.material.service.MoleculeService;
import de.ipb_halle.lbac.material.bean.mock.IndexServiceMock;
import de.ipb_halle.lbac.material.mocks.UserBeanMock;
import de.ipb_halle.lbac.material.service.MaterialService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.ProjectBean;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.SolrSearcher;
import de.ipb_halle.lbac.search.document.DocumentSearchBean;
import de.ipb_halle.lbac.search.document.DocumentSearchOrchestrator;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.search.termvector.SolrTermVectorSearch;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.search.wordcloud.WordCloudBean;
import de.ipb_halle.lbac.search.wordcloud.WordCloudWebClient;
import de.ipb_halle.lbac.service.ACListService;
import de.ipb_halle.lbac.service.CollectionService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.webservice.Updater;
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

    @Before
    public void init() {
        instance = new MaterialIndexBean();
        instance.setIndexService(indexServiceMock);
        instance.init();
    }

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("MaterialIndexBeanTest.war")
                .addClass(UserBeanMock.class)
                .addClass(ACListService.class)
                .addClass(CollectionBean.class)
                .addClass(CollectionService.class)
                .addClass(SolrAdminService.class)
                .addClass(FileService.class)
                .addClass(FileEntityService.class)
                .addClass(SolrTermVectorSearch.class)
                .addClass(CollectionOrchestrator.class)
                .addClass(EntityManagerService.class)
                .addClass(TermVectorEntityService.class)
                .addClass(DocumentSearchBean.class)
                .addClass(DocumentSearchService.class)
                .addClass(SolrSearcher.class)
                .addClass(MembershipOrchestrator.class)
                .addClass(MoleculeService.class)
                .addClass(KeyManager.class)
                .addClass(LdapProperties.class)
                .addClass(ProjectService.class)
                .addClass(CollectionWebClient.class)
                .addClass(DocumentSearchOrchestrator.class)
                .addClass(SystemSettings.class)
                .addClass(Updater.class)
                .addClass(Navigator.class)
                .addClass(WordCloudBean.class)
                .addClass(WordCloudWebClient.class)
                .addClass(MaterialIndexBean.class)
                .addClass(IndexServiceMock.class)
                .addClass(MaterialNameBean.class)
                .addClass(ProjectBean.class)
                .addClass(MaterialService.class)
                .addClass(MaterialBean.class)
                .addClass(TissueService.class)
                .addClass(TaxonomyService.class)
                .addClass(IndexServiceMock.class);
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
        Assert.assertEquals(
                3,
                instance.getIndexCategories().size()
        );
        Assert.assertEquals(
                "GESTIS/ZVG",
                instance.getIndexCategories().get(0)
        );
        Assert.assertEquals(
                "CAS/RM",
                instance.getIndexCategories().get(1)
        );
        Assert.assertEquals(
                "Carl Roth Sicherheitsdatenblatt",
                instance.getIndexCategories().get(2)
        );

    }

    @Test
    public void test03_addNewIndex() throws Exception {
        instance.setIndexValue("TestValue");
        instance.setIndexCatergory("GESTIS/ZVG");
        instance.addNewIndex();

        Assert.assertEquals(
                1,
                instance.getIndices().size()
        );

        Assert.assertEquals(
                2, instance.getIndices().get(0).getTypeId());
        Assert.assertEquals(
                "TestValue", instance.getIndices().get(0).getValue());
        Assert.assertEquals(
                2,
                instance.getIndexCategories().size()
        );
    }

    @Test
    public void test04_removeIndex() throws Exception {
        instance.setIndexValue("TestValue");
        instance.setIndexCatergory("GESTIS/ZVG");
        instance.addNewIndex();
        Assert.assertEquals(
                1,
                instance.getIndices().size()
        );
        Assert.assertEquals(
                2,
                instance.getIndexCategories().size()
        );
        instance.removeIndex(instance.getIndices().get(0));
        Assert.assertEquals(
                0,
                instance.getIndices().size()
        );
        Assert.assertEquals(
                3,
                instance.getIndexCategories().size()
        );
    }

}
