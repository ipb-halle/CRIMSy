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
package de.ipb_halle.lbac.search;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.TESTCLOUD;
import static de.ipb_halle.lbac.base.TestBase.TEST_NODE_ID;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.exp.ExpRecordService;
import de.ipb_halle.lbac.exp.ExperimentDeployment;
import de.ipb_halle.lbac.exp.ExperimentService;
import de.ipb_halle.lbac.exp.assay.AssayService;
import de.ipb_halle.lbac.exp.text.TextService;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.RemoteItem;
import de.ipb_halle.lbac.items.search.ItemSearchRequestBuilder;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.material.common.search.MaterialSearchRequestBuilder;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.webclient.LbacWebClient;
import de.ipb_halle.lbac.webclient.WebRequestSignature;
import de.ipb_halle.lbac.webservice.service.WebRequestAuthenticator;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
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
public class SearchWebServiceTest extends TestBase {

    @Inject
    protected KeyManager keyManager;

    @Inject
    protected WebRequestAuthenticator authenticator;

    @Inject
    protected SearchWebService webService;

    @Inject
    private ProjectService projectService;

    private int publicUserId;
    private int publicAclId;
    private Project project;

    @BeforeEach
    public void init() {
        initializeKeyStoreFactory();
        try {
            CloudNode cn = cloudNodeService.loadCloudNode(TESTCLOUD, TEST_NODE_ID);
            cn.setPublicKey(Base64.getEncoder().encodeToString(keyManager.getLocalPublicKey(TESTCLOUD).getEncoded()));
            cn = cloudNodeService.save(cn);
        } catch (Exception e) {

        }

        creationTools = new CreationTools("", "", "", memberService, projectService);
        publicUserId = context.getPublicAccount().getId();
        publicAclId = GlobalAdmissionContext.getPublicReadACL().getId();
    }

    @AfterEach
    public void cleanUp() {
        cleanItemsFromDb();
        cleanMaterialsFromDB();
        if (project != null) {
            cleanProjectFromDB(project, false);
            project = null;
        }
    }

    @Test
    public void test001_searchWithEmptyRequest() throws Exception {
        SearchWebRequest wr = createEmptyRequest();
        Response response = webService.search(wr);
        SearchWebResponse searchResponse = (SearchWebResponse) response.getEntity();

        Assert.assertTrue(searchResponse.getAllFoundObjects().isEmpty());
    }

    @Test
    public void test002_searchWithUnauthorisedRequest_NodeIdNull() throws Exception {
        SearchWebRequest wr = createEmptyRequest();
        wr.setNodeIdOfRequest(null);
        Response response = webService.search(wr);
        SearchWebResponse searchResponse = (SearchWebResponse) response.getEntity();
        Assert.assertTrue(searchResponse.getAllFoundObjects().isEmpty());
        Assert.assertEquals("401:Nodeid of webrequest was null", searchResponse.getStatusCode());
    }

    @Test
    public void test003_searchWithUnauthorisedRequest_wrongCryptedMessage() throws Exception {
        SearchWebRequest wr = createEmptyRequest();
        wr.getSignature().setCryptedMessage("WRONG CRYPTED MESSAGE");
        Response response = webService.search(wr);
        SearchWebResponse searchResponse = (SearchWebResponse) response.getEntity();
        Assert.assertTrue(searchResponse.getAllFoundObjects().isEmpty());
        Assert.assertEquals(String.format("401:Could note authentificate request from node %s", TEST_NODE_ID), searchResponse.getStatusCode());
    }

    @Test
    public void test004_searchWithUnauthorisedRequest_wrongClearMessage() throws Exception {
        SearchWebRequest wr = createEmptyRequest();
        wr.getSignature().setDecryptedMessage("WRONG CLEAR MESSAGE");
        Response response = webService.search(wr);
        SearchWebResponse searchResponse = (SearchWebResponse) response.getEntity();
        Assert.assertTrue(searchResponse.getAllFoundObjects().isEmpty());
        Assert.assertEquals(String.format("401:Could note authentificate request from node %s", TEST_NODE_ID), searchResponse.getStatusCode());
    }

    @Test
    public void test004_searchWithItemRequest() throws Exception {
        project = creationTools.createAndSaveProject("SearchWebServiceTest-Test");
        int materilid = materialCreator.createStructure(
                publicUserId,
                publicAclId,
                project.getId(),
                "SearchWebServiceTest-Material");
        int itemid = itemCreator.createItem(publicUserId, publicAclId, materilid, "SearchWebServiceTest-Item",project.getId());

        SearchWebRequest wr = createEmptyRequest();
        ItemSearchRequestBuilder builder = new ItemSearchRequestBuilder(
                memberService.loadUserById(publicUserId),0,25);

        wr.addRequest(Arrays.asList(((SearchRequestImpl) builder.build())));
        Response response = webService.search(wr);
        SearchWebResponse searchResponse = (SearchWebResponse) response.getEntity();

        List<Searchable> items = searchResponse.getAllFoundObjects();
        Assert.assertEquals(1, items.size());
        Assert.assertEquals(itemid, ((RemoteItem) items.get(0)).getId(), 0);
    }

    @Test
    public void test004_searchWithMaterialRequest() throws Exception {
        project = creationTools.createAndSaveProject("SearchWebServiceTest-Test004");
        materialCreator.createStructure(
                publicUserId,
                publicAclId,
                project.getId(),
                "SearchWebServiceTest-Structure");

        SearchWebRequest wr = createEmptyRequest();
        MaterialSearchRequestBuilder builder = new MaterialSearchRequestBuilder(context.getPublicAccount(), 0, 25);
        wr.addRequest(Arrays.asList(((SearchRequestImpl) builder.build())));
        Response response = webService.search(wr);
        SearchWebResponse searchResponse = (SearchWebResponse) response.getEntity();
        Assert.assertEquals(1, searchResponse.getAllFoundObjects().size());

    }

    private SearchWebRequest createEmptyRequest() throws Exception {
        LbacWebClient client = new LbacWebClient();
        WebRequestSignature wrs = client.createWebRequestSignature(
                keyManager.getLocalPrivateKey(TESTCLOUD)
        );
        SearchWebRequest wr = new SearchWebRequest();
        wr.setUser(context.getPublicAccount());
        wr.setCloudName(TESTCLOUD);
        wr.setNodeIdOfRequest(TEST_NODE_ID);
        wr.setSignature(wrs);
        return wr;
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("SearchWebServiceTest.war")
                .addClass(SearchService.class)
                .addClass(DocumentSearchService.class)
                .addClass(TermVectorEntityService.class)
                .addClass(CollectionService.class)
                .addClass(FileEntityService.class)
                .addClass(SearchWebService.class)
                .addClass(WebRequestAuthenticator.class);
        return ExperimentDeployment.add(ItemDeployment.add(UserBeanDeployment.add(deployment)));
    }

}
