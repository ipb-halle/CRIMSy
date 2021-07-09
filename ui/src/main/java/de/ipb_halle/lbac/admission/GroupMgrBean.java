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
import de.ipb_halle.lbac.admission.group.DeactivateGroupOrchestrator;
import de.ipb_halle.lbac.material.JsfMessagePresenter;
import de.ipb_halle.lbac.material.MessagePresenter;

import de.ipb_halle.lbac.service.NodeService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Named("groupMgrBean")
@SessionScoped
public class GroupMgrBean implements Serializable {
    private String oldGroupName;

    private enum MODE {
        CREATE, READ, UPDATE, DELETE
    };

    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";

    private final static Long serialVersionUID = 1L;

    @Inject
    private transient MessagePresenter messagePresenter;

    @Inject
    private NodeService nodeService;

    @Inject
    private MemberService memberService;

    @Inject
    private MembershipService membershipService;

    @Inject
    private DeactivateGroupOrchestrator deactivateOrchestrator;

    private Group group;
    private transient Logger logger;
    private boolean nestedFlag;
    private GroupNameValidator groupNameValidator;
    private User currentUser;

    private MODE mode;

    private final String OPERATIONNAME_MANAGE_MEMBERS = "manageMembers";

    /**
     * default constructor
     */
    public GroupMgrBean() {
        this.mode = MODE.READ;
        this.nestedFlag = false;
        this.logger = LogManager.getLogger(this.getClass().getName());
    }

    /**
     * Initialization depending on injected resources
     */
    @PostConstruct
    private void InitGroupMgrBean() {
        this.groupNameValidator = new GroupNameValidator(memberService);
        initGroup();
    }

    public void actionAddMembership(Member m) {
        this.membershipService.addMembership(this.group, m);
    }

    public void actionCreate() {
        if (groupNameValidator.isGroupNameValide(this.group.getName())) {
            this.memberService.save(this.group);
            initGroup();
            messagePresenter.info("groupMgr_group_added");
        } else {
            messagePresenter.error("groupMgr_no_valide_name");
        }
        this.mode = MODE.READ;

    }

    /**
     * delete a group TODO: check for several preconditions: - permission -
     * subSystemType - node - no delete for adminGroup or publicGroup - ...
     */
    public void actionDelete() {
        deactivateOrchestrator.startGroupDeactivation(group.copy(), currentUser);
        this.memberService.deactivateGroup(this.group);

        initGroup();
        this.mode = MODE.READ;
        messagePresenter.info("groupMgr_group_deactivated");
    }

    public void actionDeleteMembership(Membership ms) {
        this.membershipService.removeMembership(ms);
    }

    /**
     * toggle the state of the nested flag
     */
    public void actionToggleNestedFlag() {
        this.nestedFlag = !this.nestedFlag;
    }

    public void actionUpdate() {
        if (mode == MODE.UPDATE) {
            if (this.group.getName().equals(oldGroupName)) {
                return;
            }
        }
        if (groupNameValidator.isGroupNameValide(this.group.getName())) {
            this.memberService.save(this.group);
            messagePresenter.info("groupMgr_group_edited");
        } else {
            messagePresenter.error("groupMgr_no_valide_name");
        }
        initGroup();
        this.mode = MODE.READ;
    }

    /**
     * return a title for the modal group dialog
     *
     * @return
     */
    public String getDialogTitle() {
        switch (this.mode) {
            case CREATE:
                return Messages.getString(MESSAGE_BUNDLE, "groupMgr_mode_createGroup", null);
            case DELETE:
                return Messages.getString(MESSAGE_BUNDLE, "groupMgr_mode_deleteGroup", null);
            case UPDATE:
                return Messages.getString(MESSAGE_BUNDLE, "groupMgr_mode_updateGroup", null);
        }
        return "";
    }

    /**
     * get a list of groups. Queries directly the database.
     *
     * @return
     */
    public List<Group> getGroupList() {
        return this.memberService.loadGroups(new HashMap<>());
    }

    /**
     * return a list of memberships for the currently active group
     *
     * @return
     */
    public List<Membership> getMembershipList() {
        Map<String, Object> cmap = new HashMap<>();
        if (this.group.getId() == null) {
            return new ArrayList<>();
        }
        cmap.put("group_id", this.group.getId());

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
     * return the currently selected group.
     *
     * @return
     */
    public Group getGroup() {
        return this.group;
    }

    /**
     * return the list of Member objects, which might become members in the
     * current group. This list must not include BUILTIN Member objects (@owner,
     *
     * @public, ...).
     *
     * @return a list of Member objects (users followed by groups)
     */
    public List<Member> getMemberList() {
        List<Member> members = new ArrayList<>();
        List<User> users = this.memberService.loadUsers(createcMapForSubSystem());
        removeDeactivatedUsers(users);
        members.addAll(users);
        members.addAll(this.memberService.loadGroups(createcMapForSubSystem()));
        return members;
    }

    private HashMap<String, Object> createcMapForSubSystem() {
        HashMap<String, Object> cmap = new HashMap<>();
        cmap.put(MemberService.PARAM_SUBSYSTEM_TYPE,
                new AdmissionSubSystemType[]{
                    AdmissionSubSystemType.LOCAL,
                    AdmissionSubSystemType.LDAP,
                    AdmissionSubSystemType.LBAC_REMOTE});
        return cmap;
    }

    private void removeDeactivatedUsers(List<User> users) {
        for (int i = users.size() - 1; i >= 0; i--) {
            if (users.get(i).getName()
                    .equals(GlobalAdmissionContext.NAME_OF_DEACTIVATED_USER)) {
                users.remove(i);
            }
        }
    }

    /**
     *
     *
     *
     * @param evt the LoginEvent scheduled by UserBean
     */
    public void setCurrentAccount(@Observes LoginEvent evt) {
        currentUser = evt.getCurrentAccount();
    }

    /**
     * Fetches the Members which are not present in the current group.
     *
     * @return
     */
    public List<Member> getAvailableMemberList() {
        List<Member> availableMember = getMemberList();
        List<Membership> membersInGroup = getMembershipList();
        for (Membership ms : membersInGroup) {
            availableMember.remove(ms.getMember());
        }
        return availableMember;

    }

    /**
     * create an initial instance of Group for further data entry
     */
    public void initGroup() {
        this.group = new Group();
        this.group.setNode(this.nodeService.getLocalNode());
        this.group.setSubSystemType(AdmissionSubSystemType.LOCAL);
    }

    public void setModeCreate() {
        initGroup();
        this.mode = MODE.CREATE;
    }

    public void setModeDelete() {
        this.mode = MODE.DELETE;
    }

    public void setModeUpdate() {
        this.mode = MODE.UPDATE;
    }

    /**
     * set the currently selected group instance
     */
    public void setGroup(Group g) {
        oldGroupName = g.getName();
        this.group = g;
    }

    public void refreshGroupList() {

    }

    /**
     * Checks if a the given operation is allowed on the group. On local groups
     * every operation is allowed, on remote groups none. On public group only
     * the managment of its members is allowed.
     *
     * @param g
     * @param operation
     * @return
     */
    public boolean isOperationForbidden(Group g, String operation) {
        boolean allowed = false;
        if (g.getNode().getId().equals(nodeService.getLocalNode().getId())) {
            allowed = true;
        }
        if (g.getNode().getPublicNode()) {
            if (operation.equals(OPERATIONNAME_MANAGE_MEMBERS)) {
                allowed = true;
            }
        }
        return !allowed;
    }

    public String getOPERATIONNAME_MANAGE_MEMBERS() {
        return OPERATIONNAME_MANAGE_MEMBERS;
    }

    public boolean isDeactivationForbidden(Group g) {
        return !memberService.canGroupBeDeactivated(g);
    }
}
