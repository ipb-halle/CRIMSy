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
package de.ipb_halle.lbac.collections;

import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.announcement.membership.MembershipOrchestrator;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.cloud.solr.SolrAdminService;
import de.ipb_halle.lbac.collections.mock.CollectionWebServiceMock;
import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.ACPermission;
import de.ipb_halle.lbac.entity.Collection;
import de.ipb_halle.lbac.entity.Group;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.search.SolrSearcher;
import de.ipb_halle.lbac.search.document.DocumentSearchBean;
import de.ipb_halle.lbac.search.termvector.SolrTermVectorSearch;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.search.wordcloud.WordCloudBean;
import de.ipb_halle.lbac.service.ACListService;
import de.ipb_halle.lbac.service.CollectionService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.service.MemberService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.webservice.Updater;
import de.ipb_halle.lbac.webservice.service.WebRequestAuthenticator;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.inject.Inject;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class PermissionEditBeanTest extends TestBase {

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("PermissionEditBeanTest.war")
                .addPackage(CollectionBean.class.getPackage())
                .addPackage(UserBean.class.getPackage())
                .addPackage(MemberService.class.getPackage())
                .addPackage(Collection.class.getPackage())
                .addClass(KeyManager.class)
                .addPackage(GlobalAdmissionContext.class.getPackage())
                .addClass(UserBean.class)
                .addPackage(NodeService.class.getPackage())
                .addClass(CollectionOrchestrator.class)
                .addClass(Navigator.class)
                .addClass(CollectionWebClient.class)
                .addPackage(Logger.class.getPackage())
                .addClass(Updater.class)
                .addPackage(DocumentSearchBean.class.getPackage())
                .addPackage(WordCloudBean.class.getPackage())
                .addPackage(WebRequestAuthenticator.class.getPackage())
                .addClass(CollectionSearchState.class)
                .addPackage(CollectionBean.class.getPackage())
                .addPackage(SolrAdminService.class.getPackage())
                .addPackage(SolrSearcher.class.getPackage())
                .addPackage(CollectionSearchState.class.getPackage())
                .addClass(SolrTermVectorSearch.class)
                .addClass(MembershipOrchestrator.class)
                .addClass(CollectionWebServiceMock.class)
                .addClass(FileService.class)
                 .addClass(TermVectorEntityService.class)
                .addClass(FileEntityService.class);
    }

    private CollectionBean collectionBean;

    @Inject
    private CollectionService collectionService;

    @Inject
    private PermissionEditBean permissionEditBean;

    @Inject
    private ACListService acListService;

    @Inject
    CollectionSearchState collectionSerachState;

    @Inject
    CollectionOrchestrator orchestrator;

    @Before
    public void init() {
        resetDB(memberService);
    }

    @Test
    public void getACEntriesOfGroupsTest() {

        User u = createUser("testuser", "testuser");
        //Creates a second user which creates a second group. This group is not in the acList of the following collection
        User u2 = createUser("testuser2", "testuser2");
        ACList acList = createAcList(u, true);

        // Sets Permissionrights of the two build-in groups and the group of the user
        List<Group> g2 = memberService.loadGroups(new HashMap<>());
        for (int i = 0; i < 3; i++) {
            acList.addACE(g2.get(i), new ACPermission[]{ACPermission.permREAD});
        }

        List<Collection> colls = createLocalCollections(acList, nodeService.getLocalNode(), u, "test-collection", "test-collection", collectionService);
        UUID oldACListID = colls.get(0).getACList().getId();
        nodeService.getLocalNode();
        collectionBean = new CollectionBean();
        collectionBean.setCurrentAccount(u);
        collectionBean.setCollectionSearchState(collectionSerachState);
        collectionBean.setNodeService(nodeService);
        collectionBean.setCollectionOrchestrator(orchestrator);
        // collectionBean.initCollectionBean();
        collectionBean.setActiveCollection(colls.get(0));

        permissionEditBean = new PermissionEditBean();
        permissionEditBean.setMemberService(memberService);
        permissionEditBean.setCollectionBean(collectionBean);
        permissionEditBean.setAcListService(acListService);
        permissionEditBean.setCollectionService(collectionService);

        permissionEditBean.initModal();

        List<Group> g = permissionEditBean.getGroupsNotInAcList();
        /**
         * The second group must be found
         */
        //assertEquals(1, g.size());

        permissionEditBean.addGroupToAcList(g2.get(3));

        permissionEditBean.getAcEntries().get(3).setPermRead(true);

        permissionEditBean.applyChanges();

        List<Collection> loadedColls = collectionService.load(new HashMap<>());
        UUID newAcListId = loadedColls.get(0).getACList().getId();
        //assertFalse("IDs of ACLists must be different", newAcListId.equals(oldACListID));

    }

}
