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
package de.ipb_halle.lbac.service;

import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.MembershipService;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.admission.MembershipOrchestrator;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.cloud.solr.SolrAdminService;
import de.ipb_halle.lbac.collections.CollectionBean;
import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.ACPermission;
import de.ipb_halle.lbac.admission.Group;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.admission.User;
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
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.UUID;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * This class will provide some test cases for the ACListService class.
 */
@RunWith(Arquillian.class)
public class ACListServiceTest extends TestBase {

    private User[] users;
    private Group[] groups;
    private Integer[] aclIds;

    @Inject
    protected MembershipService membershipService;

    @Inject
    protected ACListService acListService;

    @Inject
    protected NodeService nodeService;

    @Inject
    protected CollectionService collectionService;

    @Inject
    protected MemberService memberService;

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("ACListServiceTest.war")
                .addClass(KeyManager.class)
                .addClass(CloudService.class)
                .addClass(CloudNodeService.class)
                .addClass(CollectionService.class)
                .addClass(CollectionOrchestrator.class)
                .addClass(CollectionWebClient.class)
                .addClass(Updater.class)
                .addClass(CollectionSearchState.class)
                .addClass(ACListService.class)
                .addClass(FileEntityService.class)
                .addClass(FileService.class)
                .addClass(NodeService.class)
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
     * set up a collection of users and (nested) groups to work with during
     * tests:
     * <pre>
     *
     *    Eve      Bob      Alice
     *   /        /        /
     *  public - deptA  - deptA head
     *       \
     *         - deptC - Carol
     *
     *  admins - Admin
     *
     *    deptA        deptC
     *   /            /
     *  resourceRG - resourceWG
     *
     * </pre>
     */
    private void createPeople() {
        Node localNode = nodeService.getLocalNode();
        User[] u = new User[6];
        u[0] = createUser("acl_admin", "ACL Admin");
        u[1] = createUser("acl_alice", "ACL Alice");
        u[2] = createUser("acl_bob", "ACL Bob");
        u[3] = createUser("acl_carol", "ACL Carol");
        u[4] = createUser("acl_eve", "ACL Eve");

        Group[] g = new Group[9];
        g[0] = createGroup("ACL_public", localNode, memberService, membershipService);
        g[1] = createGroup("ACL_deptA", localNode, memberService, membershipService);
        g[2] = createGroup("ACL_deptA_head", localNode, memberService, membershipService);
        g[3] = createGroup("ACL_deptC", localNode, memberService, membershipService);
        g[4] = createGroup("ACL_admins", localNode, memberService, membershipService);
        g[5] = createGroup("ACL_resourceRG", localNode, memberService, membershipService);
        g[6] = createGroup("ACL_resourceWG", localNode, memberService, membershipService);

        this.membershipService.addMembership(g[0], g[1]);
        this.membershipService.addMembership(g[1], g[2]);
        this.membershipService.addMembership(g[0], g[3]);

        this.membershipService.addMembership(g[5], g[6]);
        this.membershipService.addMembership(g[5], g[1]);
        this.membershipService.addMembership(g[6], g[3]);

        this.membershipService.addMembership(g[0], u[4]);
        this.membershipService.addMembership(g[1], u[2]);
        this.membershipService.addMembership(g[2], u[1]);
        this.membershipService.addMembership(g[3], u[3]);
        this.membershipService.addMembership(g[4], u[0]);

        this.users = u;
        this.groups = g;
    }

    private void createACLs() {
        Integer[] a = new Integer[2];

        // Admin has full access; deptA can READ,EDIT,CREATE, deptA head can also DELETE
        ACList acl = new ACList().addACE(this.users[0], new ACPermission[]{
            ACPermission.permREAD, ACPermission.permEDIT, ACPermission.permCREATE, ACPermission.permDELETE,
            ACPermission.permCHOWN, ACPermission.permGRANT, ACPermission.permSUPER})
                .addACE(this.groups[1], new ACPermission[]{
            ACPermission.permREAD, ACPermission.permEDIT, ACPermission.permCREATE})
                .addACE(this.groups[2], new ACPermission[]{
            ACPermission.permDELETE});
        acl = this.acListService.save(acl);
        a[0] = acl.getId();

        acl = new ACList().addACE(this.groups[5], new ACPermission[]{
            ACPermission.permREAD})
                .addACE(this.groups[6], new ACPermission[]{
            ACPermission.permREAD, ACPermission.permEDIT, ACPermission.permCREATE, ACPermission.permDELETE})
                .addACE(this.acListService.getOwnerAccount(), new ACPermission[]{ACPermission.permREAD});
        acl = this.acListService.save(acl);
        a[1] = acl.getId();

        this.aclIds = a;
    }

    /**
     * Test on basic Functionalities of the AcListService.
     */
    @Test
    public void testACLService() {
        String USER_NAME = "acl_someone";
        String USER_DESCRIPTION = "ACL Someone";
        String ACL_NAME = "acl_someone";

        // basic ACL creation; grants READ permission to someone
        User someone = createUser(USER_NAME, USER_DESCRIPTION);

        ACList acl = new ACList().addACE(someone, new ACPermission[]{
            ACPermission.permREAD});
        acl.setName(ACL_NAME);

        // Test for Loading the ACL List
        acl = this.acListService.save(acl);
        assertNotNull("testACLService() Id is not null", acl.getId());

        // Test of Loading the ACL List By ID
        ACList loadedAcList = this.acListService.loadById(acl.getId());
        Assert.assertEquals("Mismatch of names", ACL_NAME, loadedAcList.getName());
        assertTrue(loadedAcList.getPerm(ACPermission.permREAD, someone));
        assertFalse(loadedAcList.getPerm(ACPermission.permCREATE, someone));
        assertFalse(loadedAcList.getPerm(ACPermission.permDELETE, someone));
        assertFalse(loadedAcList.getPerm(ACPermission.permEDIT, someone));
        assertFalse(loadedAcList.getPerm(ACPermission.permGRANT, someone));
        assertFalse(loadedAcList.getPerm(ACPermission.permCHOWN, someone));
        assertFalse(loadedAcList.getPerm(ACPermission.permSUPER, someone));

        // Test of Loading the ACL List By Identity
        assertTrue(
                "Loaded ACL not equals to expected",
                this.acListService.loadExisting(loadedAcList).permEquals(acl)
        );

    }

    /**
     * basic permission tests
     */
    @Test
    public void testPermissions() {
        createPeople();
        createACLs();

        // ACL 0
        assertTrue("testPermissions() u2:Bob (deptA) is granted READ permission in ACL 0",
                this.acListService.isPermitted(ACPermission.permREAD,
                        this.acListService.loadById(this.aclIds[0]), this.users[2]));

        assertFalse("testPermissions() u2:Bob (deptA) has no DELETE permission in ACL 0",
                this.acListService.isPermitted(ACPermission.permDELETE,
                        this.acListService.loadById(this.aclIds[0]), this.users[2]));

        assertTrue("testPermissions() u1:Alice (deptA head) is granted DELETE permission in ACL 0",
                this.acListService.isPermitted(ACPermission.permDELETE,
                        this.acListService.loadById(this.aclIds[0]), this.users[1]));

        assertFalse("testPermissions() u4:Eve (public) has no permission in ACL 0",
                this.acListService.isPermitted(ACPermission.permREAD,
                        this.acListService.loadById(this.aclIds[0]), this.users[4]));

        assertTrue("testPermissions() u0:Admin is granted SUPER permission in ACL 0",
                this.acListService.isPermitted(ACPermission.permSUPER,
                        this.acListService.loadById(this.aclIds[0]), this.users[0]));

        assertFalse("testPermissions() u1:Alice (deptA head) has no SUPER permission in ACL 0",
                this.acListService.isPermitted(ACPermission.permSUPER,
                        this.acListService.loadById(this.aclIds[0]), this.users[1]));

        // ACL 1
        assertTrue("testPermissions() u2:Bob (deptA / resourceRG) is granted READ permission in ACL 1",
                this.acListService.isPermitted(ACPermission.permREAD,
                        this.acListService.loadById(this.aclIds[1]), this.users[2]));

        assertFalse("testPermissions() u2:Bob (deptA / resourceRG) has no EDIT permission in ACL 1",
                this.acListService.isPermitted(ACPermission.permEDIT,
                        this.acListService.loadById(this.aclIds[1]), this.users[2]));

        assertTrue("testPermissions() u1:Alice (deptA head / resourceRG) is granted READ permission in ACL 1",
                this.acListService.isPermitted(ACPermission.permREAD,
                        this.acListService.loadById(this.aclIds[1]), this.users[1]));

        assertFalse("testPermissions() u1:Alice (deptA head / resourceRG) has no EDIT permission in ACL 1",
                this.acListService.isPermitted(ACPermission.permEDIT,
                        this.acListService.loadById(this.aclIds[1]), this.users[1]));

        assertTrue("testPermissions() u3:Carol (deptC / resourceRG) is granted READ permission in ACL 1",
                this.acListService.isPermitted(ACPermission.permREAD,
                        this.acListService.loadById(this.aclIds[1]), this.users[3]));

        assertTrue("testPermissions() u3:Carol (deptC / resourceRG) is granted EDIT permission in ACL 1",
                this.acListService.isPermitted(ACPermission.permEDIT,
                        this.acListService.loadById(this.aclIds[1]), this.users[3]));

        // OWNER test
        assertTrue("testPermissions() OWNER is granted READ permission in ACL 1",
                this.acListService.isPermitted(ACPermission.permREAD,
                        this.acListService.loadById(this.aclIds[1]), this.acListService.getOwnerAccount()));

    }
}
