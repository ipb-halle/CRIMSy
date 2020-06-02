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
package de.ipb_halle.lbac.container.bean;

import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.entity.User;
import de.ipb_halle.lbac.project.Project;
import de.ipb_halle.lbac.project.ProjectService;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
public class ContainerEditBean implements Serializable {

    @Inject
    private ContainerOverviewBean overviewBean;

    @Inject
    private ContainerService containerService;

    @Inject
    private ProjectService projectService;

    private Container containerToCreate;
    private Container originalContainer;
    private Container containerToEdit;
    private List<ContainerType> containerTypes;
    List<Project> possibleProjects = new ArrayList<>();
    private User currentUser;
    private String containerSearchLocation;

    public void startNewContainerCreation() {
        containerTypes = containerService.loadContainerTypes();
        containerToCreate = new Container();
        containerToCreate.setType(containerTypes.get(0));

    }

    public List<ContainerType> getContainerTypes() {
        return containerTypes;
    }

    public ContainerType getContainerType() {
        if (overviewBean.getMode() == ContainerOverviewBean.Mode.CREATE) {
            return containerToCreate.getType();
        }
        if (overviewBean.getMode() == ContainerOverviewBean.Mode.EDIT) {
            return containerToEdit.getType();
        }
        return null;
    }

    public void setContainerType(ContainerType t) {
        if (overviewBean.getMode() == ContainerOverviewBean.Mode.CREATE) {
            containerToCreate.setType(t);
        }
        if (overviewBean.getMode() == ContainerOverviewBean.Mode.EDIT) {
            containerToEdit.setType(t);
        }
    }

    public void setContainerProject(String p) {
        for (Project project : possibleProjects) {
            if (p.equals(project.getName())) {
                if (overviewBean.getMode() == ContainerOverviewBean.Mode.CREATE) {
                    containerToCreate.setProject(project);
                }
                if (overviewBean.getMode() == ContainerOverviewBean.Mode.EDIT) {
                    containerToEdit.setProject(project);
                }
            }
        }

    }

    public String getContainerProject() {
        if (overviewBean.getMode() == ContainerOverviewBean.Mode.CREATE) {
            if (containerToCreate.getProject() != null) {
                return containerToCreate.getProject().getName();
            }
        }
        if (overviewBean.getMode() == ContainerOverviewBean.Mode.EDIT) {
            if (containerToEdit.getProject() != null) {
                return containerToEdit.getProject().getName();
            }
        }

        return "";
    }

    public String getContainerName() {
        if (overviewBean.getMode() == ContainerOverviewBean.Mode.CREATE) {
            return containerToCreate.getLabel();
        }
        if (overviewBean.getMode() == ContainerOverviewBean.Mode.EDIT) {
            return containerToEdit.getLabel();
        }
        return "";
    }

    public void setContainerName(String containerName) {
        if (overviewBean.getMode() == ContainerOverviewBean.Mode.CREATE) {
            containerToCreate.setLabel(containerName);
        }
        if (overviewBean.getMode() == ContainerOverviewBean.Mode.EDIT) {
            containerToEdit.setLabel(containerName);
        }
    }

    public void setCurrentAccount(@Observes LoginEvent evt) {
        currentUser = evt.getCurrentAccount();
        projectService.loadReadableProjectsOfUser(currentUser);
    }

    public String getContainerSearchLocation() {
        return containerSearchLocation;
    }

    public void setContainerSearchLocation(String containerSearchLocation) {
        this.containerSearchLocation = containerSearchLocation;
    }

    public boolean isEditPanelVisible() {
        return overviewBean.getMode() == ContainerOverviewBean.Mode.CREATE
                || overviewBean.getMode() == ContainerOverviewBean.Mode.EDIT;
    }

}
