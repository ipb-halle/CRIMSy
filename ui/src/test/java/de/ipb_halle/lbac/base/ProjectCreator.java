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
package de.ipb_halle.lbac.base;

import de.ipb_halle.lbac.admission.ACList;
import de.ipb_halle.lbac.admission.User;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import de.ipb_halle.lbac.project.ProjectType;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author fmauz
 */
public class ProjectCreator {
    
    private ProjectService projectService;
    private ACList publicReadAcl;
    private String projectName;
    private String projectDescription;
    private ACList projectAcl;
    private boolean deactivated;
    private ProjectType type;
    private SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd:HH.mm.ss.SSS");
    
    public ProjectCreator(ProjectService projectService, ACList acl) {
        this.projectService = projectService;
        this.publicReadAcl = acl;
        deactivated = false;
        setPropertiesToDefault();
    }
    
    public Project createAndSaveProject(User user) {
        Project project = new Project(type, projectName);
        project.setDescription(projectDescription);
        project.setOwner(user);
        project.setACList(projectAcl);
        project.setDeactivated(deactivated);
        project = projectService.saveProjectToDb(project);
        return project;
    }
    
    public ProjectCreator setPropertiesToDefault() {
        this.projectAcl = publicReadAcl;
        this.projectName = "Testproject " + SDF.format(new Date());
        this.projectDescription = "Testproject-Description " + SDF.format(new Date());
        this.type = ProjectType.BIOCHEMICAL_PROJECT;
        return this;
    }
    
    public ProjectCreator setProjectName(String projectName) {
        this.projectName = projectName;
        return this;
    }
    
    public ProjectCreator setProjectDescription(String description) {
        this.projectDescription = description;
        return this;
    }
    
    public ProjectCreator setProjectAcl(ACList acl) {
        this.projectAcl = acl;
        return this;
    }
    
    public ProjectCreator setType(ProjectType type) {
        this.type = type;
        return this;
    }

    public void setDeactivated(boolean deactivated) {
        this.deactivated = deactivated;
    }
    
}
