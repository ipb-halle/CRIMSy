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
package de.ipb_halle.lbac.admission;

import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.search.termvector.TermVectorEntityService;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.collections.CollectionBean;
import de.ipb_halle.lbac.collections.CollectionOrchestrator;
import de.ipb_halle.lbac.collections.CollectionSearchState;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.collections.CollectionWebClient;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.webservice.Updater;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ejb.EJBException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import org.junit.Assert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * This class will provide some test cases for the MemberService and
 * MembershipService classes.
 */
@RunWith(Arquillian.class)
public class MemberServiceTest extends TestBase {

    @Inject
    private NodeService nodeService;

    @Inject
    private MemberService memberService;

    @Inject
    private MembershipService membershipService;

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("MemberServiceTest.war")
                .addClass(KeyManager.class)
                .addClass(CollectionService.class)
                .addClass(CollectionOrchestrator.class)
                .addClass(CollectionWebClient.class)
                .addClass(Updater.class)
                .addClass(CollectionSearchState.class)
                .addClass(ACListService.class)
                .addClass(FileEntityService.class)
                .addClass(FileService.class)
                .addClass(CollectionBean.class)
                .addClass(DocumentSearchService.class)
                .addClass(TermVectorEntityService.class)
                .addClass(MembershipOrchestrator.class);
    }

    /**
     * basic user creation test
     */
    @Test
    public void testMemberService() {
        String name = "John Doe";

        User u = createUser("jdoe", name);

        User v = this.memberService.loadUserById(u.getId());

        assertEquals("testMemberService(): name mismatch ",
                name, v.getName());
    }

    /**
     * basic group creation test
     */
    @Test
    public void testMembershipService() {
        User u = createUser("joe", "Joe");
        this.membershipService.addMembership(u, u);

        Group g = createGroup("MyTestGroup1", nodeService.getLocalNode(), memberService, membershipService);

        this.membershipService.addMembership(g, g);
        this.membershipService.addMembership(g, u);

        assertEquals("testMembershipService() group size mismatch", 2,
                this.membershipService.loadMembers(g).size());

    }

    /**
     * perform group size assertions
     */
    private void testNestedGroupsAssertions(Group g[], int s[]) {
        for (int i = 0; i < g.length; i++) {
            assertEquals(String.format("testNestedGroups() group_%d size mismatch", i),
                    s[i], this.membershipService.loadMembers(g[i]).size());
        }
    }

    /**
     * Creation of nested groups. This test creates the following initial group
     * schema:
     * <pre>
     *        1 - 3 - 5 - 7 - 9
     *       /       /
     *      O       /
     *       \     /
     *        2 - 4 - 6
     *             \
     *              8
     * </pre> In the course of the test, an additional membership (2,5) is
     * created. Afterwards this membership [(2,5)] is deleted followed by
     * deletion of membership (1,3). The (non-)existence of nested memberships,
     * the number of group mebers and the number of nesting paths is monitored.
     */
    @Test
    public void testNestedGroups() {
        User u = createUser("njoe", "Nested Joe");
        this.membershipService.addMembership(u, u);

        Group[] g = new Group[10];
        g[0] = createGroup("Grp0", nodeService.getLocalNode(), memberService, membershipService);
        g[1] = createGroup("Grp1", nodeService.getLocalNode(), memberService, membershipService);
        g[2] = createGroup("Grp2", nodeService.getLocalNode(), memberService, membershipService);
        g[3] = createGroup("Grp3", nodeService.getLocalNode(), memberService, membershipService);
        g[4] = createGroup("Grp4", nodeService.getLocalNode(), memberService, membershipService);
        g[5] = createGroup("Grp5", nodeService.getLocalNode(), memberService, membershipService);
        g[6] = createGroup("Grp6", nodeService.getLocalNode(), memberService, membershipService);
        g[7] = createGroup("Grp7", nodeService.getLocalNode(), memberService, membershipService);
        g[8] = createGroup("Grp8", nodeService.getLocalNode(), memberService, membershipService);
        g[9] = createGroup("Grp9", nodeService.getLocalNode(), memberService, membershipService);

        for (int i = 0; i < 10; i++) {
            this.membershipService.addMembership(g[i], g[i]);
        }

        this.membershipService.addMembership(g[0], g[1]);
        this.membershipService.addMembership(g[0], g[2]);
        this.membershipService.addMembership(g[1], g[3]);
        this.membershipService.addMembership(g[2], g[4]);
        this.membershipService.addMembership(g[3], g[5]);
        this.membershipService.addMembership(g[4], g[5]);
        this.membershipService.addMembership(g[4], g[6]);
        this.membershipService.addMembership(g[4], g[8]);
        this.membershipService.addMembership(g[5], g[7]);
        this.membershipService.addMembership(g[7], g[9]);

        int[] s = new int[]{10, 5, 7, 4, 6, 3, 1, 2, 1, 1};

        assertTrue("testNestedGroups() membership(2,5) is nested", this.membershipService.load(g[2], g[5]).getNested());
        assertEquals("testNestedGroups() nested membership(0,9) has 2 path",
                2, this.membershipService.getNestingPathSetSize(this.membershipService.load(g[0], g[9])));
        testNestedGroupsAssertions(g, s);

        this.membershipService.addMembership(g[2], g[5]);

        assertFalse("testNestedGroups() membership(2,5) is direct", this.membershipService.load(g[2], g[5]).getNested());
        assertEquals("testNestedGroups() nested membership(0,9) has 3 path",
                3, this.membershipService.getNestingPathSetSize(this.membershipService.load(g[0], g[9])));
        testNestedGroupsAssertions(g, s);

        this.membershipService.removeMembership(this.membershipService.load(g[2], g[5]));

        assertTrue("testNestedGroups() membership(2,5) is nested", this.membershipService.load(g[2], g[5]).getNested());
        assertEquals("testNestedGroups() nested membership(0,9) has 2 path",
                2, this.membershipService.getNestingPathSetSize(this.membershipService.load(g[0], g[9])));
        testNestedGroupsAssertions(g, s);

        assertEquals("testNestedGroups() memberOf(7) set size", 7, this.membershipService.loadMemberOf(g[7]).size());
        this.membershipService.removeMembership(this.membershipService.load(g[1], g[3]));
        assertEquals("testNestedGroups() memberOf(7) set size", 6, this.membershipService.loadMemberOf(g[7]).size());

        assertNotNull("testNestedGroups() membership(0,7) exists", this.membershipService.load(g[0], g[7]));
        assertNull("testNestedGroups() membership(1,7) does not exist", this.membershipService.load(g[1], g[7]));

        assertEquals("testNestedGroups() nested membership(0,9) has 1 path",
                1, this.membershipService.getNestingPathSetSize(this.membershipService.load(g[0], g[9])));
    }

    @Test
    public void testLoadSimilarUserNames() {
        Set<String> names = memberService.loadSimilarUserNames("admi");
        Assert.assertTrue(names.size() > 0);
    }

    @Test
    public void testLoadGroupsFuzzy() {
        HashMap<String, Object> cmap = new HashMap<>();
        createGroup("FuzzyGroup", nodeService.getLocalNode(), memberService, membershipService);
        List<Group> groups = memberService.loadGroupsFuzzy(cmap);
        Assert.assertEquals(memberService.loadGroups(new HashMap<>()).size(), groups.size());
        cmap.put("NAME", "%Fuzzy%");
        groups = memberService.loadGroupsFuzzy(cmap);
        Assert.assertEquals(1, groups.size());
        cmap.clear();
        cmap.put("INSTITUTE", "%Anonymous%");
        groups = memberService.loadGroupsFuzzy(cmap);
        Assert.assertEquals(1, groups.size());
    }

    @Test
    public void testDeactivateGroup() {
        Group g = createGroup("groupToDelete", nodeService.getLocalNode(), memberService, membershipService);
        int groupsBeforeDeacitivation = memberService.loadGroups(new HashMap<>()).size();
        //Null should not be deactivated
        memberService.deactivateGroup(null);
        Assert.assertEquals(groupsBeforeDeacitivation, memberService.loadGroups(new HashMap<>()).size());
        // Admin group is not allowed to be deactivated
        Group adminGroup = loadGroupByName("Admin Group");
        memberService.deactivateGroup(adminGroup);
        Assert.assertEquals(groupsBeforeDeacitivation, memberService.loadGroups(new HashMap<>()).size());

        //Public group is not allowed to be deactivated
        Group publicGroup = loadGroupByName("Public Group");
        memberService.deactivateGroup(publicGroup);
        Assert.assertEquals(groupsBeforeDeacitivation, memberService.loadGroups(new HashMap<>()).size());

        //This group can be deactivated
        memberService.deactivateGroup(g);
        Assert.assertEquals(groupsBeforeDeacitivation - 1, memberService.loadGroups(new HashMap<>()).size());

        //Clean up the created and deactivated group
        entityManagerService.doSqlUpdate("DELETE from usersgroups WHERE id=" + g.getId());
    }

    /**
     * Saves a new USer and afterwards try to save a user with the same shortcut
     * which results in an exception
     */
    @Test
    public void test008_saveUser() {
        String email = "test008fake@mail.de";
        String logIn = "test008_login";
        String name = "test008_name";
        String pw = "test008_pw";
        String phone = "test008_phone";
        String shortCut = "TESTSHORTCUT";
        User user = new User();
        user.setEmail(email);
        user.setLogin(logIn);
        user.setModified(new Date());
        user.setName(name);
        user.setNode(nodeService.getLocalNode());
        user.setPassword(pw);
        user.setPhone(phone);
        user.setShortcut(shortCut);
        user.setSubSystemType(AdmissionSubSystemType.LOCAL);

        user = memberService.save(user);

        User loadedUser = memberService.loadUserById(user.getId());
        Assert.assertEquals(logIn, loadedUser.getLogin());
        Assert.assertEquals(email, loadedUser.getEmail());
        Assert.assertEquals(name, loadedUser.getName());
        Assert.assertEquals(pw, loadedUser.getPassword());
        Assert.assertEquals(phone, loadedUser.getPhone());
        Assert.assertEquals(shortCut, loadedUser.getShortcut());

        loadedUser.setId(null);
        // both users have the same shortcut
        Assert.assertThrows(
                EJBException.class,
                () -> {
                    memberService.save(loadedUser);
                }
        );

        entityManagerService.doSqlUpdate("DELETE from usersgroups WHERE id=" + user.getId());
    }

    @Test
    public void test009_shortcuts() {
        User user1 = new User();
        user1.setNode(nodeService.getLocalNode());
        user1.setSubSystemType(AdmissionSubSystemType.LOCAL);

        user1.setShortcut("ABC");
        user1 = memberService.save(user1);

        User user2 = new User();
        user2.setNode(nodeService.getLocalNode());
        user2.setSubSystemType(AdmissionSubSystemType.LOCAL);
        user2.setShortcut("abc");

        // duplicate shortcut -> unique constraint violation
        Assert.assertThrows(
                EJBException.class,
                () -> {
                    memberService.save(user2);
                }
        );

        User user3 = new User();
        user3.setNode(nodeService.getLocalNode());
        user3.setSubSystemType(AdmissionSubSystemType.LOCAL);
        user3.setShortcut("A2C");

        // does not match the only-letters regexp constraint
        Assert.assertThrows(
                EJBException.class,
                () -> {
                    memberService.save(user3);
                }
        );

        User user4 = new User();
        user4.setNode(nodeService.getLocalNode());
        user4.setSubSystemType(AdmissionSubSystemType.LOCAL);
        user4.setShortcut("");
        User user5 = new User();
        user5.setNode(nodeService.getLocalNode());
        user5.setSubSystemType(AdmissionSubSystemType.LOCAL);
        user5.setShortcut("");

        // empty shortcut becomes NULL and has no constraint
        memberService.save(user4);
        memberService.save(user5);

        entityManagerService.doSqlUpdate("DELETE from usersgroups WHERE id=" + user1.getId());
        entityManagerService.doSqlUpdate("DELETE from usersgroups WHERE id=" + user4.getId());
        entityManagerService.doSqlUpdate("DELETE from usersgroups WHERE id=" + user5.getId());
    }

    @Test
    public void test010_searchShortcut() {
        User user1 = new User();
        user1.setNode(nodeService.getLocalNode());
        user1.setSubSystemType(AdmissionSubSystemType.LOCAL);
        user1.setShortcut("abc");
        user1 = memberService.save(user1);

        User user2 = new User();
        user2.setNode(nodeService.getLocalNode());
        user2.setSubSystemType(AdmissionSubSystemType.LOCAL);
        user2.setShortcut("abcd");
        user2 = memberService.save(user2);

        User user3 = new User();
        user3.setNode(nodeService.getLocalNode());
        user3.setSubSystemType(AdmissionSubSystemType.LOCAL);
        user3.setShortcut("");
        user3 = memberService.save(user3);

        Map<String, Object> cmap = new HashMap<String, Object>();
        cmap.put(MemberService.PARAM_SHORTCUT, "aBc");
        cmap.put(MemberService.PARAM_SUBSYSTEM_TYPE, AdmissionSubSystemType.LOCAL);
        List<User> users = memberService.loadUsers(cmap);

        assertNotNull(users);
        // only finds exact matches
        assertEquals(1, users.size());
        assertEquals(user1, users.get(0));
        assertEquals("ABC", users.get(0).getShortcut());

        entityManagerService.doSqlUpdate("DELETE from usersgroups WHERE id=" + user1.getId());
        entityManagerService.doSqlUpdate("DELETE from usersgroups WHERE id=" + user2.getId());
        entityManagerService.doSqlUpdate("DELETE from usersgroups WHERE id=" + user3.getId());
    }

    @Test
    public void test011_loadLocalAdminUser() {

        User remoteAdmin = new User();
        Node foreignNode = createNode(nodeService, "");
        foreignNode = nodeService.save(foreignNode);
        remoteAdmin.setNode(foreignNode);
        remoteAdmin.setName("Admin");
        remoteAdmin.setSubSystemType(AdmissionSubSystemType.LBAC_REMOTE);

        memberService.save(remoteAdmin);

        User u = memberService.loadLocalAdminUser();
        Assert.assertNotNull(u);
        Assert.assertEquals("Admin", u.getName());

        entityManagerService.doSqlUpdate("DELETE from usersgroups where id=" + remoteAdmin.getId());
        entityManagerService.doSqlUpdate("DELETE from nodes where id='" + foreignNode.getId()+"'");
    }

    private Group loadGroupByName(String name) {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put("name", name);
        return memberService.loadGroups(cmap).get(0);

    }

}
