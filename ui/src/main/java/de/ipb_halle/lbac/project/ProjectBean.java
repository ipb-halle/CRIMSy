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

import de.ipb_halle.lbac.admission.ACListService;
import de.ipb_halle.lbac.admission.ACObjectBean;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.admission.ACObject;
import de.ipb_halle.lbac.admission.ACPermission;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.globals.ACObjectController;
import de.ipb_halle.lbac.admission.MemberService;
import de.ipb_halle.lbac.navigation.Navigator;
import de.ipb_halle.lbac.search.SearchResult;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.ejb.Init;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
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
public class ProjectBean implements Serializable, ACObjectBean {

    @Inject
    private ProjectService projectService;

    @Inject
    private UserBean userBean;

    @Inject
    private ACListService aclistService;

    private List<Project> readableProjects = new ArrayList<>();

    private Project projectInFocus;

    private Logger logger = LogManager.getLogger(ProjectBean.class);
    private ACObjectController acObjectController;
    private User user;
    
    @Inject
    private ProjectEditBean projectEditBean;

    @Inject
    private MemberService memberService;
    
    @Inject
    private Navigator navigator;

    public void reloadReadableProjects() {
        ProjectSearchRequestBuilder builder = new ProjectSearchRequestBuilder(user, 0, Integer.MAX_VALUE);
        SearchResult result = projectService.loadProjects(builder.buildSearchRequest());
        readableProjects = new ArrayList<>();
        readableProjects.addAll(result.getAllFoundObjects(
                Project.class, result.getNode()));

        Collections.sort(readableProjects, (p1, p2) -> {
            return p1.getName().compareTo(p2.getName());
        });
    }
    
    public void actionStartNewProjectCreation(){
        projectEditBean.startProjectCreation();
        navigator.navigate("project/projectEdit");
    }

    @Override
    public ACObjectController getAcObjectController() {
        return acObjectController;
    }

    public List<Project> getReadableProjects() {
        return readableProjects;

    }

    public Project getReadableProjectById(int projectId) {
        for (Project p : getReadableProjects()) {
            if (p.getId() == projectId) {
                return p;
            }
        }
        return null;
    }

    public void setCurrentAccount(@Observes LoginEvent evt) {
        this.user = evt.getCurrentAccount();
        reloadReadableProjects();
    }

    @Override
    public void applyAclChanges() {
        projectService.saveEditedProjectToDb(projectInFocus);
        reloadReadableProjects();
    }

    @Override
    public void cancelAclChanges() {
        projectInFocus = null;
        reloadReadableProjects();
    }

    @Override
    public void actionStartAclChange(ACObject aco) {
        projectInFocus = (Project) aco;
        acObjectController = new ACObjectController(
                projectInFocus,
                memberService.loadGroups(new HashMap<>()),
                this,
                projectInFocus.getName());
    }

    public boolean isPermissionEditAllowed(Project p) {
        return aclistService.isPermitted(ACPermission.permEDIT, p, user);
    }

    public void setProjectService(ProjectService projectService) {
        this.projectService = projectService;
    }

    public void setMemberService(MemberService memberService) {
        this.memberService = memberService;
    }

    public void setAclistService(ACListService aclistService) {
        this.aclistService = aclistService;
    }

}
