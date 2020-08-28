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
package de.ipb_halle.lbac.search.wordcloud;

import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.Document;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.search.wordcloud.mock.WordCloudWebServiceMock;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.util.ssl.SecureWebClientBuilder;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
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
public class WordCloudWebClientTest extends TestBase {

    @Inject
    public WordCloudWebClient instance;

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("WordCloudWebClientTest.war")
                .addClass(CloudNodeService.class)
                .addClass(WordCloudWebClient.class)
                .addClass(KeyManager.class)
                .addClass(WordCloudWebServiceMock.class);

    }

    @Before
    public void setUp() {
        super.setUp();
        initializeBaseUrl();
        initializeKeyStoreFactory();
    }

    @Test
    @RunAsClient
    public void getWordCloudResponseTest() {
        User u = new User();

        Node n = nodeService.getLocalNode();
        Set<String> tags = new HashSet<>();
        tags.add("tag-1");
        tags.add("tag-2");
        CloudNode cn = cloudNodeService.loadCloudNode(TESTCLOUD, TEST_NODE_ID);
        WordCloudWebRequest request = instance.getWordCloudResponse(u, cn, tags, new HashSet<>());

        Assert.assertNotNull(request);
        Assert.assertTrue(!request.getDocumentsWithTerms().isEmpty());
        Document d = request.getDocumentsWithTerms().get(0);
        Assert.assertEquals("de", d.getLanguage());
        Assert.assertNotNull(d.getTermFreqList());
        Assert.assertEquals(4, d.getTermFreqList().getTermFreq().size());

    }
}
