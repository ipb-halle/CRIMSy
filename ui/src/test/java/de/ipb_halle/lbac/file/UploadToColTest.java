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

import de.ipb_halle.kx.file.FileObject;
import de.ipb_halle.kx.file.FileObjectService;
import de.ipb_halle.kx.termvector.TermFrequency;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.file.mock.AsyncContextMock;
import de.ipb_halle.lbac.file.mock.HttpServletResponseMock.WriterMock;
import de.ipb_halle.lbac.file.mock.UploadToColMock;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
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
public class UploadToColTest extends TestBase {

    @Inject
    private CollectionService collectionService;

    @Inject
    private FileObjectService fileObjectService;

    protected String examplaDocsRootFolder = "target/test-classes/exampledocs/";
    protected User publicUser;
    protected Collection col;

    @BeforeEach
    public void init() {
        Files.delete(Paths.get("target/test-classes/collections").toFile());
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
    }

    @AfterEach
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
                fileObjectService,
                publicUser,
                new AsyncContextMock(
                        new File(examplaDocsRootFolder + "IPB_Jahresbericht_2004.pdf"),
                        col.getName()),
                collectionService,
                "target/test-classes/collections");

        upload.run();
        Map<String, Object> cmap = new HashMap<>();
        cmap.put("id", upload.fileId);

        FileObject file = fileObjectService.load(cmap).get(0);
        Assert.assertNotNull(file);

        Assert.assertTrue(!termVectorService.getTermVector(Arrays.asList(upload.fileId), 10).isEmpty());
        Assert.assertEquals(
                "informatik",
                termVectorService.loadUnstemmedWordsOfDocument(
                        upload.fileId, "informat").get(0)
                        .getOriginalWord());
                        

        WriterMock writermock = ((WriterMock) upload.response.getWriter());
        String json = writermock.getJson();
        Assert.assertEquals(String.format("{\"success\":true,\"newUuid\":\"%s\",\"uploadName\":\"IPB_Jahresbericht_2004.pdf\"}", upload.fileId), json);

    }

    @Test
    public void test002_fileUploadTestNoCollectionFound() throws Exception {
        UploadToColMock upload = new UploadToColMock(
                fileObjectService,
                publicUser,
                new AsyncContextMock(
                        new File(examplaDocsRootFolder + "IPB_Jahresbericht_2004.pdf"),
                        "test-coll-does-not-exist"),
                collectionService,
                "target/test-classes/collections");

        upload.run();
        WriterMock writermock = ((WriterMock) upload.response.getWriter());
        String json = writermock.getJson();
        Assert.assertEquals("{\"success\":false,\"error\":\"Could not find collection with name test-coll-does-not-exist\"}", json);

    }

    @Test
    public void test003_fileUploadWithSmallNumbers() throws Exception {
        createAndSaveNewCol();
        UploadToColMock upload = new UploadToColMock(
                fileObjectService,
                publicUser,
                new AsyncContextMock(
                        new File(examplaDocsRootFolder + "ShotNumberExample.docx"),
                        col.getName()),
                collectionService,
                "target/test-classes/collections");

        upload.run();
        List<TermFrequency> terms = termVectorService.getTermVector(Arrays.asList(upload.fileId), 100);
        Assert.assertEquals(4, terms.size());

    }

    @Test
    public void test004_regExForNumbers() {
        String replacement = " ";
        String regEx = "(\\[|<||-|,| |^|\\(|\\{)\\d+([\\W])*\\d*( |$|\\)|,|\\}|-|\\.|\\%|\\]|>)";

        Assert.assertEquals(" ", " 1.23 ".replaceAll(regEx, replacement));
        Assert.assertEquals(" ", "1.23 ".replaceAll(regEx, replacement));
        Assert.assertEquals(" ", " 1-23 ".replaceAll(regEx, replacement));
        Assert.assertEquals(" ", " 1,23 ".replaceAll(regEx, replacement));
        Assert.assertEquals(" ", "(1,23)".replaceAll(regEx, replacement));
        Assert.assertEquals(" ", "{1,23}".replaceAll(regEx, replacement));
        Assert.assertEquals("  ", "<1,23> ".replaceAll(regEx, replacement));
        Assert.assertEquals("386er", "386er".replaceAll(regEx, replacement));
        Assert.assertEquals("  ", "(1,23, 2-12".replaceAll(regEx, replacement));
        Assert.assertEquals(" ", "18598-18604.".replaceAll(regEx, replacement));
        Assert.assertEquals(" ", "8947–8950.".replaceAll(regEx, replacement));
        Assert.assertEquals(" is a good number ", "1,23 is a good number 2,7".replaceAll(regEx, replacement));
        Assert.assertEquals(" ", " 963.5590,".replaceAll(regEx, replacement));
        Assert.assertEquals(" ", "100%".replaceAll(regEx, replacement));
        Assert.assertEquals(" ", "[86,85]".replaceAll(regEx, replacement));
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
