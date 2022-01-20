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
package de.ipb_halle.lbac.admission.group;

import de.ipb_halle.lbac.admission.AdmissionSubSystemType;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.Group;
import de.ipb_halle.lbac.admission.group.DeactivateGroupWebService;
import de.ipb_halle.lbac.admission.MembershipWebService;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.TESTCLOUD;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.entity.Cloud;
import de.ipb_halle.lbac.entity.CloudNode;
import de.ipb_halle.lbac.entity.Node;
import de.ipb_halle.lbac.file.FileEntityService;
import de.ipb_halle.lbac.globals.KeyManager;
import de.ipb_halle.lbac.service.FileService;
import de.ipb_halle.lbac.webclient.LbacWebClient;
import de.ipb_halle.lbac.webclient.WebRequestSignature;
import de.ipb_halle.lbac.webservice.service.WebRequestAuthenticator;
import java.util.Base64;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author fmauz
 */
@ExtendWith(ArquillianExtension.class)
public class DeactivateGroupWebServiceTest extends TestBase {

    @Inject
    private DeactivateGroupWebService groupWebService;

    @Inject
    protected KeyManager keymanager;

    private User publicUser;
    private Cloud cloud;
    private Node node;

    @Deployment
    public static WebArchive createDeployment() {

        return prepareDeployment("GroupWebServiceTest.war")
                .addClass(GlobalAdmissionContext.class)
                .addClass(MembershipWebService.class)
                .addClass(GlobalAdmissionContext.class)
                .addClass(WebRequestAuthenticator.class)
                .addClass(FileService.class)
                .addClass(KeyManager.class)
                .addClass(DeactivateGroupWebService.class)
                .addClass(KeyManager.class)
                .addClass(FileEntityService.class);
    }

    @Test
    public void test001_deactivateGroup() throws Exception {
        createAndSaveCloudNode();
        Group remoteGroup = createAndSaveRemoteGroup();
        DeactivateGroupWebRequest request = createWebRequest(node, remoteGroup);
        groupWebService.handleRequest(request);

        Group deactivatedGroup = memberService.loadGroupById(remoteGroup.getId());
        Assert.assertNotNull(deactivatedGroup);
        Assert.assertEquals("deactivated", deactivatedGroup.getName());

        entityManagerService.doSqlUpdate("DELETE FROM usersgroups WHERE id=" + remoteGroup.getId());
    }

    @BeforeEach
    public void init() {
        initializeBaseUrl();
        initializeKeyStoreFactory();
        publicUser = memberService.loadUserById((GlobalAdmissionContext.PUBLIC_ACCOUNT_ID));
    }

    private Group createAndSaveRemoteGroup() {
        Group g = new Group();
        g.setNode(nodeService.getLocalNode());
        g.setSubSystemType(AdmissionSubSystemType.LBAC_REMOTE);
        g.setSubSystemData("1");
        g.setName("GroupWebServiceTest:test001_deactivateGroup");
        g = memberService.save(g);
        return g;
    }

    private void createAndSaveCloudNode() throws Exception {
        cloud = cloudService.loadByName(TESTCLOUD);
        node = createNode(nodeService, "remove this parameter");
        CloudNode testCloudNode = new CloudNode(cloud, node);
        testCloudNode.setPublicKey(Base64.getEncoder().encodeToString(keymanager.getLocalPublicKey(TESTCLOUD).getEncoded()));
        testCloudNode = cloudNodeService.save(testCloudNode);
    }

    private DeactivateGroupWebRequest createWebRequest(
            Node nodeOfRequest,
            Group g) throws Exception {
        DeactivateGroupWebRequest request = new DeactivateGroupWebRequest(g);
        request.setUser(publicUser);
        request.setCloudName(TESTCLOUD);
        request.setNodeIdOfRequest(nodeOfRequest.getId());

        LbacWebClient client = new LbacWebClient();
        WebRequestSignature signature = client.createWebRequestSignature(keymanager.getLocalPrivateKey(TESTCLOUD));
        request.setSignature(signature);
        return request;
    }

}
