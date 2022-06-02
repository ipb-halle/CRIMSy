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
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.service.CloudService;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.admission.MembershipService;
import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.admission.mock.UserBeanMock;
import de.ipb_halle.lbac.base.DocumentCreator;
import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.collections.CollectionBean;
import de.ipb_halle.lbac.collections.CollectionOrchestrator;
import de.ipb_halle.lbac.collections.CollectionWebClient;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.search.wordcloud.mock.WordCloudWebServiceMock;
import de.ipb_halle.lbac.webservice.service.WebRequestAuthenticator;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.faces.component.UIViewRoot;
import javax.faces.component.behavior.BehaviorBase;
import javax.inject.Inject;
import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.tagcloud.TagCloudItem;

/**
 *
 * @author fmauz
 */
@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class WordCloudBeanTest extends TestBase {

    @Inject
    private WordCloudBean wordCloudBean;

    @Inject
    private UserBeanMock userBean;

    @Inject
    private CollectionBean collectionBean;

    @BeforeEach
    public void before() throws IOException {
        initializeBaseUrl();
        initializeKeyStoreFactory();
        FileUtils.deleteDirectory(Paths.get("target/test-classes/collections").toFile());
        entityManagerService.doSqlUpdate("DELETE FROM collections");
        entityManagerService.doSqlUpdate("DELETE from unstemmed_words");
        entityManagerService.doSqlUpdate("DELETE from termvectors");
        entityManagerService.doSqlUpdate("DELETE from files");
        publicUser = memberService.loadUserById(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID);
        userBean.setCurrentAccount(publicUser);

        DocumentCreator documentCreator = new DocumentCreator(
                fileEntityService,
                collectionService,
                nodeService,
                termVectorEntityService);

        try {
            Collection col = documentCreator.uploadDocuments(
                    publicUser,
                    "WordCloudBeanTestCollection",
                    "Document1.pdf",
                    "Document2.pdf",
                    "Document18.pdf");
            collectionBean.getCollectionSearchState().addCollections(Arrays.asList(col));
        } catch (FileNotFoundException | InterruptedException ex) {
            throw new RuntimeException("Could not upload file");
        }
        Assert.assertNotNull(wordCloudBean);

    }

    @Test
    public void test001_startSearch() {
        wordCloudBean.setSearchTermInput("Java");
        wordCloudBean.startSearch();

        Assert.assertEquals(5, wordCloudBean.getModel().getTags().size());
        Assert.assertEquals(3, wordCloudBean.getDocSeachState().getFoundDocuments().size());

        Assert.assertEquals("java", wordCloudBean.getTagsAsStringforBadges());
    }

    @Test
    public void test002_clearCloudState() {
        wordCloudBean.setSearchTermInput("Java");
        wordCloudBean.startSearch();

        Assert.assertEquals(5, wordCloudBean.getModel().getTags().size());
        Assert.assertEquals(3, wordCloudBean.getDocSeachState().getFoundDocuments().size());

        wordCloudBean.clearCloudState();

        Assert.assertEquals(0, wordCloudBean.getModel().getTags().size());
        Assert.assertEquals(0, wordCloudBean.getDocSeachState().getFoundDocuments().size());
        Assert.assertFalse(wordCloudBean.isWordCloudVisible());

        Assert.assertEquals("", wordCloudBean.getTagsAsStringforBadges());
        Assert.assertEquals("", wordCloudBean.getSearchTermInput());
        Assert.assertEquals(0, wordCloudBean.getTagList().size());
        Assert.assertEquals(0, wordCloudBean.getTagsAsList().size());
    }

    @Test
    public void test003_selectWordInCloud() {
        wordCloudBean.setSearchTermInput("Java");
        wordCloudBean.startSearch();

        Assert.assertEquals(5, wordCloudBean.getModel().getTags().size());
        Assert.assertEquals(3, wordCloudBean.getDocSeachState().getFoundDocuments().size());

        wordCloudBean.onSelect(new SelectEventMock(new TagCloudItemMock("tiny")));

        Assert.assertEquals(1, wordCloudBean.getDocSeachState().getFoundDocuments().size());

    }

    @Deployment
    public static WebArchive createDeployment() {
        return UserBeanDeployment.add(prepareDeployment("WordCloudBeanTest.war")
                .addClass(DocumentSearchService.class)
                .addClass(WordCloudBean.class)
                .addClass(FileEntityService.class)
                .addClass(NodeService.class)
                .addClass(CloudService.class)
                .addClass(CloudNodeService.class)
                .addClass(CollectionService.class)
                .addClass(ACListService.class)
                .addClass(FileService.class)
                .addClass(MembershipService.class)
                .addClass(WordCloudWebClient.class)
                .addClass(MemberService.class)
                .addClass(CollectionService.class)
                .addClass(CollectionBean.class)
                .addClass(CollectionWebClient.class)
                .addClass(CollectionOrchestrator.class)
                .addClass(TermVectorEntityService.class)
                .addClass(KeyManager.class)
                .addClass(DocumentSearchService.class)
                .addClass(WordCloudWebService.class)
                .addClass(TermVectorEntityService.class)
                .addClass(FileEntityService.class)
                .addClass(WebRequestAuthenticator.class)
                .addClass(WordCloudWebServiceMock.class));

    }

    public class TagCloudItemMock implements TagCloudItem {

        private String text;

        public TagCloudItemMock(String text) {
            this.text = text;
        }

        @Override
        public String getLabel() {
            return text;
        }

        @Override
        public String getUrl() {
            return null;
        }

        @Override
        public int getStrength() {
            return 1;
        }
    }

    public class SelectEventMock extends SelectEvent {

        public SelectEventMock(Object object) {
            super(new UIViewRoot(),
                    new BehaviorBase(),
                    object);
        }

    }
}
