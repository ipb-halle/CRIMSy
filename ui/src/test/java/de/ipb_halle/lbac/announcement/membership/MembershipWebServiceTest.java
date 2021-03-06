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
package de.ipb_halle.lbac.announcement.membership;

import de.ipb_halle.lbac.admission.AdmissionSubSystemType;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.announcement.membership.mock.MembershipWebServiceMock;
import de.ipb_halle.lbac.announcement.membership.MembershipWebRequest;
import de.ipb_halle.lbac.announcement.membership.MembershipWebService;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.entity.Cloud;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.Group;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.service.ACListService;
import de.ipb_halle.lbac.service.CollectionService;
import de.ipb_halle.lbac.service.MemberService;
import de.ipb_halle.lbac.service.MembershipService;
import de.ipb_halle.lbac.service.NodeService;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.entity.Membership;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.util.ssl.SecureWebClientBuilder;
import de.ipb_halle.lbac.webclient.LbacWebClient;
import de.ipb_halle.lbac.webclient.WebRequestSignature;
import de.ipb_halle.lbac.webservice.Updater;
import de.ipb_halle.lbac.webservice.service.WebRequestAuthenticator;
import java.net.URL;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.apache.cxf.jaxrs.client.WebClient;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for receipt of an webrequest with the inquiry to save a remote user in
 * the local database inclusive its groups.
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class MembershipWebServiceTest extends TestBase {

    @Inject
    protected MembershipService membershipService;

    @Inject
    protected WebRequestAuthenticator authentificator;

    @Inject
    protected KeyManager keymanager;

    @Inject
    protected ACListService acListService;

    @Inject
    protected NodeService nodeService;

    @Inject
    protected CollectionService collectionService;

    @Inject
    protected MemberService memberService;

    @Inject
    protected MembershipWebService webService;

    @Deployment
    public static WebArchive createDeployment() {

        return prepareDeployment("MembershipWebServiceTest.war")
                .addPackage(MemberService.class.getPackage())
                .addPackage(NodeService.class.getPackage())
                .addClass(GlobalAdmissionContext.class)
                .addClass(MembershipWebService.class)
                .addClass(GlobalAdmissionContext.class)
                .addClass(WebRequestAuthenticator.class)
                .addClass(FileService.class)
                .addClass(KeyManager.class)
                .addClass(KeyManager.class)
                .addClass(FileEntityService.class);

    }

    @Before
    public void setUp() {
        super.setUp();
        initializeBaseUrl();
        initializeKeyStoreFactory();
    }

    /**
     * Testscenario - Announce a known user a remote node
     *
     * <ol>
     * <li>
     * <b>Initial situation</b>
     * a user "user1" is member of only one group with name "group1". The
     * membership was already submitted to the remote node and put into the db
     * </li>
     * <li>
     * <b>Actions done by user via GUI</b>
     * The user was removed from "group1" and was added to "group2". It is also
     * member of the public group but this is not yet announced to the remote
     * database
     * </li>
     * <li>
     * <b>Validation</b>
     * In the database the user "user1" must be in the database. He must be
     * member of the public group and group 2. Group2 must be added to the
     * database as a remote group. He must not be member of group1</li>
     * </ol>
     *
     * @throws Exception
     */
    @Test
    public void test() throws Exception {

        assert (webService != null);

        Cloud c = cloudService.loadByName(TESTCLOUD);
        CloudNode cn = cloudNodeService.loadCloudNode(c, nodeService.getLocalNode()); 
        final WebClient wc = SecureWebClientBuilder.createWebClient(
                cn,
                "members");
        wc.accept(MediaType.APPLICATION_XML_TYPE);
        wc.type(MediaType.APPLICATION_XML_TYPE);

        //Create a second node. It will represent the node from which the request is sent.
        Node n = createNode(
                nodeService,
                "remove this parameter"
        );

        CloudNode testCloudNode = new CloudNode(c, n);
        testCloudNode.setPublicKey(Base64.getEncoder().encodeToString(keymanager.getLocalPublicKey(TESTCLOUD).getEncoded()));
        testCloudNode = cloudNodeService.save(testCloudNode);

        User u = createUser("testUser", "testUser", n, memberService, membershipService);

        Group group1 = createGroup("group1", n, memberService, membershipService);
        membershipService.addMembership(group1,u);

        UUID groupID = UUID.randomUUID();
        Group remoteGroup2 = new Group();
        remoteGroup2.setId(groupID);
        remoteGroup2.setName("group2");
        remoteGroup2.setNode(n);
        remoteGroup2.setSubSystemData("/");
        remoteGroup2.setSubSystemType(AdmissionSubSystemType.LOCAL);
        Set<Group> groupSet = new HashSet<>();
        groupSet.add(remoteGroup2);

        Group publicGroup = memberService.loadGroupById(UUID.fromString(GlobalAdmissionContext.PUBLIC_GROUP_ID));
        groupSet.add(publicGroup);

        Set<Membership> memberships = membershipService.loadMemberOf(u);
        for (Membership m : memberships) {
            membershipService.removeMembership(m);
        }
        membershipService.addMembership(group1,u );

        LbacWebClient client = new LbacWebClient();
        WebRequestSignature signature = client.createWebRequestSignature(keymanager.getLocalPrivateKey(TESTCLOUD));

        MembershipWebRequest webRequest = new MembershipWebRequest();
        webRequest.setUser(u);
        webRequest.setCloudName(TESTCLOUD);
        webRequest.setNodeIdOfRequest(n.getId());
        webRequest.setSignature(signature);
        webRequest.setUserToAnnounce(u);
        webRequest.setGroups(groupSet);

        wc.post(webRequest);

        memberships = membershipService.loadMemberOf(u);
        Assert.assertEquals(3, memberships.size());
        boolean publicGroupFlag = false;
        boolean group2Flag = false;

        for (Membership m : memberships) {
            if (m.getGroup().equals(publicGroup)) {
                publicGroupFlag = true;
            }
            if (m.getGroup().equals(remoteGroup2)) {
                group2Flag = true;
            }
        }
        Assert.assertTrue(publicGroupFlag);
        Assert.assertTrue(group2Flag);

    }

}
