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
import de.ipb_halle.lbac.admission.MembershipOrchestrator;
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
import de.ipb_halle.lbac.service.MembershipService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.webservice.Updater;
import de.ipb_halle.lbac.webservice.service.WebRequestAuthenticator;
import java.util.List;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;
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
public class CollectionPermissionAnalyserTest extends TestBase {

    CollectionPermissionAnalyser instance;

    @Inject
    MembershipService membershipService;

    @Inject
    MemberService memberService;

    @Inject
    NodeService nodeService;

    @Inject
    CollectionService collectionService;

    @Inject
    ACListService acListService;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("CollectionPermissionAnalyserTest.war")
                .addPackage(CollectionBean.class.getPackage())
                .addPackage(UserBean.class.getPackage())
                .addPackage(MemberService.class.getPackage())
                .addPackage(Collection.class.getPackage())
                .addClass(FileService.class)
                .addClass(FileEntityService.class)
                .addClass(KeyManager.class)
                .addPackage(GlobalAdmissionContext.class.getPackage())
                .addClass(UserBean.class)
                .addClass(Navigator.class)
                .addPackage(CollectionBean.class.getPackage())
                .addPackage(WebRequestAuthenticator.class.getPackage())
                .addPackage(NodeService.class.getPackage())
                .addClass(CollectionOrchestrator.class)
                .addClass(CollectionWebClient.class)
                .addPackage(Logger.class.getPackage())
                .addPackage(DocumentSearchBean.class.getPackage())
                .addPackage(WordCloudBean.class.getPackage())
                .addClass(Updater.class)
                .addClass(TermVectorEntityService.class)
                .addClass(SolrTermVectorSearch.class)
                .addClass(CollectionSearchState.class)
                .addPackage(SolrAdminService.class.getPackage())
                .addPackage(SolrSearcher.class.getPackage())
                .addClass(MembershipOrchestrator.class)
                .addClass(CollectionWebServiceMock.class)
                .addPackage(CollectionSearchState.class.getPackage());

    }

    @Before
    public void init() {
        resetDB(memberService);
    }

    @Test
    public void isEditAllowedTest() {

        instance = new CollectionPermissionAnalyser("public", acListService);

        User u = createUser("ownerOfColl", "ownerOfColl");
        User u2 = createUser("testuser", "testuser");
        ACList acList = createAcList(u, true);
        List<Collection> colls = createLocalCollections(
                acList,
                nodeService.getLocalNode(),
                u, "test-collection",
                "test-collection",
                collectionService
        );
        Collection coll = colls.get(0);

        Assert.assertTrue(
                "User 2 must not have permissionright EDIT",
                !instance.isPermissionEditAllowed(coll, u2));

        ACList acListWithEdit = createAcList(u, new ACPermission[]{ACPermission.permEDIT});
        Group g = createGroup("group for edit", nodeService.getLocalNode(), memberService, membershipService);
        membershipService.addMembership(g, u2);
        acListWithEdit.addACE(g, new ACPermission[]{ACPermission.permEDIT});
        coll.setACList(acListWithEdit);
        acListService.save(acListWithEdit);
        collectionService.save(coll);

        Assert.assertTrue(
                "User 2 should have permissionright EDIT",
                instance.isEditAllowed(coll, u2));

        Collection publicCol = createLocalCollections(acListWithEdit, nodeService.getLocalNode(), u, "public", "public", collectionService).get(0);

        Assert.assertTrue(
                "User 2 must not have permissionright EDIT on public group",
                !instance.isEditAllowed(publicCol, u2));

    }

    @Test
    public void isDeleteAllowedTest() {

    }

    @Test
    public void isReindexingAllowedTest() {

    }

    @Test
    public void isPermissionEditAllowed() {

    }

}
