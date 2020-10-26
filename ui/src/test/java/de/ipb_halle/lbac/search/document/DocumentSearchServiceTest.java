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
package de.ipb_halle.lbac.search.document;

/**
 *
 * @author fmauz
 */
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.service.CloudService;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.admission.MembershipService;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.file.FilterDefinitionInputStreamFactory;
import de.ipb_halle.lbac.file.mock.AsyncContextMock;
import de.ipb_halle.lbac.file.mock.UploadToColMock;
import de.ipb_halle.lbac.search.NetObject;
import de.ipb_halle.lbac.search.SearchRequest;
import de.ipb_halle.lbac.search.SearchResult;
import de.ipb_halle.lbac.search.relevance.RelevanceCalculator;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import javax.inject.Inject;
import org.apache.openejb.loader.Files;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class DocumentSearchServiceTest extends TestBase {

    @Inject
    private DocumentSearchService documentSearchService;

    protected Collection col;

    protected String examplaDocsRootFolder = "target/test-classes/exampledocs/";
    protected User publicUser;
    protected AsyncContextMock asynContext;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        entityManagerService.doSqlUpdate("DELETE from unstemmed_words");
        entityManagerService.doSqlUpdate("DELETE from termvectors");
        entityManagerService.doSqlUpdate("DELETE from files");
        Files.delete(Paths.get("target/test-classes/collections").toFile());

        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);

    }

    @After
    public void cleanUp() {
        Files.delete(Paths.get("target/test-classes/collections").toFile());
        entityManagerService.doSqlUpdate("DELETE FROM unstemmed_words");
        entityManagerService.doSqlUpdate("DELETE FROM termvectors");
        if (col != null && col.getId() != null) {
            entityManagerService.doSqlUpdate("DELETE FROM collections WHERE id=" + col.getId());
        }
    }

    @Test
    public void test001_loadDocuments() throws FileNotFoundException, InterruptedException {
        createAndSaveNewCol();

        uploadDocument("Document1.pdf");
        uploadDocument("Document2.pdf");
        uploadDocument("Document3.pdf");

        DocumentSearchRequestBuilder builder = new DocumentSearchRequestBuilder(new User(), 0, 25);
        builder.addCollectionID(col.getId());

        SearchRequest request = builder.buildSearchRequest();

        SearchResult result = documentSearchService.loadDocuments(request);
        List<NetObject> netObjects = result.getAllFoundObjects(Document.class);
        Assert.assertEquals(3, netObjects.size());
    }

    @Test
    public void test002_loadDocuments_withOneWordRoot() throws FileNotFoundException, InterruptedException {
        createAndSaveNewCol();
        RelevanceCalculator calculator = new RelevanceCalculator(Arrays.asList("java"));
        uploadDocument("Document1.pdf");
        uploadDocument("Document2.pdf");
        uploadDocument("Document3.pdf");

        DocumentSearchRequestBuilder builder = new DocumentSearchRequestBuilder(new User(), 0, 25);
        builder.addCollectionID(col.getId());
        builder.addWordRoots(new HashSet<>(Arrays.asList("java")));

        SearchRequest request = builder.buildSearchRequest();

        SearchResult result = documentSearchService.loadDocuments(request);
        List<NetObject> netObjects = result.getAllFoundObjects(Document.class);
        List<Document> documents = new ArrayList<>();
        for (NetObject no : netObjects) {
            documents.add((Document) no.getSearchable());
        }
        Assert.assertEquals(2, netObjects.size());
        Assert.assertEquals(55, result.getDocumentStatistic().averageWordLength, 0);
        Assert.assertEquals(3, result.getDocumentStatistic().totalDocsInNode, 0);
        calculator.calculateRelevanceFactors(
                result.getDocumentStatistic().totalDocsInNode,
                result.getDocumentStatistic().averageWordLength,
                documents);
    }

    @Test
    public void test003_loadDocuments_withTwoWordRoot() throws FileNotFoundException, InterruptedException {
        createAndSaveNewCol();

        uploadDocument("Document1.pdf");
        uploadDocument("Document2.pdf");
        uploadDocument("Document3.pdf");

        DocumentSearchRequestBuilder builder = new DocumentSearchRequestBuilder(new User(), 0, 25);
        builder.addCollectionID(col.getId());
        builder.addWordRoots(new HashSet<>(Arrays.asList("java", "failure")));

        SearchRequest request = builder.buildSearchRequest();

        SearchResult result = documentSearchService.loadDocuments(request);
        List<NetObject> netObjects = result.getAllFoundObjects(Document.class);
        Assert.assertEquals(3, netObjects.size());
    }

    @Test
    public void getTagStringForSeachRequestTest() {
        assertEquals("", documentSearchService.getTagStringForSeachRequest(null));
        assertEquals("", documentSearchService.getTagStringForSeachRequest(new HashSet<>()));
        HashSet<String> set = new HashSet<>();
        set.add("term1");
        assertEquals("term1", documentSearchService.getTagStringForSeachRequest(set));
        set.add("term2");
        //Because there is no order in Sets both concatinations are correct
        String result = documentSearchService.getTagStringForSeachRequest(set);
        assertTrue(
                "term1 AND term2".equals(result)
                || "term2 AND term1".equals(result));
    }

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("DocumentSearchServiceTest.war")
                .addClass(DocumentSearchService.class)
                .addClass(FileEntityService.class)
                .addClass(CloudService.class)
                .addClass(CloudNodeService.class)
                .addClass(NodeService.class)
                .addClass(CollectionService.class)
                .addClass(ACListService.class)
                .addClass(FileService.class)
                .addClass(MembershipService.class)
                .addClass(MemberService.class)
                .addClass(TermVectorEntityService.class);
    }

    private void createAndSaveNewCol() {
        col = new Collection();
        col.setACList(GlobalAdmissionContext.getPublicReadACL());
        col.setDescription("xxx");
        col.setIndexPath("/");
        col.setName("DocumentSearchServiceTest");
        col.setNode(nodeService.getLocalNode());
        col.setOwner(publicUser);
        col.setStoragePath("/");
        col = collectionService.save(col);
        col.COLLECTIONS_BASE_FOLDER = "target/test-classes/collections";
    }

    private void uploadDocument(String documentName) throws FileNotFoundException, InterruptedException {
        asynContext = new AsyncContextMock(
                new File(examplaDocsRootFolder + documentName),
                col.getName());
        UploadToColMock upload = new UploadToColMock(
                FilterDefinitionInputStreamFactory.getFilterDefinition(),
                fileEntityService,
                publicUser,
                asynContext,
                collectionService,
                termVectorEntityService,
                "target/test-classes/collections");

        upload.run();
        while (!asynContext.isComplete()) {
            Thread.sleep(500);
        }
    }
}
