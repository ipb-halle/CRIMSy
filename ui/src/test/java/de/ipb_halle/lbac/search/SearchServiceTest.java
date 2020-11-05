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
package de.ipb_halle.lbac.search.xxx;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.items.service.*;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.ItemCreator;
import de.ipb_halle.lbac.base.MaterialCreator;
import de.ipb_halle.lbac.base.ProjectCreator;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.exp.ExpRecordService;
import de.ipb_halle.lbac.exp.ExperimentService;
import de.ipb_halle.lbac.exp.assay.AssayService;
import de.ipb_halle.lbac.exp.search.ExperimentSearchRequestBuilder;
import de.ipb_halle.lbac.exp.text.TextService;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.search.ItemSearchRequestBuilder;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyNestingService;
import de.ipb_halle.lbac.material.structure.MoleculeService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.material.common.search.MaterialSearchRequestBuilder;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectSearchRequestBuilder;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchRequest;

import de.ipb_halle.lbac.search.SearchRequestImpl;
import de.ipb_halle.lbac.search.SearchRequestImpl;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.search.SearchService;
import de.ipb_halle.lbac.search.SearchService;
import de.ipb_halle.lbac.search.SearchTarget;
import de.ipb_halle.lbac.search.SearchTarget;

import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import java.util.Arrays;
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
public class SearchServiceTest extends TestBase {

    private User publicUser;
    private ProjectCreator projectCreator;
    private Project project1, project2;
    private MaterialCreator materialCreator;
    private ItemCreator itemCreator;
    private int materialid1, materialid2;
    private int itemid1, itemid2;

    @Inject
    private SearchService searchService;

    @Inject
    private ProjectService projectService;

    @Inject
    private GlobalAdmissionContext context;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        cleanAllProjectsFromDb();
        projectCreator = new ProjectCreator(
                projectService,
                GlobalAdmissionContext.getPublicReadACL());
        publicUser = context.getPublicAccount();
        materialCreator = new MaterialCreator(entityManagerService);
        itemCreator = new ItemCreator(entityManagerService);

        projectCreator.setProjectName("SearchServiceTest-Project-01");
        project1 = projectCreator.createAndSaveProject(publicUser);
        projectCreator.setProjectName("SearchServiceTest-Project-02-XYZ");
        project2 = projectCreator.createAndSaveProject(publicUser);

        int publicAclId = GlobalAdmissionContext.getPublicReadACL().getId();
        materialid1 = materialCreator.createStructure(
                publicUser.getId(),
                publicAclId,
                project1.getId(),
                "Testmaterial-001");
        materialid2 = materialCreator.createStructure(
                publicUser.getId(),
                publicAclId,
                project1.getId(),
                "Testmaterial-002");

        itemid1 = itemCreator.createItem(
                publicUser.getId(),
                publicAclId,
                materialid1,
                "Testitem-001");
        itemid2 = itemCreator.createItem(
                publicUser.getId(),
                publicAclId,
                materialid2,
                "Testitem-002");

    }

    @After
    public void finish() {
        cleanItemsFromDb();
        cleanMaterialsFromDB();

    }

    @Test
    public void test001_searchEmpty() {
        SearchResult result = searchService.search(null);
        Assert.assertEquals(0, result.getAllFoundObjects().size());

        SearchRequest request = new SearchRequestImpl(publicUser, null, 0, 25);
        result = searchService.search(Arrays.asList(request));
        Assert.assertEquals(0, result.getAllFoundObjects().size());
    }

    @Test
    public void test002_searchDocuments() {
        SearchRequest request = new SearchRequestImpl(publicUser, null, 0, 25);
        request.setSearchTarget(SearchTarget.DOCUMENT);
        searchService.search(Arrays.asList(request));
    }

    @Test(expected = Exception.class)
    public void test003_searchUser() {
        SearchRequest request = new SearchRequestImpl(publicUser, null, 0, 25);
        request.setSearchTarget(SearchTarget.USER);
        searchService.search(Arrays.asList(request));
    }

    @Test(expected = Exception.class)
    public void test004_searchContainer() {
        SearchRequest request = new SearchRequestImpl(publicUser, null, 0, 25);
        request.setSearchTarget(SearchTarget.CONTAINER);
        searchService.search(Arrays.asList(request));
    }

    @Test
    public void test005_searchProject() {
        ProjectSearchRequestBuilder requestBuilder = new ProjectSearchRequestBuilder(publicUser, 0, 25);
        SearchRequest request = requestBuilder.buildSearchRequest();
        Assert.assertEquals(2, searchService.search(Arrays.asList(request)).getAllFoundObjects().size());

        requestBuilder.addExactName("SearchServiceTest-Project-02-XYZ");
        request = requestBuilder.buildSearchRequest();
        Assert.assertEquals(1, searchService.search(Arrays.asList(request)).getAllFoundObjects().size());
    }

    @Test
    public void test006_searchMaterials() {
        MaterialSearchRequestBuilder builder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        SearchRequest request = builder.buildSearchRequest();
        Assert.assertEquals(2, searchService.search(Arrays.asList(request)).getAllFoundObjects().size());

        builder.addIndexName("-002");
        request = builder.buildSearchRequest();
        Assert.assertEquals(1, searchService.search(Arrays.asList(request)).getAllFoundObjects().size());

    }

    @Test
    public void test007_searchItems() {
        ItemSearchRequestBuilder builder = new ItemSearchRequestBuilder(publicUser, 0, 25);
        SearchRequest request = builder.buildSearchRequest();
        Assert.assertEquals(2, searchService.search(Arrays.asList(request)).getAllFoundObjects().size());

        builder.addIndexName("-002");
        request = builder.buildSearchRequest();
        Assert.assertEquals(1, searchService.search(Arrays.asList(request)).getAllFoundObjects().size());
    }

    @Test
    public void test008_searchExperiments() {
        ExperimentSearchRequestBuilder builder = new ExperimentSearchRequestBuilder(publicUser, 0, 25);
        SearchRequest request = builder.buildSearchRequest();
        Assert.assertEquals(0, searchService.search(Arrays.asList(request)).getAllFoundObjects().size());

    }

    @Test
    public void test009_searchForEveryTarget() {
        ItemSearchRequestBuilder itemBuilder = new ItemSearchRequestBuilder(publicUser, 0, 25);
        MaterialSearchRequestBuilder materialBuilder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        ProjectSearchRequestBuilder projectBuilder = new ProjectSearchRequestBuilder(publicUser, 0, 25);
        SearchRequest itemRequest = itemBuilder.buildSearchRequest();
        SearchRequest materialRequest = materialBuilder.buildSearchRequest();
        SearchRequest projectRequest = projectBuilder.buildSearchRequest();

        SearchResult response = searchService.search(Arrays.asList(itemRequest, materialRequest, projectRequest));
        Assert.assertEquals(6, response.getAllFoundObjects().size());
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("SearchService.war")
                .addClass(SearchService.class)
                .addClass(ProjectService.class)
                .addClass(MoleculeService.class)
                .addClass(ArticleService.class)
                .addClass(TaxonomyService.class)
                .addClass(TissueService.class)
                .addClass(DocumentSearchService.class)
                .addClass(TermVectorEntityService.class)
                .addClass(CollectionService.class)
                .addClass(FileEntityService.class)
                .addClass(ExperimentService.class)
                .addClass(ExpRecordService.class)
                .addClass(AssayService.class)
                .addClass(TextService.class)
                .addClass(TaxonomyNestingService.class);
        return ItemDeployment.add(UserBeanDeployment.add(deployment));
    }
}
