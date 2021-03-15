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
import de.ipb_halle.lbac.base.ContainerCreator;
import de.ipb_halle.lbac.base.DocumentCreator;
import de.ipb_halle.lbac.base.ItemCreator;
import de.ipb_halle.lbac.base.MaterialCreator;
import de.ipb_halle.lbac.base.ProjectCreator;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.exp.ExpRecord;
import de.ipb_halle.lbac.exp.ExpRecordService;
import de.ipb_halle.lbac.exp.Experiment;
import de.ipb_halle.lbac.exp.ExperimentService;
import de.ipb_halle.lbac.exp.LinkedData;
import de.ipb_halle.lbac.exp.LinkedDataType;
import de.ipb_halle.lbac.exp.assay.Assay;
import de.ipb_halle.lbac.exp.assay.AssayService;
import de.ipb_halle.lbac.exp.search.ExperimentSearchRequestBuilder;
import de.ipb_halle.lbac.exp.text.TextService;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.search.ItemSearchRequestBuilder;
import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyNestingService;
import de.ipb_halle.lbac.material.structure.MoleculeService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.material.common.search.MaterialSearchRequestBuilder;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectSearchRequestBuilder;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.document.DocumentSearchRequestBuilder;

import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.apache.openejb.loader.Files;

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

    private User publicUser, anotherUser;
    private ProjectCreator projectCreator;
    private Project project1, project2, project3_deactivated, project4_notReadable;
    private MaterialCreator materialCreator;
    private ItemCreator itemCreator;
    private int materialid1, materialid2, notReadableMaterialId;
    private int itemid1, itemid2, itemid3;

    @Inject
    private SearchService searchService;

    @Inject
    private ItemService itemService;

    @Inject
    private ProjectService projectService;

    @Inject
    private ContainerService containerService;

    @Inject
    private ExperimentService experimentService;

    @Inject
    private ExpRecordService expRecordService;

    @Inject
    private MaterialService materialService;

    @Inject
    private GlobalAdmissionContext context;
    private Node localNode;
    private int publicAclId;

    private Container room, cupboard, rack;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        localNode = nodeService.getLocalNode();
        cleanAllProjectsFromDb();
        publicAclId = GlobalAdmissionContext.getPublicReadACL().getId();

        createContainer();
        createUsers();
        createProjects();
        createMaterials();
        createItems();

    }

    @After
    public void finish() {
        cleanItemsFromDb();
        cleanMaterialsFromDB();

    }

    @Test
    public void test001_searchEmpty() {
        SearchResult result = searchService.search(null, localNode);
        Assert.assertEquals(0, result.getAllFoundObjects().size());

        SearchRequest request = new SearchRequestImpl(publicUser, 0, 25);
        result = searchService.search(Arrays.asList(request), localNode);
        Assert.assertEquals(0, result.getAllFoundObjects().size());
    }

    @Test
    public void test002_searchDocuments() {
        SearchRequest request = new SearchRequestImpl(publicUser, 0, 25);
        request.setSearchTarget(SearchTarget.DOCUMENT);
        searchService.search(Arrays.asList(request), localNode);
    }

    @Test(expected = Exception.class)
    public void test003_searchUser() {
        SearchRequest request = new SearchRequestImpl(publicUser, 0, 25);
        request.setSearchTarget(SearchTarget.USER);
        searchService.search(Arrays.asList(request), localNode);
    }

    @Test(expected = Exception.class)
    public void test004_searchContainer() {
        SearchRequest request = new SearchRequestImpl(publicUser, 0, 25);
        request.setSearchTarget(SearchTarget.CONTAINER);
        searchService.search(Arrays.asList(request), localNode);
    }

    @Test
    public void test005_searchProject() {
        ProjectSearchRequestBuilder requestBuilder = new ProjectSearchRequestBuilder(publicUser, 0, 25);
        SearchRequest request = requestBuilder.build();
        Assert.assertEquals(3, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());

        requestBuilder = new ProjectSearchRequestBuilder(publicUser, 0, 25);
        requestBuilder.setDeactivated(false);
        request = requestBuilder.build();
        Assert.assertEquals(2, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());

        requestBuilder = new ProjectSearchRequestBuilder(publicUser, 0, 25);
        requestBuilder.setDeactivated(true);
        request = requestBuilder.build();
        Assert.assertEquals(1, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());

        requestBuilder = new ProjectSearchRequestBuilder(publicUser, 0, 25);
        requestBuilder.setProjectName("SearchServiceTest-Project-02-XYZ");
        request = requestBuilder.build();
        Assert.assertEquals(1, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());

        requestBuilder = new ProjectSearchRequestBuilder(publicUser, 0, 25);
        requestBuilder.setUsername("SearchServiceTest_user");
        request = requestBuilder.build();
        Assert.assertEquals(1, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());
    }

    @Test
    public void test006_searchMaterials() {
        MaterialSearchRequestBuilder builder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        SearchRequest request = builder.build();
        request.addSearchCategory(SearchCategory.DEACTIVATED, "deactivated");
        Assert.assertEquals(0, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());

        request.addSearchCategory(SearchCategory.DEACTIVATED, "activated");
        Assert.assertEquals(2, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());

        builder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        builder.setIndex("-002");
        request = builder.build();
        Assert.assertEquals(0, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());

        builder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        builder.setMaterialName("-002");
        request = builder.build();
        Assert.assertEquals(1, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());

        builder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        builder.setId(String.valueOf(materialid1));
        request = builder.build();
        Assert.assertEquals(1, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());

        builder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        builder.setIndex("Index of material");
        request = builder.build();
        Assert.assertEquals(1, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());

        builder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        builder.setProjectName("Project-01");
        request = builder.build();
        Assert.assertEquals(2, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());

        builder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        builder.setStructure("XXX");
        request = builder.build();
        Assert.assertEquals(0, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());

        builder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        builder.setUserName(publicUser.getName());
        request = builder.build();
        Assert.assertEquals(2, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());

        builder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        builder.addMaterialType(MaterialType.STRUCTURE);
        request = builder.build();
        Assert.assertEquals(2, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());

        builder = new MaterialSearchRequestBuilder(publicUser, 0, 1);
        builder.setUserName(publicUser.getName());
        request = builder.build();
        Assert.assertEquals(1, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());

    }

    @Test
    public void test007_searchItems() {
        //search for a description with read access
        ItemSearchRequestBuilder builder = new ItemSearchRequestBuilder(publicUser, 0, 25);
        builder.setDescription("estitem-001");
        SearchRequest request = builder.build();
        Assert.assertEquals(1, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());

        //search for a material with read access
        builder = new ItemSearchRequestBuilder(publicUser, 0, 25);
        builder.setMaterialName("Testmaterial-001");
        request = builder.build();
        Assert.assertEquals(1, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());

        //search for a material without read access
        builder = new ItemSearchRequestBuilder(publicUser, 0, 25);
        builder.setMaterialName("Testmaterial-003-notReadable");
        request = builder.build();
        Assert.assertEquals(0, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());

        //search for a label
        Item item1 = itemService.loadItemById(itemid1);
        builder = new ItemSearchRequestBuilder(publicUser, 0, 25);
        builder.setLabel(item1.getLabel());
        Assert.assertEquals(1, searchService.search(Arrays.asList(builder.build()), localNode).getAllFoundObjects().size());

        //search for a project
        builder = new ItemSearchRequestBuilder(publicUser, 0, 25);
        builder.setProjectName("SearchServiceTest-Project-01");
        Assert.assertEquals(1, searchService.search(Arrays.asList(builder.build()), localNode).getAllFoundObjects().size());

        //Search for an user
        builder = new ItemSearchRequestBuilder(publicUser, 0, 25);
        builder.setUserName("public");
        Assert.assertEquals(3, searchService.search(Arrays.asList(builder.build()), localNode).getAllFoundObjects().size());

        //Item not found, because material not readable
        builder = new ItemSearchRequestBuilder(publicUser, 0, 25);
        builder.setMaterialName("Testmaterial-003");
        Assert.assertEquals(0, searchService.search(Arrays.asList(builder.build()), localNode).getAllFoundObjects().size());

        //Item found, but material not readable
        builder = new ItemSearchRequestBuilder(publicUser, 0, 25);
        builder.setDescription("estitem-003");
        List<NetObject> foundItems = searchService.search(Arrays.asList(builder.build()), localNode).getAllFoundObjects();
        Assert.assertEquals(1, searchService.search(Arrays.asList(builder.build()), localNode).getAllFoundObjects().size());
        Item item = (Item) foundItems.get(0).getSearchable();
        Assert.assertEquals(MaterialType.UNKNOWN, item.getMaterial().getType());

        //search for direct location
        builder = new ItemSearchRequestBuilder(publicUser, 0, 25);
        builder.setLocation("RACK");
        Assert.assertEquals(1, searchService.search(Arrays.asList(builder.build()), localNode).getAllFoundObjects().size());

        //search for indirect location
        builder = new ItemSearchRequestBuilder(publicUser, 0, 25);
        builder.setLocation("ROOM");
        Assert.assertEquals(2, searchService.search(Arrays.asList(builder.build()), localNode).getAllFoundObjects().size());

    }

    @Test
    public void test008_searchExperiments() {
        createExp1();

        ExperimentSearchRequestBuilder builder = new ExperimentSearchRequestBuilder(publicUser, 0, 25);
        builder.setMaterialName("Testmaterial-001");
        SearchRequest request = builder.build();
        Assert.assertEquals(1, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());

        builder.setMaterialName("Testmaterial-002");
        request = builder.build();
        Assert.assertEquals(1, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());

    }

    @Ignore
    @Test
    public void test009_searchForEveryTarget() {
//        ItemSearchConditionBuilder itemBuilder = new ItemSearchConditionBuilder(null /* ItemEntityGraphBUilder */);
//        MaterialSearchConditionBuilder materialBuilder = new MaterialSearchConditionBuilder(publicUser, 0, 25);
//        ProjectSearchConditionBuilder projectBuilder = new ProjectSearchConditionBuilder(publicUser, 0, 25);
//        SearchRequest itemRequest = itemBuilder.buildSearchRequest();
//        SearchRequest materialRequest = materialBuilder.buildSearchRequest();
//        SearchRequest projectRequest = projectBuilder.buildSearchRequest();
//
//        SearchResult response = searchService.search(Arrays.asList(itemRequest, materialRequest, projectRequest), localNode);
//        Assert.assertEquals(6, response.getAllFoundObjects().size());
    }

    @Ignore
    @Test
    public void test010_searchWithAugmentedDocumentRequest() {
//        uploadDocuments();
//        materialCreator.createStructure(
//                publicUser.getId(),
//                GlobalAdmissionContext.getPublicReadACL().getId(),
//                project1.getId(),
//                "H", "wasserstoff");
//
//        MaterialSearchConditionBuilder matRequestbuilder = new MaterialSearchConditionBuilder(publicUser, 0, 25);
//        // ToDo: xxxx DOES NOT WORK matRequestbuilder.addIndexName("H");
//        DocumentSearchConditionBuilder docRequestBuilder = new DocumentSearchConditionBuilder(publicUser, 0, 25);
//        Set<String> words = new HashSet<>();
//        words.add("x");
//        docRequestBuilder.addWordRoots(words);
//        SearchResult result = searchService.search(
//                Arrays.asList(docRequestBuilder.buildSearchRequest(),
//                        matRequestbuilder.buildSearchRequest()), localNode);
//
//        Assert.assertEquals(2, result.getAllFoundObjects().size());
//
//        deleteDocuments();
    }

    private void uploadDocuments() {
        deleteDocuments();
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);

        DocumentCreator documentCreator = new DocumentCreator(
                fileEntityService,
                collectionService,
                nodeService,
                termVectorEntityService);

        try {
            Collection col = documentCreator.uploadDocuments(
                    publicUser,
                    "DocumentSearchServiceTest",
                    "Wasserstoff.docx"
            );
        } catch (FileNotFoundException | InterruptedException ex) {
            throw new RuntimeException("Could not upload file");
        }
        DocumentSearchRequestBuilder requestBuilder = new DocumentSearchRequestBuilder(publicUser, 0, 25);
        Set<String> wordRoots = new HashSet<>();
        wordRoots.add("wasserstoff");
        requestBuilder.setWordRoots(wordRoots);
        SearchResult result = searchService.search(Arrays.asList(requestBuilder.build()), localNode);
        Assert.assertEquals(1, result.getAllFoundObjects().size());
    }

    private void deleteDocuments() {

        Files.delete(Paths.get("target/test-classes/collections").toFile());
        entityManagerService.doSqlUpdate("DELETE FROM collections");
        entityManagerService.doSqlUpdate("DELETE from unstemmed_words");
        entityManagerService.doSqlUpdate("DELETE from termvectors");
        entityManagerService.doSqlUpdate("DELETE from files");
        Files.delete(Paths.get("target/test-classes/collections").toFile());
    }

    private void createMaterials() {
        materialCreator = new MaterialCreator(entityManagerService);
        materialid1 = materialCreator.createStructure(
                publicUser.getId(),
                publicAclId,
                project1.getId(),
                "Testmaterial-001");
        materialCreator.addIndexToMaterial(materialid1, 2, "Index of material 1");

        materialid2 = materialCreator.createStructure(
                publicUser.getId(),
                publicAclId,
                project1.getId(),
                "Testmaterial-002");

        notReadableMaterialId = materialCreator.createStructure(
                anotherUser.getId(),
                context.getNoAccessACL().getId(),
                project1.getId(),
                "Testmaterial-003-notReadable");
    }

    private void createItems() {
        itemCreator = new ItemCreator(entityManagerService);

        itemid1 = itemCreator.createItem(
                publicUser.getId(),
                publicAclId,
                materialid1,
                "Testitem-001",
                project1,
                rack);
        itemid2 = itemCreator.createItem(
                publicUser.getId(),
                publicAclId,
                materialid2,
                "Testitem-002",
                project2,
                room);

        itemid3 = itemCreator.createItem(
                publicUser.getId(),
                publicAclId,
                notReadableMaterialId,
                "Testitem-003",
                project2.getId());
    }

    private void createContainer() {
        ContainerCreator creator = new ContainerCreator(entityManagerService, containerService);
        room = creator.createAndSaveContainer("ROOM", null);
        cupboard = creator.createAndSaveContainer("CUPBOARD", room);
        rack = creator.createAndSaveContainer("RACK", cupboard);
    }

    private void createProjects() {
        projectCreator = new ProjectCreator(
                projectService,
                GlobalAdmissionContext.getPublicReadACL());
        projectCreator.setProjectName("SearchServiceTest-Project-01");
        project1 = projectCreator.createAndSaveProject(publicUser);
        projectCreator.setProjectName("SearchServiceTest-Project-02-XYZ");
        project2 = projectCreator.createAndSaveProject(anotherUser);

        projectCreator.setProjectName("SearchServiceTest-Project-03-deactivated");
        projectCreator.setDeactivated(true);
        project3_deactivated = projectCreator.createAndSaveProject(publicUser);
        projectService.changeDeactivationState(project3_deactivated.getId(), true);

        projectCreator.setProjectName("SearchServiceTest-Project-04-notReadable");
        projectCreator.setProjectAcl(context.getNoAccessACL());
        project4_notReadable = projectCreator.createAndSaveProject(anotherUser);
    }

    private void createUsers() {
        publicUser = context.getPublicAccount();
        anotherUser = createUser("SearchServiceTest_user", "SearchServiceTest_user");
    }

    private void createExp1() {

        Experiment exp = new Experiment(
                null,
                "SearchServiceTest:exp1",
                "SearchServiceTest:exp1_descr",
                false,
                GlobalAdmissionContext.getPublicReadACL(),
                publicUser,
                new Date());
        exp = experimentService.save(exp);
        Assay assay = new Assay();
        assay.getLinkedData().get(0).setMaterial(materialService.loadMaterialById(materialid1));
        assay.setExperiment(exp);

        LinkedData assayRecord = new LinkedData(assay,
                LinkedDataType.ASSAY_SINGLE_POINT_OUTCOME, 1);
        assayRecord.setItem(itemService.loadItemById(itemid2));     // automatically sets material
        assay.getLinkedData().add(assayRecord);
        expRecordService.save(assay, publicUser);
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
                .addClass(ItemService.class)
                .addClass(TextService.class)
                .addClass(TaxonomyNestingService.class);
        return ItemDeployment.add(UserBeanDeployment.add(deployment));
    }
}
