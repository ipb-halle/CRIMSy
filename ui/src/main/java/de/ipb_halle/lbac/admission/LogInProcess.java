package de.ipb_halle.lbac.admission;

import de.ipb_halle.lbac.service.NodeService;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Stateless;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;

/**
 *
 * @author halocal
 */
@Stateless
public class LogInProcess {

    @Inject
    NodeService nodeService;
    @Inject
    MemberService memberService;
    @Inject
    MembershipService membershipService;
    @Inject
    CredentialHandler credentialHandler;
    @Inject
    LdapProperties ldapProps;

    /**
     * Try to login a user either via LOCAL or LDAP.
     *
     * @param login user's login
     * @param password user's password
     * @return User object if login successful, null otherwise
     */
    public User tryLogIn(String login, String password) {
        UserBeanMockk userBeanMock = new UserBeanMockk();

        userBeanMock.setNodeService(nodeService);
        userBeanMock.setMemberService(memberService);
        userBeanMock.setMembershipService(membershipService);
        userBeanMock.setCredentialHandler(credentialHandler);
        userBeanMock.setLdapProperties(ldapProps);

        IAdmissionSubSystem localSub = AdmissionSubSystemType.LOCAL.getInstance();
        User u = localSub.lookup(login, userBeanMock);

        if (u != null) {
            // user is known to the local LBAC system
            if (u.getSubSystemType() == AdmissionSubSystemType.LOCAL) {
                return localSub.authenticate(u, password, userBeanMock) ? u : null;

            } else {
                // user is NOT a local LBAC user, perform lookup again
                // to guard against name changes, memberships etc.
                //u = userBeanMock.authLookup(u.getSubSystem(), login, password);
                return null;
            }
        } else {
            // User unknown locally -> try LDAP
            u = userBeanMock.authLookup(AdmissionSubSystemType.LDAP.getInstance(), login, password);

        }
        return u;
    }

    public class UserBeanMockk extends UserBean {

        private CredentialHandler testCredentialHandler;

        public void setNodeService(NodeService nodeService) {
            this.nodeService = nodeService;
        }

        public void setMemberService(MemberService memberService) {
            this.memberService = memberService;
        }

        public void setMembershipService(MembershipService membershipService) {
            this.membershipService = membershipService;
        }

        public void setLdapProperties(LdapProperties ldapProps) {
            super.ldapProperties = ldapProps;
            if (ldapProps != null) {
                ldapProps.setLdapEnabled(true);
            }
        }

        public void setCredentialHandler(CredentialHandler credentialHandler) {
            this.testCredentialHandler = credentialHandler;
        }

        @Override
        protected CredentialHandler getCredentialHandler() {
            if (testCredentialHandler != null) {
                return testCredentialHandler;
            }
            return super.getCredentialHandler();
        }

        public User authLookup(IAdmissionSubSystem ia, String logIn, String pw) {
            User user = ia.lookup(logIn, this);
            if ((user != null) && (ia.authenticate(user, pw, this))) {
                return user;
            }
            return null;
        }
    }
}
