package de.ipb_halle.lbac.admission;

import de.ipb_halle.lbac.globals.NavigationConstants;
import de.ipb_halle.lbac.i18n.UIMessage;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.service.NodeService;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.security.enterprise.credential.Credential;

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
    MembershipService memberShipService;
    @Inject
    CredentialHandler credentialHandler;
    @Inject
    LdapProperties ldapProps;

    public User tryLogIn(String logIn, String pw) {

        UserBeanMock userBeanMock = new UserBeanMock();

        userBeanMock.setNodeService(nodeService);
        userBeanMock.setMemberService(memberService);
        userBeanMock.setMemberShipService(memberShipService);
        userBeanMock.setCredentialHandler(credentialHandler);
        userBeanMock.setLdapProperties(ldapProps);

        IAdmissionSubSystem ia = AdmissionSubSystemType.LOCAL.getInstance();
        User u = ia.lookup(logIn, userBeanMock);
        if (u != null) {
            // user is known to the local LBAC system
            if (u.getSubSystemType() == AdmissionSubSystemType.LOCAL) {
                // user is a local LBAC user
                ia.authenticate(u, pw, userBeanMock);

            } else {
                // user is NOT a local LBAC user, perform lookup again
                // to guard against name changes, memberships etc.
                u = ia.lookup(logIn, userBeanMock);
            }
        } else {
            // user is yet unknown to the local LBAC system
            // lookup in LDAP
            u = userBeanMock.authLookup(AdmissionSubSystemType.LDAP.getInstance(), logIn, pw);
        }

        return u;
    }

    class UserBeanMock extends UserBean {

        private NodeService nodeService;
        private CredentialHandler credentialHandler;
        private MemberService memberService;
        private MembershipService membershipService;
        private LdapProperties ldapProperties;

        public void setNodeService(NodeService nodeService) {
            this.nodeService = nodeService;
        }

        public void setCredentialHandler(CredentialHandler handler) {
            this.credentialHandler = handler;
        }

        public void setMemberService(MemberService ms) {
            this.memberService = ms;
        }

        public void setMemberShipService(MembershipService membershipService) {
            this.membershipService = membershipService;
        }

        public void setLdapProperties(LdapProperties ldapProps) {
            this.ldapProperties = ldapProps;
        }

        @Override
        public NodeService getNodeService() {
            return this.nodeService;
        }

        public CredentialHandler getCredentialHandler() {
            return this.credentialHandler;
        }

        public MemberService getMemberService() {
            return memberService;
        }

        @Override
        public LdapProperties getLdapProperties() {
            return ldapProperties;
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
