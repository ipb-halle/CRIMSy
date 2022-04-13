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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Date;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.admission.MembershipService;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.mock.UserBeanMock;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.file.FileObject;
import de.ipb_halle.lbac.search.NetObject;
import de.ipb_halle.lbac.search.NetObjectImpl;
import de.ipb_halle.lbac.search.bean.NetObjectFactory;
import de.ipb_halle.lbac.search.document.Document;
import de.ipb_halle.lbac.search.document.download.mocks.DocumentWebClientMock;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.CloudService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.util.jsf.SendFileBeanMock;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;

/**
 * @author flange
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class DocumentDownloadBeanTest extends TestBase {
    @Inject
    private NodeService nodeService;

    @Inject
    private CollectionService collectionService;

    @Inject
    private FileEntityService fileEntityService;

    @Inject
    private DocumentDownloadBean bean;

    @Inject
    private GlobalAdmissionContext globalAdmissionContext;

    @Inject
    private UserBeanMock userBeanMock;

    @Inject
    private SendFileBeanMock sendFileBeanMock;

    @Inject
    private DocumentWebClientMock webClientMock;

    private Node localNode;
    private Node remoteNode;
    private Collection nonReadableCollection;
    private Collection readableCollection;
    private String tmpPath;

    @BeforeEach
    public void before() throws IOException {
        sendFileBeanMock.reset();
        webClientMock.setBehaviour(null);

        localNode = nodeService.getLocalNode();
        remoteNode = createNode(nodeService, "abc");
        createCloudNode(remoteNode, cloudService.loadByName(TESTCLOUD));

        userBeanMock.setCurrentAccount(publicUser);

        nonReadableCollection = createLocalCollections(globalAdmissionContext.getNoAccessACL(), localNode, adminUser,
                "DocumentDownloadBeanTest_nonReadableCollection", "desc", collectionService).get(0);
        readableCollection = createLocalCollections(GlobalAdmissionContext.getPublicReadACL(), localNode, adminUser,
                "DocumentDownloadBeanTest_readableCollection", "desc", collectionService).get(0);

        File tmpDir = Files.createTempDirectory("DocumentDownloadBeanTest").toFile();
        tmpDir.deleteOnExit();
        tmpPath = tmpDir.getAbsolutePath();
    }

    @AfterEach
    public void after() {
        entityManagerService.doSqlUpdate(String.format("DELETE FROM nodes WHERE id = '%s'", remoteNode.getId()));
        resetCollectionsInDb(collectionService);
    }

    /*
     * Tests for download of local files
     */
    @Test
    public void test_actionDownload_local_userIsNotPermitted() throws IOException {
        Document doc = new NetObjectFactory().createDocument("public", localNode, "test.pdf");
        doc.setCollection(nonReadableCollection);
        doc.setId(42);
        NetObject netObject = new NetObjectImpl(doc, localNode);

        bean.actionDownload(netObject);

        assertNoFileSent();
    }

    @Test
    public void test_actionDownload_local_documentDoesNotExistInDatabase() throws IOException {
        Document doc = new NetObjectFactory().createDocument("public", localNode, "test.pdf");
        doc.setCollection(readableCollection);
        doc.setId(42);
        NetObject netObject = new NetObjectImpl(doc, localNode);

        bean.actionDownload(netObject);

        assertNoFileSent();
    }

    @Test
    public void test_actionDownload_local_fileDoesNotExistInFilesystem() throws IOException {
        String nonexistingFilename = tmpPath + "/nonexistingFile";
        FileObject fileObject = fileEntityService.save(createFileObject(nonexistingFilename));
        Document doc = new NetObjectFactory().createDocument("public", localNode, "test.pdf");
        doc.setCollection(readableCollection);
        doc.setId(fileObject.getId());
        NetObject netObject = new NetObjectImpl(doc, localNode);

        bean.actionDownload(netObject);

        assertNoFileSent();
    }

    @Test
    public void test_actionDownload_local_successfulDownload() throws IOException {
        String content = "Hello World";
        String path = createTempFile(content);
        FileObject fileObject = fileEntityService.save(createFileObject(path));
        Document doc = new NetObjectFactory().createDocument("public", localNode, "test.pdf");
        doc.setCollection(readableCollection);
        doc.setId(fileObject.getId());
        NetObject netObject = new NetObjectImpl(doc, localNode);

        bean.actionDownload(netObject);

        assertArrayEquals(content.getBytes(), sendFileBeanMock.getContent());
        assertEquals("test.pdf", sendFileBeanMock.getFilename());
    }

    /*
     * Tests for download of remote files
     */
    @Test
    public void test_actionDownload_remote_noStreamFromDocumentWebClient() throws IOException {
        Document doc = new NetObjectFactory().createDocument("public", remoteNode, "test.pdf");
        doc.setId(42);
        NetObject netObject = new NetObjectImpl(doc, remoteNode);
        webClientMock.setBehaviour(() -> null);

        bean.actionDownload(netObject);

        assertNoFileSent();
    }

    @Test
    public void test_actionDownload_remote_successfulDownload() throws IOException {
        Document doc = new NetObjectFactory().createDocument("public", remoteNode, "test.pdf");
        doc.setId(42);
        NetObject netObject = new NetObjectImpl(doc, remoteNode);
        String content = "Hello World";
        webClientMock.setBehaviour(() -> IOUtils.toInputStream(content));

        bean.actionDownload(netObject);

        assertArrayEquals(content.getBytes(), sendFileBeanMock.getContent());
        assertEquals("test.pdf", sendFileBeanMock.getFilename());
    }

    private void assertNoFileSent() {
        assertNull(sendFileBeanMock.getContent());
        assertNull(sendFileBeanMock.getFilename());
    }

    private FileObject createFileObject(String location) {
        FileObject fO = new FileObject();
        fO.setCollection(readableCollection);
        fO.setCreated(new Date());
        fO.setDocument_language("en");
        fO.setFileLocation(location);
        fO.setName(location);
        fO.setUser(publicUser);
        return fO;
    }

    private String createTempFile(String content) throws IOException {
        File tmpFile = File.createTempFile("DocumentDownloadBeanTest", ".tmp");
        tmpFile.deleteOnExit();
        FileWriter writer = new FileWriter(tmpFile);
        writer.write(content);
        writer.close();
        return tmpFile.getAbsolutePath();
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("DocumentDownloadBeanTest.war")
                .addClass(NodeService.class)
                .addClass(FileEntityService.class)
                .addClass(CollectionService.class)
                .addClass(ACListService.class)
                .addClass(MembershipService.class)
                .addClass(MemberService.class)
                .addClass(DocumentWebClientMock.class)
                .addClass(CloudNodeService.class)
                .addClass(CloudService.class)
                .addClass(SendFileBeanMock.class)
                .addClass(DocumentDownloadBean.class);
        return UserBeanDeployment.add(deployment);
    }
}
