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
package de.ipb_halle.lbac.announcement.membership;

import de.ipb_halle.lbac.admission.AdmissionSubSystemType;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.MembershipWebRequest;
import de.ipb_halle.lbac.admission.MembershipWebService;
import de.ipb_halle.lbac.base.TestBase;
import de.ipb_halle.lbac.entity.Cloud;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.admission.Group;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.admission.MembershipService;
import de.ipb_halle.lbac.service.NodeService;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.admission.Membership;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.util.ssl.SecureWebClientBuilder;
import de.ipb_halle.lbac.webclient.LbacWebClient;
import de.ipb_halle.lbac.webclient.WebRequestSignature;
import de.ipb_halle.lbac.webservice.service.WebRequestAuthenticator;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import org.apache.cxf.jaxrs.client.WebClient;
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
                .addClass(MembershipWebService.class)
                .addClass(WebRequestAuthenticator.class)
                .addClass(KeyManager.class);

    }

    @BeforeEach
    public void init() {
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

        User u = createUser("testUser", "testUser");

        Group group1 = createGroup("group1", n, memberService, membershipService);
        membershipService.addMembership(group1, u);

        Group remoteGroup2 = new Group();
        remoteGroup2.setId(-1000);
        remoteGroup2.setName("group2");
        remoteGroup2.setNode(n);
        remoteGroup2.setSubSystemData("/");
        remoteGroup2.setSubSystemType(AdmissionSubSystemType.LOCAL);
        Set<Group> groupSet = new HashSet<>();
        groupSet.add(remoteGroup2);

        Group publicGroup = memberService.loadGroupById(GlobalAdmissionContext.PUBLIC_GROUP_ID);
        groupSet.add(publicGroup);

        Set<Membership> memberships = membershipService.loadMemberOf(u);
        for (Membership m : memberships) {
            membershipService.removeMembership(m);
        }
        membershipService.addMembership(group1, u);

        LbacWebClient client = new LbacWebClient();
        WebRequestSignature signature = client.createWebRequestSignature(keymanager.getLocalPrivateKey(TESTCLOUD));
        u.setNode(n);
        MembershipWebRequest webRequest = new MembershipWebRequest();
        webRequest.setUser(u);
        webRequest.setCloudName(TESTCLOUD);
        webRequest.setNodeIdOfRequest(n.getId());
        webRequest.setSignature(signature);
        webRequest.setUserToAnnounce(u);
        webRequest.setGroups(groupSet);

        wc.post(webRequest);

        Integer remoteId = (Integer) entityManagerService.doSqlQuery(
                String.format("SELECT id FROM usersgroups ug WHERE ug.subsystemdata ='%s'",
                        u.getId().toString())).get(0);
        memberships = membershipService.loadMemberOf(memberService.loadUserById(remoteId));

        Assert.assertEquals(3, memberships.size());
        boolean publicGroupFlag = false;
        boolean group2Flag = false;

        for (Membership m : memberships) {
            if (m.getGroup().equals(publicGroup)) {
                publicGroupFlag = true;
            } else if (m.getGroup().getSubSystemData().equals(remoteGroup2.getId().toString())) {
                group2Flag = true;
            }
        }
        Assert.assertTrue(publicGroupFlag);
        Assert.assertTrue(group2Flag);

    }

}
