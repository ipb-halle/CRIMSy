/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.forum.postings;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.announcement.membership.MembershipOrchestrator;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.cloud.solr.SolrAdminService;
import de.ipb_halle.lbac.collections.CollectionBean;
import de.ipb_halle.lbac.collections.CollectionWebService;
import de.ipb_halle.lbac.collections.PermissionEditBean;
import de.ipb_halle.lbac.collections.mock.CollectionWebServiceMock;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.forum.ForumService;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.search.SolrSearcher;
import de.ipb_halle.lbac.search.document.DocumentSearchBean;
import de.ipb_halle.lbac.search.termvector.SolrTermVectorSearch;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.search.wordcloud.WordCloudBean;
import de.ipb_halle.lbac.search.wordcloud.WordCloudWebService;
import de.ipb_halle.lbac.service.CollectionService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.service.MemberService;
import de.ipb_halle.lbac.service.MembershipService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.webclient.LbacWebClient;
import de.ipb_halle.lbac.webclient.WebRequestSignature;
import de.ipb_halle.lbac.webservice.Updater;
import de.ipb_halle.lbac.webservice.service.WebRequestAuthenticator;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class PostingWebServiceTest
        extends TestBase {

    @Inject
    protected NodeService nodeService;

    @Inject
    protected CollectionService collectionService;

    @Inject
    protected MemberService memberService;

    @Inject
    protected MembershipService membershipService;

    @Inject
    protected KeyManager keyManager;

    @Inject
    protected WebRequestAuthenticator authenticator;

    @Inject
    PostingWebService postingWebService;

    @Before
    @Override
    public void setUp() {
        super.setUp();
        initializeBaseUrl();
        initializeKeyStoreFactory();
    }

    @Test
    public void testPostingWebService() throws Exception {
        User publicUser = memberService.loadUserById(UUID.fromString(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID));
        LbacWebClient client = new LbacWebClient();
        WebRequestSignature wrs = client.createWebRequestSignature(
                keyManager.getLocalPrivateKey(TESTCLOUD)
        );

        PostingWebRequest wr = new PostingWebRequest();
        wr.setUser(publicUser);
        wr.setCloudName(TESTCLOUD);
        wr.setNodeIdOfRequest(TEST_NODE_ID);
        wr.setSignature(wrs);

        postingWebService.setAuthenticator(authenticator);
        Response resp = postingWebService.addPosting(wr);
    }

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("PostingWebServiceTest.war")
                .addClass(GlobalAdmissionContext.class)
                .addClass(KeyManager.class)
                .addClass(WebRequestAuthenticator.class)
                .addClass(CollectionWebServiceMock.class)
                .addClass(WebRequestAuthenticator.class)
                .addClass(PostingWebService.class)
                .addClass(ForumService.class)
                .addClass(PostingWebClient.class);

    }
}
