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
package de.ipb_halle.lbac.collections;

import com.corejsf.util.Messages;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.entity.ACEntry;
import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.ACPermission;
import de.ipb_halle.lbac.entity.Collection;
import de.ipb_halle.lbac.entity.Group;
import de.ipb_halle.lbac.service.ACListService;
import de.ipb_halle.lbac.service.CollectionService;
import de.ipb_halle.lbac.service.MembershipService;
import de.ipb_halle.lbac.service.MemberService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import javax.faces.view.ViewScoped;

import javax.inject.Inject;
import javax.inject.Named;
import org.apache.logging.log4j.Logger;import org.apache.logging.log4j.LogManager;

/**
 * Implementation of the api for a modal to edit the permission rights of an ac
 * list
 *
 * @author fmauz
 */
@ViewScoped
@Named
public class PermissionEditBean implements PermissionEdit, Serializable {

    private List<Group> groupsNotInAcList;
    private List<ACEntry> acEntries = new ArrayList<>();
    private ACList acList;
    private final String MESG_KEY_TITLE = "aclEdit_modal_title";
    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";

    private boolean dummy = true;
    private Logger logger = LogManager.getLogger(PermissionEditBean.class);

    public boolean isDummy() {
        return dummy;
    }

    public void setDummy(boolean dummy) {
        this.dummy = dummy;
    }

    @Inject
    protected ACListService acListService;

    @Inject
    private CollectionBean collectionBean;

    @Inject
    private UserBean userBean;

    @Inject
    private MembershipService membershipService;

    @Inject
    private MemberService memberService;

    @Inject
    private CollectionService collectionService;

    @Override
    public String getTitleOfModal() {
        return Messages.getString(MESSAGE_BUNDLE, MESG_KEY_TITLE, new String[]{collectionBean.getActiveCollection().getName()});
    }

    /**
     * Initialises the content of the bean. Must be triggered manually because
     * the combination of ViewScope and SinglePageApplication does not garantees
     * a new initialisiation if the corresponding modal is triggered to visible
     */
    public void initModal() {
        acEntries = getACEntriesOfGroups();
        groupsNotInAcList = memberService.loadGroups(new HashMap<>());
        acList = collectionBean.getActiveCollection().getACList();
        for (ACEntry m : acList.getACEntries().values()) {
            groupsNotInAcList.remove(m.getMember());
        }
    }

    /**
     * Saves the changes of the aclist into the database
     */
    public void applyChanges() {
        //Creates a new AC List based on the infomation of the old List
        ACList newAcList = new ACList();
        newAcList.setId(UUID.randomUUID());
        newAcList.setName(acList.getName());
        // Puts all Entries into the AC List
        for (ACEntry e : acEntries) {
            newAcList.addACE(e.getMember(), e.getAcPermissionArray());
        }
        // Saves the new AC List. If there are no changes or a AC List with
        // exactly the same permissions exists, instead of saving the new list,
        // the existing list will be given back
        newAcList = acListService.save(newAcList);

        //Actualising the aclist of the collection and saving it to the database
        collectionBean.getActiveCollection().setACList(newAcList);
        collectionService.save(collectionBean.getActiveCollection());
    }

    /**
     * Gets the AC Entries of the current active collection.Only Entries for
     * groups other entries will be ignored
     *
     * @return List of ACEntries with groups as member
     */
    @Override
    public List<ACEntry> getACEntriesOfGroups() {
        if (collectionBean.getActiveCollection() == null
                || collectionBean.getActiveCollection().getACList() == null
                || collectionBean.getActiveCollection().getACList().getACEntries() == null) {
            return new ArrayList<>();
        }
        ArrayList<ACEntry> entryList = new ArrayList<>();
        for (ACEntry e : collectionBean.getActiveCollection().getACList().getACEntries().values()) {
            if (e.getMember().isGroup()) {
                entryList.add(e);
            }
        }
        return entryList;
    }

    @Override
    public List<Group> getGroupsNotInAcList() {
        return groupsNotInAcList;
    }

    /**
     * Removes a group from the current, cached aclist.
     *
     * @param e ACEntry to remove
     */
    public void removeGroupFromAcList(ACEntry e) {
        Group g = (Group) e.getMember();
        groupsNotInAcList.add(g);
        acEntries.remove(e);
    }

    /**
     * Gets the active collection and checks if the user of the current accout
     * has the GRANT right on the collection.
     *
     * @param col collection to check GRANT right for
     * @return true if user has GRANT right
     */
    @Override
    public boolean isEditOfAclAllowed(Collection col) {
        return collectionBean
                .getActiveCollection()
                .getACList()
                .getPerm(
                        ACPermission.permGRANT,
                        userBean.getCurrentAccount()
                );
    }

    /**
     * Adds the selected group to the AC list with no permissions
     *
     * @param g Group to add
     */
    @Override
    public void addGroupToAcList(Group g) {
        ACEntry ace = new ACEntry();
        ace.setMember(g);
        acEntries.add(ace);
        groupsNotInAcList.remove(g);
    }

    // Default Getter and Setter
    // 
    public MembershipService getMembershipService() {
        return membershipService;
    }

    public void setMembershipService(MembershipService membershipService) {
        this.membershipService = membershipService;
    }

    public MemberService getMemberService() {
        return memberService;
    }

    public void setMemberService(MemberService memberService) {
        this.memberService = memberService;
    }

    public CollectionBean getCollectionBean() {
        return collectionBean;
    }

    public void setCollectionBean(CollectionBean collectionBean) {
        this.collectionBean = collectionBean;
    }

    public List<ACEntry> getAcEntries() {
        return acEntries;
    }

    public void setAcEntries(List<ACEntry> acEntries) {
        this.acEntries = acEntries;
    }

    public ACListService getAcListService() {
        return acListService;
    }

    public void setAcListService(ACListService acListService) {
        this.acListService = acListService;
    }

    public CollectionService getCollectionService() {
        return collectionService;
    }

    public void setCollectionService(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

}
