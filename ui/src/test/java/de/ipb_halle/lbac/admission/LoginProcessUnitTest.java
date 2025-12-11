package de.ipb_halle.lbac.admission;

import de.ipb_halle.lbac.service.NodeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginProcessUnitTest {

    // EJB-Dependencies Mocks
    @Mock NodeService nodeService;
    @Mock MemberService memberService;
    @Mock MembershipService memberShipService;
    @Mock CredentialHandler credentialHandler;
    @Mock LdapProperties ldapProps;

    // SubSystem Mocks
    @Mock IAdmissionSubSystem localSubSystem;
    @Mock IAdmissionSubSystem ldapSubSystem;

    // Testing object
    LogInProcess logInProcess;

    @BeforeEach
    void setUp() {
        // class with our mocks
        logInProcess = new LogInProcess() {
            @Override
            protected IAdmissionSubSystem getLocalSubSystem() {
                return localSubSystem;
            }

            @Override
            protected IAdmissionSubSystem getLdapSubSystem() {
                return ldapSubSystem;
            }
        };

        // we are in the same package de.ipb_halle.lbac.admission,
        // that's why we can use package-private directly
        logInProcess.nodeService = nodeService;
        logInProcess.memberService = memberService;
        logInProcess.memberShipService = memberShipService;
        logInProcess.credentialHandler = credentialHandler;
        logInProcess.ldapProps = ldapProps;
    }

    @Test
    void tryLogIn_LocalUser_ReturnsUser() {
        String login = "john";
        String pw = "secret";

        User user = new User();
        user.setSubSystemType(AdmissionSubSystemType.LOCAL);

        when(localSubSystem.lookup(eq(login), any(UserBean.class)))
                .thenReturn(user);
        when(localSubSystem.authenticate(eq(user), eq(pw), any(UserBean.class)))
                .thenReturn(true);

        User result = logInProcess.tryLogIn(login, pw);

        assertSame(user, result);
        verify(localSubSystem).lookup(eq(login), any(UserBean.class));
        verify(localSubSystem).authenticate(eq(user), eq(pw), any(UserBean.class));
        verifyNoInteractions(ldapSubSystem);
    }

    @Test
    void tryLogIn_NonLocalUser_SecondLookupAndReturn() {
        String login = "john";
        String pw = "secret";

        User user = new User();
        user.setSubSystemType(AdmissionSubSystemType.LDAP); // НЕ LOCAL

        when(localSubSystem.lookup(eq(login), any(UserBean.class)))
                .thenReturn(user, user);

        User result = logInProcess.tryLogIn(login, pw);

        assertSame(user, result);
        verify(localSubSystem, times(2)).lookup(eq(login), any(UserBean.class));
        verify(localSubSystem, never()).authenticate(any(), anyString(), any());
        verifyNoInteractions(ldapSubSystem);
    }

    @Test
    void tryLogIn_UnknownLocal_UsesLdapAndReturnsUser() {
        String login = "john";
        String pw = "secret";

        when(localSubSystem.lookup(eq(login), any(UserBean.class)))
                .thenReturn(null);

        User ldapUser = new User();
        ldapUser.setSubSystemType(AdmissionSubSystemType.LDAP);

        when(ldapSubSystem.lookup(eq(login), any(UserBean.class)))
                .thenReturn(ldapUser);
        when(ldapSubSystem.authenticate(eq(ldapUser), eq(pw), any(UserBean.class)))
                .thenReturn(true);

        User result = logInProcess.tryLogIn(login, pw);

        assertSame(ldapUser, result);

        verify(localSubSystem).lookup(eq(login), any(UserBean.class));
        verify(ldapSubSystem).lookup(eq(login), any(UserBean.class));
        verify(ldapSubSystem).authenticate(eq(ldapUser), eq(pw), any(UserBean.class));
    }
}
