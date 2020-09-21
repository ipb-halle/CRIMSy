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
import de.ipb_halle.lbac.collections.CollectionBean;
import de.ipb_halle.lbac.file.FilterDefinitionInputStreamFactory;
import de.ipb_halle.lbac.file.mock.AsyncContextMock;
import de.ipb_halle.lbac.file.mock.UploadToColMock;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.search.mocks.DocumentSearchBeanMock;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.inject.Inject;
import org.apache.openejb.loader.Files;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class DocumentSearchBeanTest extends TestBase {

    @Inject
    private DocumentSearchService documentSearchService;

    @Inject
    private CollectionService collectionService;

    @Inject
    private DocumentSearchOrchestrator orchestrator;

    protected Collection col;

    protected String examplaDocsRootFolder = "target/test-classes/exampledocs/";
    protected User publicUser;
    private AsyncContextMock asyncContext;

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
    }

    @Test
    public void test001_searchDocuments() throws FileNotFoundException, InterruptedException {
        createAndSaveNewCol();
        uploadDocument("Document1.pdf");
        while (!asyncContext.isComplete()) {
            Thread.sleep(500);
        }
        CollectionBean collectionBean = new CollectionBean();
        collectionBean.getCollectionSearchState().addCollections(Arrays.asList(col));
        DocumentSearchBean bean = new DocumentSearchBeanMock()
                .setCollectionBean(collectionBean)
                .setCollectionService(collectionService)
                .setDocumentSearchOrchestrator(orchestrator)
                .setDocumentSearchService(documentSearchService)
                .setFileEntityService(fileEntityService)
                .setNodeService(nodeService);
        bean.setSearchFieldText("a java test");

        bean.actionStartSearch();
        
        Assert.assertEquals(1,bean.getFoundDocuments().size());
        bean.getFoundDocuments().get(0).getRelevance();

    }

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("DocumentSearchBeanTest.war")
                .addClass(DocumentSearchService.class)
                .addClass(FileEntityService.class)
                .addClass(CloudService.class)
                .addClass(CloudNodeService.class)
                .addClass(NodeService.class)
                .addClass(CollectionService.class)
                .addClass(ACListService.class)
                .addClass(FileService.class)
                .addClass(MembershipService.class)
                .addClass(DocumentSearchOrchestrator.class)
                .addClass(KeyManager.class)
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
        asyncContext = new AsyncContextMock(
                new File(examplaDocsRootFolder + documentName),
                col.getName());
        UploadToColMock upload = new UploadToColMock(
                FilterDefinitionInputStreamFactory.getFilterDefinition(),
                fileEntityService,
                publicUser,
                asyncContext,
                collectionService,
                termVectorEntityService,
                "target/test-classes/collections");
        upload.run();
    }
}
