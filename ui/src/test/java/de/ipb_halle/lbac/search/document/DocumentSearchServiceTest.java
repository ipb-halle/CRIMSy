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
import de.ipb_halle.lbac.search.SolrSearcher;
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
import de.ipb_halle.lbac.entity.Document;
import de.ipb_halle.lbac.file.FileObject;
import de.ipb_halle.lbac.file.FileSearchRequest;
import de.ipb_halle.lbac.file.mock.AsyncContextMock;
import de.ipb_halle.lbac.file.mock.UploadToColMock;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
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

    protected String filterDefinition = "target/test-classes/fileParserFilterDefinition.json";
    protected String examplaDocsRootFolder = "target/test-classes/exampledocs/";
    protected User publicUser;

    @Before
    @Override
    public void setUp() {
        super.setUp();
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
    public void test002_loadDocuments() throws FileNotFoundException {
        createAndSaveNewCol();

        uploadDocument("Document1.pdf");
        uploadDocument("Document2.pdf");
        uploadDocument("Document3.pdf");
        FileSearchRequest request = new FileSearchRequest();
        request.wordsToSearchFor.add("java");
        request.holder = col;
        Set<Document> documents = documentSearchService.loadDocuments(request,10);
        Assert.assertEquals(2, documents.size());
    }

    @Test
    public void test003_createSqlWhereClause() {
        Set<String> stemmedWords = new HashSet<>();

        Assert.assertEquals("", documentSearchService.createSqlReplaceString(stemmedWords));

        stemmedWords.add("java");
        String trimmedString = documentSearchService.createSqlReplaceString(stemmedWords).replace("  ", " ").trim();
        Assert.assertEquals("AND tv.wordroot='java'", trimmedString);

        stemmedWords.add("hibernate");
        trimmedString = documentSearchService.createSqlReplaceString(stemmedWords).replace("  ", " ").trim();
        boolean p1 = "AND tv.wordroot='java' AND tv.wordroot='hibernate'".equals(trimmedString);
        boolean p2 = "AND tv.wordroot='hibernate' AND tv.wordroot='java'".equals(trimmedString);
        Assert.assertTrue(p1 || p2);
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
        return prepareDeployment("WordCloudWebServiceTest.war")
                .addClass(DocumentSearchService.class)
                .addClass(FileEntityService.class)
                .addClass(SolrSearcher.class)
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
        col.setDescription("test001_saveDocumentToCollection()");
        col.setIndexPath("/");
        col.setName("test-coll");
        col.setNode(nodeService.getLocalNode());
        col.setOwner(publicUser);
        col.setStoragePath("/");
        col = collectionService.save(col);
        col.COLLECTIONS_BASE_FOLDER = "target/test-classes/collections";
    }

    private void uploadDocument(String documentName) throws FileNotFoundException {
        UploadToColMock upload = new UploadToColMock(
                new FileInputStream(new File(filterDefinition)),
                fileEntityService,
                publicUser,
                new AsyncContextMock(
                        new File(examplaDocsRootFolder + documentName),
                        col.getName()),
                collectionService,
                termVectorEntityService,
                "target/test-classes/collections");

        upload.run();
    }
}
