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
package de.ipb_halle.lbac.file.save;

import de.ipb_halle.kx.file.FileObject;
import de.ipb_halle.kx.file.FileObjectService;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.file.mock.FileServiceMock;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import jakarta.inject.Inject;
import org.apache.openejb.loader.Files;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author fmauz
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class FileSaverTest extends TestBase {

    @Inject
    private CollectionService collectionService;

    @Inject
    private FileObjectService fileObjectService;

    private FileServiceMock fileService;

    private String exampleDocsRootFolder = "target/test-classes/exampledocs/";

    private User publicUser;
    private Collection col;

    @BeforeEach
    public void init() {
        fileService = new FileServiceMock(fileObjectService, FileServiceMock.COLLECTIONS_MOCK_FOLDER);
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
    }

    @AfterEach
    public void cleanUp() {
        Files.delete(Paths.get(col.getStoragePath()).toFile());
        entityManagerService.doSqlUpdate("DELETE FROM collections WHERE id=" + col.getId());
    }

    @Test
    public void test001_saveDocumentToCollection() throws NoSuchAlgorithmException, IOException {
        col = new Collection();
        col.setACList(GlobalAdmissionContext.getPublicReadACL());
        col.setDescription("test001_saveDocumentToCollection()");
        col.setName("test-coll");
        col.setNode(nodeService.getLocalNode());
        col.setOwner(publicUser);
        col.setStoragePath(fileService.getCollectionPath(col).toString());
        col = collectionService.save(col);

        File f = new File(exampleDocsRootFolder + "Document1.pdf");
        FileInputStream stream = new FileInputStream(f);
        FileObject fo  = fileService.saveFile(col, "Document1.pdf", stream, publicUser);

        Assertions.assertEquals(1, fileObjectService.getDocumentCount(col.getId()));
        Assertions.assertEquals(col.getId(), fo.getCollectionId());
        Assertions.assertEquals("en", fo.getDocumentLanguage());
        Assertions.assertEquals("Document1.pdf", fo.getName());
        Assertions.assertEquals(publicUser.getId(), fo.getUserId());

        f = new File(exampleDocsRootFolder + "DocumentX.docx");
        stream = new FileInputStream(f);
        fo = fileService.saveFile(col, "DocumentX.docx", stream, publicUser);
        Assertions.assertEquals(2, fileObjectService.getDocumentCount(col.getId()));
        Assertions.assertEquals(col.getId(), fo.getCollectionId());
        Assertions.assertEquals("DocumentX.docx", fo.getName());
        Assertions.assertEquals(publicUser.getId(), fo.getUserId());

        f = new File(exampleDocsRootFolder + "TestTabelle.xlsx");
        stream = new FileInputStream(f);
        fo = fileService.saveFile(col, "TestTabelle.xlsx", stream, publicUser);
        Assertions.assertEquals(3, fileObjectService.getDocumentCount(col.getId()));
        Assertions.assertEquals(col.getId(), fo.getCollectionId());
        Assertions.assertEquals("TestTabelle.xlsx", fo.getName());
        Assertions.assertEquals(publicUser.getId(), fo.getUserId());

        f = new File(exampleDocsRootFolder + "TestTabelle.xlsx");
        stream = new FileInputStream(f);
        fo = fileService.saveFile(col, "TestTabelle.xlsx", stream, publicUser);
        Assertions.assertEquals(4, fileObjectService.getDocumentCount(col.getId()));
        Assertions.assertEquals(col.getId(), fo.getCollectionId());
        Assertions.assertEquals("a9eed28584c7e6df1d061c77884820524a7d2b4c6644ef5d13b0c2daedaf4d10d040b7c7380df448f91a28eb7fba94cf0b4a964ae141032c63a0b571aeaa5ccf", fo.getHash());
        Assertions.assertEquals("TestTabelle.xlsx", fo.getName());
        Assertions.assertEquals(publicUser.getId(), fo.getUserId());
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("FileSaverTest.war")
                .addClass(ProjectService.class);
        return UserBeanDeployment.add(ItemDeployment.add(deployment));
    }

}
