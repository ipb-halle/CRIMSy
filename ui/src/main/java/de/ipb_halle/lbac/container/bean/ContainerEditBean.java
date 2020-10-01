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
package de.ipb_halle.lbac.container.bean;

import com.corejsf.util.Messages;
import de.ipb_halle.lbac.admission.LoginEvent;
import de.ipb_halle.lbac.container.Container;
import de.ipb_halle.lbac.container.ContainerType;
import de.ipb_halle.lbac.container.bean.ContainerOverviewBean.Mode;
import de.ipb_halle.lbac.container.service.ContainerService;
import de.ipb_halle.lbac.admission.User;
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
 * Representation of the controller of the ContainerEdit dialog with the data
 * tier
 *
 * @author fmauz
 */
@SessionScoped
@Named
public class ContainerEditBean implements Serializable {

    @Inject
    protected ContainerService containerService;

    @Inject
    protected ProjectService projectService;

    private Integer containerHeight;
    private Container containerLocation;
    private Container containerToCreate;
    private List<ContainerType> containerTypes = new ArrayList<>();
    private Integer containerWidth;
    private User currentUser;
    private String gvoClass;
    private Logger logger = LogManager.getLogger(this.getClass().getName());
    private Mode mode;
    private Container originalContainer;
    protected final List<String> possibleSecuritylevel = new ArrayList<>();
    private String preferredProjectName;
    protected ContainerLocalizer localizer = new ContainerLocalizer();

    public void setCurrentAccount(@Observes LoginEvent evt) {
        currentUser = evt.getCurrentAccount();
        projectService.loadReadableProjectsOfUser(currentUser);
        clearEditBean();
        initSecurityLevels();
    }

    /**
     * removes all information from temporary container attribures
     */
    public void clearEditBean() {
        originalContainer = null;
        containerToCreate = null;
        preferredProjectName = null;
        containerLocation = null;
        containerWidth = null;
        containerHeight = null;
        gvoClass = null;
    }

    public Integer getContainerHeight() {
        return containerHeight;
    }

    public String getContainerName() {
        if (containerToCreate != null) {
            return containerToCreate.getLabel();
        } else {
            return null;
        }
    }

    public Container getContainerLocation() {
        return containerLocation;
    }

    public Container getContainerToCreate() {
        return containerToCreate;
    }

    /**
     * if the container is not yet set a mock Containertype is created to bypass
     * null pointer issues
     *
     * @return
     */
    public ContainerType getContainerType() {
        if (containerToCreate != null) {
            return containerToCreate.getType();
        } else {
            return new ContainerType("XXX", 1000, false, false);
        }
    }

    public List<ContainerType> getContainerTypesWithRankGreaterZero() {
        List<ContainerType> filteredContainerTypes = new ArrayList<>();
        for (ContainerType t : containerTypes) {
            if (t.getRank() > 0) {
                filteredContainerTypes.add(t);
                t.setLocalizedName(localizer.localizeString("container_type_" + t.getName()));
            }
        }
        return filteredContainerTypes;
    }

    public Integer getContainerWidth() {
        return containerWidth;
    }

    public String getDialogTitle() {
        if (mode == Mode.CREATE) {
            return localizer.localizeString("container_edit_titel_create");
        } else {
            if (originalContainer != null) {
                return localizer.localizeString("container_edit_titel_edit", originalContainer.getLabel());
            } else {
                return "";
            }
        }
    }

    public String getFireSection() {
        return containerToCreate.getFireSection();
    }

    public String getGvoClass() {
        return gvoClass;
    }

    public List<String> getGvoClasses() {
        return possibleSecuritylevel;
    }

    public String getPreferredProjectName() {
        return preferredProjectName;
    }

    public String getSecurityLevel() {
        if (containerToCreate != null) {
            return containerToCreate.getGmosavety();
        } else {
            return null;
        }
    }

    public void initSecurityLevels() {
        possibleSecuritylevel.clear();
        possibleSecuritylevel.add(localizer.localizeString("container_securitylevel_none"));
        possibleSecuritylevel.add("S1");
        possibleSecuritylevel.add("S2");
        possibleSecuritylevel.add("S3");
        possibleSecuritylevel.add("S4");

    }

    public boolean isDimensionVisible() {
        if (containerToCreate != null) {
            return containerToCreate.getType().getRank() != ContainerType.HIGHEST_RANK;
        } else {
            return false;
        }
    }

    public boolean isEditable() {
        return mode == Mode.CREATE;
    }

    public boolean isSecurityLevelVisible() {
        if (containerToCreate != null) {
            return containerToCreate.getType().getRank() == ContainerType.HIGHEST_RANK;
        } else {
            return false;
        }
    }

    public void setContainerHeight(Integer containerHeight) {
        this.containerHeight = containerHeight;
    }

    public void setContainerLocation(Container containerLocation) {
        this.containerLocation = containerLocation;
    }

    public void setContainerName(String containerName) {
        containerToCreate.setLabel(containerName);
    }

    public void setContainerType(ContainerType t) {

        containerToCreate.setType(t);
    }

    public void setContainerWidth(Integer containerWidth) {
        this.containerWidth = containerWidth;
    }

    public void setFireSection(String fireSection) {
        containerToCreate.setFireSection(fireSection);
    }

    public void setGvoClass(String gvoClass) {
        this.gvoClass = gvoClass;
    }

    public void setPreferredProjectName(String preferredProjectName) {
        this.preferredProjectName = preferredProjectName;
    }

    public void setSecurityLevel(String securityLevel) {
        containerToCreate.setGmosavety(securityLevel);
    }

    public void startContainerEdit(Container c) {
        clearEditBean();
        mode = Mode.EDIT;
        containerTypes = containerService.loadContainerTypes();
        originalContainer = c;
        containerToCreate = c.copy();
        if (c.getProject() != null) {
            preferredProjectName = c.getProject().getName();
        } else {
            preferredProjectName = null;
        }
        c.setParentContainer(containerLocation);
        if (c.getParentContainer() != null) {
            containerLocation = c.getParentContainer();
        } else {
            containerLocation = null;
        }
        if (c.getDimensionIndex() != null) {
            containerWidth = c.getDimensionIndex()[0];
            containerHeight = c.getDimensionIndex()[1];
        } else {
            containerWidth = null;
            containerHeight = null;
        }
    }

    public void startNewContainerCreation() {
        clearEditBean();
        containerToCreate = new Container();
        mode = Mode.CREATE;
        containerTypes = containerService.loadContainerTypes();
        containerToCreate.setType(containerTypes.get(0));
    }

    public Container getOriginalContainer() {
        return originalContainer;
    }

}
