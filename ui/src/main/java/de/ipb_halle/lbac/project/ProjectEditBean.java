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
package de.ipb_halle.lbac.project;

import com.corejsf.util.Messages;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.entity.ACEntry;
import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.ACPermission;
import de.ipb_halle.lbac.entity.Group;
import de.ipb_halle.lbac.material.common.MaterialDetailType;
import de.ipb_halle.lbac.material.MaterialType;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.service.MemberService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class ProjectEditBean implements Serializable {

    @Inject
    private Navigator navigator;

    @Inject
    private ProjectService projectService;

    @Inject
    private UserBean userBean;

    private Logger logger = LogManager.getLogger(this.getClass().getName());

    private Project projectToEdit;
    private ProjectType currentProjectType = ProjectType.CHEMICAL_PROJECT;
    private boolean hasBudget = false;
    private Mode mode;
    private List<Group> possibleGroupsToAdd = new ArrayList<>();

    private Map<MaterialDetailType, ACList> detailTemplates = new HashMap<>();
    private ACList projectACL;

    private String projectDescription;
    private String projectName;

    private String projectBudget;

    @Inject
    private MemberService memberService;

    private final static String MESSAGE_BUNDLE = "de.ipb_halle.lbac.i18n.messages";

    public enum Mode {
        CREATE, EDIT
    }

    @PostConstruct
    public void init() {

        startProjectCreation();
    }

    public List<ACEntry> getACEntriesOfProject() {
        return new ArrayList<>(projectACL.getACEntries().values());
    }

    public List<Group> getAddableGroupsForProject() {
        ACList acInFocus = projectACL;
        List<Group> groupsToAdd = new ArrayList<>();
        for (Group g : possibleGroupsToAdd) {
            boolean alreadyIn = false;
            for (ACEntry ace : acInFocus.getACEntries().values()) {
                if (ace.getMemberId().equals(g.getId())) {
                    alreadyIn = true;
                }
            }
            if (!alreadyIn) {
                groupsToAdd.add(g);
            }
        }
        return groupsToAdd;
    }

    public void startProjectCreation() {
        mode = Mode.CREATE;
        possibleGroupsToAdd = memberService.loadGroups(new HashMap<>());
        projectACL = new ACList();
        for (MaterialDetailType mdt : MaterialDetailType.values()) {
            detailTemplates.put(mdt, new ACList());
        }
    }

    public void startProjectEdit(Project p) {
        mode = Mode.EDIT;
        this.projectToEdit = p;
        this.currentProjectType = p.getProjectType();
        possibleGroupsToAdd = memberService.loadGroups(new HashMap<>());

    }

    public List<ACEntry> getACEntriesForDetailRole(String detailType) {
        ACList acListOfDetail = detailTemplates.get(MaterialDetailType.valueOf(detailType));
        Collection<ACEntry> aces = acListOfDetail.getACEntries().values();
        return new ArrayList<>(aces);
    }

    public List<Group> getAddableGroupsForRoleTemplates(String materialDetail) {
        ACList acInFocus = detailTemplates.get(MaterialDetailType.valueOf(materialDetail));
        List<Group> groupsToAdd = new ArrayList<>();
        for (Group g : possibleGroupsToAdd) {
            boolean alreadyIn = false;
            for (ACEntry ace : acInFocus.getACEntries().values()) {
                if (ace.getMemberId().equals(g.getId())) {
                    alreadyIn = true;
                }
            }
            if (!alreadyIn) {
                groupsToAdd.add(g);
            }
        }
        return groupsToAdd;
    }

    public void addAceToRoleTemplate(Group g, String materialDetail) {
        logger.info("ADD mit Argument " + materialDetail);
        ACList acl = detailTemplates.get(MaterialDetailType.valueOf(materialDetail));
        acl.addACE(g, new ACPermission[]{});
    }

    public boolean hasCreationRight() {
        return true;
    }

    public List<ProjectType> getProjectTypes() {
        ArrayList<ProjectType> projectTypesWithoutDummy = new ArrayList<>(Arrays.asList(ProjectType.values()));
        projectTypesWithoutDummy.remove(0);
        return projectTypesWithoutDummy;
    }

    public void changeProjectType() {

    }

    public ProjectType getCurrentProjectType() {
        return currentProjectType;
    }

    public void setCurrentProjectType(ProjectType currentProjectType) {
        this.currentProjectType = currentProjectType;

    }

    public boolean isVisible(String panel) {
        for (MaterialType mt : currentProjectType.getMaterialTypes()) {
            if (mt.getPossibleDetailTypes().contains(MaterialDetailType.valueOf(panel))) {
                return true;
            }
        }
        return false;
    }

    public boolean isHasBudget() {
        return hasBudget;
    }

    public void setHasBudget(boolean hasBudget) {
        this.hasBudget = hasBudget;
    }

    public void addGroupToProjectACL(Group g) {
        this.projectACL.addACE(g, new ACPermission[]{});
    }

    public void setMemberService(MemberService memberService) {
        this.memberService = memberService;
    }

    public void removeAceFromProjectACL(ACEntry ace) {
        UUID entryToRemove = null;

        for (UUID id : this.projectACL.getACEntries().keySet()) {
            if (this.projectACL.getACEntries().get(id).getMemberId().equals(ace.getMemberId())) {
                entryToRemove = id;
            }
        }

        if (entryToRemove != null) {
            this.projectACL.getACEntries().remove(entryToRemove);
        }
    }

    public void removeAceFromRoleTemplateACL(ACEntry ace, String materialDetail) {
        UUID entryToRemove = null;
        ACList aclInFocus = detailTemplates.get(MaterialDetailType.valueOf(materialDetail));
        for (UUID id : aclInFocus.getACEntries().keySet()) {
            if (aclInFocus.getACEntries().get(id).getMemberId().equals(ace.getMemberId())) {
                entryToRemove = id;
            }
        }

        if (entryToRemove != null) {
            aclInFocus.getACEntries().remove(entryToRemove);
        }
    }

    public void saveProject() {

        if (mode == Mode.CREATE) {
            Project p = new Project(currentProjectType, projectName);
            p.setOwner(userBean.getCurrentAccount());
            p.setUserGroups(projectACL);

            if (isHasBudget()) {
                p.setBudget(Double.parseDouble(projectBudget));
            } else {
                p.setBudget(null);
            }

            p.setDescription(projectDescription);
            p.setDetailTemplates(detailTemplates);

            projectService.saveProjectToDb(p);
        }
        navigator.navigate("project/projectOverview");
    }

    public String getProjectDescription() {
        return projectDescription;
    }

    public void setProjectDescription(String projectDescription) {
        this.projectDescription = projectDescription;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getMaterialDetailPanelHeader(String detailType) {
        return Messages.getString(MESSAGE_BUNDLE, "projectEdit_detailinfo_" + detailType, null);
    }

}
