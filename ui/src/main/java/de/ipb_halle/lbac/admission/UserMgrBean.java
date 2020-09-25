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

import com.corejsf.util.Messages;

import de.ipb_halle.lbac.service.NodeService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Named("userMgrBean")
@SessionScoped
public class UserMgrBean implements Serializable {

    /**
     * This bean manages users. This includes creating, updating and deleting of
     * local users and managing memberships for all type of users.
     *
     * TODO: more restrictive permission and plausibility checks for (all?)
     * action* methods.
     */
    private enum MODE {
        CREATE, READ, UPDATE, DELETE
    };

    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";

    public final static String USERMGR_PERM_KEY = "ADMISSION_MGR_ENABLE";

    private final static Long serialVersionUID = 1L;

    @Inject
    private ACListService aclistService;

    @Inject
    private NodeService nodeService;

    @Inject
    private MemberService memberService;

    @Inject
    private MembershipOrchestrator membershipOrchestrator;

    @Inject
    private MembershipService membershipService;

    @Inject
    private UserBean userBean;

    private User user;
    private transient CredentialHandler credentialHandler;
    private transient Logger logger;
    private boolean nestedFlag;

    private MODE mode;

    private String tempPassword;

    /**
     * default constructor
     */
    public UserMgrBean() {
        this.mode = MODE.READ;
        this.credentialHandler = new CredentialHandler();
        this.nestedFlag = false;
        this.tempPassword = "";
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    /**
     * Initialization depending on injected resources
     */
    @PostConstruct
    private void initUserMgrBean() {
        // uses nodeService, which is not available in constructor!
        initUser();
    }

    /**
     * add a Membership for the current user
     *
     * @param g
     */
    public void actionAddMembership(Group g) {
        this.membershipService.addMembership(g, this.user);
    }

    /**
     * create a new user
     */
    public void actionCreate() {
        this.logger.info("actionCreate(): creating Account");
        this.user.setPassword(this.credentialHandler.computeDigest(this.tempPassword));
        this.user = this.memberService.save(this.user);
        this.tempPassword = "";
        Group publicGroup = memberService.loadGroupById(
                GlobalAdmissionContext.PUBLIC_GROUP_ID);

        this.membershipService.addMembership(publicGroup, user);
        initUser();
        this.mode = MODE.READ;
        this.logger.info("actionCreate() finished.");
    }

    /**
     * Deactivates a user in the database and removes all of its memberships. It
     * will got a name 'deactivated' and all its private informations are
     * cleared.
     */
    public void actionDeactivateUser() {
        this.user.setLogin(GlobalAdmissionContext.NAME_OF_DEACTIVATED_USER);
        this.user.setEmail("");
        this.user.setPassword("");
        this.user.setPhone("");
        this.user.setName(GlobalAdmissionContext.NAME_OF_DEACTIVATED_USER);
        this.user.setSubSystemData("");
        this.user.setSubSystemType(AdmissionSubSystemType.LOCAL);

        Set<Membership> members = membershipService.loadMemberOf(user);
        for (Membership ms : members) {
            this.membershipService.removeMembership(ms);
        }
        this.memberService.save(user);
        this.membershipOrchestrator.startMemberShipAnnouncement(user);
        initUser();
        this.mode = MODE.READ;
    }

    /**
     * delete a membership
     *
     * @param ms
     */
    public void actionDeleteMembership(Membership ms) {
        this.membershipService.removeMembership(ms);
    }

    /**
     * change a password
     */
    public void actionChangePassword() {
        this.user.setPassword(this.credentialHandler.computeDigest(this.tempPassword));
        this.user = this.memberService.save(this.user);
        this.tempPassword = "";
        this.mode = MODE.READ;
    }

    /**
     * toggle the state of the nested flag
     */
    public void actionToggleNestedFlag() {
        this.nestedFlag = !this.nestedFlag;
    }

    public void actionUpdate() {
        this.memberService.save(this.user);
        this.tempPassword = "";
        initUser();
        this.mode = MODE.READ;
    }

    /**
     * return a title for the modal user dialog
     *
     * @return
     */
    public String getDialogTitle() {
        switch (this.mode) {
            case CREATE:
                return Messages.getString(MESSAGE_BUNDLE, "userMgr_mode_createUser", null);
            case DELETE:
                return Messages.getString(MESSAGE_BUNDLE, "userMgr_mode_deleteUser", null);
            case UPDATE:
                return Messages.getString(MESSAGE_BUNDLE, "userMgr_mode_updateUser", null);
        }
        return "";
    }

    /**
     * get a list of local(i.e.AdmissionSubSystemType.LOCAL) group objects, the
     * current user is not a direct member
     *
     * @return
     */
    public List<Group> getGroupList() {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(MemberService.PARAM_NODE_ID, this.nodeService.getLocalNode().getId());
        cmap.put(MemberService.PARAM_SUBSYSTEM_TYPE, AdmissionSubSystemType.LOCAL);
        // TODO: filter out groups, the current user is already a member of
        return this.memberService.loadGroups(cmap);
    }

    /**
     * return a list of memberships for the currently active user
     *
     * @return
     */
    public List<Membership> getMembershipList() {
        Map<String, Object> cmap = new HashMap<>();
        if (this.user.getId() == null) {
            return new ArrayList<Membership>();
        }
        cmap.put("member_id", this.user.getId());

        // nestedFlag == true means show all (nested & direct)!
        if (!this.nestedFlag) {
            cmap.put("nested", Boolean.FALSE);
        }
        return this.membershipService.load(cmap);
    }

    public String getMode() {
        return this.mode.toString();
    }

    public boolean getNestedFlag() {
        return this.nestedFlag;
    }

    /**
     * return the cleartext temporary password for the current account
     *
     * @return
     */
    public String getTempPassword() {
        return this.tempPassword;
    }

    /**
     * return the currently selected user.The user instance may be a detached
     * instance.
     *
     * @return
     */
    public User getUser() {
        return this.user;
    }

    /**
     * return the list of users
     *
     * @return
     */
    public List<User> getUserList() {
        Map<String, Object> cmap = new HashMap<>();
        cmap.put(MemberService.PARAM_NODE_ID, this.nodeService.getLocalNode().getId());
        cmap.put(MemberService.PARAM_SUBSYSTEM_TYPE, 
                new AdmissionSubSystemType[]{AdmissionSubSystemType.LOCAL, AdmissionSubSystemType.LDAP});
        List<User> users = this.memberService.loadUsers(cmap);
        for (int i = users.size() - 1; i >= 0; i--) {
            if (users.get(i).getName().equals(GlobalAdmissionContext.NAME_OF_DEACTIVATED_USER)) {
                users.remove(i);
            }
        }
        return users;
    }

    /**
     * create an initial instance of User for further data entry
     */
    public void initUser() {
        this.user = new User();
        this.user.setNode(this.nodeService.getLocalNode());
        this.user.setSubSystemType(AdmissionSubSystemType.LOCAL);
    }

    /**
     * set create mode
     */
    public void setModeCreate() {
        initUser();
        this.mode = MODE.CREATE;
    }

    public void setModeDelete() {
        this.mode = MODE.DELETE;
    }

    public void setModeUpdate() {
        this.mode = MODE.UPDATE;
    }

    /**
     * set the temporary cleartext password for current account.
     *
     * @param p the cleartext temporary password. NOTE: this method trims
     * whitespace!
     */
    public void setTempPassword(String p) {
        this.tempPassword = p.trim();
    }

    /**
     * set the currently selected user instance
     *
     * @param u
     */
    public void setUser(User u) {
        this.user = u;
    }

    public void refreshUserList() {

    }
}
