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

import de.ipb_halle.kx.file.FileObjectService;
import de.ipb_halle.kx.termvector.TermVectorService;
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
import de.ipb_halle.lbac.datalink.LinkedData;
import de.ipb_halle.lbac.datalink.LinkedDataType;
import de.ipb_halle.lbac.exp.ExperimentDeployment;
import de.ipb_halle.lbac.exp.assay.Assay;
import de.ipb_halle.lbac.exp.assay.AssayService;
import de.ipb_halle.lbac.exp.search.ExperimentSearchRequestBuilder;
import de.ipb_halle.lbac.exp.text.TextService;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.search.ItemSearchRequestBuilder;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.material.biomaterial.BioMaterial;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyNestingService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.material.common.IndexEntry;

import de.ipb_halle.lbac.material.common.MaterialName;
import de.ipb_halle.lbac.material.common.StorageInformation;
import de.ipb_halle.lbac.material.common.search.MaterialSearchRequestBuilder;
import de.ipb_halle.lbac.material.common.service.MaterialService;
import de.ipb_halle.lbac.material.composition.CompositionType;
import de.ipb_halle.lbac.material.composition.MaterialComposition;
import de.ipb_halle.lbac.material.sequence.Sequence;
import de.ipb_halle.lbac.material.sequence.SequenceData;
import de.ipb_halle.lbac.material.sequence.SequenceType;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectSearchRequestBuilder;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.document.DocumentSearchRequestBuilder;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.search.mocks.SearchQueryStemmerMock;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.inject.Inject;
import org.apache.openejb.loader.Files;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author fmauz
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class SearchServiceTest extends TestBase {

    private User publicUser, anotherUser;
    private ProjectCreator projectCreator;
    private Project project1, project2, project3_deactivated, project4_notReadable;
    private MaterialCreator materialCreator;
    private ItemCreator itemCreator;
    private int materialid1, materialid2, notReadableMaterialId;
    private int itemid1, itemid2, itemid3, itemid4;
    private int expid1, expid2, expid3, expid4;
    private BioMaterial bioMaterial;

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
    private TaxonomyService taxonomyService;

    private Node localNode;
    private int publicAclId;

    private Container room, cupboard, rack;

    @BeforeEach
    public void init() {
        localNode = nodeService.getLocalNode();
        cleanAllProjectsFromDb();
        publicAclId = GlobalAdmissionContext.getPublicReadACL().getId();

        createContainer();
        createUsers();
        createProjects();
        createMaterials();
        createItems();
    }

    @AfterEach
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

    @Test
    public void test003_searchUser() {
        SearchRequest request = new SearchRequestImpl(publicUser, 0, 25);
        request.setSearchTarget(SearchTarget.USER);
        Assert.assertThrows(Exception.class, () -> searchService.search(Arrays.asList(request), localNode));
    }

    @Test
    public void test004_searchContainer() {
        SearchRequest request = new SearchRequestImpl(publicUser, 0, 25);
        request.setSearchTarget(SearchTarget.CONTAINER);
        Assert.assertThrows(Exception.class, () -> searchService.search(Arrays.asList(request), localNode));
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

        request = builder.build();
        request.addSearchCategory(SearchCategory.DEACTIVATED, "activated");
        Assert.assertEquals(3, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());

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
        Assert.assertEquals(3, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());

        builder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        builder.setStructure("CCOCC");
        request = builder.build();
        Assert.assertEquals(0, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());

        builder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        builder.setStructure("CCC");
        request = builder.build();
        Assert.assertEquals(1, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());

        builder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        builder.setUserName(publicUser.getName());
        request = builder.build();
        List<NetObject> results = searchService.search(Arrays.asList(request), localNode).getAllFoundObjects();
        Assert.assertEquals(3, results.size());

        builder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        builder.addMaterialType(MaterialType.STRUCTURE);
        request = builder.build();
        Assert.assertEquals(2, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());

        builder = new MaterialSearchRequestBuilder(publicUser, 0, 1);
        builder.setUserName(publicUser.getName());
        request = builder.build();
        Assert.assertEquals(1, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());

        MaterialComposition composition = new MaterialComposition(
                0,
                Arrays.asList(new MaterialName("composition-1", "de", 0)),
                project1.getId(),
                new HazardInformation(),
                new StorageInformation(), CompositionType.EXTRACT);
        composition.addComponent(materialService.loadMaterialById(materialid1), 0d, null);
        composition.addComponent(materialService.loadMaterialById(materialid2), 0d, null);
        materialService.saveMaterialToDB(composition, project1.getACList().getId(), new HashMap<>(), publicUser);

        builder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        builder.setStructure("CC");
        request = builder.build();
        results = searchService.search(Arrays.asList(request), localNode).getAllFoundObjects();
        Assert.assertEquals(2, results.size());

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
        Assert.assertEquals(MaterialType.INACCESSIBLE, item.getMaterial().getType());

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
        //material1:readable | item2->material2: readable
        createExp1_allReadable();
        //notReadableMaterialId:not readable | item2->material2: readable
        createExp2_notReadableMaterial();
        //notReadableMaterialId:not readable | item4->notReadableMaterialId: not readable
        createExp3_notReadableMaterialAndItem();
        //material1:readable | item4->notReadableMaterialId: not readable
        createExp4_notReadableItem();

        ExperimentSearchRequestBuilder builder = new ExperimentSearchRequestBuilder(publicUser, 0, 25);
        builder.setMaterialName("Testmaterial-001");
        SearchRequest request = builder.build();
        List<Experiment> results = searchService.search(Arrays.asList(request), localNode).getAllFoundObjects(Experiment.class, localNode);
        List<Integer> ids = results.stream()
                .map(exp -> exp.getId())
                .collect(Collectors.toList());
        Assert.assertEquals(2, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());
        Assert.assertTrue(ids.contains(expid1));
        Assert.assertTrue(ids.contains(expid4));

        //For experiment 4 only the material is readable, the item not
        List<ExpRecord> records = expRecordService.load(createExperimentCMap(expid4), publicUser);
        Assert.assertNull(records.get(0).getLinkedData().get(0).getItem());
        Assert.assertEquals(materialid1, records.get(0).getLinkedData().get(0).getMaterial().getId());
        Assert.assertEquals(-1, records.get(0).getLinkedData().get(1).getItem().getId(), 0);
        Assert.assertEquals(-1, records.get(0).getLinkedData().get(1).getMaterial().getId());

        //For experiment 2 only the item is readable, the material not
        builder.setMaterialName("Testmaterial-002");
        request = builder.build();
        results = searchService.search(Arrays.asList(request), localNode).getAllFoundObjects(Experiment.class, localNode);
        ids = results.stream()
                .map(exp -> exp.getId())
                .collect(Collectors.toList());
        Assert.assertEquals(2, searchService.search(Arrays.asList(request), localNode).getAllFoundObjects().size());
        Assert.assertTrue(ids.contains(expid1));
        Assert.assertTrue(ids.contains(expid2));
        records = expRecordService.load(createExperimentCMap(expid2), publicUser);
        Assert.assertNull(records.get(0).getLinkedData().get(0).getItem());
        Assert.assertEquals(-1, records.get(0).getLinkedData().get(0).getMaterial().getId(), 0);
        Assert.assertEquals(itemid2, records.get(0).getLinkedData().get(1).getItem().getId(), 0);
        Assert.assertEquals(materialid2, records.get(0).getLinkedData().get(1).getMaterial().getId());

        createExp5_withBioAssay();
        builder = new ExperimentSearchRequestBuilder(publicUser, 0, 25);
        builder.setMaterialName("BioMaterial001");
        request = builder.build();
        results = searchService.search(Arrays.asList(request), localNode).getAllFoundObjects(Experiment.class, localNode);
        Assert.assertEquals(1, results.size());

    }

    @Test
    public void test009_searchForEveryTarget() {
        ItemSearchRequestBuilder itemBuilder = new ItemSearchRequestBuilder(publicUser, 0, 25);
        MaterialSearchRequestBuilder materialBuilder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        ProjectSearchRequestBuilder projectBuilder = new ProjectSearchRequestBuilder(publicUser, 0, 25);

        SearchRequest itemRequest = itemBuilder.build();
        SearchRequest materialRequest = materialBuilder.build();
        SearchRequest projectRequest = projectBuilder.build();

        SearchResult response = searchService.search(Arrays.asList(itemRequest, materialRequest, projectRequest), localNode);
        Assert.assertEquals(10, response.getAllFoundObjects().size());
    }

    @Test
    public void test010_searchWithAugmentedDocumentRequest() {
        uploadDocuments();
        materialCreator.createStructure(
                publicUser.getId(),
                GlobalAdmissionContext.getPublicReadACL().getId(),
                project1.getId(),
                "H", "wasserstoff");

        MaterialSearchRequestBuilder matRequestbuilder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        matRequestbuilder.setMaterialName("H");

        DocumentSearchRequestBuilder docRequestBuilder = new DocumentSearchRequestBuilder(publicUser, 0, 25);
        docRequestBuilder.setWordRoot("x");
        // provide stemmer with material names
        searchService.setSearchQueryStemmer(new SearchQueryStemmerMock("wasserstoff h"));
        SearchResult result = searchService.search(
                Arrays.asList(docRequestBuilder.build(),
                        matRequestbuilder.build()), localNode);

        Assert.assertEquals(2, result.getAllFoundObjects().size());

        // now provide stemmer with 'original' search term
        searchService.setSearchQueryStemmer(new SearchQueryStemmerMock("x"));
        result = searchService.search(
                Arrays.asList(docRequestBuilder.build()),
                localNode);
        Assert.assertEquals(0, result.getAllFoundObjects().size());
        deleteDocuments();
    }

    @Test
    public void test011_searchForEmptyComposition() {
        cleanItemsFromDb();
        cleanMaterialsFromDB();

        //Create a Composition with readable and one without readable ACL
        MaterialComposition composition = new MaterialComposition(null, project1.getId(), CompositionType.MIXTURE);
        composition.getNames().add(new MaterialName("Composition X", "de", 0));
        MaterialComposition composition2 = new MaterialComposition(null, project1.getId(), CompositionType.MIXTURE);
        composition.getNames().add(new MaterialName("Composition Y", "de", 0));
        materialService.saveMaterialToDB(composition, publicAclId, new HashMap<>(), adminUser);
        materialService.saveMaterialToDB(composition2, context.getAdminOnlyACL().getId(), new HashMap<>(), adminUser);

        MaterialSearchRequestBuilder matRequestbuilder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        matRequestbuilder.setMaterialName("Composition");
        SearchResult result = searchService.search(
                Arrays.asList(matRequestbuilder.build()),
                localNode);
        Assert.assertEquals(1, result.getAllFoundObjects().size());
    }

    @Test
    public void test012_searchForComposition() {
        MaterialComposition composition = new MaterialComposition(null, project1.getId(), CompositionType.EXTRACT);
        composition.addComponent(materialService.loadMaterialById(materialid1), 0.1d, null);
        composition.addComponent(materialService.loadMaterialById(notReadableMaterialId), 0.2d, null);
        composition.addComponent(bioMaterial, 0.3d, null);

        materialService.saveMaterialToDB(composition, publicAclId, new HashMap<>(), publicUser);

        MaterialSearchRequestBuilder matRequestbuilder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        matRequestbuilder.setMaterialName("Testmaterial-001");
        SearchResult result = searchService.search(
                Arrays.asList(matRequestbuilder.build()),
                localNode);
        Assert.assertEquals(2, result.getAllFoundObjects().size());

        matRequestbuilder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        matRequestbuilder.setMaterialName("notReadable");
        result = searchService.search(
                Arrays.asList(matRequestbuilder.build()),
                localNode);
        Assert.assertEquals(0, result.getAllFoundObjects().size());

    }

    @Test
    public void test012_searchForSequence() {
        createSequence();
        //Search by name
        MaterialSearchRequestBuilder matRequestbuilder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        matRequestbuilder.setMaterialName("SequenceX");
        SearchResult result = searchService.search(
                Arrays.asList(matRequestbuilder.build()),
                localNode);
        Assert.assertEquals(1, result.getAllFoundObjects().size());
        //Search by index
        matRequestbuilder = new MaterialSearchRequestBuilder(publicUser, 0, 25);
        matRequestbuilder.setIndex("IndexValue");
        result = searchService.search(
                Arrays.asList(matRequestbuilder.build()),
                localNode);
        Assert.assertEquals(1, result.getAllFoundObjects().size());

    }

    private Sequence createSequence() {
        List<MaterialName> names = new ArrayList<>();
        names.add(new MaterialName("SequenceX", "en", 0));
        Sequence seq = new Sequence(
                null,
                names,
                project1.getId(),
                new HazardInformation(),
                new StorageInformation(),
                SequenceData.builder()
                        .sequenceType(SequenceType.DNA)
                        .sequenceString("AAA")
                        .build());
        seq.getIndices().add(new IndexEntry(2, "IndexValue", "de"));

        materialService.saveMaterialToDB(seq, publicAclId, new HashMap<>(), publicUser);
        return seq;
    }

    private void uploadDocuments() {
        deleteDocuments();
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);

        DocumentCreator documentCreator = new DocumentCreator(
                fileObjectService,
                collectionService,
                nodeService,
                termVectorService);

        try {
            Collection col = documentCreator.uploadDocuments(
                    publicUser,
                    "DocumentSearchServiceTest",
                    "Wasserstoff.docx"
            );
        } catch (Exception ex) {
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
                "CCCCCCCCC",
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
        createTaxanomy(0, "Life", 1, publicAclId, publicUser.getId());
        bioMaterial = creationTools.createBioMaterial(project1, "BioMaterial001", taxonomyService.loadRootTaxonomy(), null);
        materialService.saveMaterialToDB(bioMaterial, publicAclId, new HashMap<>(), publicUser.getId());
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
        itemid4 = itemCreator.createItem(
                publicUser.getId(),
                context.getNoAccessACL().getId(),
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

    private void createExp1_allReadable() {

        Experiment exp = new Experiment(
                null,
                "SearchServiceTest:exp1",
                "SearchServiceTest:exp1_descr",
                false,
                GlobalAdmissionContext.getPublicReadACL(),
                publicUser,
                new Date());
        exp = experimentService.save(exp);
        expid1 = exp.getExperimentId();
        Assay assay = new Assay();
        assay.getLinkedData().get(0).setMaterial(materialService.loadMaterialById(materialid1));
        assay.setExperiment(exp);

        LinkedData assayRecord = new LinkedData(assay,
                LinkedDataType.ASSAY_SINGLE_POINT_OUTCOME, 1);
        assayRecord.setItem(itemService.loadItemById(itemid2)); // automatically sets material
        assay.getLinkedData().add(assayRecord);
        expRecordService.save(assay, publicUser);
    }

    private void createExp2_notReadableMaterial() {
        Experiment exp = new Experiment(
                null,
                "SearchServiceTest:exp2",
                "SearchServiceTest:exp2_descr",
                false,
                GlobalAdmissionContext.getPublicReadACL(),
                publicUser,
                new Date());
        exp = experimentService.save(exp);
        expid2 = exp.getExperimentId();
        Assay assay = new Assay();
        assay.getLinkedData().get(0).setMaterial(materialService.loadMaterialById(notReadableMaterialId));
        assay.setExperiment(exp);

        LinkedData assayRecord = new LinkedData(assay,
                LinkedDataType.ASSAY_SINGLE_POINT_OUTCOME, 1);
        assayRecord.setItem(itemService.loadItemById(itemid2)); // automatically sets material
        assay.getLinkedData().add(assayRecord);
        expRecordService.save(assay, publicUser);
    }

    private void createExp3_notReadableMaterialAndItem() {
        Experiment exp = new Experiment(
                null,
                "SearchServiceTest:exp3",
                "SearchServiceTest:exp3_descr",
                false,
                GlobalAdmissionContext.getPublicReadACL(),
                publicUser,
                new Date());
        exp = experimentService.save(exp);
        expid3 = exp.getExperimentId();
        Assay assay = new Assay();
        assay.getLinkedData().get(0).setMaterial(materialService.loadMaterialById(notReadableMaterialId));
        assay.setExperiment(exp);

        LinkedData assayRecord = new LinkedData(assay,
                LinkedDataType.ASSAY_SINGLE_POINT_OUTCOME, 1);
        assayRecord.setItem(itemService.loadItemById(itemid4)); // automatically sets material
        assay.getLinkedData().add(assayRecord);
        expRecordService.save(assay, publicUser);
    }

    private void createExp4_notReadableItem() {
        Experiment exp = new Experiment(
                null,
                "SearchServiceTest:exp4",
                "SearchServiceTest:exp4_descr",
                false,
                GlobalAdmissionContext.getPublicReadACL(),
                publicUser,
                new Date());
        exp = experimentService.save(exp);
        expid4 = exp.getExperimentId();
        Assay assay = new Assay();
        assay.getLinkedData().get(0).setMaterial(materialService.loadMaterialById(materialid1));
        assay.setExperiment(exp);

        LinkedData assayRecord = new LinkedData(assay,
                LinkedDataType.ASSAY_SINGLE_POINT_OUTCOME, 1);
        assayRecord.setItem(itemService.loadItemById(itemid4)); // automatically sets material
        assay.getLinkedData().add(assayRecord);
        expRecordService.save(assay, publicUser);
    }

    private void createExp5_withBioAssay() {
        Experiment exp = new Experiment(
                null,
                "SearchServiceTest:exp5",
                "SearchServiceTest:exp5_descr",
                false,
                GlobalAdmissionContext.getPublicReadACL(),
                publicUser,
                new Date());
        exp = experimentService.save(exp);
        expid4 = exp.getExperimentId();
        Assay assay = new Assay();
        assay.setExperiment(exp);
        assay.setTarget(bioMaterial);
        expRecordService.save(assay, publicUser);

    }

    private Map<String, Object> createExperimentCMap(int id) {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put("EXPERIMENT_ID", id);
        return cmap;
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("SearchService.war")
                .addClass(SearchService.class)
                .addClass(ProjectService.class)
                .addClass(ArticleService.class)
                .addClass(TaxonomyService.class)
                .addClass(TissueService.class)
                .addClass(DocumentSearchService.class)
                .addClass(TermVectorService.class)
                .addClass(CollectionService.class)
                .addClass(FileObjectService.class)
                .addClass(ExperimentService.class)
                .addClass(ExpRecordService.class)
                .addClass(AssayService.class)
                .addClass(ItemService.class)
                .addClass(TextService.class)
                .addClass(TaxonomyNestingService.class);
        return ExperimentDeployment.add(ItemDeployment.add(UserBeanDeployment.add(deployment)));
    }
}
