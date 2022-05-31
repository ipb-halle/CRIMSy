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
import static org.junit.Assert.assertNull;
import java.io.IOException;
import java.io.InputStream;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.io.IOUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.search.bean.NetObjectFactory;
import de.ipb_halle.lbac.search.document.Document;
import de.ipb_halle.lbac.search.document.download.mocks.DocumentWebServiceMock;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.CloudService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;

/**
 * @author flange
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class DocumentWebClientTest extends TestBase {
    @Inject
    private DocumentWebServiceMock mockEndpoint;

    @Inject
    private DocumentWebClient client;

    private Node localNode;
    private CloudNode cn;

    @BeforeEach
    public void before() {
        mockEndpoint.setBehaviour(null);

        initializeBaseUrl();
        initializeKeyStoreFactory();

        localNode = nodeService.getLocalNode();
        cn = cloudNodeService.loadCloudNode(TESTCLOUD, TEST_NODE_ID);
    }

    @Test
    @RunAsClient
    public void test_downloadDocument_documentWebServiceReturnsError() {
        mockEndpoint.setBehaviour((request) -> Response.status(Status.NOT_FOUND).build());
        Document doc = new NetObjectFactory().createDocument("public", localNode, "test.pdf");
        doc.setId(42);

        assertNull(client.downloadDocument(cn, publicUser, doc));
    }

    @Test
    @RunAsClient
    public void test_downloadDocument_requestHasCorrectFileObjectId_and_requestIsSigned_return_successfulDownload()
            throws IOException {
        String responseText = "Hello World";
        int expectedId = 42;
        mockEndpoint.setBehaviour((request) -> {
            if ((request.getFileObjectId() == expectedId) && (request.getSignature() != null)) {
                return Response.ok(IOUtils.toInputStream(responseText), MediaType.APPLICATION_OCTET_STREAM).build();
            } else {
                return null;
            }
        });
        Document doc = new NetObjectFactory().createDocument("public", localNode, "test.pdf");
        doc.setId(expectedId);

        InputStream received = client.downloadDocument(cn, publicUser, doc);

        assertArrayEquals(responseText.getBytes(), IOUtils.toByteArray(received));
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("DocumentWebClientTest.war")
                .addClass(NodeService.class)
                .addClass(KeyManager.class)
                .addClass(CloudNodeService.class)
                .addClass(CloudService.class)
                .addClass(DocumentWebServiceMock.class)
                .addClass(DocumentWebClient.class);
        return UserBeanDeployment.add(deployment);
    }
}