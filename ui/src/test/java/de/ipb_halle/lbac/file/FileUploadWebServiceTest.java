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
import de.ipb_halle.lbac.file.mock.FileUploadCollectionMock;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.nio.file.Paths;
import org.apache.openejb.loader.Files;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 *
 * @author fmauz
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class FileUploadWebServiceTest extends TestBase {

    protected Collection col;
    protected User publicUser;

    @BeforeEach
    public void init() {
        Files.delete(Paths.get(FileUploadCollectionMock.COLLECTIONS_MOCK_FOLDER).toFile());
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);

    }

    @AfterEach
    public void cleanUp() {
        Files.delete(Paths.get(FileUploadCollectionMock.COLLECTIONS_MOCK_FOLDER).toFile());

    }

    @Test
    public void test001_fileUploadTest() throws Exception {

    }

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("FileUploadWebServiceTest.war");
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
        col.COLLECTIONS_BASE_FOLDER = FileUploadCollectionMock.COLLECTIONS_MOCK_FOLDER;
    }

}
