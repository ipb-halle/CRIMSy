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
package de.ipb_halle.lbac.globals;

import com.corejsf.util.Messages;
import de.ipb_halle.lbac.admission.ACObjectBean;
import de.ipb_halle.lbac.entity.ACEntry;
import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.ACObject;
import de.ipb_halle.lbac.entity.ACPermission;
import de.ipb_halle.lbac.entity.Group;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.CloseEvent;

/**
 *
 * @author fmauz
 */
public class ACObjectController {

    private ACList acList;
    private final String MESG_KEY_TITLE = "aclEdit_modal_title";
    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";
    private Logger logger = LogManager.getLogger(ACObjectController.class);
    private ACObjectBean bean;
    private final ACObject objectToChange;
    private List<Group> possibleGroupsToAdd = new ArrayList<>();
    private boolean aclEdited;
    private String title;
    private ACList originalAcl;

    public ACObjectController(
            ACObject objectToChange,
            List<Group> possibleGroupsToAdd,
            ACObjectBean bean,
            String title) {
        this.bean = bean;
        this.objectToChange = objectToChange;
        aclEdited = false;
        this.title = title;
        this.possibleGroupsToAdd = possibleGroupsToAdd;
        originalAcl = objectToChange.getACList();
        objectToChange.setACList(copyAcList(originalAcl));
    }

    public String getTitleOfModal() {
        return Messages.getString(MESSAGE_BUNDLE, MESG_KEY_TITLE, new String[]{title});
    }

    public List<ACEntry> getAcEntries() {
        return new ArrayList<>(objectToChange.getACList().getACEntries().values());
    }

    public void removeGroupFromAcList(ACEntry ace) {

        objectToChange.getACList().getACEntries().remove(ace.getMemberId());
    }

    public List<Group> getGroupsNotInAcList() {
        List<Group> groupsNotInAcList = new ArrayList<>();
        for (Group g : possibleGroupsToAdd) {
            groupsNotInAcList.add(g);
        }
        for (UUID aceId : objectToChange.getACList().getACEntries().keySet()) {
            for (int i = groupsNotInAcList.size() - 1; i >= 0; i--) {
                if (groupsNotInAcList.get(i).getId().equals(aceId)) {
                    groupsNotInAcList.remove(i);
                }
            }
        }
        return groupsNotInAcList;
    }

    public void addGroupToAcList(Group group) {
        aclEdited = true;
        objectToChange.getACList().addACE(group, new ACPermission[]{});
    }

    public void actionApplyChanges() {
        bean.applyAclChanges(0, acList);
    }

    public void actionCancelChanges() {
        objectToChange.setACList(originalAcl);
        bean.cancelAclChanges();
    }

    public List<Group> getPossibleGroupsToAdd() {
        return possibleGroupsToAdd;
    }

    public void setPossibleGroupsToAdd(List<Group> possibleGroupsToAdd) {
        this.possibleGroupsToAdd = possibleGroupsToAdd;
    }

    public void handleClose(CloseEvent event) {
        if (!aclEdited) {
            actionCancelChanges();
        }

    }

    public void saveNewAcList() {
        aclEdited = true;
        actionApplyChanges();
    }

    private ACList copyAcList(ACList original) {
        ACList newAcl = new ACList();
        newAcl.setName(original.getName());
        for (UUID aceid : original.getACEntries().keySet()) {
            newAcl.addACE(
                    original.getACEntries().get(aceid).getMember(),
                    original.getACEntries().get(aceid).getAcPermissionArray()
            );
        }
        return newAcl;
    }

}
