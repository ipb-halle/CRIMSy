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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        userBean.setLdapProperties(ldapProperties);
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
    public void test003_authenticate_withoutExistingLDAPUser() {
        entityManagerService.doSqlUpdate("INSERT INTO info(key,value) VALUES('LDAP_ENABLE','true')");
        ldapHelper.addLdapObject("CN=ldap_user_name", "ldap_email", "ldap_login", "ldac_user_name_edited", "+xxx", MemberType.USER, "uniqueId-001");
        Assert.assertFalse(system.authenticate(publicUser, "admin", userBean));
    }

    /**
     * An existing user is found in the LDAP system but the name and email was
     * changed. The data should be persisted in the database.
     */
    @Test
    public void test004_authenticate_withExistingLBACUser() {
        entityManagerService.doSqlUpdate("INSERT INTO info(key,value) VALUES('LDAP_ENABLE','true')");
        ldapHelper.addLdapObject("CN=ldap_user_name", "ldap_email_edited", "ldap_login", "ldac_user_name_edited", "+xxx", MemberType.USER, "uniqueId-001");
        ldapProperties.LdapBasicsInit();

        User ldapUser = createUser("ldap_login", "ldap_name");
        ldapUser.setSubSystemType(AdmissionSubSystemType.LDAP);
        ldapUser.setSubSystemData("uniqueId-001");

        memberService.save(ldapUser);
        Assert.assertTrue(system.authenticate(ldapUser, "ldac_user", userBean));
        User updatedUser = memberService.loadUserById(ldapUser.getId());
        Assert.assertEquals("ldac_user_name_edited", updatedUser.getName());
        Assert.assertEquals("ldap_email_edited", updatedUser.getEmail());
        
        entityManagerService.doSqlUpdate("Delete from usersgroups where id="+updatedUser.getId());
    }

    @Test
    public void test005_authenticate_withNotExistingLBACUser() {
        entityManagerService.doSqlUpdate("INSERT INTO info(key,value) VALUES('LDAP_ENABLE','true')");
        ldapHelper.addLdapObject("CN=ldap_user_name", "ldap_email", "ldap_login", "ldac_user_name_edited", "+xxx", MemberType.USER, "uniqueId-001");
        ldapProperties.LdapBasicsInit();
        User u = new User();
        u.setLogin("ldap_login");
        Assert.assertTrue(system.authenticate(u, "ldac_user", userBean));
        User loadedUser = memberService.loadUserById(u.getId());
        Assert.assertNotNull(loadedUser);
        Set<Membership> memberShips = membershipService.loadMemberOf(loadedUser);
        //User should be in public group and assigned to itself
        Assert.assertEquals(2, memberShips.size());
    }

    @Test
    public void test006_authenticate_withNotExistingLBACUser_withDirectGroup() {
        entityManagerService.doSqlUpdate("INSERT INTO info(key,value) VALUES('LDAP_ENABLE','true')");
        ldapHelper.addLdapObject("CN=ldap_user_name", "ldap_email", "ldap_login", "ldac_user_name_edited", "+xxx", MemberType.USER, "uniqueId-001");
        ldapHelper.addLdapObject("CN=LBAC_User,OU=BioactivesCloud,OU=group,DC=ipb-halle,DC=de", null, null, "LBAC_User", null, MemberType.GROUP, "uniqueId-002");
        ldapProperties.LdapBasicsInit();
        User u = new User();
        u.setLogin("ldap_login");
        Assert.assertTrue(system.authenticate(u, "ldac_user", userBean));
        User loadedUser = memberService.loadUserById(u.getId());
        Assert.assertNotNull(loadedUser);
        Set<Membership> memberShips = membershipService.loadMemberOf(loadedUser);
        //User should be in public group, the created group and assigned to itself
        Assert.assertEquals(3, memberShips.size());

        Assert.assertEquals(1, loadGroupByName("LBAC_User").size());
    }

    @Test
    public void test007_authenticate_withNotExistingLBACUser_withIndirectAndDirectGroup() {
        entityManagerService.doSqlUpdate("INSERT INTO info(key,value) VALUES('LDAP_ENABLE','true')");
        ldapHelper.addLdapObject("CN=ldap_user_name", "ldap_email", "ldap_login", "ldac_user_name_edited", "+xxx", MemberType.USER, "uniqueId-001");
        ldapHelper.addLdapObject("CN=LBAC_User,OU=BioactivesCloud,OU=group,DC=ipb-halle,DC=de", null, null, "LBAC_User", null, MemberType.GROUP, "uniqueId-002");
        ldapHelper.addLdapObject("CN=Institute,OU=BioactivesCloud,OU=group,DC=ipb-halle,DC=de", null, null, "Institute", null, MemberType.GROUP, "uniqueId-003");
        ldapProperties.LdapBasicsInit();
        User u = new User();
        u.setLogin("ldap_login");
        Assert.assertTrue(system.authenticate(u, "ldac_user", userBean));
        User loadedUser = memberService.loadUserById(u.getId());
        Assert.assertNotNull(loadedUser);
        Set<Membership> memberShips = membershipService.loadMemberOf(loadedUser);
        //User should be in public group(direct), the created group(direct) 
        // and assigned to itself. An indirect membership should be in the institute group
        Assert.assertEquals(4, memberShips.size());

        Assert.assertEquals(1, loadGroupByName("LBAC_User").size());
        Assert.assertEquals(1, loadGroupByName("Institute").size());

        //Check if LBAC_User is member of Institute group
        Set<Membership> memberShipsOfLBACUser = membershipService.loadMembers(loadGroupByName("LBAC_User").get(0));
        Assert.assertEquals(2, memberShipsOfLBACUser.size());
    }

    @Deployment
    public static WebArchive createDeployment() {
        return UserBeanDeployment
                .add(prepareDeployment("LdapAdmissionSubSystemTest.war")
                        .addClass(InfoObjectService.class));
    }

    private List<Group> loadGroupByName(String name) {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put("name", name);
        return memberService.loadGroups(cmap);
    }

}
