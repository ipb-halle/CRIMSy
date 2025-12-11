package de.ipb_halle.lbac.admission;

import de.ipb_halle.lbac.base.TestBase;
import static de.ipb_halle.lbac.base.TestBase.prepareDeployment;
import de.ipb_halle.testcontainers.PostgresqlContainerExtension;
import org.jboss.arquillian.junit5.ArquillianExtension;

import jakarta.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
//@ExtendWith(MockitoExtension.class)
public class LogInProcessTest extends TestBase {

    @Inject
    protected LogInProcess logInProcess;

    @Inject
    protected CredentialHandler credentialHandler;

    @Inject
    protected LdapProperties ldapProperties;

    @Deployment
    public static WebArchive createDeployment() {
        return prepareDeployment("LogInProcessTest.war")
                .addClass(LogInProcess.class)
                .addClass(CredentialHandler.class)
                .addClass(LdapProperties.class);
    }

    @Test
    public void tryLogIn_Local_Test() {

        Map<User, Group> ug = createUser_Group("localUser", "Local", "pass123", AdmissionSubSystemType.LOCAL, "L");
        User u = ug.keySet().iterator().next();
        assertNotNull(u);

        User uTest = logInProcess.tryLogIn("localUser", "pass123");
        assertNotNull(uTest);
    }

    @Test
    public void tryLogIn_Not_Local_Test() {

        Map<User, Group> ug = createUser_Group("notLocalUser", "notLocal", "pass123", AdmissionSubSystemType.BUILTIN, "L");
        User u = ug.keySet().iterator().next();
        assertNotNull(u);

        User uTest = logInProcess.tryLogIn("notLocalUser", "notLocal");
        // refactor the method "tryLogin" to handle other types of AdmissionSubSystemType besides LOCAL and Ldap
        assertEquals(uTest, null);
    }

    /*  @Test
    public void tryLogIn_Ldap_Test() throws Exception {
        // Create a mock LDAP user
        Map<User, Group> ug = createUser_Group("fabian", "LDAP User", "ldapPass", AdmissionSubSystemType.LDAP, "uniqueId-001");
        User u = ug.keySet().iterator().next();
        assertNotNull(u, "Test user should be created!");

        // Mock the LDAP helper
        LdapHelper ldapHelperMock = mock(LdapHelper.class);
        logInProcess.ldapHelper = ldapHelperMock; // Manually inject the mock

        // Mock the LDAP user object
        LdapObject ldapObjectUser = new LdapObject()
                .setLogin(u.getLogin())
                .setName(u.getName())
                .setType(MemberType.USER)
                .setEmail("fabian@example.com")
                .setDN("cn=fabian,dc=example,dc=com")
                .setUniqueId("uniqueId-001");
        ldapObjectUser.addMembership("cn=ldapGroup,dc=example,dc=com");

        // Define mock behavior for LDAP
        when(ldapHelperMock.authenticate(eq("fabian"), eq("ldapPass"))).thenReturn(true);

        Map<String, LdapObject> mockLdapObjects = new HashMap<>();
        when(ldapHelperMock.queryLdapUser(eq(u.getLogin()), eq(mockLdapObjects)))
                .thenReturn(ldapObjectUser);

        // Attempt login
        User result = logInProcess.tryLogIn("fabian", "ldapPass");

        System.out.println("Login result: " + result); // Log the result to verify if it's null or not.

        // Verify result
        assertNotNull(result, "Login successful!");  // Assert that a user is returned
        assertEquals("fabian", result.getLogin(), "Login should match the username");
        assertEquals(AdmissionSubSystemType.LDAP, result.getSubSystemType(), "User subsystem should be LDAP!");

        // Verify mock interactions
        verify(ldapHelperMock).authenticate(eq("fabian"), eq("ldapPass"));  // Ensure authenticate was called
        verify(ldapHelperMock).queryLdapUser(eq("fabian"), anyMap());  // Ensure queryLdapUser was called

    }
     */
    public Map<User, Group> createUser_Group(String login, String name, String password, AdmissionSubSystemType admissionSubSystemType, String subSystemDataString) {

        User u = new User();
        u.setLogin(login);
        u.setName(name);
        u.setPassword(credentialHandler.computeDigest(password));
        u.setNode(nodeService.getLocalNode());
        u.setSubSystemType(admissionSubSystemType);
        u = memberService.save(u);

        Group g = new Group();
        g.setName("Group of user " + u.getLogin());
        g.setNode(nodeService.getLocalNode());
        g.setSubSystemData(subSystemDataString);
        g.setSubSystemType(admissionSubSystemType);
        g = memberService.save(g);

        membershipService.addMembership(u, u);
        membershipService.addMembership(g, u);

        Map<User, Group> UserGroupMap = new HashMap<>();
        UserGroupMap.put(u, g);
        return UserGroupMap;
    }
}
