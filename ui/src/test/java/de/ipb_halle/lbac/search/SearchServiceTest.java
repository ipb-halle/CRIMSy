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
package de.ipb_halle.lbac.search;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.items.service.*;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.ProjectCreator;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.exp.ExperimentService;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyNestingService;
import de.ipb_halle.lbac.material.structure.MoleculeService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectSearchRequestBuilder;
import de.ipb_halle.lbac.project.ProjectService;
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
import org.junit.Ignore;
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
        projectCreator = new ProjectCreator(
                projectService,
                GlobalAdmissionContext.getPublicReadACL());
        publicUser = context.getPublicAccount();

        projectCreator.setProjectName("SearchServiceTest-Project-01");
        project1 = projectCreator.createAndSaveProject(publicUser);
        projectCreator.setProjectName("SearchServiceTest-Project-02-XYZ");
        project2 = projectCreator.createAndSaveProject(publicUser);
    }

    @After
    public void finish() {
        cleanItemsFromDb();
        cleanMaterialsFromDB();
        cleanProjectFromDB(project1, false);
        cleanProjectFromDB(project2, false);

    }

    @Test
    public void test001_searchEmpty() {
        SearchResult result = searchService.search(null);
        Assert.assertEquals(0, result.getAllFoundObjects().size());

        SearchRequest request = new SearchRequestImpl(publicUser, null, 0, 25);
        result = searchService.search(Arrays.asList(request));
        Assert.assertEquals(0, result.getAllFoundObjects().size());
    }

    @Test(expected = Exception.class)
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
                .addClass(TaxonomyNestingService.class);
        return ItemDeployment.add(UserBeanDeployment.add(deployment));
    }

    private void createProjects() {

    }

}
