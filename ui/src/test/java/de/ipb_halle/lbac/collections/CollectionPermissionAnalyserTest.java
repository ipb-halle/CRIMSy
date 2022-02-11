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
package de.ipb_halle.lbac.collections;

import de.ipb_halle.lbac.admission.UserBeanDeployment;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.collections.mock.CollectionWebServiceMock;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.Group;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.exp.ExperimentDeployment;
import de.ipb_halle.lbac.exp.ExperimentService;
import de.ipb_halle.lbac.items.ItemDeployment;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.SearchService;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.webservice.Updater;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.List;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
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
public class CollectionPermissionAnalyserTest extends TestBase {

    CollectionPermissionAnalyser instance;

    @Inject
    ACListService acListService;

    @PersistenceContext(name = "de.ipb_halle.lbac")
    private EntityManager em;

    @Deployment
    public static WebArchive createDeployment() {
        WebArchive deployment = prepareDeployment("CollectionPermissionAnalyserTest.war")
                .addClass(FileService.class)
                .addClass(FileEntityService.class)
                .addClass(Navigator.class)
                .addClass(CollectionOrchestrator.class)
                .addClass(CollectionWebClient.class)
                .addClass(Updater.class)
                .addClass(TermVectorEntityService.class)
                .addClass(SearchService.class)
                .addClass(ProjectService.class)
                .addClass(ExperimentService.class)
                .addClass(DocumentSearchService.class)
                .addClass(CollectionSearchState.class)
                .addClass(CollectionWebServiceMock.class);
        return  ExperimentDeployment.add(ItemDeployment.add(UserBeanDeployment.add(deployment)));

    }

    @BeforeEach
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
}
