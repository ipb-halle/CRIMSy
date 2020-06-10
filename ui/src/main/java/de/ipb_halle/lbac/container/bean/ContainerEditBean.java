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

    private Integer containerHeight;
    private Integer containerWidth;
    private Container containerToCreate;
    private Container originalContainer;
    private List<ContainerType> containerTypes;
    List<Project> possibleProjects = new ArrayList<>();
    private User currentUser;
    private String containerLocation;
    private String gvoClass;
    private final List<String> possibleSecuritylevel = new ArrayList<>();
    private String preferredProjectName;
    Logger logger = LogManager.getLogger(this.getClass().getName());

    public void startNewContainerCreation() {
        containerTypes = containerService.loadContainerTypes();
        containerToCreate = new Container();
        containerToCreate.setType(containerTypes.get(0));
        preferredProjectName = "";
        containerLocation = "";
    }

    public void startContainerEdit(Container c) {
        containerTypes = containerService.loadContainerTypes();
        originalContainer = c;
        containerToCreate = c.copy();
        if (c.getProject() != null) {
            preferredProjectName = c.getProject().getName();
        }
        if (c.getParentContainer() != null) {
            containerLocation = c.getParentContainer().getLabel();
        }
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

        return containerToCreate.getType();

    }

    public Container getContainerToCreate() {
        return containerToCreate;
    }

    public void setContainerType(ContainerType t) {

        containerToCreate.setType(t);

    }

    public String getContainerName() {
        return containerToCreate.getLabel();
    }

    public void setContainerName(String containerName) {
        containerToCreate.setLabel(containerName);
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

        containerToCreate.setGmosavety(securityLevel);

    }

    public String getSecurityLevel() {

        return containerToCreate.getGmosavety();

    }

    public void setFireSection(String fireSection) {

        containerToCreate.setFireSection(fireSection);

    }

    public String getFireSection() {

        return containerToCreate.getFireSection();

    }

    public boolean isSecurityLevelVisible() {

        return containerToCreate.getType().getRank() == ContainerType.HIGHEST_RANK;

    }

    public boolean isDimensionVisible() {

        return containerToCreate.getType().getRank() != ContainerType.HIGHEST_RANK;

    }

    public void setPreferredProjectName(String preferredProjectName) {
        this.preferredProjectName = preferredProjectName;
    }

    public String getPreferredProjectName() {
        return preferredProjectName;
    }

    public Integer getContainerHeight() {
        return containerHeight;
    }

    public void setContainerHeight(Integer containerHeight) {
        this.containerHeight = containerHeight;
    }

    public Integer getContainerWidth() {
        return containerWidth;
    }

    public void setContainerWidth(Integer containerWidth) {
        this.containerWidth = containerWidth;
    }

}
