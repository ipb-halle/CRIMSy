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
package de.ipb_halle.lbac.service;

import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.announcement.membership.MembershipOrchestrator;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.cloud.solr.SolrAdminService;
import de.ipb_halle.lbac.collections.CollectionBean;
import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.ACPermission;
import de.ipb_halle.lbac.entity.Collection;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.collections.CollectionOrchestrator;
import de.ipb_halle.lbac.collections.CollectionSearchState;
import de.ipb_halle.lbac.search.termvector.SolrTermVectorSearch;
import de.ipb_halle.lbac.collections.CollectionWebClient;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.search.SolrSearcher;
import de.ipb_halle.lbac.search.document.DocumentSearchBean;
import de.ipb_halle.lbac.search.document.DocumentSearchOrchestrator;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.webservice.Updater;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.transaction.UserTransaction;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.junit.Assert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * This class will provide some test cases for the CollectionService class.
 */
@RunWith(Arquillian.class)
public class CollectionServiceTest extends TestBase {

    private final static UUID TEST_COLLECTION_PUBLIC_ID = UUID.fromString("34bffdef-f488-4931-bc70-49e60894c1be");
    private final static String TEST_COLLECTION_PUBLIC_NAME = "public";

    @Inject
    private CollectionService collectionService;

    @Inject
    private NodeService nodeService;

    @Resource
    private UserTransaction userTransaction;

    @Inject
    private MemberService memberService;

    @Inject
    private MembershipService memberShipService;

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("CollectionServiceTest.war")
                .addClass(KeyManager.class)
                .addClass(CollectionService.class)
                .addClass(CollectionOrchestrator.class)
                .addClass(CollectionWebClient.class)
                .addClass(Updater.class)
                .addClass(CollectionSearchState.class)
                .addClass(ACListService.class)
                .addClass(FileEntityService.class)
                .addClass(FileService.class)
                .addClass(SolrSearcher.class)
                .addClass(SolrAdminService.class)
                .addClass(CollectionBean.class)
                .addClass(SolrTermVectorSearch.class)
                .addClass(DocumentSearchBean.class)
                .addClass(DocumentSearchService.class)
                .addClass(TermVectorEntityService.class)
                .addClass(DocumentSearchOrchestrator.class)
                .addClass(MembershipOrchestrator.class);
    }
    
    /**
     * Test the collection service
     */
    @Test
    public void testCollectionService() {

        User u = createUser(
                "testuser",
                "testuser",
                this.nodeService.getLocalNode(),
                memberService,
                memberShipService);

        ACList acl = new ACList();
        acl.setName("test");
        acl.setId(UUID.randomUUID());
        acl.addACE(u, ACPermission.values());

        Collection col = new Collection();
        col.setNode(this.nodeService.getLocalNode());
        col.setName("Test_Collection1");
        col.setDescription("Test_Collection1_Description");
        col.setId(UUID.randomUUID());
        col.setIndexPath("/doc/test.pdf");
        col.setACList(acl);
        col.setOwner(u);

        collectionService.save(col);
        HashMap<String, Object> crits = new HashMap<>();
        crits.put("name", "Test_Collection1");
        List<Collection> loadedCols = collectionService.load(crits);
        Assert.assertTrue("Size of loaded collections must greater than one", loadedCols.size()>=1);

        assertTrue(col.getACList().getPerm(ACPermission.permREAD, u));
    }

}
