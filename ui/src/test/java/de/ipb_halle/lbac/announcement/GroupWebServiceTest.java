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
package de.ipb_halle.lbac.announcement;

import de.ipb_halle.lbac.admission.AdmissionSubSystemType;
import de.ipb_halle.lbac.admission.GlobalAdmissionContext;
import de.ipb_halle.lbac.admission.Group;
import de.ipb_halle.lbac.admission.GroupWebRequest;
import de.ipb_halle.lbac.admission.GroupWebService;
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
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class GroupWebServiceTest extends TestBase {

    @Inject
    private GroupWebService groupWebService;

    @Inject
    protected KeyManager keymanager;

    @Deployment
    public static WebArchive createDeployment() {

        return prepareDeployment("GroupWebServiceTest.war")
                .addClass(GlobalAdmissionContext.class)
                .addClass(MembershipWebService.class)
                .addClass(GlobalAdmissionContext.class)
                .addClass(WebRequestAuthenticator.class)
                .addClass(FileService.class)
                .addClass(KeyManager.class)
                .addClass(GroupWebService.class)
                .addClass(KeyManager.class)
                .addClass(FileEntityService.class);
    }

    @Test
    public void test01_groupWebService() throws Exception {
          Cloud c = cloudService.loadByName(TESTCLOUD);
           Node n = createNode(
                nodeService,
                "remove this parameter"
        );
           
             CloudNode testCloudNode = new CloudNode(c, n);
        testCloudNode.setPublicKey(Base64.getEncoder().encodeToString(keymanager.getLocalPublicKey(TESTCLOUD).getEncoded()));
        testCloudNode = cloudNodeService.save(testCloudNode);
        
        
        Group g = new Group();
        g.setNode(nodeService.getLocalNode());
        g.setSubSystemType(AdmissionSubSystemType.LBAC_REMOTE);
        g.setName("GroupWebServiceTest:test01_groupWebService");
        g = memberService.save(g);
        LbacWebClient client = new LbacWebClient();
        WebRequestSignature signature = client.createWebRequestSignature(keymanager.getLocalPrivateKey(TESTCLOUD));
        User publicUser = memberService.loadUserById((GlobalAdmissionContext.PUBLIC_ACCOUNT_ID));

        GroupWebRequest request = new GroupWebRequest(g);
        request.setUser(publicUser);
        request.setCloudName(TESTCLOUD);
        request.setNodeIdOfRequest(testCloudNode.getNode().getId());
        request.setSignature(signature);
        groupWebService.handleRequest(request);

        Group deactivatedGroup = memberService.loadGroupById(g.getId());
        Assert.assertNotNull(deactivatedGroup);
        Assert.assertEquals("deactivated",deactivatedGroup.getName());
        
        entityManagerService.doSqlUpdate("DELETE FROM usersgroups WHERE id="+g.getId());
    }

    @Before
    public void setUp() {
        super.setUp();
        initializeBaseUrl();
        initializeKeyStoreFactory();
    }

}
