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
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.UserBeanMock;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.device.print.PrintBeanDeployment;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.cloud.solr.SolrAdminService;
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
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyNestingService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.StorageClassInformation;
import de.ipb_halle.lbac.material.common.bean.mock.MateriaBeanMock;
import de.ipb_halle.lbac.material.common.entity.index.MaterialIndexHistoryEntity;
import de.ipb_halle.lbac.material.common.service.IndexService;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.structure.MoleculeService;
import de.ipb_halle.lbac.material.structure.MoleculeStructureModel;
import de.ipb_halle.lbac.material.structure.StructureInformation;
import de.ipb_halle.lbac.material.structure.V2000;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.project.Project;
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
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.service.CollectionService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.webservice.Updater;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class MaterialCreationSaverTest extends TestBase {

    @Inject
    private MaterialService materialService;

    @Inject
    private MoleculeService moleculeService;

    private CreationTools creationTools;
    @Inject
    private ProjectService projectService;

    @Before
    public void init() {
        creationTools = new CreationTools("", "", "", memberService, projectService);
        cleanItemsFromDb();
        cleanMaterialsFromDB();

    }

    @Test
    public void test001_saveNewStructure() {

        MaterialNameBean nameBean = new MaterialNameBean();
        MaterialCreationSaver saver = new MaterialCreationSaver(moleculeService, nameBean, materialService);
        UserBeanMock userBean = new UserBeanMock();
        userBean.setCurrentAccount(memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID));
        materialService.setUserBean(userBean);
        Project p = creationTools.createProject();
        MoleculeStructureModel moleculeModel = new V2000();
        StructureInformation structureInfos = new StructureInformation();
        StorageClassInformation sci = new StorageClassInformation();
        sci.setRemarks("test-remark");
        //sci.setStorageClass(storageClass);
        saver.saveNewStructure(true, moleculeModel, structureInfos, p, new HazardInformation(), sci, new ArrayList<>());

        List<Object> o = entityManagerService.doSqlQuery("SELECT * FROM materials");
        Assert.assertEquals(1, o.size());
        o = entityManagerService.doSqlQuery("SELECT * FROM structures");
        Assert.assertEquals(1, o.size());

        o = entityManagerService.doSqlQuery("SELECT * FROM storages");
        Assert.assertEquals(1, o.size());
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment
                = prepareDeployment("MaterialCreationSaverTest.war")
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
                        .addClass(MoleculeService.class)
                        .addClass(ProjectBean.class)
                        .addClass(IndexService.class)
                        .addClass(MaterialNameBean.class)
                        .addClass(MaterialIndexBean.class)
                        .addClass(ProjectService.class)
                        .addClass(CollectionWebClient.class)
                        .addClass(DocumentSearchOrchestrator.class)
                        .addClass(Updater.class)
                        .addClass(TissueService.class)
                        .addClass(TaxonomyService.class)
                        .addClass(Navigator.class)
                        .addClass(WordCloudBean.class)
                        .addClass(ACListService.class)
                        .addClass(WordCloudWebClient.class)
                        .addClass(MateriaBeanMock.class)
                        .addClass(MaterialOverviewBean.class)
                        .addClass(ContainerService.class)
                        .addClass(ItemService.class)
                        .addClass(ContainerPositionService.class)
                        .addClass(ArticleService.class)
                        .addClass(ItemOverviewBean.class)
                        .addClass(ContainerNestingService.class)
                        .addClass(TaxonomyNestingService.class)
                        .addClass(ItemBean.class)
                        .addClass(MaterialIndexHistoryEntity.class)
                        .addClass(MaterialService.class);
        deployment = UserBeanDeployment.add(deployment);
        return PrintBeanDeployment.add(deployment);
    }
}
