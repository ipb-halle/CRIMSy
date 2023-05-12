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
package de.ipb_halle.lbac.material.common.bean;

import de.ipb_halle.lbac.material.Material;
import de.ipb_halle.lbac.material.MessagePresenter;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectType;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.LinkedHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class MaterialEditState implements Serializable {

    private static final long serialVersionUID = 1L;

    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private Project currentProject;
    private Date currentVersiondate;
    private Material materialBeforeEdit;
    private Material materialToEdit;
    private Project defaultProject;
    private MaterialHazardBuilder hazardController;

    private LinkedHashMap<Integer, Project> possibleProjects;

    private transient MessagePresenter messagePresenter;

    public MaterialEditState(MessagePresenter messagePresenter) {        
        possibleProjects = new LinkedHashMap<> ();
        defaultProject = new Project(
                ProjectType.DUMMY_PROJECT, 
                messagePresenter.presentMessage("materialCreation_dummyProject"));
        possibleProjects.put(0, defaultProject);
        currentProject = defaultProject;
    }

    public MaterialEditState(
            Project currentProject,
            Date currentVersiondate,
            Material materialBeforeEdit,
            Material materialToEdit,
            MaterialHazardBuilder hazards,
            MessagePresenter messagePresenter) {
        possibleProjects = new LinkedHashMap<> ();
        this.currentProject = currentProject;
        this.currentVersiondate = currentVersiondate;
        this.materialBeforeEdit = materialBeforeEdit;
        this.materialToEdit = materialToEdit;
        this.hazardController = hazards;
    }

    public Collection<Project> getPossibleProjects() {
        return possibleProjects.values();
    }

    protected void addPossibleProjects(Collection<Project> projects) {
        for(Project p : projects) {
            possibleProjects.put(p.getId(), p);
        }
    }

    public Project getCurrentProject() {
        return currentProject;
    }

    public void setCurrentProject(Project p) {
        currentProject = p;
    }

    public int getCurrentProjectId() {
        return currentProject.getId();
    }

    public void setCurrentProjectId(int id) {
        this.currentProject = Objects.requireNonNull(possibleProjects.get(id));
    }

    public Date getCurrentVersiondate() {
        return currentVersiondate;
    }

    public void setCurrentVersiondate(Date currentVersiondate) {
        this.currentVersiondate = currentVersiondate;
    }

    public Material getMaterialBeforeEdit() {
        return materialBeforeEdit;
    }

    public void setMaterialBeforeEdit(Material materialBeforeEdit) {
        this.materialBeforeEdit = materialBeforeEdit;
    }

    public Material getMaterialToEdit() {
        return materialToEdit;
    }

    public void setMaterialToEdit(Material materialToEdit) {
        this.materialToEdit = materialToEdit;
    }

    public void changeVersionDateToPrevious(Date date) {
        setCurrentVersiondate(
                getMaterialBeforeEdit()
                        .getHistory()
                        .getPreviousKey(date));

    }

    public void changeVersionDateToNext(Date date) {
        setCurrentVersiondate(
                getMaterialBeforeEdit()
                        .getHistory()
                        .getFollowingKey(date));
    }

    public boolean isMostRecentVersion() {
        return materialToEdit.getHistory().isMostRecentVersion(currentVersiondate);
    }

    public MaterialHazardBuilder getHazardController() {
        return hazardController;
    }

}
