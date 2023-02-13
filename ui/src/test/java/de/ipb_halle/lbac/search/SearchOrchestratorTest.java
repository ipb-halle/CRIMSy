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

import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.entity.Cloud;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.exp.ExpRecordService;
import de.ipb_halle.lbac.exp.ExperimentDeployment;
import de.ipb_halle.lbac.exp.ExperimentService;
import de.ipb_halle.lbac.exp.assay.AssayService;
import de.ipb_halle.lbac.exp.text.TextService;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.items.service.ArticleService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyNestingService;
import de.ipb_halle.lbac.material.biomaterial.TaxonomyService;
import de.ipb_halle.lbac.material.biomaterial.TissueService;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.bean.SearchState;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.search.mocks.SearchWebClientMock;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.service.CloudNodeService;
import de.ipb_halle.lbac.service.CloudService;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import jakarta.inject.Inject;
import static org.awaitility.Awaitility.await;
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
public class SearchOrchestratorTest extends TestBase {

    @Inject
    private CloudNodeService cloudNodeService;

    @Inject
    private CloudService cloudService;

    @Inject
    private SearchOrchestrator orchestrator;

    private Node node;

    @BeforeEach
    public void init() {
        cleanAllProjectsFromDb();
        entityManagerService.doSqlUpdate("DELETE FROM nodes WHERE local=false AND publicnode=false");

        node = createRemoteNode("Test-Institute");
        nodeService.save(node);

        Cloud cloud = cloudService.load().get(0);
        cloudNodeService.save(new CloudNode(cloud, node));
    }

    @AfterEach
    public void cleanUp() {
        entityManagerService.doSqlUpdate(
                String.format(
                        "DELETE FROM nodes WHERE id=Cast('%s' AS UUID)",
                        node.getId()));

    }

    @Test
    public void test001_startRemoteSearch() throws InterruptedException {
        SearchWebClientMock clientMock = new SearchWebClientMock(node, 1000);
        SearchState searchState = new SearchState();
        orchestrator.setSearchWebClient(clientMock);
        orchestrator.startRemoteSearch(searchState, null, new ArrayList<>());
        Assert.assertTrue(searchState.isSearchActive());
        await().atMost(5, TimeUnit.SECONDS).until(() -> !searchState.isSearchActive());

        Assert.assertFalse(searchState.isSearchActive());
        Assert.assertEquals(1, searchState.getFoundObjects().size());
    }

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("SearchService.war")
                .addClass(SearchService.class)
                .addClass(ProjectService.class)
                .addClass(ArticleService.class)
                .addClass(TaxonomyService.class)
                .addClass(TissueService.class)
                .addClass(DocumentSearchService.class)
                .addClass(TermVectorEntityService.class)
                .addClass(CollectionService.class)
                .addClass(FileEntityService.class)
                .addClass(ExperimentService.class)
                .addClass(ExpRecordService.class)
                .addClass(SearchOrchestrator.class)
                .addClass(SearchWebClient.class)
                .addClass(AssayService.class)
                .addClass(TextService.class)
                .addClass(TaxonomyNestingService.class);
        return ExperimentDeployment.add(ItemDeployment.add(UserBeanDeployment.add(deployment)));
    }

    private Node createRemoteNode(String institute) {
        Node node = new Node();
        node.setId(UUID.randomUUID());
        node.setBaseUrl("");
        node.setInstitution(institute);
        node.setLocal(false);
        node.setPublicNode(false);
        node.setVersion("1.0");
        return node;
    }

}
