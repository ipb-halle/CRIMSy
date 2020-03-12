/*
 * Leibniz Bioactives Cloud
 * Copyright 2017 Leibniz-Institut f. Pflanzenbiochemie
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

import de.ipb_halle.lbac.entity.Group;
import de.ipb_halle.lbac.entity.Member;
import de.ipb_halle.lbac.entity.Membership;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.service.ACListService;
import de.ipb_halle.lbac.service.MemberService;
import de.ipb_halle.lbac.service.MembershipService;
import de.ipb_halle.lbac.service.NodeService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Logger;

@Named("groupMgrBean")
@SessionScoped
public class GroupMgrBean implements Serializable {

    private enum MODE {
        CREATE, READ, UPDATE, DELETE
    };

    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";

    private final static Long serialVersionUID = 1L;

    @Inject
    private ACListService aclistService;

  

    @Inject
    private NodeService nodeService;

    @Inject
    private MemberService memberService;

    @Inject
    private MembershipService membershipService;

    @Inject
    private UserBean userBean;

    private Group group;
    private transient Logger logger;
    private boolean nestedFlag;

    private MODE mode;

    private final String OPERATIONNAME_MANAGE_MEMBERS = "manageMembers";

    /**
     * default constructor
     */
    public GroupMgrBean() {
        this.mode = MODE.READ;
        this.nestedFlag = false;
        this.logger = Logger.getLogger(this.getClass().getName());
    }


    /**
     * Initialization depending on injected resources
     */
    @PostConstruct
    private void InitGroupMgrBean() {
        initGroup(); 
    }

    public void actionAddMembership(Member m) {
        this.membershipService.addMembership(this.group, m);
    }

    public void actionCreate() {
        this.memberService.save(this.group);
        initGroup();
        this.mode = MODE.READ;
    }

    /**
     * delete a group TODO: check for several preconditions: - permission -
     * subSystemType - node - no delete for adminGroup or publicGroup - ...
     */
    public void actionDelete() {
//      this.memberService.delete(this.group);
        initGroup();
        this.mode = MODE.READ;
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
        this.memberService.save(this.group);
        initGroup();
        this.mode = MODE.READ;
    }

    /**
     * return a title for the modal group dialog
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
     * get a list of groups
     */
    public List<Group> getGroupList() {
        return this.memberService.loadGroups(new HashMap<String, Object>());
    }

    /**
     * return a list of memberships for the currently active group
     */
    public List<Membership> getMembershipList() {
        Map<String, Object> cmap = new HashMap<String, Object>();
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
     * return the currently selected group. The group instance may be a detached
     * instance.
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
        HashMap<String, Object> cmap = new HashMap<String, Object>();
        cmap.put("subSystemType", new AdmissionSubSystemType[]{AdmissionSubSystemType.LOCAL, AdmissionSubSystemType.LDAP, AdmissionSubSystemType.LBAC_REMOTE});
        List<Member> members = new ArrayList<Member>();

        List<User> users = this.memberService.loadUsers(cmap);
        for (int i = users.size() - 1; i >= 0; i--) {
            if (users.get(i).getName().equals(GlobalAdmissionContext.NAME_OF_DEACTIVATED_USER)) {
                users.remove(i);
            }
        }

        members.addAll(users);
        members.addAll(this.memberService.loadGroups(cmap));
        return members;
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
    
    
}
