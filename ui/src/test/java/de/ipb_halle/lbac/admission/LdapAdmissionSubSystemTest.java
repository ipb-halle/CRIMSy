/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2021 Leibniz-Institut f. Pflanzenbiochemie
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

import de.ipb_halle.lbac.admission.mock.LdapHelperMock;
import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.lbac.service.InfoObjectService;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author fmauz
 */
@RunWith(Arquillian.class)
public class LdapAdmissionSubSystemTest extends TestBase {

    private static final long serialVersionUID = 1L;

    @Inject
    private UserBeanMock userBean;

    @Inject
    private LdapProperties ldapProperties;

    LdapAdmissionSubSystem system;
    LdapHelperMock ldapHelper;

    @Before
    public final void init() {
        userBean = new UserBeanMock();
        userBean.setCurrentAccount(publicUser);
        userBean.setLdabProperties(ldapProperties);
        userBean.setNodeService(nodeService);

        ldapHelper = new LdapHelperMock();
        system = new LdapAdmissionSubSystem(ldapHelper);
        ldapProperties.LdapBasicsInit();
    }

    @After
    public void finish() {
        entityManagerService.doSqlUpdate("DELETE FROM info");

    }

    @Test
    public void test001_authenticate_withoutLdapEntry() {
        Assert.assertFalse(system.authenticate(publicUser, "admin", userBean));
    }

    @Test
    public void test002_authenticate_withoutLdapEnabled() {
        entityManagerService.doSqlUpdate("INSERT INTO info(key,value) VALUES('LDAP_ENABLE','false')");
        Assert.assertFalse(system.authenticate(publicUser, "admin", userBean));
    }

    @Test
    public void test003_authenticate_withoutExistingUser() {
        entityManagerService.doSqlUpdate("INSERT INTO info(key,value) VALUES('LDAP_ENABLE','true')");
        ldapHelper.userExists = false;
        Assert.assertFalse(system.authenticate(publicUser, "admin", userBean));
    }

    @Deployment
    public static WebArchive createDeployment() {
        return UserBeanDeployment
                .add(prepareDeployment("LdapAdmissionSubSystemTest.war")
                        .addClass(InfoObjectService.class));
    }

}
