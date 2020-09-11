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
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.cloud.solr.SolrAdminService;
import de.ipb_halle.lbac.collections.CollectionBean;
import de.ipb_halle.lbac.collections.CollectionOrchestrator;
import de.ipb_halle.lbac.collections.CollectionSearchState;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.search.termvector.SolrTermVectorSearch;
import de.ipb_halle.lbac.collections.CollectionWebClient;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.search.document.DocumentSearchBean;
import de.ipb_halle.lbac.search.document.DocumentSearchOrchestrator;
import de.ipb_halle.lbac.search.document.DocumentSearchService;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.service.NodeService;
import de.ipb_halle.lbac.webservice.Updater;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
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

}
