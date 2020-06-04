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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.SelectEvent;

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
    private String containerLocation;
    private String gvoClass;
    private List<String> possibleSecuritylevel = new ArrayList<>();
    Logger logger = LogManager.getLogger(this.getClass().getName());
    

    public void startNewContainerCreation() {
        containerTypes = containerService.loadContainerTypes();
        containerToCreate = new Container();
        containerToCreate.setType(containerTypes.get(0));

    }

    public List<ContainerType> getContainerTypes() {
        List<ContainerType> filteredContainerTypes = new ArrayList<>();
        for (ContainerType t : containerTypes) {
            if (t.getRank() > 0) {
                filteredContainerTypes.add(t);
            }
        }
        return filteredContainerTypes;
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

    public Container getContainerToCreate() {
        return containerToCreate;
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
        possibleSecuritylevel.add("keine");
        possibleSecuritylevel.add("S1");
        possibleSecuritylevel.add("S2");
        possibleSecuritylevel.add("S3");
        possibleSecuritylevel.add("S4");
    }

    public String getContainerLocation() {
        return containerLocation;
    }

    public void setContainerLocation(String containerLocation) {
        this.containerLocation = containerLocation;
    }

   

    public boolean isEditPanelVisible() {
        return overviewBean.getMode() == ContainerOverviewBean.Mode.CREATE
                || overviewBean.getMode() == ContainerOverviewBean.Mode.EDIT;
    }

    public String getGvoClass() {
        return gvoClass;
    }

    public void setGvoClass(String gvoClass) {
        this.gvoClass = gvoClass;
    }

    public List<String> getGvoClasses() {
        return possibleSecuritylevel;
    }

    public void setSecurityLevel(String securityLevel) {
        if (overviewBean.getMode() == ContainerOverviewBean.Mode.CREATE) {
            containerToCreate.setGmosavety(securityLevel);
        }
        if (overviewBean.getMode() == ContainerOverviewBean.Mode.EDIT) {
            containerToEdit.setGmosavety(securityLevel);
        }
    }

    public String getSecurityLevel() {
        if (overviewBean.getMode() == ContainerOverviewBean.Mode.CREATE) {
            return containerToCreate.getGmosavety();
        }
        if (overviewBean.getMode() == ContainerOverviewBean.Mode.EDIT) {
            return containerToEdit.getGmosavety();
        }
        return "";
    }

    public void setFireSection(String fireSection) {
        if (overviewBean.getMode() == ContainerOverviewBean.Mode.CREATE) {
            containerToCreate.setFireSection(fireSection);
        }
        if (overviewBean.getMode() == ContainerOverviewBean.Mode.EDIT) {
            containerToEdit.setFireSection(fireSection);
        }
    }

    public String getFireSection() {
        if (overviewBean.getMode() == ContainerOverviewBean.Mode.CREATE) {
            return containerToCreate.getFireSection();
        }
        if (overviewBean.getMode() == ContainerOverviewBean.Mode.EDIT) {
            return containerToEdit.getFireSection();
        }
        return "";
    }

    public boolean isSecurityLevelVisible() {
        if (overviewBean.getMode() == ContainerOverviewBean.Mode.CREATE) {
            return containerToCreate.getType().getRank() == ContainerType.HIGHEST_RANK;
        }
        if (overviewBean.getMode() == ContainerOverviewBean.Mode.EDIT) {
            return containerToEdit.getType().getRank() == ContainerType.HIGHEST_RANK;

        }
        return false;
    }

    public boolean isDimensionVisible() {
        if (overviewBean.getMode() == ContainerOverviewBean.Mode.CREATE) {
            return containerToCreate.getType().getRank() != ContainerType.HIGHEST_RANK;
        }
        if (overviewBean.getMode() == ContainerOverviewBean.Mode.EDIT) {
            return containerToEdit.getType().getRank() != ContainerType.HIGHEST_RANK;

        }
        return false;
    }

    public boolean isInputValide() {
        boolean isNameUsed = containerService.loadContainerByName(getContainerInFocus().getLabel()) == null;
        return isNameUsed;
    }

    private Container getContainerInFocus() {
        if (overviewBean.getMode() == ContainerOverviewBean.Mode.CREATE) {
            return containerToCreate;
        } else {
            return containerToEdit;
        }
    }

   
    
}
