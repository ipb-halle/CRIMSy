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
import de.ipb_halle.lbac.material.common.HazardInformation;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectType;
import java.io.Serializable;
import java.util.Date;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author fmauz
 */
public class MaterialEditState implements Serializable{

    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private Project currentProject;
    private Date currentVersiondate;
    private Material materialBeforeEdit;
    private Material materialToEdit;
    private Project defaultProject;
    private HazardInformation hazards=new HazardInformation();

    public MaterialEditState() {
        defaultProject = new Project(ProjectType.DUMMY_PROJECT, "bitte das Projekt auswählen");
        currentProject = defaultProject;
    }

    public MaterialEditState(
            Project currentProject,
            Date currentVersiondate,
            Material materialBeforeEdit,
            Material materialToEdit,
            HazardInformation hazards) {
        this.currentProject = currentProject;
        this.currentVersiondate = currentVersiondate;
        this.materialBeforeEdit = materialBeforeEdit;
        this.materialToEdit = materialToEdit;
        this.hazards = hazards;

        defaultProject = new Project(ProjectType.DUMMY_PROJECT, "bitte das Projekt auswählen");

    }

    public Project getCurrentProject() {
        return currentProject;
    }

    public void setCurrentProject(Project currentProject) {
        this.currentProject = currentProject;
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

    public Project getDefaultProject() {
        return defaultProject;
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

    public HazardInformation getHazards() {
        return hazards;
    }

}
