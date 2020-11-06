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
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.exp.ExpRecordService;
import de.ipb_halle.lbac.exp.ExperimentService;
import de.ipb_halle.lbac.exp.assay.AssayService;
import de.ipb_halle.lbac.exp.text.TextService;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.search.ItemSearchRequestBuilder;
import de.ipb_halle.lbac.material.CreationTools;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.webclient.LbacWebClient;
import de.ipb_halle.lbac.webclient.WebRequestSignature;
import de.ipb_halle.lbac.webservice.service.WebRequestAuthenticator;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
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

    @Before
    @Override
    public void setUp() {
        super.setUp();
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

    @Test
    public void test001_searchWithEmptyRequest() throws Exception {
        SearchWebRequest wr = createEmptyRequest();
        Response response = webService.search(wr);
        SearchWebResponse searchResponse = (SearchWebResponse) response.getEntity();
        Assert.assertTrue(searchResponse.getSearchResult().getAllFoundObjects().isEmpty());
    }

    @Test
    public void test002_searchWithUnauthorisedRequest_NodeIdNull() throws Exception {
        SearchWebRequest wr = createEmptyRequest();
        wr.setNodeIdOfRequest(null);
        Response response = webService.search(wr);
        SearchWebResponse searchResponse = (SearchWebResponse) response.getEntity();
        Assert.assertNull(searchResponse.getSearchResult());
        Assert.assertEquals("401:Nodeid of webrequest was null", searchResponse.getStatusCode());
    }

    @Test
    public void test003_searchWithUnauthorisedRequest_wrongCryptedMessage() throws Exception {
        SearchWebRequest wr = createEmptyRequest();
        wr.getSignature().setCryptedMessage("WRONG CRYPTED MESSAGE");
        Response response = webService.search(wr);
        SearchWebResponse searchResponse = (SearchWebResponse) response.getEntity();
        Assert.assertNull(searchResponse.getSearchResult());
        Assert.assertEquals(String.format("401:Could note authentificate request from node %s", TEST_NODE_ID), searchResponse.getStatusCode());
    }

    @Test
    public void test004_searchWithUnauthorisedRequest_wrongClearMessage() throws Exception {
        SearchWebRequest wr = createEmptyRequest();
        wr.getSignature().setDecryptedMessage("WRONG CLEAR MESSAGE");
        Response response = webService.search(wr);
        SearchWebResponse searchResponse = (SearchWebResponse) response.getEntity();
        Assert.assertNull(searchResponse.getSearchResult());
        Assert.assertEquals(String.format("401:Could note authentificate request from node %s", TEST_NODE_ID), searchResponse.getStatusCode());
    }

    @Test
    public void test004_searchWithItemRequest() throws Exception {
        Project project = creationTools.createAndSaveProject("SearchWebServiceTest-Test");
        int materilid = materialCreator.createStructure(
                publicUserId,
                publicAclId,
                project.getId(),
                "SearchWebServiceTest-Material");
        int itemid = itemCreator.createItem(publicUserId, publicAclId, materilid, "SearchWebServiceTest-Item");

        SearchWebRequest wr = createEmptyRequest();
        ItemSearchRequestBuilder builder = new ItemSearchRequestBuilder(context.getPublicAccount(), 0, 25);

        wr.addRequest(Arrays.asList(((SearchRequestImpl) builder.buildSearchRequest())));
        Response response = webService.search(wr);
        SearchWebResponse searchResponse = (SearchWebResponse) response.getEntity();
        Node node = searchResponse.getSearchResult().getNode();
        List<Item> items = searchResponse.getSearchResult().getAllFoundObjects(Item.class, node);
        Assert.assertEquals(1, items.size());
        Assert.assertEquals(itemid, items.get(0).getId(), 0);
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
                .addClass(ExperimentService.class)
                .addClass(SearchWebService.class)
                .addClass(WebRequestAuthenticator.class)
                .addClass(ExpRecordService.class)
                .addClass(AssayService.class)
                .addClass(TextService.class);
        return ItemDeployment.add(UserBeanDeployment.add(deployment));
    }

}
