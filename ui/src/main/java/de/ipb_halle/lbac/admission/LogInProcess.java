package de.ipb_halle.lbac.admission;

import de.ipb_halle.lbac.service.NodeService;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

/**
 *
 * @author halocal
 */
@Stateless
public class LogInProcess {

    @Inject
    private NodeService nodeService;

    @Inject
    private MemberService memberService;

    @Inject
    private MembershipService membershipService;

    @Inject
    private LdapProperties ldapProps;

    @Inject
    private GlobalAdmissionContext globalAdmissionContext;

    /**
     * Try to login a user either via LOCAL or LDAP.
     *
     * @param login user's login
     * @param password user's password
     * @return User object if login successful, null otherwise
     */
    public User tryLogIn(String login, String password) {
        UserBeanMock userBeanMock = new UserBeanMock();

        userBeanMock.setNodeService(nodeService);
        userBeanMock.setMemberService(memberService);
        userBeanMock.setMembershipService(membershipService);
        userBeanMock.setLdapProperties(ldapProps);
        userBeanMock.setGlobalAdmissionContext(globalAdmissionContext);

        /*
         * This is the same logic like in UserBean.actionLogin().
         */
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
        	IAdmissionSubSystem ldapSub = AdmissionSubSystemType.LDAP.getInstance();
        	u = ldapSub.lookup(login, userBeanMock);

            if ((u != null) && (ldapSub.authenticate(u, password, userBeanMock))) {
                return u;
            }

            return null;
        }
    }

    /* 
     * This mock class is used because UserBean is SessionScoped and cannot simply
     * be injected into this stateless service.
     */
    public class UserBeanMock extends UserBean {
        public void setNodeService(NodeService nodeService) {
            this.nodeService = nodeService;
        }

        public void setGlobalAdmissionContext(GlobalAdmissionContext globalAdmissionContext) {
			this.globalAdmissionContext = globalAdmissionContext;
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
    }
}
