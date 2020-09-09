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
package de.ipb_halle.lbac.file;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.file.mock.AsyncContextMock;
import de.ipb_halle.lbac.file.mock.HttpServletResponseMock.WriterMock;
import de.ipb_halle.lbac.file.mock.UploadToColMock;
import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class UploadToColTest extends TestBase {

    @Inject
    private CollectionService collectionService;

    @Inject
    private FileEntityService fileEntityService;

    protected String filterDefinition = "target/test-classes/fileParserFilterDefinition.json";
    protected String examplaDocsRootFolder = "target/test-classes/exampledocs/";
    protected User publicUser;
    protected Collection col;

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
    public void test001_fileUploadTest() throws Exception {
        createAndSaveNewCol();
        UploadToColMock upload = new UploadToColMock(
                filterDefinition,
                fileEntityService,
                publicUser,
                new AsyncContextMock(
                        new File(examplaDocsRootFolder + "IPB_Jahresbericht_2004.pdf"),
                        col.getName()),
                collectionService,
                termVectorEntityService,
                "target/test-classes/collections");

        upload.run();
        Map<String, Object> cmap = new HashMap<>();
        cmap.put("id", upload.fileId);

        FileObject file = fileEntityService.load(cmap).get(0);
        Assert.assertNotNull(file);

        Assert.assertTrue(!termVectorEntityService.getTermVector(Arrays.asList(upload.fileId), 10).isEmpty());
        Assert.assertEquals(
                "informatik",
                termVectorEntityService.loadUnstemmedWordsOfDocument(
                        upload.fileId, "informat").get(0)
                        .getOriginalWord()
                        .iterator()
                        .next());

        WriterMock writermock = ((WriterMock) upload.response.getWriter());
        String json = writermock.getJson();
        Assert.assertEquals("{\"success\":true,\"newUuid\":\"1\",\"uploadName\":\"IPB_Jahresbericht_2004.pdf\"}", json);

    }

    @Test
    public void test002_fileUploadTestNoCollectionFound() throws Exception {
        UploadToColMock upload = new UploadToColMock(
                filterDefinition,
                fileEntityService,
                publicUser,
                new AsyncContextMock(
                        new File(examplaDocsRootFolder + "IPB_Jahresbericht_2004.pdf"),
                        "test-coll"),
                collectionService,
                termVectorEntityService,
                "target/test-classes/collections");

        upload.run();
        WriterMock writermock = ((WriterMock) upload.response.getWriter());
        String json = writermock.getJson();
        Assert.assertEquals("{\"success\":false,\"error\":\"Could not find collection with name test-coll\"}", json);

    }

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("UploadToColTest.war");
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

}
