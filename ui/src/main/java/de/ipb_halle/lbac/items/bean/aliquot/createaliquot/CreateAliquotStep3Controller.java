/*
 * Cloud Resource & Information Management System (CRIMSy)
 * Copyright 2022 Leibniz-Institut f. Pflanzenbiochemie
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
package de.ipb_halle.lbac.items.bean.aliquot.createaliquot;

import java.io.Serializable;
import java.util.List;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.admission.UserBean;
import de.ipb_halle.lbac.items.Item;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectSearchRequestBuilder;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.SearchResult;

/**
 * Controls the third step of the create aliquot wizard: The user defines a
 * project.
 * 
 * @author flange
 */
public class CreateAliquotStep3Controller implements Serializable {
    private static final long serialVersionUID = 1L;

    private final List<Project> availableProjects;
    private Project selectedProject;

    public CreateAliquotStep3Controller(Item parentItem, ProjectService projectService, UserBean userBean) {
        availableProjects = loadReadableProjects(userBean.getCurrentAccount(), projectService);
        selectedProject = parentItem.getProject();
    }

    private List<Project> loadReadableProjects(User user, ProjectService projectService) {
        ProjectSearchRequestBuilder builder = new ProjectSearchRequestBuilder(user, 0, Integer.MAX_VALUE);
        builder.setDeactivated(false);
        SearchResult response = projectService.loadProjects(builder.build());
        return response.getAllFoundObjects(Project.class, response.getNode());
    }

    /*
     * Getters/setters
     */
    public List<Project> getAvailableProjects() {
        return availableProjects;
    }

    public Project getSelectedProject() {
        return selectedProject;
    }

    public void setSelectedProject(Project selectedProject) {
        this.selectedProject = selectedProject;
    }
}