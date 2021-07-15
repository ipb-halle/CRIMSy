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
import java.util.Set;
import javax.inject.Inject;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 *
 * @author fmauz
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
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
        userBean.setMemberService(memberService);
        userBean.setMemberShipService(membershipService);
        userBean.setGlobalAdmissionContext(context);

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
    public void test003_authenticate_withoutExistingLdabUser() {
        entityManagerService.doSqlUpdate("INSERT INTO info(key,value) VALUES('LDAP_ENABLE','true')");
        ldapHelper.addLdapObject("CN=ldab_user_name", "ldab_email", "ldab_login", "ldac_user_name_edited", "+xxx", MemberType.USER, "uniqueId-001");
        Assert.assertFalse(system.authenticate(publicUser, "admin", userBean));
    }

    @Test
    public void test004_authenticate_withExistingLBACUser() {
        entityManagerService.doSqlUpdate("INSERT INTO info(key,value) VALUES('LDAP_ENABLE','true')");
        ldapHelper.addLdapObject("CN=ldab_user_name", "ldab_email_edited", "ldab_login", "ldac_user_name_edited", "+xxx", MemberType.USER, "uniqueId-001");
        ldapProperties.LdapBasicsInit();

        User ldabUser = createUser("ldab_login", "ldab_name");
        ldabUser.setSubSystemType(AdmissionSubSystemType.LDAP);
        ldabUser.setSubSystemData("1");

        memberService.save(ldabUser);
        Assert.assertTrue(system.authenticate(ldabUser, "ldac_user", userBean));

        User updatedUser = memberService.loadUserById(ldabUser.getId());
        Assert.assertEquals("ldac_user_name_edited", updatedUser.getName());
        Assert.assertEquals("ldab_email_edited", updatedUser.getEmail());
    }

    @Test
    public void test005_authenticate_withNotExistingLBACUser() {
        entityManagerService.doSqlUpdate("INSERT INTO info(key,value) VALUES('LDAP_ENABLE','true')");
        ldapHelper.addLdapObject("CN=ldab_user_name", "ldab_email", "ldab_login", "ldac_user_name_edited", "+xxx", MemberType.USER, "uniqueId-001");
        ldapProperties.LdapBasicsInit();
        User u = new User();
        u.setLogin("ldab_login");
        Assert.assertTrue(system.authenticate(u, "ldac_user", userBean));
        User loadedUser = memberService.loadUserById(u.getId());
        Assert.assertNotNull(loadedUser);
        Set<Membership> memberShips = membershipService.loadMemberOf(loadedUser);
        //User should be in public group and assigned to itself
        Assert.assertEquals(2, memberShips.size());
    }

    @Deployment
    public static WebArchive createDeployment() {
        return UserBeanDeployment
                .add(prepareDeployment("LdapAdmissionSubSystemTest.war")
                        .addClass(InfoObjectService.class));
    }

}
