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
package de.ipb_halle.lbac.search.wordcloud;

import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.Collection;
import de.ipb_halle.lbac.entity.FileObject;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.search.SolrSearcher;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.service.ACListService;
import de.ipb_halle.lbac.service.CollectionService;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.service.CloudService;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.service.MemberService;
import de.ipb_halle.lbac.service.MembershipService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.search.wordcloud.mock.WordCloudWebServiceMock;
import de.ipb_halle.lbac.util.HexUtil;
import de.ipb_halle.lbac.util.ssl.SecureWebClientBuilder;
import de.ipb_halle.lbac.webservice.service.WebRequestAuthenticator;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class WordCloudWebServiceTest extends TestBase {

    @Inject
    private TermVectorEntityService termVectorEntityService;

    @Inject
    private WordCloudWebService instance;

    @Inject
    private KeyManager keyManager;

    @Inject
    private NodeService nodeService;

    @Inject
    private WordCloudWebClient webClient;

    @Inject
    private MembershipService membershipService;

    @Inject
    private MemberService memberService;

    @Inject
    private CollectionService collectionService;

    @Deployment
    public static WebArchive createDeployment() {

        return prepareDeployment("WordCloudWebServiceTest.war")
                .addClass(DocumentSearchService.class)
                .addClass(FileEntityService.class)
                .addClass(SolrSearcher.class)
                .addClass(NodeService.class)
                .addClass(CloudService.class)
                .addClass(CloudNodeService.class)
                .addClass(CollectionService.class)
                .addClass(ACListService.class)
                .addClass(FileService.class)
                .addClass(MembershipService.class)
                .addClass(WordCloudWebClient.class)
                .addClass(MemberService.class)
                .addClass(TermVectorEntityService.class)
                .addClass(KeyManager.class)
                .addClass(DocumentSearchService.class)
                .addClass(WordCloudWebService.class)
                .addClass(TermVectorEntityService.class)
                .addClass(FileEntityService.class)
                .addClass(SolrSearcher.class)
                .addClass(WebRequestAuthenticator.class)
                .addClass(WordCloudWebServiceMock.class);

    }

    @Before
    public void setUp() {
        super.setUp();
        initializeBaseUrl();
        initializeKeyStoreFactory();
    }

    @Test
    public void getDocumentsWithTermVectorTest() throws Exception {

        User user1 = createUser(
                "testUser",
                "testUser", nodeService.getLocalNode(),
                memberService,
                membershipService);

        ACList acl = createAcList(user1, true);

        List<Collection> colls = createLocalCollections(
                acl,
                nodeService.getLocalNode(),
                user1,
                "collection1",
                "collection1",
                collectionService
        );

        WordCloudWebRequest request = new WordCloudWebRequest();

        request.getIdsOfReadableCollections().add(colls.get(0).getId());
        request.getTerms().add("term1");
        request.setCloudName(TESTCLOUD);
        request.setNodeIdOfRequest(nodeService.getLocalNodeId());
        request.setSignature(
                webClient.createWebRequestSignature(
                        keyManager.getLocalPrivateKey(TESTCLOUD))
        );

        instance.getDocumentsWithTermVector(request);
    }
}
