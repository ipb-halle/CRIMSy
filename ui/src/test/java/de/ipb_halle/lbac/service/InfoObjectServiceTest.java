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

import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.MembershipService;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.admission.AdmissionSubSystemType;
import de.ipb_halle.lbac.admission.MembershipOrchestrator;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.cloud.solr.SolrAdminService;
import de.ipb_halle.lbac.collections.CollectionBean;
import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.Group;
import de.ipb_halle.lbac.entity.InfoObject;
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
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.transaction.UserTransaction;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This class will provide some test cases for the ACListService class.
 */
@RunWith(Arquillian.class)
public class InfoObjectServiceTest extends TestBase {

    private User[] users;
    private Group[] groups;
    private ACList[] aclists;
    private InfoObject[] infoEntities;

    @Inject
    private ACListService acListService;

//      @EJB    // @Inject does not work here!
//      private GlobalAdmissionContext globalAdmissionContext;
    @Inject
    private InfoObjectService infoObjectService;

    @Inject
    private MemberService memberService;

    @Inject
    private MembershipService membershipService;

    @Inject
    private NodeService nodeService;

    @Resource
    private UserTransaction userTransaction;

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("InfoObjectServiceTest.war")
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
    
    @Override
    public User createUser(String login, String name) {
        User u = new User();
        u.setLogin(login);
        u.setName(name);
        u.setNode(this.nodeService.getLocalNode());
        u.setSubSystemType(AdmissionSubSystemType.LOCAL);
        u=this.memberService.save(u);
        this.membershipService.addMembership(u, u);
        return u;
    }

    private Group createGroup(String name) {
        Group g = new Group();
        g.setName(name);
        g.setNode(this.nodeService.getLocalNode());
        g.setSubSystemType(AdmissionSubSystemType.LOCAL);
        g=this.memberService.save(g);
        this.membershipService.addMembership(g, g);
        return g;
    }

    /**
     * set up a collection of users and (nested) groups to work with during
     * tests:
     * <pre>
     *
     *  public - Admin, Alice
     *
     *  admins - Admin
     *
     * </pre>
     */
    private void createPeople() {
        User[] u = new User[3];
        u[0] = createUser("IE_admin", "IE Admin");
        u[1] = createUser("IE_alice", "IE Alice");
        u[2] = createUser("IE_bob", "IE Bob");

        Group[] g = new Group[2];
        g[0] = createGroup("IE_public");
        g[1] = createGroup("IE_admins");

        // public
        this.membershipService.addMembership(g[0], u[0]);
        this.membershipService.addMembership(g[0], u[1]);
        this.membershipService.addMembership(g[0], u[2]);

        // admins
        this.membershipService.addMembership(g[1], u[0]);

        this.users = u;
        this.groups = g;
    }

    private void createACLs() {
        this.aclists = new ACList[2];

        // admin has full access; public can READ
        ACList acl = new ACList().addACE(this.groups[0], new ACPermission[]{ACPermission.permREAD})
                .addACE(this.groups[1], new ACPermission[]{
            ACPermission.permREAD, ACPermission.permEDIT, ACPermission.permCREATE, ACPermission.permDELETE,
            ACPermission.permCHOWN, ACPermission.permGRANT, ACPermission.permSUPER});

        acl = this.acListService.save(acl);
        this.aclists[0] = acl;

        // admin has full access; public has NO permission; OWNER has READ & EDIT permission
        acl = new ACList().addACE(this.groups[1], new ACPermission[]{
            ACPermission.permREAD, ACPermission.permEDIT, ACPermission.permCREATE, ACPermission.permDELETE,
            ACPermission.permCHOWN, ACPermission.permGRANT, ACPermission.permSUPER})
                .addACE(this.acListService.getOwnerAccount(), new ACPermission[]{ACPermission.permREAD, ACPermission.permEDIT});

        acl = this.acListService.save(acl);
        this.aclists[1] = acl;

    }

    private void createIEs() {
        InfoObject ie;
        this.infoEntities = new InfoObject[2];

        ie = new InfoObject("IE_TEST_0");
        ie.setValue("public can read");
        ie.setOwner(this.users[0]);
        ie.setACList(this.aclists[0]);
        this.infoObjectService.save(ie);
        this.infoEntities[0] = ie;

        ie = new InfoObject("IE_TEST_1");
        ie.setValue("admin full, owner read / edit");
        ie.setOwner(this.users[2]);
        ie.setACList(this.aclists[1]);
        this.infoObjectService.save(ie);
        this.infoEntities[1] = ie;
    }

    /**
     * basic permission tests
     */
    @Test
    public void testPermissions() {
        createPeople();
        createACLs();
        createIEs();

        // ACL 0
        InfoObject io = this.infoObjectService.loadByKey("IE_TEST_0");
        assertTrue("testPermissions(): u1 has READ access to IE_TEST_0",
                acListService.isPermitted(ACPermission.permREAD, io, this.users[1]));

        assertFalse("testPermissions(): u1 does NOT have EDIT access to IE_TEST_0",
                this.acListService.isPermitted(ACPermission.permEDIT, this.infoEntities[0], this.users[1]));

        // ACL 1
        io = this.infoObjectService.loadByKey("IE_TEST_1");
        assertFalse("testPermissions(): u1 does NOT have READ access to IE_TEST_1",
                acListService.isPermitted(ACPermission.permREAD, io, this.users[1]));

        assertTrue("testPermissions(): u2 has EDIT access to IE_TEST_1",
                this.acListService.isPermitted(ACPermission.permEDIT, this.infoEntities[1], this.users[2]));

        io = this.infoObjectService.loadByKey("IE_TEST_1");
        assertTrue("testPermissions(): u2(OWNER) has READ access to IE_TEST_1",
                acListService.isPermitted(ACPermission.permREAD, io, this.users[2]));

    }
}
