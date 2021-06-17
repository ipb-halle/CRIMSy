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
package de.ipb_halle.lbac.admission;

import de.ipb_halle.lbac.collections.Collection;
import de.ipb_halle.lbac.globals.NavigationConstants;
import de.ipb_halle.lbac.i18n.UIMessage;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.entity.InfoObject;
import de.ipb_halle.lbac.collections.CollectionService;
import de.ipb_halle.lbac.service.InfoObjectService;
import de.ipb_halle.lbac.service.NodeService;

import java.io.Serializable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Any;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Size;
import org.apache.commons.lang.exception.ExceptionUtils;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@SessionScoped
@Named("userBean")
public class UserBean implements Serializable {

    private final static long serialVersionUID = 1L;
    /**
     * Manages login, logout and password changes. Password changes are
     * restricted to local users. This Bean holds all necessary services for
     * accessing ACLs, Groups, Users, Memberships, etc.
     */

    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";

    @Inject
    private ACListService aclistService;

    @Inject
    private GlobalAdmissionContext globalAdmissionContext;

    @Inject
    private InfoObjectService infoObjectService;

    @Inject
    private LdapProperties ldapProperties;

    @Inject
    private MemberService memberService;

    @Inject
    private MembershipService membershipService;

    @Inject
    private NodeService nodeService;

    @Inject
    MembershipOrchestrator membershipOrchestrator;

    @Inject
    @Any
    private Event<LoginEvent> loginEvent;

    @Inject
    private CollectionService collectionService;

    @Inject
    private UserPluginSettingsBean pluginSettings;

    @Inject
    private UserTimeZoneSettingsBean timeZoneSettings;

    @Inject
    private Navigator navigator;

    private String login = "";

    @Size(max = 50)
    private String oldPassword = "";

    @Size(min = 8, max = 50)
    private String newPassword;

    @Size(min = 8, max = 50)
    private String newPasswordRepeat;

    private User currentAccount;

    private Map<ResourcePermission, Boolean> permissionCache;
    private transient Logger logger;

    /**
     * default constructor
     */
    public UserBean() {

    }

    /**
     * set up and load the various system accounts, groups, etc. into this
     * session for later use.
     */
    @PostConstruct
    public void init() {
        // current account
        this.logger = LogManager.getLogger(this.getClass().getName());
        this.permissionCache = new HashMap<ResourcePermission, Boolean>();
        this.oldPassword = "";
        this.newPassword = "";
        this.newPasswordRepeat = "";
        setCurrentAccount(this.globalAdmissionContext.getPublicAccount());

    }

    /**
     * perform a password change operation.
     */
    public void actionChangePassword() {
        CredentialHandler ch = this.globalAdmissionContext.getCredentialHandler();

        if (!ch.match(this.oldPassword, this.currentAccount.getPassword())) {
            // old password mismatch
            UIMessage.warn(MESSAGE_BUNDLE, "admission_password_change_failed");
            return;
        }

        if (this.newPassword.equals(this.newPasswordRepeat)
                && (this.newPassword.length() >= 8)
                && (!this.currentAccount.isPublicAccount())
                && (this.currentAccount.getSubSystemType() == AdmissionSubSystemType.LOCAL)) {

            this.currentAccount.setPassword(ch.computeDigest(this.newPassword));
            this.memberService.save(this.currentAccount);
            UIMessage.info(MESSAGE_BUNDLE, "admission_password_changed");
            return;
        }
        UIMessage.warn(MESSAGE_BUNDLE, "admission_password_change_failed");
    }

    /**
     * perform the login procedure, i.e. lookup the account and perform
     * authentication. Set the currentAccount.
     */
    public void actionLogin() {
        User u;

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress != null) {
            ipAddress = ipAddress.split(",")[0];
        }

        if (this.globalAdmissionContext.intruderLockoutCheck(this.login, ipAddress)) {
            navigator.navigate(NavigationConstants.DEFAULT);
            UIMessage.warn(MESSAGE_BUNDLE, "admission_login_failure");
            return;
        }

        IAdmissionSubSystem ia = AdmissionSubSystemType.LOCAL.getInstance();
        u = ia.lookup(this.login, this);
        if (u != null) {
            // user is known to the local LBAC system
            if (u.getSubSystemType() == AdmissionSubSystemType.LOCAL) {
                // user is a local LBAC user
                if (ia.authenticate(u, this.oldPassword, this)) {
                    this.oldPassword = "";
                    setCurrentAccount(u);
                    UIMessage.info(MESSAGE_BUNDLE, "admission_login_succeeded");
                    navigator.navigate(NavigationConstants.DEFAULT);
                    this.globalAdmissionContext.intruderLockoutUnlock(this.login, ipAddress);
                    return;
                }
            } else {
                // user is NOT a local LBAC user, perform lookup again
                // to guard against name changes, memberships etc.
                if (authLookup(u.getSubSystem())) {
                    navigator.navigate(NavigationConstants.DEFAULT);
                    UIMessage.info(MESSAGE_BUNDLE, "admission_login_succeeded");
                    this.globalAdmissionContext.intruderLockoutUnlock(this.login, ipAddress);
                    return;
                }
            }
        } else {
            // user is yet unknown to the local LBAC system
            // lookup in LDAP
            if (authLookup(AdmissionSubSystemType.LDAP.getInstance())) {
                navigator.navigate(NavigationConstants.DEFAULT);
                UIMessage.info(MESSAGE_BUNDLE, "admission_login_succeeded");
                this.globalAdmissionContext.intruderLockoutUnlock(this.login, ipAddress);
                return;
            }
        }

        UIMessage.warn(MESSAGE_BUNDLE, "admission_login_failure");
        this.globalAdmissionContext.intruderLockoutLock(this.login, ipAddress);
    }

    /**
     * clear current user and make public user
     */
    public void actionLogout() {
        setCurrentAccount(this.globalAdmissionContext.getPublicAccount());
        navigator.initStartPage();
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
    }

    /**
     * update account information
     */
    public void actionModify() {
        if (!this.currentAccount.isPublicAccount()) {
            if (this.currentAccount.getSubSystemType() == AdmissionSubSystemType.LOCAL) {
                try {
                    this.currentAccount = this.memberService.save(this.currentAccount);
                    UIMessage.info(MESSAGE_BUNDLE, "admission_account_updated");
                } catch (Exception e) {
                    logger.error(ExceptionUtils.getStackTrace(e));
                    UIMessage.error(MESSAGE_BUNDLE, "admission_account_updated_failed");
                }
            }
        }
    }

    /**
     * obtain the list of remote nodes, a detached user instance as well as all
     * groups the user is memberOf and announce this to the nodes via
     * MembershipWebClient. The client implements Callable and uses a
     * ManagedExecutorService to perform this task.
     *
     * @param u the user to announce cloud wide
     */
    private void announceUser(User u) {

        User user = this.memberService.loadUserById(u.getId());
        user.obfuscate();
        membershipOrchestrator.startMemberShipAnnouncement(user);

    }

    /**
     * perform an account lookup and authentication and set the currentAccount
     * on success. This method is called by actionLogin for non-LBAC admission
     * subsystems.
     *
     * @param ia the AdmissionSubSystem
     * @return true on success
     */
    private boolean authLookup(IAdmissionSubSystem ia) {
        User u = ia.lookup(this.login, this);
        if ((u != null) && (ia.authenticate(u, this.oldPassword, this))) {
            this.oldPassword = "";
            setCurrentAccount(u);
            return true;
        }
        return false;
    }

    protected CredentialHandler getCredentialHandler() {
        return this.globalAdmissionContext.getCredentialHandler();
    }

    public User getCurrentAccount() {
        return currentAccount;
    }

    protected LdapProperties getLdapProperties() {
        return this.ldapProperties;
    }

    public String getLogin() {
        return login;
    }

    protected MemberService getMemberService() {
        return this.memberService;
    }

    protected MembershipService getMembershipService() {
        return this.membershipService;
    }

    protected NodeService getNodeService() {
        return this.nodeService;
    }

    public UserPluginSettingsBean getPluginSettings() {
        return pluginSettings;
    }

    public UserTimeZoneSettingsBean getTimeZoneSettings() {
        return timeZoneSettings;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public String getNewPasswordRepeat() {
        return newPasswordRepeat;
    }

    public String getOldPassword() {
        return oldPassword;
    }

    public Group getPublicGroup() {
        return this.globalAdmissionContext.getPublicGroup();
    }

    /**
     * Check if current user is permitted to access requested resource
     *
     * @param resource name of the resource to check permission
     * @param permission type of permission (READ, EDIT, UPDATE, CREATE, DELETE,
     * GRANT, SUPER)
     * @return
     */
    public boolean getPermission(String resource, String permission) {

        try {
            ACPermission acp = ACPermission.valueOf(permission);
            ResourcePermission rp = new ResourcePermission(resource, acp);

            Boolean v = this.permissionCache.get(rp);
            if (v != null) {
                return v.booleanValue();
            }

            InfoObject ie = this.infoObjectService.loadByKey(resource);
            if (ie == null) {
                this.logger.warn(String.format("getPermission() unknown resource %s", resource));
            } else {
                if (this.aclistService.isPermitted(ACPermission.valueOf(permission), ie, this.currentAccount)) {
                    this.permissionCache.put(rp, Boolean.TRUE);
                    return true;
                }
            }
            this.permissionCache.put(rp, Boolean.FALSE);
        } catch (Exception e) {
            this.logger.warn(String.format("getPermission(%s, %s) caught an exception", resource, permission), (Throwable) e);
        }
        return false;
    }

    public boolean isAdminAccount() {
        return currentAccount != null
                && currentAccount.getId().equals(globalAdmissionContext.getAdminAccount().getId());
    }

    /**
     * Set the currently logged in account for this session. If no user is
     * currently logged in (session start or upon logout), this method will be
     * called with publicAccount. This method will notify observers in other
     * beans via firing a LoginEvent. If the u is NOT the publicAccount, remote
     * nodes are informed via MembershipAnnouncement about the account and its
     * memberships.
     *
     * @param u the new currentAccount
     */
    private void setCurrentAccount(User u) {

        this.currentAccount = u;
        this.permissionCache.clear();
        this.loginEvent.fire(new LoginEvent(u));
        if (!u.equals(this.globalAdmissionContext.getPublicAccount())) {
            announceUser(u);
        }
    }

    public void setLogin(String l) {
        this.login = l;
    }

    public void setNewPassword(String n) {
        this.newPassword = n;
    }

    public void setNewPasswordRepeat(String n) {
        this.newPasswordRepeat = n;
    }

    public void setOldPassword(String o) {
        this.oldPassword = o;
    }

    /**
     * Checks if there is at least one collection for that the current user has
     * an upload permission or is the owner. The public account will not have a
     * permmision to upload
     *
     * @return
     */
    public boolean hasUploadPermission() {
        if (currentAccount.getId().toString().equals(GlobalAdmissionContext.PUBLIC_ACCOUNT_ID)) {
            return false;
        }
        List<Collection> colls = collectionService.load(null);
        for (Collection c : colls) {
            boolean isOwner = c.getOwner().equals(currentAccount);
            if (isOwner || aclistService.isPermitted(ACPermission.permCREATE, c, currentAccount)) {
                return true;
            }
        }
        return false;
    }

    public boolean isComponentAccessable(String s) {
        if (s.equals("InhouseDB")) {
            return !currentAccount.isPublicAccount();
        } else {
            return true;
        }
    }

}
