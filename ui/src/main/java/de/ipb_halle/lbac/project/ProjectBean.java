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
package de.ipb_halle.lbac.project;

import de.ipb_halle.lbac.admission.ACObjectBean;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.entity.ACList;
import de.ipb_halle.lbac.entity.ACObject;
import de.ipb_halle.lbac.entity.Group;
import de.ipb_halle.lbac.globals.ACObjectController;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Init;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.inject.Named;

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

    private List<Project> readableProjects = new ArrayList<>();

    private Project projectInFocus;

    @Init
    public void init() {
        readableProjects = projectService.loadReadableProjectsOfUser(userBean.getCurrentAccount());
    }

    private ACObjectController acObjectController;

    public ACObjectController getAcObjectController() {
        return acObjectController;
    }

    public List<Project> getReadableProjects() {
        return projectService.loadReadableProjectsOfUser(userBean.getCurrentAccount());
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

    }

    @Override
    public void applyAclChanges(int acobjectid, ACList newAcList) {

    }

    @Override
    public void cancelAclChanges() {

    }

    @Override
    public void startAclChange(List<Group> possibleGroupstoAdd) {
        acObjectController = new ACObjectController(
                projectInFocus,
                possibleGroupstoAdd,
                this,
                "Title of ACL-EDit of Project " + projectInFocus.getName());
    }

    public void startAclEdit(Project p) {
        projectInFocus = p;
        startAclChange(new ArrayList<>());
    }

}
