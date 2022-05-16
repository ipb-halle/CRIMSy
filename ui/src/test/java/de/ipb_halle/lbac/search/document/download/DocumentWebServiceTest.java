/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.search.document.download;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.Date;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.admission.MembershipService;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.file.FileObject;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.CloudService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.webclient.LbacWebClient;
import de.ipb_halle.lbac.webservice.service.WebRequestAuthenticator;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;

/**
 * @author flange
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class DocumentWebServiceTest extends TestBase {
    @Inject
    private DocumentWebService documentWebService;

    @Inject
    protected KeyManager keyManager;

    private String tmpPath;
    private Node localNode;
    private CloudNode cn;
    private User localRequestingUser;
    private User remoteRequestingUser;
    private Collection nonReadableCollection;
    private Collection readableCollection;

    @BeforeEach
    public void before() throws Exception {
        initializeBaseUrl();
        initializeKeyStoreFactory();

        File tmpDir = Files.createTempDirectory("DocumentWebServiceTest").toFile();
        tmpDir.deleteOnExit();
        tmpPath = tmpDir.getAbsolutePath();

        localNode = nodeService.getLocalNode();

        cn = cloudNodeService.loadCloudNode(TESTCLOUD, TEST_NODE_ID);
        cn.setPublicKey(Base64.getEncoder().encodeToString(keyManager.getLocalPublicKey(TESTCLOUD).getEncoded()));
        cn = cloudNodeService.save(cn);

        localRequestingUser = createUser("DocumentWebServiceTest_local", "DocumentWebServiceTest_local");
        remoteRequestingUser = createRemoteUser(localRequestingUser);

        nonReadableCollection = createLocalCollections(createAcList(remoteRequestingUser, false), localNode, adminUser,
                "DocumentDownloadBeanTest_nonReadableCollection", "desc", collectionService).get(0);
        readableCollection = createLocalCollections(createAcList(remoteRequestingUser, true), localNode, adminUser,
                "DocumentDownloadBeanTest_readableCollection", "desc", collectionService).get(0);
    }

    @AfterEach
    public void after() {
        resetCollectionsInDb(collectionService);
    }

    @Test
    public void test_downloadDocument_unsignedRequest() {
        DocumentWebRequest request = new DocumentWebRequest();
        request.setFileObjectId(42);

        Response response = documentWebService.downloadDocument(request);

        assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
        assertFalse(response.hasEntity());
    }

    @Test
    public void test_downloadDocument_documentDoesNotExistInDatabase() throws Exception {
        DocumentWebRequest request = new DocumentWebRequest();
        request.setFileObjectId(42);
        signRequest(request, localNode, localRequestingUser);

        Response response = documentWebService.downloadDocument(request);

        assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertFalse(response.hasEntity());
    }

    @Test
    public void test_downloadDocument_userIsNotPermitted() throws Exception {
        String nonexistingFilename = tmpPath + "/nonexistingFile";
        FileObject fileObject = fileEntityService.save(createFileObject(nonexistingFilename, nonReadableCollection));
        DocumentWebRequest request = new DocumentWebRequest();
        request.setFileObjectId(fileObject.getId());
        signRequest(request, localNode, localRequestingUser);

        Response response = documentWebService.downloadDocument(request);

        assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
        assertFalse(response.hasEntity());
    }

    @Test
    public void test_downloadDocument_fileDoesNotExistInFilesystem() throws Exception {
        String nonexistingFilename = tmpPath + "/nonexistingFile";
        FileObject fileObject = fileEntityService.save(createFileObject(nonexistingFilename, readableCollection));
        DocumentWebRequest request = new DocumentWebRequest();
        request.setFileObjectId(fileObject.getId());
        signRequest(request, localNode, localRequestingUser);

        Response response = documentWebService.downloadDocument(request);

        assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());
        assertFalse(response.hasEntity());
    }

    @Test
    public void test_downloadDocument_successfulDownload() throws Exception {
        String content = "Hello World";
        String path = createTempFile(content);
        FileObject fileObject = fileEntityService.save(createFileObject(path, readableCollection));
        DocumentWebRequest request = new DocumentWebRequest();
        request.setFileObjectId(fileObject.getId());
        signRequest(request, localNode, localRequestingUser);

        Response response = documentWebService.downloadDocument(request);

        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        /*
         * This is a (local) method call, so the File object is not turned into an
         * InputStream for the client.
         */
        // assertArrayEquals(content.getBytes(), IOUtils.toByteArray(response.readEntity(InputStream.class)));
        assertEquals(path, response.readEntity(File.class).getAbsolutePath());
    }

    private FileObject createFileObject(String location, Collection collection) {
        FileObject fO = new FileObject();
        fO.setCollection(collection);
        fO.setCreated(new Date());
        fO.setDocument_language("en");
        fO.setFileLocation(location);
        fO.setName(location);
        fO.setUser(adminUser);
        return fO;
    }

    private String createTempFile(String content) throws IOException {
        File tmpFile = File.createTempFile("DocumentWebServiceTest", ".tmp");
        tmpFile.deleteOnExit();
        FileWriter writer = new FileWriter(tmpFile);
        writer.write(content);
        writer.close();
        return tmpFile.getAbsolutePath();
    }

    private void signRequest(DocumentWebRequest request, Node node, User user) throws Exception {
        String cloudName = cn.getCloud().getName();
        request.setCloudName(cloudName);
        request.setNodeIdOfRequest(node.getId());
        request.setSignature(new LbacWebClient().createWebRequestSignature(keyManager.getLocalPrivateKey(cloudName)));
        request.setUser(user);
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("DocumentWebServiceTest.war")
                .addClass(FileEntityService.class)
                .addClass(CollectionService.class)
                .addClass(ACListService.class)
                .addClass(MembershipService.class)
                .addClass(MemberService.class)
                .addClass(NodeService.class)
                .addClass(WebRequestAuthenticator.class)
                .addClass(CloudNodeService.class)
                .addClass(CloudService.class)
                .addClass(LbacWebClient.class)
                .addClass(DocumentWebService.class);
        return UserBeanDeployment.add(deployment);
    }
}