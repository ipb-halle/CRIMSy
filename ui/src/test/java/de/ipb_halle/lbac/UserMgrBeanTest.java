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
package de.ipb_halle.lbac;

import de.ipb_halle.lbac.admission.*;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.container.mock.CallBackControllerMock;
import de.ipb_halle.lbac.material.mocks.MessagePresenterMock;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
public class UserMgrBeanTest extends TestBase {

    private UserMgrBean userMgrBean;

    @Deployment
    public static WebArchive createDeployment() {
        return UserBeanDeployment
                .add(prepareDeployment("GroupMgrBeanTest.war"));
    }

    @Test
    public void test001_createNewUser() {
        MessagePresenterMock presenterMock = MessagePresenterMock.getInstance();
        userMgrBean = new UserMgrBean(
                nodeService,
                memberService,
                membershipService,
                presenterMock,
                new CallBackControllerMock());

        userMgrBean.getUser().setName(("test001_userName"));
        userMgrBean.getUser().setLogin(("test001_userlogin"));
        userMgrBean.getUser().setShortcut(("test001_shortCut"));
        userMgrBean.getUser().setPassword(("test001_pw"));
        userMgrBean.actionCreate();

        userMgrBean.getUser().setName(("test001_userName2"));
        userMgrBean.getUser().setLogin(("test001_userlogin2"));
        userMgrBean.getUser().setShortcut(("test001_shortCut"));
        userMgrBean.getUser().setPassword(("test001_pw2"));
        userMgrBean.actionCreate();
        Assert.assertEquals("userMgr_error_duplicateShortcut", presenterMock.getLastErrorMessage());
        entityManagerService.doSqlUpdate("DELETE FROM usersgroups WHERE id>5");
    }
}
