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
package de.ipb_halle.lbac.exp;

import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectSearchConditionBuilder;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.search.SearchResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author fmauz
 */
public class ExpProjectController {

    private Project choosenProject;
    private List<Project> choosableProjects = new ArrayList<>();
    private ProjectService projectService;
    private User currentUser;
    private Map<String, Project> nameMap = new HashMap<>();

    public ExpProjectController(ProjectService projectService, User currentUser) {
        choosableProjects = new ArrayList<>();
        this.projectService = projectService;
        this.currentUser = currentUser;
        if (currentUser != null) {
            ProjectSearchConditionBuilder builder = new ProjectSearchConditionBuilder(currentUser, 0, Integer.MAX_VALUE);
            builder.addDeactivated(false);
            SearchResult result = projectService.loadProjects(builder.buildSearchRequest());
            choosableProjects = result.getAllFoundObjects(Project.class, result.getNode());
            this.nameMap = new HashMap<>();
            for (Project p : choosableProjects) {
                nameMap.put(p.getName(), p);
            }

        }
    }

    public List<Project> getChoosableProjects() {
        return choosableProjects;
    }

    public Project getChoosenProject() {
        return choosenProject;
    }

    public void setChoosenProject(Project choosenProject) {
        this.choosenProject = choosenProject;
    }

    public Project getProjectByName(String name) {
        return nameMap.get(name);

    }
}
