
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(PostgresqlContainerExtension.class)
@ExtendWith(ArquillianExtension.class)
@ExtendWith(MockitoExtension.class)
public class LogInProcessTest extends TestBase {


    @Mock
    LdapObject ldapObjectMock;

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
                .addClass(LdapProperties.class)
                .addClass(LdapHelper.class)
                .addClass(LdapObject.class)
                .addClass(LdapAdmissionSubSystem.class);
    }

    @Test
    public void tryLogIn_Local_Test() {

        Map<User, Group> ug = createUser_Group("localUser", "pass123", AdmissionSubSystemType.LOCAL);

        User u = null;
        Group g = null;

        for (User us : ug.keySet()) {
            u = us;
            g = ug.get(us);
        }
        assertNotNull(u);
        assertNotNull(g);

        User uTest = logInProcess.tryLogIn(u.getLogin(), u.getPassword());
        assertNotNull(uTest);
    }

    @Test
    public void tryLogIn_Not_Local_Test() {

        Map<User, Group> ug = createUser_Group("notLocalUser", "pass123", AdmissionSubSystemType.BUILTIN);

        User u = null;
        Group g = null;

        for (User us : ug.keySet()) {
            u = us;
            g = ug.get(us);
        }
        assertNotNull(u);
        assertNotNull(g);
        User uTest = logInProcess.tryLogIn(u.getLogin(), u.getPassword());
        // refactor the method "tryLogin" to handle other types of AdmissionSubSystemType besides LOCAL and Ldap
        assertEquals(uTest, null);
    }

    @Test
    public void tryLogIn_Ldap_Test() throws Exception {

        Map<User, Group> ug = createUser_Group("fabian", "pass", AdmissionSubSystemType.LDAP);

        User u = null;
        Group g = null;

        for (User us : ug.keySet()) {
            u = us;
            g = ug.get(us);
        }
        assertNotNull(u);
        assertNotNull(g);


        User finalU = u;
        try (MockedConstruction<LdapHelper> mocked = mockConstruction(
                LdapHelper.class,
                (mock, context) -> {
                    when(mock.queryLdapUser(eq(finalU.getLogin()), anyMap()))
                            .thenReturn(ldapObjectMock);
                }
        )) {
            // ggf. LdapAdmissionSubSystem auch mocken, falls nötig:
            User finalU1 = u;
            try (MockedConstruction<LdapAdmissionSubSystem> mockedSub = mockConstruction(
                    LdapAdmissionSubSystem.class,
                    (mock, context) -> {
                        when(mock.lookupLbacUser(eq(ldapObjectMock), any(UserBean.class))).thenReturn(finalU1);
                    }
            )) {

                User result = logInProcess.tryLogIn(u.getLogin(), "pass");
                assertNotNull(result);
                assertEquals(u.getLogin(), result.getLogin());
            }
        }
    }


    public Map<User, Group> createUser_Group(String login, String password, AdmissionSubSystemType admissionSubSystemType) {

        String name = "userTest" + " " + System.currentTimeMillis();
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
        g.setSubSystemData("L");
        g.setSubSystemType(admissionSubSystemType);
        g = memberService.save(g);

        membershipService.addMembership(u, u);
        membershipService.addMembership(g, u);

        Map<User, Group> UserGroupMap = new HashMap<>();
        UserGroupMap.put(u, g);
        return UserGroupMap;
    }
}
